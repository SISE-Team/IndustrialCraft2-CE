"""Check actual rewrite ownership, source boundaries, retained assets and embedded engine."""
from pathlib import Path
import argparse
import hashlib
import json
import re
import zipfile
from project_artifacts import PROJECT, JAVA_ROOTS, artifact_path
from migrate_java_namespace import rewrite
from audit_resource_fixes import verify_resource_changes

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--baseline', type=Path, required=True)
parser.add_argument('--baseline-sources', type=Path, required=True)
parser.add_argument('--output', type=Path, required=True)
args = parser.parse_args()
ownership = json.loads((PROJECT / 'validation/rewrite-ownership.json').read_text('utf-8'))
owned = {row['file'] for row in ownership['completedImplementations']}
integrated = {row['file'] for row in ownership.get('integrationChanges', [])}
sha = lambda data: hashlib.sha256(data).hexdigest()
inventory = []
declaration_file = PROJECT / 'validation/rewrite-declaration-review.json'
declaration_review = json.loads(declaration_file.read_text('utf-8')) if declaration_file.exists() else {'entries': []}
declarations = {row['file']: row for row in declaration_review['entries']}
namespace = json.loads((PROJECT / 'validation/namespace-migration.json').read_text('utf-8'))
renamed = {row['after']: row['before'] for row in namespace['sources']}
renamed.update(ownership.get('retargetedNamespaceSources', {}))
source_roots = ('platform/neoforge/src/main/java/', 'api/src/main/java/', 'engine/src/main/java/',
                'third-party/ejml/src/main/java/')
