// A byte ceiling alone does not bound a stalled or slowly streamed request.
export class BodyReadError extends Error {
 constructor(status,message){super(message);this.status=status;}
}
export async function readBoundedBytes(stream,maxBytes,timeoutMs=15000,signal){
 if(!stream)return new Uint8Array();
 const reader=stream.getReader(),chunks=[];let size=0,timer;
 let onAbort;
 const deadline=new Promise((_,reject)=>{
  timer=setTimeout(()=>reject(new BodyReadError(408,'Request or document read timed out. Retry later.')),timeoutMs);
  onAbort=()=>reject(new BodyReadError(408,'Request or document read timed out. Retry later.'));
  if(signal?.aborted)onAbort();
  else signal?.addEventListener('abort',onAbort,{once:true});
 });
 try{
  while(true){
   const {done,value}=await Promise.race([reader.read(),deadline]);
   if(done)break;
   size+=value.byteLength;
   if(size>maxBytes || chunks.length>=16384)throw new BodyReadError(413,'Request or document exceeds the permitted size or chunk count.');
   chunks.push(value);
  }
  const bytes=new Uint8Array(size);let offset=0;
  for(const chunk of chunks){bytes.set(chunk,offset);offset+=chunk.byteLength;}
  return bytes;
 }catch(error){
  // An untrusted stream's cancel algorithm may itself never settle. Request
  // completion must not wait for it; observe rejection without logging data.
  reader.cancel().catch(()=>{});
  throw error;
 }finally{
  clearTimeout(timer);signal?.removeEventListener('abort',onAbort);reader.releaseLock();
 }
}

// Bound the complete upstream request plus response parsing, including fetchers
// that delay headers. Abort transport and cancel response reads on failure.
export async function fetchBoundedJson(fetcher,url,options={},timeoutMs=15000,maxBytes=2*1024*1024){
 const controller=new AbortController();let timer;
 const deadline=new Promise((_,reject)=>{
  timer=setTimeout(()=>{controller.abort();reject(new Error('Upstream response unavailable'));},timeoutMs);
 });
 const work=async()=>{
  const response=await fetcher(url,{...options,signal:controller.signal});
  const bytes=await readBoundedBytes(response.body,maxBytes,timeoutMs,controller.signal);
  const data=JSON.parse(new TextDecoder('utf-8',{fatal:true}).decode(bytes));
  return {response,data};
 };
 try{return await Promise.race([work(),deadline]);}
 catch{controller.abort();throw new Error('Upstream response unavailable');}
 finally{clearTimeout(timer);}
}
