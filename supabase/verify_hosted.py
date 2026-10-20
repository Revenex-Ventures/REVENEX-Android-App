"""Read-only hosted checks. Passwords are entered locally, hidden, and never persisted."""
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from urllib.parse import urlparse
import getpass
import json
import sys

ROOT=Path(__file__).resolve().parent.parent

def main():
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

    def call(path,token=None,method="GET",body=None):
        headers={"apikey":key,"Content-Type":"application/json"}
        if token: headers["Authorization"]="Bearer "+token
        request=Request(base+path,data=json.dumps(body).encode() if body is not None else None,headers=headers,method=method)
        try:
            with urlopen(request,timeout=30) as response:
                data=response.read()
                return json.loads(data) if data else None
        except HTTPError as error:
            # Do not print HTTP bodies/headers; those can contain private service diagnostics.
            raise RuntimeError("HTTP "+str(error.code)+" for "+path.split("?")[0]) from None

    report=[]
    for role in ["PRINCIPAL","TEACHER","STUDENT","PARENT"]:
        email=role.lower()+"@revenex.demo"
        password=getpass.getpass("Password for "+email+" (local, hidden): ")
        session=call("/auth/v1/token?grant_type=password",method="POST",body={"email":email,"password":password})
        password=None
        token=session["access_token"]
        try:
            profile=call("/rest/v1/profiles?select=id,role,school_id,session_id",token)
            assert len(profile)==1 and profile[0]["role"]==role,"Expected one correctly provisioned profile"
            profile=profile[0]
            assert profile["school_id"]=="revenex_demo" and profile["session_id"]=="demo_2026","Run only against the synthetic demo school"
            rows=call("/rest/v1/students?select=id,school_id,session_id,section_id",token)
            assert all(row["school_id"]==profile["school_id"] and row["session_id"]==profile["session_id"] for row in rows),"School/session boundary failure"
            if role in ["STUDENT","PARENT"]:
                assert [row["id"] for row in rows]==["demo_student"],"Unexpected student/parent access"
            fees=call("/rest/v1/fee_records?select=id,school_id,session_id",token)
            if role=="TEACHER": assert not fees,"Teacher must not see fee balances"
            for table in ["classes","sections","timetables","notices","assignments","marks","attendance"]:
                accessible=call("/rest/v1/"+table+"?select=id,school_id,session_id",token)
                assert all(row["school_id"]==profile["school_id"] and row["session_id"]==profile["session_id"] for row in accessible),"Context mismatch in "+table
            inbox=call("/rest/v1/notifications?select=id,recipient_id,school_id,session_id",token)
            assert all(row["recipient_id"]==profile["id"] for row in inbox),"Inbox recipient boundary failure"
            report.append({"role":role,"passed":True,"students_visible":len(rows),"notifications_visible":len(inbox)})
            print("PASS hosted login, profile, scoped reads and inbox: "+role)
        finally:
            call("/auth/v1/logout?scope=local",token,method="POST")
            token=None
            session=None
    output=ROOT/"app/build/reports/supabase-hosted.json"
    output.parent.mkdir(parents=True,exist_ok=True)
    output.write_text(json.dumps({"checks":report,"writes_verified":False,"realtime_transport_verified":False,"storage_service_verified":False},indent=2),encoding="utf-8")
    print("Read-only checks finished. Verify writes, private file delivery and two-device Realtime separately.")

if __name__=="__main__":
    try: main()
    except (Exception,KeyboardInterrupt) as error:
        if isinstance(error,(RuntimeError,AssertionError,FileNotFoundError)):
            print("Hosted check incomplete: "+str(error),file=sys.stderr)
        else: print("Hosted check incomplete. Check local configuration, credentials and network.",file=sys.stderr)
        sys.exit(1)
