import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

spec=importlib.util.spec_from_file_location('verify_hosted',Path(__file__).resolve().parents[1]/'verify_hosted.py')
module=importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class HostedVerifierTest(unittest.TestCase):
    def test_known_auth_codes_produce_fixed_messages_without_echoing_provider_text(self):
        message=module.safe_auth_failure(400,json.dumps({'error_code':'invalid_credentials','message':'PRIVATE_RESPONSE_SENTINEL'}))
        self.assertIn('password was rejected',message)
        self.assertNotIn('PRIVATE_RESPONSE_SENTINEL',message)
        self.assertIn('not confirmed',module.safe_auth_failure(400,'{"code":"email_not_confirmed"}'))

    def test_unknown_malformed_and_nonobject_responses_never_leak(self):
        for body in ['PRIVATE_RESPONSE_SENTINEL','[]','null','{"code":"PRIVATE_RESPONSE_SENTINEL"}']:
            self.assertNotIn('PRIVATE_RESPONSE_SENTINEL',module.safe_auth_failure(400,body))

    def test_completed_role_evidence_survives_an_incomplete_run(self):
        with tempfile.TemporaryDirectory() as directory:
            target=Path(directory)/'run.json'
            module.save_progress(target,{'requested_roles':['PRINCIPAL','TEACHER']},[{'role':'PRINCIPAL','passed':True}])
            report=json.loads(target.read_text())
            self.assertFalse(report['complete'])
            self.assertEqual(report['checks'],[{'role':'PRINCIPAL','passed':True}])
            self.assertEqual(report,json.loads((target.parent/'supabase-hosted.json').read_text()))

if __name__=='__main__':unittest.main()
