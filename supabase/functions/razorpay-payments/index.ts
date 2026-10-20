import { createServerDependencies, handlePaymentRequest } from './core.mjs';

Deno.serve((request: Request) => handlePaymentRequest(request, createServerDependencies({
  SUPABASE_URL: Deno.env.get('SUPABASE_URL'),
  SUPABASE_ANON_KEY: Deno.env.get('SUPABASE_ANON_KEY'),
  SUPABASE_SERVICE_ROLE_KEY: Deno.env.get('SUPABASE_SERVICE_ROLE_KEY'),
  RAZORPAY_KEY_ID: Deno.env.get('RAZORPAY_KEY_ID'),
  RAZORPAY_KEY_SECRET: Deno.env.get('RAZORPAY_KEY_SECRET'),
  RAZORPAY_WEBHOOK_SECRET: Deno.env.get('RAZORPAY_WEBHOOK_SECRET'),
})));
