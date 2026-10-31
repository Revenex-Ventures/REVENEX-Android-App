import {BodyReadError,readBoundedBytes,fetchBoundedJson} from '../_shared/bounded-body.mjs';
// Format validation, not malware scanning. No untrusted file is rendered here.
class InspectionError extends Error {constructor(status,message){super(message);this.status=status;}}
const fail=(status,message)=>{throw new InspectionError(status,message);};
const json=(value,status=200)=>Response.json(value,{status,headers:{'Cache-Control':'no-store','X-Content-Type-Options':'nosniff',...(status===429?{'Retry-After':'60'}:{})}});
const uuid=value=>typeof value==='string'&&/^[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}$/i.test(value);
const MAX_FILE=10*1024*1024,MAX_EXPANDED=20*1024*1024;
async function bounded(stream,max){
 try{return await readBoundedBytes(stream,max);}catch(error){
  if(error instanceof BodyReadError)fail(error.status,error.message);
  throw error;
 }
}
const invalid=()=>fail(400,'Document content does not match the supported format.');
function crc32(bytes){let value=0xffffffff;for(const byte of bytes){value^=byte;for(let bit=0;bit<8;bit++)value=(value>>>1)^((value&1)?0xedb88320:0);}return (value^0xffffffff)>>>0;}
async function docx(bytes){
 const view=new DataView(bytes.buffer,bytes.byteOffset,bytes.byteLength),length=bytes.length;
 const u16=p=>view.getUint16(p,true),u32=p=>view.getUint32(p,true);
 let end=-1;
 for(let p=length-22;p>=Math.max(0,length-65557);p--)if(u32(p)===0x06054b50&&p+22+u16(p+20)===length){end=p;break;}
 if(end<0||u16(end+4)!==0||u16(end+6)!==0||u16(end+8)!==u16(end+10))invalid();
 const entries=u16(end+10),directory=u32(end+16),directorySize=u32(end+12);
 if(entries<2||entries>128||directory+directorySize!==end)invalid();
 const names=new Set(),ranges=[];let position=directory,expanded=0;
 for(let entry=0;entry<entries;entry++){
  if(position+46>end||u32(position)!==0x02014b50)invalid();
  const flags=u16(position+8),method=u16(position+10),checksum=u32(position+16),packed=u32(position+20),size=u32(position+24);
  const nameLength=u16(position+28),extra=u16(position+30),comment=u16(position+32),offset=u32(position+42);
  if((flags&~0x808)!==0||![0,8].includes(method)||nameLength<1||nameLength>256||position+46+nameLength+extra+comment>end||
   (u32(position+38)>>>16&0xf000)===0xa000||u16(position+34)!==0||size>MAX_EXPANDED||packed>MAX_FILE||size>Math.max(1,packed)*100)invalid();
  const name=new TextDecoder('utf-8',{fatal:true}).decode(bytes.subarray(position+46,position+46+nameLength));
  const normalized=name.toLowerCase();
  if(name.includes('\\')||name.includes(':')||Array.from(name).some(character=>character.codePointAt(0)<32)||name.startsWith('/')||name.split('/').some(part=>part==='..'||part==='.')||names.has(normalized)||
   normalized.includes('vbaproject')||normalized.startsWith('word/embeddings/')||normalized.endsWith('.exe')||normalized.endsWith('.dll'))invalid();
  names.add(normalized);
  if(offset+30>directory||u32(offset)!==0x04034b50||u16(offset+6)!==flags||u16(offset+8)!==method||u16(offset+26)!==nameLength)invalid();
  const localName=new TextDecoder('utf-8',{fatal:true}).decode(bytes.subarray(offset+30,offset+30+nameLength));
  const start=offset+30+nameLength+u16(offset+28),finish=start+packed;
  if(localName!==name||finish>directory||start>finish||ranges.some(([a,b])=>offset<b&&finish>a))invalid();
  ranges.push([offset,finish]);
  if(!(flags&8)&&(u32(offset+14)!==checksum||u32(offset+18)!==packed||u32(offset+22)!==size))invalid();
  const compressed=bytes.subarray(start,finish);
  const content=method===0?compressed:await bounded(new Blob([compressed]).stream().pipeThrough(new DecompressionStream('deflate-raw')),Math.min(size,MAX_EXPANDED-expanded));
  expanded+=content.length;
  if(content.length!==size||expanded>MAX_EXPANDED||crc32(content)!==checksum)invalid();
  if(normalized==='[content_types].xml'||normalized==='word/document.xml'){
   const text=new TextDecoder('utf-8',{fatal:true}).decode(content);
   if(/<!DOCTYPE|<!ENTITY|macroenabled|vbaProject/i.test(text))invalid();
   if(normalized==='[content_types].xml'?!/<(?:\w+:)?Types\b/.test(text):!/<(?:\w+:)?document\b/.test(text))invalid();
  }
  position+=46+nameLength+extra+comment;
 }
 if(position!==end||!names.has('[content_types].xml')||!names.has('word/document.xml'))invalid();
}
async function validateFormat(bytes,mime){
 const text=new TextDecoder();
 if(mime==='application/pdf'){
  if(text.decode(bytes.subarray(0,5))!=='%PDF-'||!text.decode(bytes.subarray(Math.max(0,bytes.length-1024))).includes('%%EOF'))invalid();
 }else if(mime==='image/png'){
  const signature=[137,80,78,71,13,10,26,10];
  if(bytes.length<45||signature.some((byte,i)=>bytes[i]!==byte)||text.decode(bytes.subarray(12,16))!=='IHDR'||text.decode(bytes.subarray(bytes.length-8,bytes.length-4))!=='IEND')invalid();
  const view=new DataView(bytes.buffer,bytes.byteOffset,bytes.byteLength),width=view.getUint32(16),height=view.getUint32(20);
  if(view.getUint32(8)!==13||!width||!height||width>10000||height>10000||width*height>40000000)invalid();
  let position=8,hasData=false,ended=false,chunks=0;
  while(position+12<=bytes.length){
   if(++chunks>1024)invalid();const size=view.getUint32(position),finish=position+12+size;
   if(finish>bytes.length)invalid();const kind=text.decode(bytes.subarray(position+4,position+8));
   if(!/^[A-Za-z]{4}$/.test(kind)||crc32(bytes.subarray(position+4,finish-4))!==view.getUint32(finish-4))invalid();
   if(kind==='IHDR'&&position!==8)invalid();if(kind==='IDAT'){if(!size)invalid();hasData=true;}
   if(kind==='IEND'){if(size||finish!==bytes.length)invalid();ended=true;}
   position=finish;
  }
  if(!hasData||!ended||position!==bytes.length)invalid();
 }else if(mime==='image/jpeg'){
  if(bytes.length<8||bytes[0]!==255||bytes[1]!==216||bytes[2]!==255||bytes[bytes.length-2]!==255||bytes[bytes.length-1]!==217)invalid();
 }else if(mime==='application/vnd.openxmlformats-officedocument.wordprocessingml.document')await docx(bytes);
 else invalid();
}
export async function handleDocumentInspection(request,deps){
 try{
  if(request.method!=='POST')return json({message:'POST required.'},405);
  const bearer=request.headers.get('authorization');
  if(!bearer?.startsWith('Bearer ')||bearer.length<10)fail(401,'Sign in to the school account first.');
  const user=await deps.auth(bearer);if(!uuid(user?.id))fail(401,'School session expired. Sign in again.');
  let body;try{body=JSON.parse(new TextDecoder('utf-8',{fatal:true}).decode(await bounded(request.body,4096)));}catch(error){if(error instanceof InspectionError)throw error;fail(400,'Send a valid document reference.');}
  if(!body||Array.isArray(body)||Object.keys(body).some(key=>key!=='file_id')||!uuid(body.file_id))fail(400,'Send only the document reference.');
  if(typeof deps.quota!=='function')fail(503,'Document inspection is not configured.');
  if((await deps.quota(user.id,'document_inspect')) !== true)fail(429,'Document inspection limit reached. Try again later.');
  const metadata=await deps.metadata(bearer,body.file_id);
  if(!metadata||metadata.id!==body.file_id||metadata.owner_id!==user.id||
   !/^[A-Za-z0-9_-]+$/.test(metadata.school_id)||!/^[A-Za-z0-9_-]+$/.test(metadata.session_id)||
   metadata.object_path!==`${metadata.school_id}/${metadata.session_id}/${user.id}/${body.file_id}`)fail(403,'Document access denied.');
  if(!Number.isSafeInteger(metadata.size_bytes)||metadata.size_bytes<1||metadata.size_bytes>MAX_FILE)fail(400,'Document size is invalid.');
  const before=await deps.identity(body.file_id,user.id);
  if(!uuid(before?.id)||typeof before.updated_at!=='string'||!Number.isFinite(Date.parse(before.updated_at)))fail(409,'The stored document is unavailable. Upload it again.');
  const response=await deps.object(bearer,metadata.object_path);
  if(!response.ok)fail(503,'Stored document could not be inspected.');
  const bytes=await bounded(response.body,MAX_FILE);
  if(bytes.length!==metadata.size_bytes)fail(400,'Stored document size does not match its upload metadata.');
  try{await validateFormat(bytes,metadata.mime_type);}catch(error){if(error instanceof InspectionError&&error.status!==413)throw error;invalid();}
  const after=await deps.identity(body.file_id,user.id);
  if(!after||after.id!==before.id||after.updated_at!==before.updated_at)fail(409,'Document changed during inspection. Retry with the current upload.');
  const digest=Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',bytes)),byte=>byte.toString(16).padStart(2,'0')).join('');
  const confirmed=await deps.confirm(body.file_id,user.id,before.id,before.updated_at,digest);
  if(confirmed?.validated!==true)fail(503,'Document inspection was not confirmed.');
  return json({validated:true,inspection:'FORMAT_ONLY'});
 }catch(error){
  if(error instanceof InspectionError)return json({message:error.message},error.status);
  if(error?.code==='42501')return json({message:'Document access denied.'},403);
  if(['23514','40001'].includes(error?.code))return json({message:'Document changed or is unavailable. Retry inspection.'},409);
  return json({message:'Document inspection unavailable. No content approval was assumed.'},503);
 }
}
export function createInspectionDependencies(env,fetcher=fetch){
 const base=env.SUPABASE_URL,key=env.SUPABASE_ANON_KEY,service=env.SUPABASE_SERVICE_ROLE_KEY;
 const admin={apikey:service,Authorization:'Bearer '+service};
 const call=async(path,headers,body)=>{
  const {response,data}=await fetchBoundedJson(fetcher,base+path,{method:body===undefined?'GET':'POST',headers:{...headers,'Content-Type':'application/json'},body:body===undefined?undefined:JSON.stringify(body)});
  if(!response.ok){const error=new Error('School service unavailable');error.code=data?.code;throw error;}return data;
 };
 const rpc=(name,body)=>call('/rest/v1/rpc/'+name,admin,body);
 return {
  auth:async bearer=>{try{return await call('/auth/v1/user',{apikey:key,Authorization:bearer});}catch{return null;}},
  quota:(actor,operation)=>rpc('reserve_backend_request',{p_actor_id:actor,p_operation:operation}),
  metadata:async(bearer,id)=>(await call('/rest/v1/file_metadata?'+new URLSearchParams({id:'eq.'+id,select:'id,owner_id,school_id,session_id,object_path,mime_type,size_bytes',limit:'1'}),{apikey:key,Authorization:bearer}))[0],
  identity:(file,actor)=>rpc('document_inspection_identity',{p_file:file,p_actor:actor}),
  object:(bearer,path)=>fetcher(base+'/storage/v1/object/authenticated/erp-documents/'+path.split('/').map(encodeURIComponent).join('/'),{headers:{apikey:key,Authorization:bearer},signal:AbortSignal.timeout(15000)}),
  confirm:(file,actor,object,updated,sha)=>rpc('confirm_document_inspection',{p_file:file,p_actor:actor,p_object:object,p_updated_at:updated,p_sha256:sha})
 };
}
