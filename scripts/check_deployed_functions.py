"""Deployment liveness checks; no model inference, payment, login or database write."""
import os
from urllib.request import Request,urlopen
from urllib.error import HTTPError

base=os.environ['SUPABASE_URL'].rstrip('/')
for name in ['revenex-ai','razorpay-payments']:
    request=Request(base+'/functions/v1/'+name,method='POST',data=b'{}',headers={'apikey':os.environ['SUPABASE_PUBLISHABLE_KEY'],'Content-Type':'application/json'})
    try:
        with urlopen(request,timeout=30) as response: code=response.status
    except HTTPError as error: code=error.code
    if code!=401:raise SystemExit(f'{name}: expected anonymous rejection (401), got {code}')
    print(name+': deployed and rejects anonymous callers')
