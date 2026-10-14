"""Validate the explicit metadata repairs while preserving existing IC2 material values."""
import json


def verify_resource_changes(project, baseline, release, names):
    manifest = json.loads((project / 'validation/rewrite-resource-fixes.json').read_text('utf-8'))
    languages = {row['resource']: row for row in manifest['languageAdditions']}
    background = manifest['changedAdvancementBackground']
    allowed = set(languages) | {background['resource']}
    changed = []
    for name in sorted(names):
        before, after = baseline.read(name), release.read(name)
        if before == after:
            continue
        assert name in allowed, 'Unreviewed material/data change: ' + name
        a, b = json.loads(before), json.loads(after)
        if name in languages:
            keys = set(languages[name]['addedKeys'])
            assert keys.isdisjoint(a), name
            assert b.keys() == a.keys() | keys, name
            assert all(b[key] == value for key, value in a.items()), name
            assert all(isinstance(b[key], str) and b[key].strip() for key in keys), name
        else:
            assert a['display']['background'] == background['before'], name
            assert b['display']['background'] == background['after'], name
            b['display']['background'] = a['display']['background']
            assert a == b, 'Advancement gameplay data changed: ' + name
            namespace, path = background['after'].split(':', 1)
            assert f'assets/{namespace}/textures/{path}.png' in release.namelist()
        changed.append(name)
    assert set(changed) == allowed, 'Every declared metadata repair must be present'
    return changed
