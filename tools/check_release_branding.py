"""Check project branding, optional author attribution, and release separation."""
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile
from project_artifacts import PROJECT, PROPERTIES, artifact_path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--baseline', type=Path)
parser.add_argument('--output', type=Path)
parser.add_argument('--asm-jar', type=Path, help='ASM 9.9.1 JAR for comparing debug-variable-name differences')
parser.add_argument('--java', type=Path, help='Java 25 executable for the optional class comparison')
args = parser.parse_args()
credit = PROPERTIES['mod_authors'].split(';')[-1].strip()
author = credit.split(' (', 1)[0].lower().encode('utf-8')
assert author and '(IndustrialCraft2-CE port)' in credit
for key in ('mod_archive_name','mod_name','mod_version','mod_group_id'):
    assert author not in PROPERTIES[key].lower().encode('utf-8'), key
assert PROPERTIES['mod_id'] == 'ic2'
failures = []
for source in (PROJECT/'src').rglob('*'):
    if source.is_file() and (author in str(source.relative_to(PROJECT)).lower().encode('utf-8')
                             or author in source.read_bytes().lower()):
        failures.append('Author marker in source: '+str(source.relative_to(PROJECT)))
for classifier in (None, 'sources'):
    path = artifact_path(classifier)
    assert path.is_file(), path
    assert author not in path.name.lower().encode('utf-8')
    with zipfile.ZipFile(path) as jar:
        for name in jar.namelist():
            if author in name.lower().encode('utf-8'):
                failures.append('Author marker in archive path: '+name)
            data = jar.read(name)
            if author in data.lower():
                if name == 'META-INF/neoforge.mods.toml':
                    for line in data.splitlines():
                        if author in line.lower() and not line.strip().startswith(b'authors='):
                            failures.append('Author marker outside authors metadata: '+name)
                else:
                    failures.append('Author marker in archive content: '+name)
            if classifier is None and name.startswith(('ic2/verification/', 'net/minecraft/', 'org/lwjgl/')):
                failures.append('Test or game classes in release: '+name)
result = {'project':PROPERTIES['mod_archive_name'], 'version':PROPERTIES['mod_version'],
          'release':artifact_path().name, 'sources':artifact_path('sources').name,
          'authorAttributionPreserved':True, 'failures':failures, 'complete':not failures}
if args.baseline:
    with zipfile.ZipFile(args.baseline) as old, zipfile.ZipFile(artifact_path()) as new:
        gameplay = lambda n: n.endswith('.class') or n.startswith(('assets/', 'data/'))
        names = {n for n in old.namelist()+new.namelist() if gameplay(n)}
        old_names = set(old.namelist()); new_names = set(new.namelist())
        changed = [n for n in sorted(names) if n not in old_names or n not in new_names or old.read(n) != new.read(n)]
        result['baselineGameplayEntries'] = len(names)
        changed_resources = [n for n in changed if not n.endswith('.class')]
        assert not changed_resources, changed_resources
        result['byteDifferentClassEntries'] = changed
        result['changedResourceEntries'] = changed_resources
        if changed:
            assert args.asm_jar and args.asm_jar.is_file(), 'Class bytes differ; supply --asm-jar for a strict class-model comparison'
            java = str(args.java) if args.java else str(Path(os.environ['JAVA_HOME'])/'bin/java') if 'JAVA_HOME' in os.environ else shutil.which('java')
            assert java, 'Java 25 is required for the class comparison'
            comparison = subprocess.check_output([java, '--class-path', str(args.asm_jar), str(PROJECT/'tools/ClassEquivalenceProbe.java'),
                                                   str(args.baseline), str(artifact_path())], text=True, encoding='utf-8')
            result['classComparison'] = json.loads(comparison)
            assert result['classComparison']['executableClassDifferences'] == 0
        result['changedGameplayEntries'] = []
assert not failures, failures
if args.output:
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2)+'\n', encoding='utf-8')
print(json.dumps(result))
