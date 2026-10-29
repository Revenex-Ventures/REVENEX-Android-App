// Portable Web API handler, exercised by Node tests and hosted by Deno.
class PaymentError extends Error {
 constructor(status,message){super(message);this.status=status;}
}
const fail=(status,message)=>{throw new PaymentError(status,message);};
const json=(body,status=200)=>Response.json(body,{status,headers:{'Cache-Control':'no-store'}});
const validId=(value,prefix)=>typeof value==='string' && new RegExp('^'+prefix+'_[A-Za-z0-9]+$').test(value);
const uuid=value=>typeof value==='string' && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value);
async function limitedText(request,limit) {
 if(!request.body) return '';
 const reader=request.body.getReader();let size=0;const chunks=[];
 while(true){const {value,done}=await reader.read();if(done)break;size+=value.length;if(size>limit){await reader.cancel();fail(413,'Request too large');}chunks.push(value);}
 const bytes=new Uint8Array(size);let offset=0;for(const part of chunks){bytes.set(part,offset);offset+=part.length;}
 return new TextDecoder().decode(bytes);
}
async function validSignature(secret,text,signature) {
 if(!secret || typeof signature!=='string' || !/^[0-9a-f]{64}$/i.test(signature))return false;
 const key=await crypto.subtle.importKey('raw',new TextEncoder().encode(secret),{name:'HMAC',hash:'SHA-256'},false,['verify']);
 const bytes=Uint8Array.from(signature.match(/../g),pair=>parseInt(pair,16));
 return crypto.subtle.verify('HMAC',key,bytes,new TextEncoder().encode(text));
}
function testConfig(deps){if(!deps.keyId?.startsWith('rzp_test_') || !deps.keySecret)fail(503,'Configure Razorpay test secrets on the server.');}
async function settle(order,payment,deps) {
 if(payment.order_id!==order.provider_order_id || payment.amount!==order.amount || payment.currency!=='INR' || !validId(payment.id,'pay'))
  fail(409,'Provider payment does not match this school fee order.');
 if(payment.status==='failed' && payment.captured===false) {
  if(order.status==='paid') return json({status:'pending',reference:order.id,message:'This order is already recorded as paid. Refresh Fees for the verified receipt.'});
  await deps.db.failed(order.id,payment.id,payment.amount,payment.currency,payment.status,payment.captured);
  return json({status:'failed',reference:order.id,message:'Payment failed. The fee remains pending; the school inbox has been updated.'});
 }
 if(payment.status!=='captured' || payment.captured!==true)return json({status:'pending',reference:order.id,message:'Payment is not captured yet. Check status again.'});
 const receipt=await deps.db.settle(order.id,payment.id,payment.amount,payment.currency,payment.status,payment.captured);
 return json({status:'paid',reference:order.id,payment_id:payment.id,receipt_no:receipt.receipt_no,amount:receipt.amount,currency:'INR'});
}
export async function handlePaymentRequest(request,deps) {
 try {
  if(request.method!=='POST')return json({message:'POST required'},405);
  const webhook=new URL(request.url).pathname.endsWith('/webhook');
  if(webhook){
   testConfig(deps);
   if(!deps.webhookSecret)fail(503,'Configure the server webhook secret.');
   const raw=await limitedText(request,262144);
   if(!await validSignature(deps.webhookSecret,raw,request.headers.get('x-razorpay-signature')))fail(400,'Invalid webhook signature.');
   let event;try{event=JSON.parse(raw);}catch{fail(400,'Invalid JSON');}
   if(!['payment.captured','payment.failed'].includes(event.event))return json({ignored:true});
   const entity=event.payload?.payment?.entity;
   if(!validId(entity?.id,'pay') || !validId(entity?.order_id,'order'))fail(400,'Invalid payment event.');
   const order=await deps.db.providerOrder(entity.order_id);
   if(!order)fail(409,'Payment order is not registered.');
   return await settle(order,await deps.provider.payment(entity.id),deps);
  }
  const bearer=request.headers.get('authorization');
  if(!bearer?.startsWith('Bearer ') || bearer.length<10)fail(401,'Sign in to the school account first.');
  const user=await deps.auth(bearer);
  if(!user?.id)fail(401,'School session expired. Sign in again.');
  testConfig(deps);
  let body;try{body=JSON.parse(await limitedText(request,8192));}catch(e){if(e instanceof PaymentError)throw e;fail(400,'Invalid JSON');}
  const allowed={create:['action','fee_id'],verify:['action','reference','payment_id','provider_order_id','signature'],status:['action','reference']};
  if(!body || !Object.hasOwn(allowed,body.action) || Object.keys(body).some(key=>!allowed[body.action].includes(key)))fail(400,'Invalid payment request.');
  if(body.action==='create'){
   if(typeof body.fee_id!=='string' || !body.fee_id || body.fee_id.length>200)fail(400,'Select a fee record.');
   let order=await deps.db.reserve(body.fee_id,user.id,deps.keyId);
   if(order.key_id!==deps.keyId)fail(409,'Order key changed; server reconciliation required.');
   if(order.new_reservation){
    const created=await deps.provider.create({amount:order.amount,currency:'INR',receipt:order.id,partial_payment:false});
    if(!validId(created.id,'order') || created.amount!==order.amount || created.currency!=='INR')fail(502,'Invalid provider order.');
    order=await deps.db.bind(order.id,created.id);
   }
   if(!order.provider_order_id){
    // A provider POST may have succeeded before the connection/bind was interrupted.
    // Recover only its exact server-generated receipt; never blindly POST again.
    const found=await deps.provider.ordersByReceipt(order.id);
    const matches=found.items?.filter(candidate=>candidate.receipt===order.id)??[];
    if(matches.length!==1 || found.items.length>=100)fail(409,'Order outcome needs reconciliation. Keep this reference: '+order.id+'. Do not pay another order.');
    const recovered=matches[0];
    if(!validId(recovered.id,'order') || recovered.amount!==order.amount || recovered.currency!=='INR')
     fail(409,'Recovered provider order does not match this invoice. Contact the school with reference '+order.id+'.');
    order=await deps.db.bind(order.id,recovered.id);
    if(recovered.status==='paid')fail(409,'This order already has a payment. Check payment status for the verified receipt.');
   }
   if(!order.provider_order_id)fail(409,'Order binding was not confirmed. Check the existing payment reference before retrying.');
   return json({reference:order.id,order_id:order.provider_order_id,key_id:order.key_id,amount:order.amount,currency:'INR'});
  }
  if(!uuid(body.reference))fail(400,'Invalid payment reference.');
  const order=await deps.db.order(bearer,body.reference);
  if(!order?.provider_order_id)fail(403,'Payment order is unavailable to this account.');
  if(order.key_id!==deps.keyId)fail(409,'Order key changed; server reconciliation required.');
  if(body.action==='verify'){
   if(!validId(body.payment_id,'pay') || body.provider_order_id!==order.provider_order_id ||
    !await validSignature(deps.keySecret,order.provider_order_id+'|'+body.payment_id,body.signature))fail(400,'Payment signature could not be verified.');
   return await settle(order,await deps.provider.payment(body.payment_id),deps);
  }
  const payments=await deps.provider.payments(order.provider_order_id);
  const captured=payments.items?.find(p=>p.status==='captured' && p.captured===true);
  if(captured) return await settle(order,captured,deps);
  const failed=payments.items?.find(p=>p.status==='failed' && p.captured===false);
  if(failed) return await settle(order,failed,deps);
  return json({status:'pending',reference:order.id,message:'No captured payment yet. You can retry checkout or check status later.'});
 }catch(error){
  if(error instanceof PaymentError)return json({message:error.message},error.status);
  if(error?.code==='42501')return json({message:'Fee access denied.'},403);
  if(['23514','23505'].includes(error?.code))return json({message:'Fee balance or pending order changed. Reconcile the existing payment first.'},409);
  return json({message:'Payment service unavailable. Check server deployment and retry; no success was assumed.'},503);
 }
}

