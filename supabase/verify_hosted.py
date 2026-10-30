"""Read-only hosted checks. Passwords are entered locally, hidden, and never persisted."""
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from urllib.parse import urlparse
import getpass
import json
import sys
import argparse
import warnings
from datetime import datetime, timezone
from urllib.parse import urlencode

ROOT=Path(__file__).resolve().parent.parent

def require(condition,message):
    if not condition:raise RuntimeError(message)

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--roles',nargs='+',choices=['PRINCIPAL','TEACHER','STUDENT','PARENT'],default=['PRINCIPAL','TEACHER','STUDENT'])
    parser.add_argument('--required-schema',type=int,default=19)
    args=parser.parse_args()
    if not sys.stdin.isatty():
        raise RuntimeError("Run this check in your local interactive terminal so password input stays hidden.")
    config={}
    for line in (ROOT/"supabase.local.properties").read_text(encoding="utf-8").splitlines():
        if line.strip() and not line.lstrip().startswith("#") and "=" in line:
            name,value=line.split("=",1);config[name.strip()]=value.strip()
    base=config.get("SUPABASE_URL","").rstrip("/")
    key=config.get("SUPABASE_PUBLISHABLE_KEY","")
    parsed=urlparse(base)
    if parsed.scheme!="https" or not parsed.hostname or parsed.username or parsed.query or not key.startswith("sb_publishable_"):
        raise RuntimeError("Invalid public-only local configuration.")

    def call(path,token=None,method="GET",body=None,count=False):
        headers={"apikey":key,"Content-Type":"application/json"}
        if count:headers['Prefer']='count=exact'
        if token: headers["Authorization"]="Bearer "+token
        request=Request(base+path,data=json.dumps(body).encode() if body is not None else None,headers=headers,method=method)
        try:
            with urlopen(request,timeout=30) as response:
                data=response.read()
                result=json.loads(data) if data else None
                if count:
                    total=response.headers.get('Content-Range','').rsplit('/',1)[-1]
                    if not total.isdigit():raise RuntimeError('Server did not provide an exact row count')
                    return result,int(total)
                return result
        except HTTPError as error:
            # Do not print HTTP bodies/headers; those can contain private service diagnostics.
            raise RuntimeError("HTTP "+str(error.code)+" for "+path.split("?")[0]) from None

    def read_table(table,token,selection):
        rows=[];seen=set();after=None;expected=None
        while True:
            params={'select':selection,'order':'id.asc','limit':'200'}
            if after is not None:params['id']='gt.'+after
            page,remaining=call('/rest/v1/'+table+'?'+urlencode(params),token,count=True)
            if expected is None:expected=remaining
            if expected>100000 or remaining!=expected-len(rows) or len(page)>remaining or (not page and remaining):
                raise RuntimeError('School data changed or completeness could not be verified for '+table)
            for row in page:
                if row['id'] in seen:raise RuntimeError('Repeated school record during pagination')
                seen.add(row['id']);rows.append(row)
            if len(rows)==expected:
                _,confirmed=call('/rest/v1/'+table+'?'+urlencode({'select':'id','order':'id.asc','limit':'1'}),token,count=True)
                if confirmed!=expected:raise RuntimeError('School count changed before verification completed')
                return rows
            after=page[-1]['id']

    report=[]
    for role in args.roles:
        email=role.lower()+"@revenex.demo"
        with warnings.catch_warnings():
            # Never fall back to an echoing password prompt on an unsuitable terminal.
            warnings.simplefilter('error',getpass.GetPassWarning)
            password=getpass.getpass("Password for "+email+" (local, hidden): ")
        session=call("/auth/v1/token?grant_type=password",method="POST",body={"email":email,"password":password})
        password=None
        token=session["access_token"]
        try:
            profile=call("/rest/v1/profiles?select=id,role,school_id,session_id",token)
            require(len(profile)==1 and profile[0]["role"]==role,"Expected one correctly provisioned profile")
            profile=profile[0]
            require(profile["school_id"]=="revenex_demo" and profile["session_id"]=="demo_2026","Run only against the synthetic demo school")
            capabilities=call('/rest/v1/rpc/erp_capabilities',token,method='POST',body={})
            if not capabilities.get('loaded') or capabilities.get('schema_version',0)<args.required_schema:
                raise RuntimeError('Required deployed schema capability version is unavailable; do not apply migrations blindly')
            rows=read_table('students',token,'id,school_id,session_id,section_id')
            require(all(row["school_id"]==profile["school_id"] and row["session_id"]==profile["session_id"] for row in rows),"School/session boundary failure")
            if role in ["STUDENT","PARENT"]:
                require([row["id"] for row in rows]==["demo_student"],"Unexpected student/parent access")
            fees=read_table('fee_records',token,'id,school_id,session_id')
            if role=="TEACHER": require(not fees,"Teacher must not see fee balances")
            for table in ["classes","sections","timetables","notices","assignments","marks","attendance"]:
                accessible=read_table(table,token,'id,school_id,session_id')
                require(all(row["school_id"]==profile["school_id"] and row["session_id"]==profile["session_id"] for row in accessible),"Context mismatch in "+table)
            inbox=read_table('notifications',token,'id,recipient_id,school_id,session_id')
            require(all(row["recipient_id"]==profile["id"] for row in inbox),"Inbox recipient boundary failure")
            report.append({"role":role,"passed":True,"schema_capability_version":capabilities['schema_version'],"students_visible":len(rows),"notifications_visible":len(inbox)})
            print("PASS hosted login, profile, scoped reads and inbox: "+role)
        finally:
            call("/auth/v1/logout?scope=local",token,method="POST")
            token=None
            session=None
    output=ROOT/"app/build/reports/supabase-hosted.json"
    output.parent.mkdir(parents=True,exist_ok=True)
    output.write_text(json.dumps({"checked_at_utc":datetime.now(timezone.utc).isoformat(),"environment_host":parsed.hostname,"school":"revenex_demo","session":"demo_2026","checks":report,"schema_definitions_compared":False,"writes_verified":False,"realtime_transport_verified":False,"storage_service_verified":False},indent=2),encoding="utf-8")
    print("Read-only checks finished. Verify writes, private file delivery and two-device Realtime separately.")

if __name__=="__main__":
    try: main()
    except (Exception,KeyboardInterrupt) as error:
        if isinstance(error,(RuntimeError,AssertionError,FileNotFoundError)):
            print("Hosted check incomplete: "+str(error),file=sys.stderr)
        else: print("Hosted check incomplete. Check local configuration, credentials and network.",file=sys.stderr)
        sys.exit(1)