with zipfile.ZipFile(args.baseline) as old, zipfile.ZipFile(args.baseline_sources) as old_sources, zipfile.ZipFile(artifact_path()) as release, zipfile.ZipFile(artifact_path('sources')) as sources:
    names = set(release.namelist())
    for retired in ownership.get('retiredSources', []):
        file = retired.get('file', '')
        if '/java/' in file and file.endswith('.java'):
            entry = file.split('/java/', 1)[1]
            assert entry[:-5] + '.class' not in names and entry not in sources.namelist(), 'Retired source leaked into an artifact: ' + entry
    assets = {n for n in old.namelist() + release.namelist() if n.startswith(('assets/', 'data/')) and not n.endswith('/')}
    metadata_repairs = verify_resource_changes(PROJECT, old, release, assets)
    packages = set()
    for root in JAVA_ROOTS:
        for path in sorted(root.rglob('*.java')):
            entry = path.relative_to(root).as_posix()
            assert entry not in packages, entry
            packages.add(entry)
            data = path.read_bytes()
            assert sources.read(entry) == data, entry
            relative = path.relative_to(PROJECT).as_posix()
            former = renamed.get(relative, relative)
            former_entry = next((former[len(prefix):] for prefix in source_roots if former.startswith(prefix)), entry)
            existed = former_entry in old_sources.namelist()
            reference = old_sources.read(former_entry) if existed else None
            retained = existed and (reference == data or rewrite(reference.decode('utf-8-sig')).encode('utf-8') == data)
            assert retained or relative in owned or relative in integrated, 'Changed implementation lacks declared scope: ' + relative
            if relative in owned: assert not retained, 'Relocation alone cannot count as a rewrite: ' + relative
            inventory.append({'file': relative, 'sourceArchiveEntry': entry, 'sha256': sha(data),
                'referenceSourceArchiveEntry': former_entry if existed else None,
                'status': 'retained' if retained else 'integrated' if relative in integrated else 'reimplemented' if existed else 'new'})
            if relative in declarations:
                assert relative not in owned, 'A declaration review cannot inflate implementation ownership: ' + relative
                assert declarations[relative]['sha256'] == sha(data), 'Reviewed declaration changed without a new review: ' + relative
                assert declarations[relative]['countsAsImplementationRewrite'] is False
    assert owned == {row['file'] for row in inventory if row['status'] not in ('retained', 'integrated')}
    if declarations:
        assert declarations.keys() == {row['file'] for row in inventory if row['status'] in ('retained', 'integrated')}, 'Declaration review must cover every remaining signature/definition and no other source'
    engine = [p for p in (PROJECT / 'engine/src/main/java').rglob('*.java')]
    for path in engine:
        assert not re.search(r'^import\s+(?:net\.minecraft|net\.neoforged|com\.mojang|ic2ce\.api|ic2ce\.core\.(?!util\.Config))', path.read_text('utf-8'), re.M), path
    engine_classes = [p.relative_to(PROJECT / 'engine/build/classes/java/main').as_posix() for p in (PROJECT / 'engine/build/classes/java/main').rglob('*.class')]
    for name in engine_classes: assert release.read(name) == (PROJECT / 'engine/build/classes/java/main' / name).read_bytes(), name
    platform_output = PROJECT / 'platform/neoforge/build/classes/java/main'
    platform_classes = [p.relative_to(platform_output).as_posix() for p in platform_output.rglob('*.class')]
    assert platform_classes, 'The native platform must compile as its own Gradle module'
    for name in platform_classes: assert release.read(name) == (platform_output / name).read_bytes(), name
    fixture_output = PROJECT / 'platform/neoforge/build/classes/java/migrationVerification'
    fixture_classes = {p.relative_to(fixture_output).as_posix() for p in fixture_output.rglob('*.class')}
    for source in (PROJECT / 'verification/src/main/java').rglob('*.java'):
        expected = source.relative_to(PROJECT / 'verification/src/main/java').with_suffix('.class').as_posix()
        assert expected in fixture_classes, 'Native fixture compilation is incomplete: ' + expected
    assert not names.intersection(fixture_classes), 'Native verification classes leaked into the release'
    assert not any(n.startswith(('ic2/', 'ic2ce/verification/', 'reference/')) or 'EngineContractTest' in n for n in names)
    assert not any(n.startswith('ic2ce/forge/') for n in names)
    assert not any(n.startswith('ic2ce/core/block/invslot/') for n in names)
    assert not any(n.startswith('reference/') or 'EngineContractTest' in n for n in sources.namelist())
    old_classes = {n for n in old.namelist() if n.endswith('.class')}
    classes = {n for n in names if n.endswith('.class')}
    assert not any(n.startswith(('net/minecraft/', 'net/neoforged/')) for n in classes)
    result = {'project': 'IndustrialCraft2-CE', 'releaseBoundaryComplete': True,
        'wholeRewriteComplete': ownership.get('wholeRewriteComplete', False), 'baselineCommit': ownership['baselineCommit'],
        'releaseSha256': sha(artifact_path().read_bytes()), 'sourcesSha256': sha(artifact_path('sources').read_bytes()),
        'runtimeSources': len(inventory), 'newOrReimplementedSources': len(owned),
        'retainedRuntimeSources': sum(r['status'] in ('retained', 'integrated') for r in inventory),
        'retainedSourcesWithIntegrationChanges': sum(r['status'] == 'integrated' for r in inventory),
        'reviewedDeclarationSources': len(declarations),
        'retainedBehaviorSources': sum(r['status'] in ('retained', 'integrated') and r['file'] not in declarations for r in inventory),
        'embeddedStandaloneEngineClasses': len(engine_classes), 'assetDataEntries': len(assets),
        'retainedAssetDataEntries': len(assets) - len(metadata_repairs), 'reviewedResourceMetadataRepairs': metadata_repairs,
        'separateNativePlatformClasses': len(platform_classes), 'excludedNativeVerificationClasses': len(fixture_classes),
        'releaseClasses': len(classes), 'addedClassEntries': sorted(classes - old_classes),
        'removedClassEntries': sorted(old_classes - classes),
        'byteChangedClasses': sorted(n for n in classes & old_classes if release.read(n) != old.read(n)),
        'remainingFunctionalAreas': ownership['remainingFunctionalAreas'], 'sources': inventory}
args.output.parent.mkdir(parents=True, exist_ok=True)
args.output.write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
print(json.dumps({k: v for k, v in result.items() if k not in ('sources', 'byteChangedClasses', 'addedClassEntries', 'removedClassEntries')}, indent=2))