/** Secrets exist only in the hosted Edge Function environment. No SDK dependency. */
export function createServerDependencies(env,fetcher=fetch) {
 const base=env.SUPABASE_URL;
 const service=env.SUPABASE_SERVICE_ROLE_KEY;
 const anon=env.SUPABASE_ANON_KEY;
 const request=async(url,headers,body)=>{
  const response=await fetcher(url,{method:body===undefined?'GET':'POST',headers:{...headers,'Content-Type':'application/json'},body:body===undefined?undefined:JSON.stringify(body),signal:AbortSignal.timeout(15000)});
  let data;try{data=await response.json();}catch{throw new Error('Invalid service response');}
  if(!response.ok){const error=new Error('Service request failed');error.code=data?.code;throw error;}return data;
 };
 const admin={apikey:service,Authorization:'Bearer '+service};
 const rpc=(name,body)=>request(base+'/rest/v1/rpc/'+name,admin,body);
 const rows=(query,headers)=>request(base+'/rest/v1/razorpay_orders?'+query,headers);
 const providerHeaders={Authorization:'Basic '+btoa(env.RAZORPAY_KEY_ID+':'+env.RAZORPAY_KEY_SECRET)};
 return {
  keyId:env.RAZORPAY_KEY_ID,keySecret:env.RAZORPAY_KEY_SECRET,webhookSecret:env.RAZORPAY_WEBHOOK_SECRET,
  auth:async bearer=>{try{return await request(base+'/auth/v1/user',{apikey:anon,Authorization:bearer});}catch{return null;}},
  db:{
   reserve:(fee,actor,key)=>rpc('reserve_razorpay_order',{p_fee_id:fee,p_actor_id:actor,p_key_id:key}),
   bind:(id,provider)=>rpc('bind_razorpay_order',{p_reference:id,p_provider_id:provider}),
   order:async(bearer,id)=>(await rows('id=eq.'+encodeURIComponent(id)+'&select=*',{apikey:anon,Authorization:bearer}))[0],
   providerOrder:async id=>(await rows('provider_order_id=eq.'+encodeURIComponent(id)+'&select=*',admin))[0],
   failed:(id,payment,amount,currency,status,captured)=>rpc('record_razorpay_failure',{p_reference:id,p_payment_id:payment,p_amount:amount,p_currency:currency,p_status:status,p_captured:captured}),
   settle:(id,payment,amount,currency,status,captured)=>rpc('settle_razorpay_payment',{p_reference:id,p_payment_id:payment,p_amount:amount,p_currency:currency,p_status:status,p_captured:captured})
  },
  provider:{
   create:body=>request('https://api.razorpay.com/v1/orders',providerHeaders,body),
   ordersByReceipt:receipt=>request('https://api.razorpay.com/v1/orders?receipt='+encodeURIComponent(receipt)+'&count=100',providerHeaders),
   payment:id=>request('https://api.razorpay.com/v1/payments/'+encodeURIComponent(id),providerHeaders),
   payments:id=>request('https://api.razorpay.com/v1/orders/'+encodeURIComponent(id)+'/payments',providerHeaders)
  }
 };
}
