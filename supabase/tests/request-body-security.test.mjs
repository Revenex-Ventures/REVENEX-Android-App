import assert from 'node:assert/strict';
import {test} from 'node:test';
import {handlePaymentRequest} from '../functions/razorpay-payments/core.mjs';
import {handleAssistantRequest} from '../functions/revenex-ai/core.mjs';
import {handleDocumentInspection} from '../functions/inspect-document/core.mjs';
import {readBoundedBytes} from '../functions/_shared/bounded-body.mjs';

const actor='00000000-0000-0000-0000-000000000001';
const settleTasks=async()=>{for(let n=0;n<40;n++)await Promise.resolve();};
const handlers={payment:handlePaymentRequest,webhook:handlePaymentRequest,assistant:handleAssistantRequest,inspection:handleDocumentInspection};
test('a stalled private object download cannot hang or publish inspection evidence',async t=>{
 t.mock.timers.enable({apis:['setTimeout']});
 const file='00000000-0000-0000-0000-000000000002';
 let cancelled=0,confirmed=0,status='pending';
 const stream=new ReadableStream({cancel(){cancelled++;return new Promise(()=>{});}});
 const deps={auth:async()=>({id:actor}),quota:async()=>true,
  metadata:async()=>({id:file,owner_id:actor,school_id:'demo',session_id:'session',object_path:`demo/session/${actor}/${file}`,mime_type:'application/pdf',size_bytes:10}),
  identity:async()=>({id:file,updated_at:'2026-10-30T00:00:00Z'}),object:async()=>new Response(stream),
  confirm:async()=>{confirmed++;return {validated:true};}};
 const request=new Request('https://example.test/inspect-document',{method:'POST',headers:{Authorization:'Bearer synthetic-token'},body:JSON.stringify({file_id:file})});
 const pending=handleDocumentInspection(request,deps).then(r=>{status=r.status;return r;});
 await settleTasks();t.mock.timers.tick(15001);await settleTasks();
 assert.equal(status,408);assert.equal(cancelled,1);assert.equal(confirmed,0);await pending;
});
test('byte ceilings accept the boundary and reject excess content',async()=>{
 assert.deepEqual(await readBoundedBytes(new Response(new Uint8Array([1,2,3])).body,3),new Uint8Array([1,2,3]));
 await assert.rejects(()=>readBoundedBytes(new Response(new Uint8Array(4)).body,3),e=>e.status===413);
});
test('a tiny-chunk flood is bounded independently of its declared byte size',async()=>{
 let cancelled=false;
 const stream=new ReadableStream({pull(c){c.enqueue(new Uint8Array([1]));},cancel(){cancelled=true;}});
 await assert.rejects(()=>readBoundedBytes(stream,10*1024*1024),e=>e.status===413);
 assert.equal(cancelled,true);
});
for(const [name,handler] of Object.entries(handlers)){
 test(`${name}: stalled request expires even when cancellation never finishes`,async t=>{
  t.mock.timers.enable({apis:['setTimeout']});
  let cancelled=0,controller,status='pending',work=0;
  const stream=new ReadableStream({start(c){controller=c;},cancel(){cancelled++;return new Promise(()=>{});}});
  const request=new Request('https://example.test/'+(name==='webhook'?'razorpay-payments/webhook':name),{method:'POST',headers:name==='webhook'?{}:{Authorization:'Bearer synthetic-token'},body:stream,duplex:'half'});
  const deps={auth:async()=>({id:actor}),keyId:'rzp_test_unit',keySecret:'unit-only',webhookSecret:'unit-only',
   quota:async()=>{work++;return true;},context:async()=>{work++;throw Error('No school/provider work expected');}};
  const pending=handler(request,deps).then(r=>{status=r.status;return r;});
  try{
   await settleTasks();t.mock.timers.tick(15001);await settleTasks();
   assert.equal(status,408);assert.equal(work,0);assert.equal(cancelled,1);
   assert.match((await (await pending).json()).message,/timed out/i);
  }finally{try{controller.close();}catch{/* Cancelled readers already closed the controller. */}}
 });
 test(`${name}: small chunks cannot restart the total request deadline`,async t=>{
  t.mock.timers.enable({apis:['setTimeout']});
  let controller,status='pending';
  const stream=new ReadableStream({start(c){controller=c;}});
  const request=new Request('https://example.test/'+(name==='webhook'?'razorpay-payments/webhook':name),{method:'POST',headers:name==='webhook'?{}:{Authorization:'Bearer synthetic-token'},body:stream,duplex:'half'});
  const pending=handler(request,{auth:async()=>({id:actor}),keyId:'rzp_test_unit',keySecret:'unit-only',webhookSecret:'unit-only'}).then(r=>{status=r.status;return r;});
  try{
   await settleTasks();t.mock.timers.tick(10000);controller.enqueue(new TextEncoder().encode('{'));
   await settleTasks();t.mock.timers.tick(5001);await settleTasks();assert.equal(status,408);await pending;
  }finally{try{controller.close();}catch{/* Cancelled by the reader. */}}
 });
}
