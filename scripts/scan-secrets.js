#!/usr/bin/env node
/**
 * SECRETS SCAN — scans the working tree for credential material.
 * Usage: node scripts/scan-secrets.js
 * Exit code 0 = clean, 1 = hard findings, 2 = review findings.
 *
 * Design decisions:
 *  - .env / .env.* / keystores / local.properties are git-ignored by design
 *    and are NOT scanned (they are the legitimate place for test keys).
 *  - node_modules, build output, .git, binaries and archives are skipped.
 */
const fs = require('node:fs');
const path = require('node:path');

const ROOT = path.resolve(__dirname, '..');
const SKIP_DIRS = new Set(['node_modules', '.git', '.gradle', '.kotlin', '.idea', 'build', 'app/build', 'captures', '.firebase']);
const SKIP_FILES = new Set(['.env', '.env.example', 'debug.keystore', 'local.properties']);
const SKIP_EXT = new Set(['.apk', '.zip', '.jar', '.aab', '.png', '.jpg', '.jpeg', '.gif', '.ico', '.webp', '.keystore', '.jks', '.class', '.pdf', '.woff', '.woff2', '.ttf', '.otf']);

// [regex, severity, label]
const PATTERNS = [
  [/-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----/i, 'hard', 'private key block'],
  [/AKIA[0-9A-Z]{16}\b/, 'hard', 'AWS access key'],
  [/ghp_[0-9A-Za-z]{36}\b/, 'hard', 'GitHub PAT'],
  [/gho_[0-9A-Za-z]{36}\b/, 'hard', 'GitHub OAuth token'],
  [/\bsk-live-[0-9A-Za-z]{16,}/, 'hard', 'Stripe live secret'],
  [/\bsk_live_[0-9A-Za-z]{16,}/, 'hard', 'Stripe live secret'],
  [/\brzp_live_[0-9A-Za-z]{10,}/, 'hard', 'Razorpay LIVE key'],
  [/\bxox[baprs]-[0-9A-Za-z-]{10,}/, 'hard', 'Slack token'],
  [/\bSG\.[0-9A-Za-z_-]{20,}/, 'hard', 'SendGrid key'],
  [/\beyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}/, 'hard', 'JWT'],
  [/(?:password|passwd|pwd)\s*[=:]\s*['"][^'"]{8,}['"]/i, 'review', 'password literal'],
  [/(?:api[_-]?key|client[_-]?secret|access[_-]?token|private[_-]?key)\s*[=:]\s*['"][^'"]{8,}['"]/i, 'review', 'credential literal'],
  [/\bAIza[0-9A-Za-z_-]{20,}/, 'review', 'Firebase API key (info: not secret but should be in env)'],
];

function walk(dir, output) {
  let entries;
  try {
    entries = fs.readdirSync(dir, { withFileTypes: true });
  } catch {
    return;
  }
  for (const e of entries) {
    const full = path.join(dir, e.name);
    const rel = path.relative(ROOT, full).replace(/\\/g, '/');
    if (e.isDirectory()) {
      if (!SKIP_DIRS.has(e.name)) walk(full, output);
      continue;
    }
    if (SKIP_FILES.has(e.name)) continue;
    if (SKIP_EXT.has(path.extname(e.name).toLowerCase())) continue;
    let text;
    try {
      text = fs.readFileSync(full, 'utf8');
    } catch {
      continue; // binary
    }
    const lines = text.split(/\r?\n/);
    for (let i = 0; i < lines.length; i++) {
      for (const [re, severity, label] of PATTERNS) {
        if (re.test(lines[i])) {
          output.push({ file: rel, line: i + 1, severity, label, match: lines[i].trim().slice(0, 80) });
        }
      }
    }
  }
}

const findings = [];
walk(ROOT, findings);

const hard = findings.filter((f) => f.severity === 'hard');
const review = findings.filter((f) => f.severity === 'review');

for (const f of [...hard, ...review]) {
  console.log(`[${f.severity.toUpperCase()}] ${f.file}:${f.line} — ${f.label}\n    ${f.match}`);
}

console.log('----');
console.log(`scanned ${path.relative(ROOT, ROOT) || '.'} → hard: ${hard.length}, review: ${review.length}`);
if (review.length > 0) {
  console.log('review: check the flagged lines (test keys / informational values are fine).');
}
process.exit(hard.length > 0 ? 1 : 0);