const { initializeTestEnvironment } = require('@firebase/rules-unit-testing');
const fs = require('node:fs');
const path = require('node:path');

async function main() {
  const rules = fs.readFileSync(path.resolve(__dirname, '..', '..', 'firestore.rules'), 'utf8');
  const env = await initializeTestEnvironment({
    projectId: 'demo-revenex',
    firestore: { host: '127.0.0.1', port: 8085, rules },
  });
  await env.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().doc('schools/s1').set({ name: 'x' });
    await ctx.firestore().doc('schools/s1/users/me').set({ schoolId: 's1', role: 'PRINCIPAL' });
  });

  const pr = env.authenticatedContext('me', { role: 'PRINCIPAL', schoolId: 's1', sessionId: '2026-2027' });
  const db = pr.firestore();

  // A principal creating a session-scoped class must succeed if claims match.
  try {
    await db.doc('schools/s1/classes/c11').set({ schoolId: 's1', sessionId: '2026-2027', grade: '11' });
    console.log('RESULT: principal class create = ALLOW');
  } catch (e) {
    console.log('RESULT: principal class create = DENY');
    console.log('DENY MSG:', String(e.message).replace(/\s+/g, ' ').slice(0, 400));
  }

  try {
    await db.doc('schools/s1/users/me').update({ fullName: 'P' });
    console.log('RESULT: principal self update = ALLOW');
  } catch (e) {
    console.log('RESULT: principal self update = DENY');
    console.log('DENY MSG:', String(e.message).replace(/\s+/g, ' ').slice(0, 400));
  }

  await env.cleanup();
}

main().catch((e) => { console.error(e); process.exit(1); });