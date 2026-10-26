# Revenex Assistant

The current assistant is a **bounded local guide**, not a configured Gemini or other live-model integration.

- It provides deterministic general study guidance and ERP navigation help.
- A student can see only their loaded attendance and fee summary.
- A parent can see only explicitly linked children’s loaded summaries.
- It refuses all consequential changes; users must use the normal, authorised ERP workflow.
- Demo mode labels every loaded-record answer as demo data. Production mode states that live AI is unavailable until a server-side provider is configured.

`AssistantReplyEngine` keeps this policy deterministic and unit-testable. The cloud integration architecture uses the Supabase Edge Function (`supabase/functions/revenex-ai/`), sending bounded history and Auth JWT. The backend derives user profile, school session, and permitted student scope on the server side, keeping all AI provider keys in backend secrets.

