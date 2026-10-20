import assert from 'node:assert/strict';
Deno.test('real AI entrypoint loads the server prompt and rejects anonymous HTTP',async()=>{
 const serve=Deno.serve;let server;
 Deno.serve=handler=>{server=serve({hostname:'127.0.0.1',port:0,onListen(){}},handler);return server;};
 try {
  await import('../functions/revenex-ai/index.ts');
  const response=await fetch('http://127.0.0.1:'+server.addr.port,{method:'POST',headers:{'Content-Type':'application/json'},body:'{"message":"hello"}'});
  assert.equal(response.status,401);
  assert.equal(JSON.stringify(await response.json()).includes('SERVER ENFORCEMENT'),false);
 } finally {Deno.serve=serve;if(server)await server.shutdown();}
});
