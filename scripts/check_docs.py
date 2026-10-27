"""Validate local links in maintained project docs without fetching external URLs."""
from pathlib import Path
import re
import sys
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parent.parent
LINK = re.compile(r'\[[^\]\n]*\]\(([^)\n]+)\)')

def main():
    files = [ROOT / 'README.md', ROOT / 'scripts/README.md',
             ROOT / 'supabase/README.md', ROOT / 'supabase/RAZORPAY_SETUP.md']
    files += sorted((ROOT / 'docs').rglob('*.md'))
    errors = []
    checked = 0
    for source in files:
        if not source.is_file():
            errors.append(f'{source.relative_to(ROOT)}: missing maintained document')
            continue
        text = source.read_text(encoding='utf-8')
        if any(ord(c) < 32 and c not in '\n\r\t' for c in text):
            errors.append(f'{source.relative_to(ROOT)}: unexpected control character')
        for match in LINK.finditer(text):
            target = match.group(1).strip().strip('<>')
            if target.startswith('#') or urlsplit(target).scheme:
                continue
            path = unquote(target.split('#', 1)[0])
            if not path:
                continue
            resolved = (source.parent / path).resolve()
            checked += 1
            if not resolved.is_relative_to(ROOT) or not resolved.exists():
                line = text.count('\n', 0, match.start()) + 1
                errors.append(f'{source.relative_to(ROOT)}:{line}: broken local link: {target}')
    if errors:
        print('\n'.join(errors))
        return 1
    print(f'PASS {len(files)} maintained documents; {checked} local links')
    return 0

if __name__ == '__main__':
    sys.exit(main())
