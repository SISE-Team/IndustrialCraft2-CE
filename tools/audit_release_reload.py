"""Read the closed isolated world after a regular, uninstrumented IC2 reload."""
import argparse
import gzip
import hashlib
import json
from pathlib import Path
import zipfile
from audit_saved_fixtures import NbtReader

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--world', type=Path, required=True)
parser.add_argument('--metadata', type=Path, required=True)
parser.add_argument('--artifact', type=Path, required=True)
parser.add_argument('--base', type=Path, required=True)
parser.add_argument('--output', type=Path, required=True)
args = parser.parse_args()
workspace = Path(__file__).resolve().parents[2]
world = args.world.resolve()
assert world.is_relative_to(workspace) and world.parent.name.startswith('test-ic2-')
metadata = json.loads(args.metadata.read_text(encoding='utf-8'))
assert metadata['exitCode'] == 0, metadata
assert not any('ic2.verify' in part or 'ic2.resume' in part for part in metadata['command'])
with zipfile.ZipFile(args.artifact) as jar:
    assert not any(name.startswith(('ic2/verification/', 'ic2ce/verification/')) for name in jar.namelist())
storage = world / 'data/ic2_verification/command_storage.dat'
data = NbtReader(gzip.decompress(storage.read_bytes())).root()['data']['contents']['reload']
assert data['completed'] == 1
summary = data['summary']
expected = json.loads(args.base.read_text(encoding='utf-8'))['machines']
assert len(expected) == 104 and sorted(summary['passed']) == sorted(expected)
assert summary['passedCount'] == 104 and summary['failuresCount'] == 0
assert summary['failures'] == []
assert summary['persistedChecksCount'] == 16 and len(summary['persistedChecks']) == 16
result = {
    'complete': True,
    'artifact': args.artifact.name,
    'sha256': hashlib.sha256(args.artifact.read_bytes()).hexdigest(),
    'verificationJvmFlags': False,
    'verificationClassesPresent': False,
    'loadedMachineTypes': summary['passedCount'],
    'persistedRuntimeChecks': summary['persistedChecks'],
    'method': 'Vanilla commands read all 104 actual loaded block entities after regular JAR reload; only command storage is written',
    'closedWorld': str(world),
    'serverExitCode': metadata['exitCode'],
    'failures': summary['failures'],
}
args.output.write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
print(json.dumps({k: v for k, v in result.items() if k != 'persistedRuntimeChecks'}))
