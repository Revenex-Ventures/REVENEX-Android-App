// A byte ceiling alone does not bound a stalled or slowly streamed request.
export class BodyReadError extends Error {
 constructor(status,message){super(message);this.status=status;}
}
export async function readBoundedBytes(stream,maxBytes,timeoutMs=15000){
 if(!stream)return new Uint8Array();
 const reader=stream.getReader(),chunks=[];let size=0,timer;
 const deadline=new Promise((_,reject)=>{
  timer=setTimeout(()=>reject(new BodyReadError(408,'Request or document read timed out. Retry later.')),timeoutMs);
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
  clearTimeout(timer);reader.releaseLock();
 }
}
