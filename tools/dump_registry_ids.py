"""Extract registration IDs and block-entity/block pairings from platform sources.

Relocating definitions must leave this snapshot unchanged: `dump_registry_ids.py --write` records a baseline,
`dump_registry_ids.py` compares the current sources with it. Owners are compared by field name only, so a field
may move to another holder class without changing the snapshot.
"""
from pathlib import Path
import argparse
import collections
import json
import re

PROJECT = Path(__file__).resolve().parents[1]
PLATFORM = PROJECT / 'platform/neoforge/src/main/java'
BASELINE = PROJECT / 'validation/registry-id-baseline.json'
COMMENTS = re.compile(r'//[^\n]*|/\*[\s\S]*?\*/')
# Registration helpers may live on another class; only the registered IDs are compared.
CALL = re.compile(r'\b(register\w*|ManagedMenus\.(?:block|item))\(\s*"([^"]+)"')
FIELD = re.compile(r'\bstatic\s+final\s+[\w<>?,\s.\[\]]+?\s+([A-Z][A-Z0-9_]*)\s*=\s*(?:[\w.]+\.)?(?:register\w*|ManagedMenus\.(?:block|item))\(\s*"([^"]+)"(.*?);', re.S)
BLOCK_REFERENCE = re.compile(r'\b[A-Z]\w*Blocks?\w*\.([A-Z][A-Z0-9_]*)\b|\bBlockDefinitions\.([A-Z][A-Z0-9_]*)\b')


def snapshot():
    calls, fields, entities = {}, {}, {}
    for source in sorted(PLATFORM.rglob('*.java')):
        text = COMMENTS.sub('', source.read_text('utf-8'))
        for _, name in CALL.findall(text): calls.setdefault(source, []).append(name)
        for field, name, arguments in FIELD.findall(text):
            fields[name + ' ' + field] = True
            if 'BlockEntityType' in text and '::new' in arguments:
                entities[name] = sorted(a or b for a, b in BLOCK_REFERENCE.findall(arguments))
    return {'registrationCalls': sorted(name for names in calls.values() for name in names),
            'definitionFields': sorted(fields), 'blockEntityBlocks': dict(sorted(entities.items()))}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--write', action='store_true')
    args = parser.parse_args()
    current = snapshot()
    if args.write:
        BASELINE.write_text(json.dumps(current, indent=2) + '\n', encoding='utf-8')
        print(json.dumps({key: len(value) for key, value in current.items()}))
        return
    baseline = json.loads(BASELINE.read_text('utf-8'))
    if isinstance(baseline['registrationCalls'], dict):
        baseline['registrationCalls'] = sorted(n for names in baseline['registrationCalls'].values() for n in names)
    differences = []
    for key in baseline:
        if baseline[key] == current[key]: continue
        if isinstance(baseline[key], dict):
            for name in sorted(baseline[key].keys() | current[key].keys()):
                if baseline[key].get(name) != current[key].get(name):
                    differences.append(f'{key}.{name}: {baseline[key].get(name)} -> {current[key].get(name)}')
        else:
            before, after = collections.Counter(baseline[key]), collections.Counter(current[key])
            differences += [f'{key}: missing {v}' for v in sorted((before - after).elements())]
            differences += [f'{key}: added {v}' for v in sorted((after - before).elements())]
    if differences: raise SystemExit('Registry snapshot changed:\n' + '\n'.join(differences[:200]))
    print('Registry snapshot unchanged')


if __name__ == '__main__': main()
