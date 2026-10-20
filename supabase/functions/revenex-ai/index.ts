import {createAssistantDependencies,handleAssistantRequest} from './core.mjs';
const prompt=await Deno.readTextFile(new URL('./prompt.md',import.meta.url));
Deno.serve((request:Request)=>handleAssistantRequest(request,createAssistantDependencies({
 SUPABASE_URL:Deno.env.get('SUPABASE_URL'),SUPABASE_ANON_KEY:Deno.env.get('SUPABASE_ANON_KEY'),
 OPENAI_API_KEY:Deno.env.get('OPENAI_API_KEY'),OPENAI_MODEL:Deno.env.get('OPENAI_MODEL'),
 REVENEX_AI_ENABLED:Deno.env.get('REVENEX_AI_ENABLED')
},prompt)));
