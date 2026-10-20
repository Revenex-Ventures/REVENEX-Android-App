"""CI build configuration: a strict public-value allowlist, never a private secret."""
import os
from pathlib import Path
from urllib.parse import urlparse

url=os.environ.get('SUPABASE_URL','').strip()
key=os.environ.get('SUPABASE_PUBLISHABLE_KEY','').strip()
if not url and not key:
    print('Public backend config unavailable; building the independent local demo.')
else:
    parsed=urlparse(url)
    if parsed.scheme!='https' or not parsed.hostname or parsed.path not in ('','/') or parsed.username or parsed.query or parsed.fragment:
        raise SystemExit('Invalid public Supabase URL.')
    if not key.startswith('sb_publishable_') or any(c.isspace() for c in key):
        raise SystemExit('Only a public Supabase publishable key is allowed in Android.')
    Path('supabase.local.properties').write_text('SUPABASE_URL='+url+'\nSUPABASE_PUBLISHABLE_KEY='+key+'\n',encoding='utf-8')
    print('Configured the two public Android backend values.')
