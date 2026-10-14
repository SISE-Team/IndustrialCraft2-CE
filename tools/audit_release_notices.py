"""Audit copyright notices, semantic version metadata and retained IC2 media."""
import argparse
import hashlib
import json
from pathlib import Path
import tomllib
import zipfile

from project_artifacts import PROJECT, PROPERTIES, RESOURCE_ROOT, artifact_path


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def gameplay(name):
    return name.endswith(('.class', '.java')) or name.startswith(('assets/', 'data/'))


def parity(archive, baseline):
    with zipfile.ZipFile(baseline) as old:
        previous = {name for name in old.namelist() if gameplay(name) and not name.endswith('/')}
        current = {name for name in archive.namelist() if gameplay(name) and not name.endswith('/')}
        assert previous == current, 'Gameplay entry sets differ from the baseline'
        changed = [name for name in sorted(current) if old.read(name) != archive.read(name)]
        assert not changed, f'Gameplay bytes differ from the baseline: {changed}'
        return {'baseline': baseline.name, 'baselineSha256': sha256(baseline.read_bytes()),
                'identicalEntries': len(current), 'changedEntries': changed}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--baseline', type=Path, help='Prior normal JAR for exact gameplay parity')
    parser.add_argument('--baseline-sources', type=Path, help='Prior sources JAR for exact source parity')
    parser.add_argument('--original', type=Path, help='Original IC2 reference JAR for media ownership provenance')
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()

    expected = {f'META-INF/licenses/{name}': (PROJECT / name).read_bytes()
                for name in ('LICENSE', 'NOTICE')}
    expected.update({f'META-INF/licenses/{path.relative_to(PROJECT / "licenses").as_posix()}': path.read_bytes()
                     for path in (PROJECT / 'licenses').rglob('*') if path.is_file()})
    original_authors = [name.strip() for name in PROPERTIES['mod_authors'].split(';')[0].split(',')]
    result = {'project': PROPERTIES['mod_name'], 'version': PROPERTIES['mod_version'],
              'artifacts': {}, 'originalAuthorCredits': original_authors}
    for classifier in (None, 'sources'):
        path = artifact_path(classifier)
        with zipfile.ZipFile(path) as archive:
            names = archive.namelist()
            assert len(names) == len(set(names)), 'Duplicate archive entries'
            license_entries = {name for name in names if name.startswith('META-INF/licenses/') and not name.endswith('/')}
            assert license_entries == set(expected), f'Missing or unexpected license entries: {path.name}'
            for name, data in expected.items():
                assert archive.read(name) == data, f'Stale or incomplete notice: {name}'
            for name in ('LICENSE', 'NOTICE'):
                notice = ' '.join(archive.read(f'META-INF/licenses/{name}').decode('utf-8').split())
                for phrase in ('official IndustrialCraft 2 (IC2) project', 'sound effects',
                               'original copyright holders', 'independent reimplementation',
                               'source code used as a reference', 'does not claim ownership'):
                    assert phrase in notice, f'Missing attribution in {name}: {phrase}'
            notice = archive.read('META-INF/licenses/NOTICE').decode('utf-8')
            assert all(author in notice for author in original_authors), 'Missing original author credit'
            license_text = ' '.join(archive.read('META-INF/licenses/LICENSE').decode('utf-8').split())
            for phrase in ('MIT License', 'Copyright (c) 2026 SISE-Team and IndustrialCraft2-CE contributors',
                           'Permission is hereby granted, free of charge',
                           'copies or substantial portions of the Software',
                           'THE SOFTWARE IS PROVIDED "AS IS"',
                           'MIT License for project code does not apply to original IC2 code',
                           'All rights reserved'):
                assert phrase in license_text, f'Missing license text or scope: {phrase}'
            manifest = archive.read('META-INF/MANIFEST.MF').decode('utf-8')
            assert f'Implementation-Version: {PROPERTIES["mod_version"]}\r\n' in manifest
            assert f'Implementation-Title: {PROPERTIES["mod_name"]}\r\n' in manifest
            metadata = archive.read('META-INF/neoforge.mods.toml')
            if classifier is None:
                info = tomllib.loads(metadata.decode('utf-8'))
                mod = next(mod for mod in info['mods'] if mod['modId'] == PROPERTIES['mod_id'])
                assert mod['version'] == PROPERTIES['mod_version'], 'Minecraft version leaked into mod version'
                assert info['license'] == PROPERTIES['mod_license']
                assert 'MIT' in info['license'] and 'original IC2 materials' in info['license']
                assert mod['authors'] == PROPERTIES['mod_authors']
                assert mod['description'] == PROPERTIES['mod_description']
                assert not any(name.startswith('org/ejml/') and name.endswith('.class') for name in names)
                assert not any(name.startswith(('ic2ce/verification/', 'net/minecraft/', 'org/lwjgl/')) for name in names)
            else:
                assert metadata == (RESOURCE_ROOT / 'META-INF/neoforge.mods.toml').read_bytes()
            record = {'file': path.name, 'sha256': sha256(path.read_bytes()),
                      'licenseEntries': {name: sha256(data) for name, data in sorted(expected.items())},
                      'manifestVersion': PROPERTIES['mod_version'], 'noticesMatchRepository': True}
            record['projectCodeLicense'] = 'MIT'
            record['originalIc2MaterialLicense'] = 'All rights reserved'
            baseline = args.baseline if classifier is None else args.baseline_sources
            if baseline:
                record['gameplayParity'] = parity(archive, baseline)
            if classifier is None:
                record['modMetadataVersion'] = mod['version']
            result['artifacts'][classifier or 'normal'] = record

    with zipfile.ZipFile(artifact_path()) as normal, zipfile.ZipFile(artifact_path('sources')) as sources:
        media = {name for name in normal.namelist() if name.startswith(('assets/', 'data/')) and not name.endswith('/')}
        assert media == {name for name in sources.namelist() if name.startswith(('assets/', 'data/')) and not name.endswith('/')}
        assert all(normal.read(name) == sources.read(name) for name in media), 'Source and normal resources differ'
        result['identicalReleaseAndSourceResourceEntries'] = len(media)
        if args.original:
            original_hash = sha256(args.original.read_bytes())
            assert original_hash in (PROJECT / 'NOTICE').read_text(encoding='utf-8'), 'Reference hash is not credited'
            with zipfile.ZipFile(args.original) as original:
                textures = [name for name in original.namelist()
                            if name.startswith('assets/ic2/textures/') and name.endswith(('.png', '.mcmeta'))]
                sounds = [name for name in original.namelist()
                          if name.startswith('assets/ic2/sounds/') and name.endswith('.ogg')]
                assert textures and sounds, 'Reference contains no IC2 media'
                for name in textures + sounds:
                    migrated = name.replace('/textures/blocks/', '/textures/block/').replace('/textures/items/', '/textures/item/')
                    assert normal.read(migrated) == original.read(name), f'Original media changed: {name}'
                result['ic2Media'] = {'reference': args.original.name, 'referenceSha256': original_hash,
                                      'unchangedTexturesAndMetadata': len(textures), 'unchangedSoundEffects': len(sounds)}
    result['complete'] = True
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(result, indent=2))


if __name__ == '__main__':
    main()
