"""Prepare a versioned demo manifest only. Never migrate, deploy, seed or enable AI."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys
from datetime import datetime, timezone

ROOT=Path(__file__).resolve().parent.parent
PROJECT='nhaxnuaxyqkgnlkwpqkw'
FUNCTIONS=['razorpay-payments','revenex-ai','inspect-document']

def git(*args):
    return subprocess.check_output(['git',*args],cwd=ROOT)

def manifest(source):
    if not re.fullmatch(r'HEAD|[0-9a-fA-F]{7,40}',source):raise ValueError('Use HEAD or a commit hash.')
    sha=git('rev-parse','--verify',source+'^{commit}').decode().strip()
    subprocess.run(['git','merge-base','--is-ancestor','f7b881d1',sha],cwd=ROOT,check=True)
    paths=git('ls-tree','-r','--name-only',sha).decode().splitlines()
    migrations=[p for p in paths if re.fullmatch(r'supabase/migrations/20261030002[0-4]_.+\.sql',p)]
    selected=migrations+[p for p in paths if p.startswith('supabase/functions/')]+['supabase/config.toml',
      'app/src/main/java/com/example/data/datasource/SupabaseFileStore.kt',
      'app/src/main/java/com/example/data/datasource/ResultBatch.kt',
      'app/src/main/java/com/example/data/datasource/CompleteSchoolRead.kt']
    subprocess.run(['git','diff','--exit-code',sha,'--',*selected],cwd=ROOT,check=True,stdout=subprocess.DEVNULL)
    files={p:{'sha256':hashlib.sha256(git('show',sha+':'+p)).hexdigest(),'git_blob':git('rev-parse',sha+':'+p).decode().strip()} for p in selected}
    export=ROOT/'app/build/release'/('source-'+sha)
    for path in selected:
        target=export/path
        if not target.resolve().is_relative_to((ROOT/'app/build/release').resolve()):raise ValueError('Export path escapes release directory.')
        content=git('show',sha+':'+path)
        if target.exists() and target.read_bytes()!=content:raise ValueError('Existing release export differs; do not overwrite it.')
        target.parent.mkdir(parents=True,exist_ok=True)
        target.write_bytes(content)
    return {'source_commit':sha,'base_commit':git('rev-parse','f7b881d1').decode().strip(),
      'project_ref':PROJECT,'tenant':'revenex_demo','session':'demo_2026','cli_version':'2.121.0',
      'prepared_at_utc':datetime.now(timezone.utc).isoformat(),'files':files,'server_export_directory':str(export),
      'migration_order_review_only':sorted(migrations),'confirmed_pending_migrations':None,
      'function_order':FUNCTIONS,'ai_model_execution_authorized':False,'target_reconciled':False,
      'applied_migrations':[],'deployed_functions':[],
      'function_commands_after_reconciliation':[['npx','--yes','supabase@2.121.0','--workdir',str(export),'functions','deploy',f,'--project-ref',PROJECT,'--use-api'] for f in FUNCTIONS],
      'safety':'No operation executed. Reconcile actual definitions/history; never db push, reset, seed or automatically repair history.'}

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--source',default='HEAD');args=parser.parse_args()
    report=manifest(args.source);out=ROOT/'app/build/release/release-manifest.json';out.parent.mkdir(parents=True,exist_ok=True)
    out.write_text(json.dumps(report,indent=2),encoding='utf-8')
    print('Prepared source '+report['source_commit']+' for demo '+PROJECT+'. No deployment performed.')

if __name__=='__main__':
    try:main()
    except (ValueError,subprocess.CalledProcessError):
        print('Release preparation blocked: commit/export/source mismatch. No deployment performed.',file=sys.stderr)
        sys.exit(1)
