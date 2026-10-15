"""Move Java types between packages from a relocation table. Moves never count as rewritten behavior.

The table is a JSON file: {"domain", "report", "moves": {"old.pkg.Type": "new.pkg.Type", "old.pkg.*": "new.pkg"}}.
A "*" key moves every top-level type of that package (not subpackages). Imports, wildcard imports,
implicit same-package references, fully qualified names and reflective class-name strings are retargeted
in every source root; ownership ledgers follow the moved paths.
"""
from pathlib import Path
import argparse
import hashlib
import json
import re
import subprocess
from migrate_java_namespace import PROJECT, ROOTS, LEXEMES

PACKAGE = re.compile(r'^package\s+([\w.]+);', re.M)
IMPORT = re.compile(r'^import\s+([\w.]+?)(\.\*)?;[ \t]*\r?\n', re.M)
TOP_LEVEL = re.compile(r'^(?:(?:public|final|abstract|sealed|non-sealed|strictfp)\s+)*(?:class|interface|enum|record|@interface)\s+([A-Za-z_$][\w$]*)', re.M)


class Source:
    def __init__(self, root, path):
        self.root, self.path = root, path
        self.before = path.read_bytes()
        self.text = self.before.decode('utf-8-sig')
        self.package = PACKAGE.search(self.text)[1]
        self.types = {self.path.stem} | set(TOP_LEVEL.findall(self.text))

    def fqcns(self):
        return {self.package + '.' + name for name in self.types}


def code_identifiers(text):
    """Simple identifiers used outside comments, strings and the package/import header."""
    body = IMPORT.sub('', PACKAGE.sub('', text))
    names = set()
    for match in LEXEMES.finditer(body):
        value = match.group()
        if value[0] in '"\'/': continue
        start = match.start()
        if start and body[start - 1] == '.': continue
        names.add(value)
    return names


def load_sources():
    sources = []
    for root_name in ROOTS:
        root = PROJECT / root_name
        if root.exists(): sources += [Source(root, path) for path in sorted(root.rglob('*.java'))]
    return sources


def expand(moves, sources):
    by_package = {}
    for source in sources:
        for fqcn in source.fqcns(): by_package.setdefault(source.package, set()).add(fqcn)
    result = {}
    for before, after in moves.items():
        if before.endswith('.*'):
            package = before[:-2]
            assert package in by_package, 'No types in package ' + package
            for fqcn in sorted(by_package[package]): result[fqcn] = after + '.' + fqcn.rsplit('.', 1)[1]
        else:
            result[before] = after
    known = set().union(*(source.fqcns() for source in sources))
    for before, after in result.items():
        assert before in known, 'Unknown type ' + before
        assert after not in known or after in result, 'Destination already exists: ' + after
    return result


def rewrite(source, moves, after_packages, before_packages):
    """Return the retargeted text of one source file."""
    text = source.text
    package_before = source.package
    package_after = package_before
    if source.fqcns() & moves.keys():
        package_after = moves[source.package + '.' + source.path.stem].rsplit('.', 1)[0]
        for fqcn in source.fqcns(): assert moves.get(fqcn, '').rsplit('.', 1)[0] == package_after, 'Split file ' + str(source.path)
    used = code_identifiers(text)
    imports = [(m[1], bool(m[2])) for m in IMPORT.finditer(text)]
    explicit = {name.rsplit('.', 1)[1]: name for name, wildcard in imports if not wildcard}
    wildcards = {name for name, wildcard in imports if wildcard}
    required = set()

    # Types that were visible by simple name through a wildcard or the former package.
    for before, after in moves.items():
        old_package, simple = before.rsplit('.', 1)
        new_package, renamed = after.rsplit('.', 1)
        if simple not in used or simple in source.types: continue
        if simple in explicit and explicit[simple] != before: continue
        if old_package in wildcards or old_package == package_before:
            if new_package != package_after: required.add(after)
    # A moved file keeps seeing the types left in its former package.
    if package_after != package_before:
        for fqcn in before_packages.get(package_before, ()):
            simple = fqcn.rsplit('.', 1)[1]
            if fqcn in moves or simple not in used or simple in source.types: continue
            if simple in explicit and explicit[simple] != fqcn: continue
            required.add(fqcn)

    # Qualified references everywhere: code, javadoc and reflective strings. Longest names first.
    for before in sorted(moves, key=len, reverse=True):
        after = moves[before]
        text = re.sub(r'(?<![\w$.])' + re.escape(before) + r'(?![\w$])', after, text)
        if before.rsplit('.', 1)[1] != after.rsplit('.', 1)[1]:
            raise SystemExit('Renames are not supported by relocate.py: ' + before)
    if package_after != package_before:
        text = PACKAGE.sub('package ' + package_after + ';', text, count=1)

    # Edit only the import lines that change; untouched files keep their exact import block.
    newline = '\r\n' if '\r\n' in text else '\n'
    seen, kept = set(), []
    matches = list(IMPORT.finditer(text))
    for match in matches:
        name, wildcard = match[1], bool(match[2])
        entry = name + ('.*' if wildcard else '')
        redundant = entry in seen
        if wildcard:
            redundant |= name == package_after
            redundant |= name in before_packages and name not in after_packages
        else:
            redundant |= name.rsplit('.', 1)[0] == package_after and name in after_packages.get(package_after, ())
        seen.add(entry)
        kept.append((match, redundant))
    wildcard_now = {m[1] for m, gone in kept if m[2] and not gone}
    explicit_now = {m[1] for m, gone in kept if not m[2] and not gone}
    additions = sorted(fqcn for fqcn in required if fqcn not in explicit_now and fqcn.rsplit('.', 1)[0] not in wildcard_now)
    for start, end in reversed([m.span() for m, gone in kept if gone]): text = text[:start] + text[end:]
    for fqcn in additions:
        line = 'import ' + fqcn + ';' + newline
        current = list(IMPORT.finditer(text))
        later = [m for m in current if m[1] + (m[2] or '') > fqcn]
        if later: position = later[0].start()
        elif current: position = current[-1].end()
        else:
            header = PACKAGE.search(text)
            text = text[:header.end()] + newline + newline + line.rstrip('\r\n') + text[header.end():]
            continue
        text = text[:position] + line + text[position:]
    return text


