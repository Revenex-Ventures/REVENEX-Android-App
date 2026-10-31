import importlib.util
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[2]
spec=importlib.util.spec_from_file_location('release_inspect',ROOT/'supabase/release_inspect.py')
inspect=importlib.util.module_from_spec(spec);spec.loader.exec_module(inspect)

class ReleasePreparationTests(unittest.TestCase):
    def test_only_the_exact_prepared_https_target_is_accepted(self):
        inspect.validate_target('SUPABASE_URL=https://nhaxnuaxyqkgnlkwpqkw.supabase.co')
        for url in ['https://other.supabase.co','http://nhaxnuaxyqkgnlkwpqkw.supabase.co',
                    'https://secret@nhaxnuaxyqkgnlkwpqkw.supabase.co',
                    'https://nhaxnuaxyqkgnlkwpqkw.supabase.co?token=private']:
            with self.assertRaises(RuntimeError):inspect.validate_target('SUPABASE_URL='+url)

    def test_function_inventory_never_copies_secret_fields(self):
        data=inspect.summarize_functions([{'slug':'revenex-ai','version':2,'secret':'PRIVATE','access_token':'PRIVATE'}])
        self.assertEqual(data[0]['version'],2)
        self.assertNotIn('PRIVATE',str(data))

    def test_malformed_inventory_cannot_be_claimed_complete(self):
        with self.assertRaises(RuntimeError):inspect.summarize_functions({'token':'PRIVATE'})

    def test_management_redirect_cannot_forward_operator_credentials(self):
        with self.assertRaises(RuntimeError):
            inspect.RejectRedirect().redirect_request(None,None,302,'',{},'https://other.example')

if __name__=='__main__':unittest.main()
