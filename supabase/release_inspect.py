"""Read-only management inventory; existing operator token is entered hidden locally."""
import getpass
import json
from pathlib import Path
import sys
import warnings
from datetime import datetime, timezone
from urllib.error import HTTPError, URLError
from urllib.parse import urlsplit
from urllib.request import Request, build_opener, HTTPRedirectHandler

ROOT=Path(__file__).resolve().parent.parent
PROJECT='nhaxnuaxyqkgnlkwpqkw'
EXPECTED=('razorpay-payments','revenex-ai','inspect-document')

class RejectRedirect(HTTPRedirectHandler):
    def redirect_request(self,req,fp,code,msg,headers,newurl):
        raise RuntimeError('Management redirect refused; operator credential target must not change.')

def validate_target(config):
    values=dict(line.split('=',1) for line in config.splitlines() if '=' in line and not line.lstrip().startswith('#'))
    url=urlsplit(values.get('SUPABASE_URL','').strip())
    if url.scheme!='https' or url.hostname!=PROJECT+'.supabase.co' or url.username is not None or url.port not in (None,443) or url.path not in ('','/') or url.query or url.fragment:
        raise RuntimeError('Configured target differs from the prepared demo project; no request sent.')

def summarize_functions(rows):
    if not isinstance(rows,list):raise RuntimeError('Unexpected function inventory; stop reconciliation.')
    return [{'slug':r.get('slug'),'name':r.get('name'),'version':r.get('version'),
             'status':r.get('status'),'verify_jwt':r.get('verify_jwt'),'updated_at':r.get('updated_at')}
            for r in rows if isinstance(r,dict)]

def main():
    if not sys.stdin.isatty():raise RuntimeError('Run in a local interactive terminal; hidden entry is required.')
    validate_target((ROOT/'supabase.local.properties').read_text(encoding='utf-8'))
    with warnings.catch_warnings():
        warnings.simplefilter('error',getpass.GetPassWarning)
        token=getpass.getpass('Existing Supabase operator access token (local, hidden; not saved): ')
    if not token:raise RuntimeError('No token entered; no request sent.')
    result={'project_ref':PROJECT,'checked_at_utc':datetime.now(timezone.utc).isoformat(),
            'applied_migrations':[],'deployed_functions':[],'reconciliation_complete':False}
    opener=build_opener(RejectRedirect())
    def call(path,query=None):
        data=None if query is None else json.dumps({'query':query}).encode()
        req=Request('https://api.supabase.com/v1/projects/'+PROJECT+path,data=data,
                    headers={'Authorization':'Bearer '+token,'Content-Type':'application/json'},method='GET' if query is None else 'POST')
        try:
            with opener.open(req,timeout=30) as response:
                raw=response.read(4*1024*1024+1)
                if len(raw)>4*1024*1024:raise RuntimeError('Inventory exceeds safe report limit; stop.')
                return json.loads(raw)
        except HTTPError as error:
            raise RuntimeError('Management inspection HTTP '+str(error.code)+'; no provider body or credentials logged.') from None
    try:
        project=call('')
        if not isinstance(project,dict) or project.get('id',project.get('ref'))!=PROJECT:raise RuntimeError('Management project mismatch; stop.')
        result['project_status']=project.get('status')
        result['functions']=summarize_functions(call('/functions'))
        result['expected_function_presence']={name:any(r.get('slug')==name for r in result['functions']) for name in EXPECTED}
        inventory=call('/database/query/read-only',(ROOT/'supabase/release_preflight_readonly.sql').read_text(encoding='utf-8'))
        if not isinstance(inventory,list) or len(inventory)!=1 or not isinstance(inventory[0].get('release_inventory'),dict):
            raise RuntimeError('Unexpected schema inventory; compare privately in the authorized SQL editor.')
        result['schema']=inventory[0]['release_inventory']
        if result['schema'].get('migration_history_available'):
            rows=call('/database/query/read-only','select version from supabase_migrations.schema_migrations order by version')
            result['migration_history_versions']=[r['version'] for r in rows]
        else:result['migration_history_versions']=None
        if result['schema'].get('synthetic_marker') is not True:raise RuntimeError('Synthetic tenant marker not confirmed; no deployment permitted.')
        result['inventory_complete']=True
        print('PASS read-only project/function/schema inventory. Reconcile definitions before approving any write.')
    finally:
        token=None
        target=ROOT/('app/build/release/operator-inventory-'+datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%SZ')+'.json')
        target.parent.mkdir(parents=True,exist_ok=True)
        target.write_text(json.dumps(result,indent=2),encoding='utf-8')
        print('Sanitized inventory saved locally under app/build/release; no token saved.')

if __name__=='__main__':
    try:main()
    except KeyboardInterrupt:
        print('Operator inspection cancelled; no deployment performed.',file=sys.stderr)
        sys.exit(1)
    except RuntimeError as error:
        print('Operator inspection incomplete: '+str(error),file=sys.stderr)
        sys.exit(1)
    except (URLError,ValueError,OSError,getpass.GetPassWarning):
        print('Operator inspection incomplete. Confirm token read permissions/project, or run the prepared SELECT-only SQL in the authorized project SQL editor. No deployment performed.',file=sys.stderr)
        sys.exit(1)
