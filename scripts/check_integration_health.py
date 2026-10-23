"""Read-only public reachability check; never proves authenticated workflows or capture."""
import json
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
from datetime import datetime, timezone

def main():
    root = Path(__file__).resolve().parent.parent
    config = {}
    for line in (root / 'supabase.local.properties').read_text(encoding='utf-8').splitlines():
        if '=' in line and not line.lstrip().startswith('#'):
            k, v = line.split('=', 1)
            config[k.strip()] = v.strip()
    base = config['SUPABASE_URL'].rstrip('/')
    key = config['SUPABASE_PUBLISHABLE_KEY']
    if not base.startswith('https://') or not key.startswith('sb_publishable_'):
        raise SystemExit('Expected HTTPS and public publishable configuration only')
    checks = []
    paths = [('/auth/v1/health', 'GET'),
             ('/rest/v1/students?select=id&limit=0', 'GET'),
             ('/rest/v1/fee_records?select=id&limit=0', 'GET'),
             ('/rest/v1/razorpay_orders?select=id&limit=0', 'GET'),
             ('/functions/v1/razorpay-payments', 'POST'),
             ('/functions/v1/revenex-ai', 'POST')]
    for path, method in paths:
        req = Request(base + path, method=method,
                      data=b'{}' if method == 'POST' else None,
                      headers={'apikey': key, 'Content-Type': 'application/json'})
        try:
            with urlopen(req, timeout=20) as response:
                code = response.status
        except HTTPError as error:
            code = error.code
        except (URLError, TimeoutError):
            code = 'network_error'
        checks.append({'path': path.split('?')[0], 'status': code})
        print(path.split('?')[0], code)
    report = {'checked_at_utc': datetime.now(timezone.utc).isoformat(),
              'checks': checks, 'authenticated_flows_verified': False,
              'razorpay_capture_verified': False,
              'note': '401 is anonymous rejection, not proof of configuration. No login, payment or database write performed.'}
    target = root / 'app/build/reports/integration-health.json'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(report, indent=2), encoding='utf-8')

if __name__ == '__main__':
    main()

