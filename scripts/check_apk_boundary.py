"""Assert that the server prompt and private configuration names were not packaged."""
import sys,zipfile
from pathlib import Path
forbidden=[b'OPENAI_API_KEY',b'SUPABASE_SERVICE_ROLE_KEY',b'RAZORPAY_KEY_SECRET',b'You are Revenex AI, an intelligent, warm, precise AI assistant']
with zipfile.ZipFile(Path(sys.argv[1])) as apk:
 for name in apk.namelist():
  data=apk.read(name)
  if any(marker in data for marker in forbidden):raise SystemExit('Server-only configuration unexpectedly packaged in '+name)
print('PASS APK server-prompt/private-configuration boundary')
