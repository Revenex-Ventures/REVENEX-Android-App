const fs = require('node:fs');
const path = require('node:path');
const { initializeTestEnvironment, assertSucceeds, assertFails } = require('@firebase/rules-unit-testing');

async function main() {
  const env = await initializeTestEnvironment({
    projectId: 'demo-revenex',
    storage: { host: '127.0.0.1', port: 9199, rules: fs.readFileSync(path.resolve(__dirname, '../../storage.rules'), 'utf8') },
  });
  let passed = 0;
  const check = async (promise, allowed) => {
    await (allowed ? assertSucceeds(promise) : assertFails(promise));
    passed++;
  };
  try {
    const studentPath = 'schools/s1/students/student1/identity.pdf';
    const materialPath = 'schools/s1/materials/lesson.pdf';
    const upload = { contentType: 'application/pdf' };
    await env.withSecurityRulesDisabled(async ctx => {
      await ctx.storage().ref(studentPath).put(new Uint8Array([1, 2, 3]), upload);
      await ctx.storage().ref(materialPath).put(new Uint8Array([1, 2, 3]), upload);
    });
    const storage = (uid, role, schoolId = 's1', extra = {}) =>
      env.authenticatedContext(uid, { role, schoolId, ...extra }).storage();
    const self = storage('student1', 'STUDENT');
    const other = storage('student2', 'STUDENT');
    const linked = storage('parent1', 'PARENT', 's1', { studentIds: ['student1'] });
    const unrelated = storage('parent2', 'PARENT', 's1', { studentIds: ['student2'] });
    const teacher = storage('teacher1', 'TEACHER');
    await check(self.ref(studentPath).getMetadata(), true);
    await check(storage('firebase-auth-uid', 'STUDENT', 's1', { studentIds: ['student1'] }).ref(studentPath).getMetadata(), true);
    await check(storage('other-auth-uid', 'STUDENT', 's1', { studentIds: ['student2'] }).ref(studentPath).getMetadata(), false);
    await check(other.ref(studentPath).getMetadata(), false);
    await check(linked.ref(studentPath).getMetadata(), true);
    await check(unrelated.ref(studentPath).getMetadata(), false);
    await check(teacher.ref(studentPath).getMetadata(), true);
    await check(storage('teacher2', 'TEACHER', 's2').ref(studentPath).getMetadata(), false);
    await check(env.unauthenticatedContext().storage().ref(studentPath).getMetadata(), false);
    await check(other.ref(materialPath).getMetadata(), true);
    await check(other.ref(materialPath).put(new Uint8Array([1]), upload), false);
    await check(teacher.ref('schools/s1/materials/new.pdf').put(new Uint8Array([1]), upload), true);
    await check(teacher.ref(studentPath).delete(), false);
    await check(storage('admin1', 'ADMIN').ref(studentPath).delete(), true);
    console.log(`Storage security tests: ${passed}/14 passed`);
  } finally {
    await env.cleanup();
  }
}
main().catch(err => { console.error(err); process.exitCode = 1; });
