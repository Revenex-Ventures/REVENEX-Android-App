import {test} from 'node:test';
import assert from 'node:assert/strict';
import {fetchBoundedJson} from '../functions/_shared/bounded-body.mjs';
import {createServerDependencies} from '../functions/razorpay-payments/core.mjs';
import {createAssistantDependencies} from '../functions/revenex-ai/core.mjs';
import {createInspectionDependencies} from '../functions/inspect-document/core.mjs';

const settle=async()=>{for(let n=0;n<50;n++)await Promise.resolve();};
test('all selected upstream adapters reject oversized JSON before using its contents',async()=>{
 const env={SUPABASE_URL:'https://example.test',SUPABASE_ANON_KEY:'unit',SUPABASE_SERVICE_ROLE_KEY:'unit',RAZORPAY_KEY_ID:'rzp_test_unit',RAZORPAY_KEY_SECRET:'unit',OPENAI_API_KEY:'unit',OPENAI_MODEL:'unit'};
 const fetcher=async()=>Response.json({items:[],padding:'PRIVATE'.repeat(400000)});
 const calls=[()=>createServerDependencies(env,fetcher).provider.ordersByReceipt('synthetic'),
  ()=>createAssistantDependencies(env,'',fetcher).model({}),
  ()=>createInspectionDependencies(env,fetcher).metadata('Bearer unit','synthetic')];
 for(const call of calls)await assert.rejects(call,e=>e.message==='Upstream response unavailable');
});
test('upstream response has a byte ceiling and transport is aborted on overflow',async()=>{
 let signal,cancelled=false;
 const fetcher=async(_url,options)=>{signal=options.signal;return new Response(new ReadableStream({start(c){c.enqueue(new Uint8Array(101));},cancel(){cancelled=true;return new Promise(()=>{});}}));};
 await assert.rejects(()=>fetchBoundedJson(fetcher,'https://example.test',{},15000,100),/Upstream response unavailable/);
 assert.equal(signal.aborted,true);assert.equal(cancelled,true);
});
test('upstream headers cannot keep work alive beyond the total deadline',async t=>{
 t.mock.timers.enable({apis:['setTimeout']});let signal,error;
 const task=fetchBoundedJson(async(_url,o)=>{signal=o.signal;return new Promise(()=>{});},'https://example.test').catch(e=>{error=e;});
 await settle();t.mock.timers.tick(15001);await settle();
 assert.equal(signal.aborted,true);assert.match(error.message,/Upstream response unavailable/);await task;
});
test('deadline aborts and cancels a stalled upstream body without awaiting cleanup',async t=>{
 t.mock.timers.enable({apis:['setTimeout']});let signal,cancelled=false,error;
 const task=fetchBoundedJson(async(_url,o)=>{signal=o.signal;return new Response(new ReadableStream({cancel(){cancelled=true;return new Promise(()=>{});}}));},'https://example.test').catch(e=>{error=e;});
 await settle();t.mock.timers.tick(15001);await settle();
 assert.equal(signal.aborted,true);assert.equal(cancelled,true);assert.match(error.message,/Upstream response unavailable/);await task;
});
test('bounded JSON parsing keeps status but never surfaces invalid upstream content',async()=>{
 const {response,data}=await fetchBoundedJson(async()=>Response.json({allowed:false},{status:403}),'https://example.test');
 assert.equal(response.status,403);assert.deepEqual(data,{allowed:false});
 await assert.rejects(()=>fetchBoundedJson(async()=>new Response('PRIVATE_UPSTREAM_CONTENT'),'https://example.test'),e=>e.message==='Upstream response unavailable');
});
