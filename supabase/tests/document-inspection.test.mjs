import assert from 'node:assert/strict';
import {test} from 'node:test';
import {deflateRawSync} from 'node:zlib';
import {handleDocumentInspection,createInspectionDependencies} from '../functions/inspect-document/core.mjs';
const actor='00000000-0000-0000-0000-000000000001';
const file='00000000-0000-0000-0000-000000000002';
const object='00000000-0000-0000-0000-000000000003';
const pdf=new TextEncoder().encode('%PDF-1.7\nSynthetic test bytes\n%%EOF');
const request=(body={file_id:file},auth=true)=>new Request('https://example.test/inspect-document',{method:'POST',headers:auth?{Authorization:'Bearer test-user-token'}:{},body:JSON.stringify(body)});
function fixture(bytes=pdf,mime='application/pdf'){
 const calls=[];
 const deps={auth:async()=>({id:actor}),quota:async()=>true,
  metadata:async()=>({id:file,owner_id:actor,school_id:'synthetic',session_id:'demo_2026',object_path:`synthetic/demo_2026/${actor}/${file}`,mime_type:mime,size_bytes:bytes.length}),
  identity:async()=>({id:object,updated_at:'2026-10-30T00:00:00Z'}),
  object:async(bearer,path)=>{calls.push(['download',bearer,path]);return new Response(bytes);},
  confirm:async(...args)=>{calls.push(['confirm',...args]);return {validated:true};}};
 return {deps,calls};
}
function crc(bytes){let value=0xffffffff;for(const b of bytes){value^=b;for(let i=0;i<8;i++)value=(value>>>1)^((value&1)?0xedb88320:0);}return (value^0xffffffff)>>>0;}
function docx(names=['[Content_Types].xml','word/document.xml'],compressed=false){
 const local=[],central=[];let offset=0;
 for(const name of names){
  const n=Buffer.from(name),raw=Buffer.from(name==='[Content_Types].xml'?'<Types></Types>':'<w:document xmlns:w="x"></w:document>');
  const data=compressed?deflateRawSync(raw):raw;const method=compressed?8:0;
  const header=Buffer.alloc(30);header.writeUInt32LE(0x04034b50);header.writeUInt16LE(20,4);header.writeUInt16LE(method,8);header.writeUInt32LE(crc(raw),14);header.writeUInt32LE(data.length,18);header.writeUInt32LE(raw.length,22);header.writeUInt16LE(n.length,26);
  const entry=Buffer.alloc(46);entry.writeUInt32LE(0x02014b50);entry.writeUInt16LE(20,4);entry.writeUInt16LE(20,6);entry.writeUInt16LE(method,10);entry.writeUInt32LE(crc(raw),16);entry.writeUInt32LE(data.length,20);entry.writeUInt32LE(raw.length,24);entry.writeUInt16LE(n.length,28);entry.writeUInt32LE(offset,42);
  local.push(header,n,data);central.push(entry,n);offset+=header.length+n.length+data.length;
 }
 const centralBytes=Buffer.concat(central),end=Buffer.alloc(22);end.writeUInt32LE(0x06054b50);end.writeUInt16LE(names.length,8);end.writeUInt16LE(names.length,10);end.writeUInt32LE(centralBytes.length,12);end.writeUInt32LE(offset,16);
 return Buffer.concat([...local,centralBytes,end]);
}
test('unsigned or invalid Auth callers cannot read any file',async()=>{
 for(const auth of [false,true]){const {deps,calls}=fixture();deps.auth=async()=>null;
  assert.equal((await handleDocumentInspection(request(undefined,auth),deps)).status,401);assert.equal(calls.length,0);}
});
test('quota is mandatory and denied requests cannot download or confirm',async()=>{
 for(const quota of [undefined,async()=>false,async()=>({allowed:true})]){const {deps,calls}=fixture();deps.quota=quota;
  assert.ok([429,503].includes((await handleDocumentInspection(request(),deps)).status));assert.equal(calls.length,0);}
});
test('ownership and canonical path cannot be overridden by the caller',async()=>{
 for(const change of [{owner_id:'foreign'},{object_path:'../../private'},{object_path:`synthetic/demo_2026/${actor}/foreign`}]){
  const {deps,calls}=fixture();const get=deps.metadata;deps.metadata=async()=>({...await get(),...change});
  assert.equal((await handleDocumentInspection(request(),deps)).status,403);assert.equal(calls.length,0);
 }
 const {deps}=fixture();assert.equal((await handleDocumentInspection(request({file_id:file,actor_id:actor}),deps)).status,400);
});
test('PDF inspection binds the digest to the observed storage object version',async()=>{
 const {deps,calls}=fixture();const response=await handleDocumentInspection(request(),deps);
 assert.equal(response.status,200);assert.deepEqual(await response.json(),{validated:true,inspection:'FORMAT_ONLY'});
 const confirmed=calls.find(c=>c[0]==='confirm');assert.equal(confirmed[1],file);assert.equal(confirmed[2],actor);assert.equal(confirmed[3],object);assert.match(confirmed[5],/^[a-f0-9]{64}$/);
 assert.equal(calls[0][1],'Bearer test-user-token');
});
test('declared MIME and actual bytes must agree; metadata size is not trusted',async()=>{
 for(const [bytes,mime] of [[pdf,'image/png'],[new TextEncoder().encode('<html>fake</html>'),'application/pdf'],[pdf,'text/html']]){
  const {deps,calls}=fixture(bytes,mime);assert.equal((await handleDocumentInspection(request(),deps)).status,400);assert.ok(!calls.some(c=>c[0]==='confirm'));
 }
 const {deps}=fixture();const get=deps.metadata;deps.metadata=async()=>({...await get(),size_bytes:pdf.length+1});
 assert.equal((await handleDocumentInspection(request(),deps)).status,400);
});
test('streaming bounds reject oversized downloads even with a small Content-Length',async()=>{
 const {deps,calls}=fixture();deps.object=async()=>new Response(new Uint8Array(10*1024*1024+1),{headers:{'Content-Length':'1'}});
 assert.equal((await handleDocumentInspection(request(),deps)).status,413);assert.ok(!calls.some(c=>c[0]==='confirm'));
});
test('PNG chunk integrity and JPEG boundary signatures match the declared media type',async()=>{
 const png=Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4z8AAAAMBAQDJ/pLvAAAAAElFTkSuQmCC','base64');
 const jpeg=Buffer.from([255,216,255,224,0,2,255,217]);
 for(const [bytes,mime] of [[png,'image/png'],[jpeg,'image/jpeg']]){
  const {deps}=fixture(bytes,mime);assert.equal((await handleDocumentInspection(request(),deps)).status,200);
 }
 const corrupt=Buffer.from(png);corrupt[30]^=1;const {deps}=fixture(corrupt,'image/png');assert.equal((await handleDocumentInspection(request(),deps)).status,400);
});
test('replacement or update while downloading cannot inherit verification',async()=>{
 for(const changed of [{id:'00000000-0000-0000-0000-000000000004'},{updated_at:'2026-10-30T00:00:01Z'}]){
  const {deps,calls}=fixture();let checks=0;deps.identity=async()=>({id:object,updated_at:'2026-10-30T00:00:00Z',...(++checks>1?changed:{})});
  assert.equal((await handleDocumentInspection(request(),deps)).status,409);assert.ok(!calls.some(c=>c[0]==='confirm'));
 }
});
test('stored and deflated DOCX packages require real bounded ZIP contents',async()=>{
 for(const compressed of [false,true]){const {deps}=fixture(docx(undefined,compressed),'application/vnd.openxmlformats-officedocument.wordprocessingml.document');assert.equal((await handleDocumentInspection(request(),deps)).status,200);}
});
test('DOCX traversal, macros, encrypted or malformed archives are rejected',async()=>{
 const encrypted=docx();encrypted.writeUInt16LE(1,6);
 const bomb=docx();const directory=bomb.indexOf(Buffer.from([0x50,0x4b,0x01,0x02]));bomb.writeUInt32LE(100*1024*1024,directory+24);
 for(const bytes of [docx(['../word/document.xml']),docx(['[Content_Types].xml','word/vbaProject.bin']),encrypted,bomb,Buffer.from('PK\x03\x04broken')]){
  const {deps}=fixture(bytes,'application/vnd.openxmlformats-officedocument.wordprocessingml.document');assert.equal((await handleDocumentInspection(request(),deps)).status,400);
 }
});
test('errors never expose service responses, credentials or metadata',async()=>{
 const {deps}=fixture();deps.confirm=async()=>{throw Error('PRIVATE_RESPONSE_SENTINEL');};const response=await handleDocumentInspection(request(),deps);
 assert.equal(response.status,503);assert.equal((await response.text()).includes('PRIVATE_RESPONSE_SENTINEL'),false);
});
test('service adapter uses caller authorization to download and service authorization only for protected RPCs',async()=>{
 const seen=[];const deps=createInspectionDependencies({SUPABASE_URL:'https://example.supabase.co',SUPABASE_ANON_KEY:'public-test',SUPABASE_SERVICE_ROLE_KEY:'server-test'},async(url,init)=>{
  seen.push({url,init});return Response.json(url.includes('file_metadata')?[{id:file}]:{validated:true});
 });
 await deps.object('Bearer caller',`synthetic/demo_2026/${actor}/${file}`);
 assert.equal(seen[0].init.headers.Authorization,'Bearer caller');assert.equal(seen[0].init.headers.apikey,'public-test');
 await deps.confirm(file,actor,object,'2026-10-30T00:00:00Z','a'.repeat(64));
 assert.equal(seen[1].init.headers.Authorization,'Bearer server-test');assert.match(seen[1].url,/confirm_document_inspection$/);
});
