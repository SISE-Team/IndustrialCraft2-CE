"""Export finite recorded functionality and source API edges for replacement work."""
import argparse
import hashlib
import json
from pathlib import Path
import re
from project_artifacts import PROJECT, RESOURCE_ROOT

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--output', type=Path, default=PROJECT / 'validation/rewrite-contracts.json')
args = parser.parse_args()
ledger_path = PROJECT / 'validation/item-usage-ledger.json'
machine_path = PROJECT / 'validation/full-review-machine-results.json'
ledger = json.loads(ledger_path.read_text('utf-8'))
machine = json.loads(machine_path.read_text('utf-8'))
language = json.loads((RESOURCE_ROOT / 'assets/ic2/lang/en_us.json').read_text('utf-8-sig'))
assert ledger['registeredItems'] == ledger['coveredItems'] == len(ledger['usage']) == 508
assert machine['results']['registeredMachineTypes'] == len(machine['results']['machines']) == 104
imports = []
for source in sorted((PROJECT / 'api/src/main/java').rglob('*.java')):
    for dependency in re.findall(r'^import\s+(ic2ce\.core\.[\w.]+);', source.read_text('utf-8'), re.M):
        imports.append({'source': source.relative_to(PROJECT).as_posix(), 'runtimeDependency': dependency})
items = []
for identifier, cases in sorted(ledger['usage'].items()):
    key = 'item.' + identifier.replace(':', '.')
    items.append({'id': identifier, 'englishDisplayName': language.get(key),
        'recordedOperations': sorted({case['operation'] for case in cases}),
        'historicalEvidence': 'validation/item-usage-ledger.json', 'independentReplacementAccepted': False})
machines = []
for identifier, record in sorted(machine['results']['machines'].items()):
    machines.append({'id': 'ic2:' + identifier, 'recordedBehaviorRequirements': list(record['behavior']),
        'actualGuiRequired': record['checks'].get('opens actual client GUI', False),
        'historicalEvidence': 'validation/full-review-machine-results.json', 'independentControllerReplacementAccepted': False})
digest = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
result = {'purpose': 'Acceptance inventory for continued independent implementation, not proof that every legacy behavior is fully specified.',
    'referenceVersion': machine['version'], 'referenceEvidenceHashes': {'items': digest(ledger_path), 'machines': digest(machine_path)},
    'registeredItemContracts': len(items), 'registeredMachineContracts': len(machines),
    'recordedMachineBehaviorRequirements': sum(len(row['recordedBehaviorRequirements']) for row in machines),
    'sourceImportEdgesFromApiToRuntime': imports, 'apiDependencyScope': 'Explicit source imports only; not a complete compiled dependency graph.',
    'items': items, 'machines': machines,
    'ownershipPolicy': 'Shared engine integration and old-artifact gameplay evidence do not count as a full independent item/controller replacement.'}
assert result['recordedMachineBehaviorRequirements'] == 219
args.output.parent.mkdir(parents=True, exist_ok=True)
args.output.write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'items': 508, 'machines': 104, 'behaviors': 219, 'sourceApiRuntimeEdges': len(imports)}))
