"""Scan release candidates and optional Git history. Reports locations, never values."""
from pathlib import Path
import argparse,base64,json,re,subprocess,sys
ROOT=Path(__file__).resolve().parent.parent
PATTERNS={
 'model API credential':re.compile(r"sk-(?:proj-)?[A-Za-z0-9_-]{24,}"),
 'GitHub credential':re.compile(r"gh[pousr]_[A-Za-z0-9]{30,}"),
 'Supabase private credential':re.compile(r"sb_secret_[A-Za-z0-9_-]{15,}"),
 'private key material':re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----[\r\n]+[A-Za-z0-9+/=\r\n]{40,}"),
}
JWT=re.compile(r"eyJ[A-Za-z0-9_-]+\.eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]{20,}")
PRIVATE_LITERAL=re.compile(r"""["']?(?:OPENAI_API_KEY|GEMINI_API_KEY|RAZORPAY_KEY_SECRET|SUPABASE_SERVICE_ROLE_KEY|SUPABASE_ACCESS_TOKEN|SUPABASE_DB_PASSWORD)["']?\s*[:=]\s*["']([^"'\r\n]+)["']""")
def git(*args):return subprocess.check_output(['git',*args],cwd=ROOT)
def scan(text,label,findings):
 for kind,pattern in PATTERNS.items():
  for match in pattern.finditer(text):findings.append((label,text.count('\n',0,match.start())+1,kind))
 for match in JWT.finditer(text):
  try:
   payload=match.group().split('.')[1];claims=json.loads(base64.urlsafe_b64decode(payload+'='*(-len(payload)%4)))
   if claims.get('role')=='service_role':findings.append((label,text.count('\n',0,match.start())+1,'Supabase service-role JWT'))
  except (ValueError,UnicodeError):pass
 for match in PRIVATE_LITERAL.finditer(text):
  value=match.group(1)
  if len(value)>=12 and not value.lower().startswith(('test','unit','example','placeholder','your','dummy','must-never','<','${')):
   findings.append((label,text.count('\n',0,match.start())+1,'private credential literal'))
def main():
 parser=argparse.ArgumentParser();parser.add_argument('--history',action='store_true');args=parser.parse_args();findings=[]
 files=git('ls-files','--cached','--others','--exclude-standard','-z').decode().split('\0')
 for name in sorted(set(files)-{''}):
  path=ROOT/name
  if path.name in ['.env','supabase.local.properties','local.properties','google-services.json'] or path.suffix in ['.jks','.keystore','.pem','.key']:
   findings.append((name,0,'private/local configuration must not be tracked'));continue
  if not path.is_file() or path.stat().st_size>2_000_000:continue
  try:text=path.read_text(encoding='utf-8')
  except UnicodeError:continue
  scan(text,name,findings)
 if args.history:
  process=subprocess.Popen(['git','cat-file','--batch'],cwd=ROOT,stdin=subprocess.PIPE,stdout=subprocess.PIPE)
  try:
   for entry in git('rev-list','--objects','--all').decode().splitlines():
    oid,_,name=entry.partition(' ')
    if not name:continue
    process.stdin.write((oid+'\n').encode());process.stdin.flush()
    header=process.stdout.readline().decode().split()
    if len(header)!=3:raise RuntimeError('Invalid Git object metadata')
    size=int(header[2]);raw=process.stdout.read(size);process.stdout.read(1)
    if header[1]!='blob' or size>2_000_000:continue
    try:text=raw.decode('utf-8')
    except UnicodeError:continue
    scan(text,'history:'+oid[:12]+':'+name,findings)
  finally:
   process.stdin.close();process.wait()
 if findings:
  for label,line,kind in sorted(set(findings)):print(f'{label}:{line}: {kind}')
  print('Secret scan failed; credential values are intentionally omitted.');return 1
 print('PASS release credential scan'+(' and Git history' if args.history else ''));return 0
if __name__=='__main__':sys.exit(main())