def warn_shadowing(sources, moves, after_packages):
    """Same-package types take precedence over wildcard imports; report any binding that could change."""
    arrived = {}
    for after in moves.values(): arrived.setdefault(after.rsplit('.', 1)[0], set()).add(after.rsplit('.', 1)[1])
    warnings = []
    for source in sources:
        package = moves.get(source.package + '.' + source.path.stem, source.package + '.x').rsplit('.', 1)[0]
        names = arrived.get(package)
        if not names: continue
        used = code_identifiers(source.text)
        for name, wildcard in ((m[1], bool(m[2])) for m in IMPORT.finditer(source.text)):
            if not wildcard: continue
            for fqcn in after_packages.get(name, ()):
                simple = fqcn.rsplit('.', 1)[1]
                if simple in names and simple in used: warnings.append(f'{source.path}: {simple} may now bind to {package}.{simple} instead of {fqcn}')
    return warnings


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('table', type=Path)
    parser.add_argument('--dry-run', action='store_true')
    args = parser.parse_args()
    table = json.loads(args.table.read_text('utf-8'))
    sources = load_sources()
    moves = expand(table['moves'], sources)

    before_packages, after_packages = {}, {}
    for source in sources:
        for fqcn in source.fqcns():
            before_packages.setdefault(source.package, set()).add(fqcn)
            moved = moves.get(fqcn, fqcn)
            after_packages.setdefault(moved.rsplit('.', 1)[0], set()).add(moved)

    for warning in warn_shadowing(sources, moves, after_packages): print('WARNING', warning)
    report = PROJECT / 'validation' / table['report']
    ledger = PROJECT / 'validation/rewrite-ownership.json'
    review = PROJECT / 'validation/rewrite-declaration-review.json'
    ownership = json.loads(ledger.read_text('utf-8'))
    declarations = json.loads(review.read_text('utf-8')) if review.exists() else None
    original_paths = {row['after']: row['before'] for row in json.loads((PROJECT / 'validation/namespace-migration.json').read_text('utf-8'))['sources']}
    changes = []
    for source in sources:
        after_text = rewrite(source, moves, after_packages, before_packages)
        after = after_text.encode('utf-8')
        package = PACKAGE.search(after_text)[1]
        target = source.root / Path(package.replace('.', '/')) / source.path.name
        if after == source.before and target == source.path: continue
        old_path, new_path = source.path.relative_to(PROJECT).as_posix(), target.relative_to(PROJECT).as_posix()
        changes.append({'before': old_path, 'after': new_path, 'beforeSha256': hashlib.sha256(source.before).hexdigest(),
                        'afterSha256': hashlib.sha256(after).hexdigest(), 'countsAsImplementationRewrite': False})
        if args.dry_run: continue
        if target != source.path:
            assert not target.exists(), target
            target.parent.mkdir(parents=True, exist_ok=True)
            subprocess.run(['git', 'mv', str(source.path), str(target)], cwd=PROJECT, check=True)
            retargeted = ownership['retargetedNamespaceSources']
            retargeted[new_path] = retargeted.pop(old_path, original_paths.get(old_path, old_path))
            for row in ownership['completedImplementations'] + ownership.get('integrationChanges', []):
                if row['file'] == old_path: row['file'] = new_path
            if declarations:
                for row in declarations['entries']:
                    if row['file'] == old_path: row['file'] = new_path
        target.write_bytes(after)
    for source in sources:
        parent = source.path.parent
        while not args.dry_run and parent != source.root and parent.exists() and not any(parent.iterdir()):
            parent.rmdir(); parent = parent.parent
    summary = {'movedTypes': len(moves), 'changedSources': len(changes), 'movedSources': sum(c['before'] != c['after'] for c in changes)}
    if not args.dry_run:
        ledger.write_text(json.dumps(ownership, indent=2) + '\n', encoding='utf-8')
        if declarations: review.write_text(json.dumps(declarations, indent=2) + '\n', encoding='utf-8')
        report.write_text(json.dumps({'domain': table['domain'], 'countsAsImplementationRewrite': False, 'sources': changes}, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(summary))


if __name__ == '__main__': main()
