"""Name independently implemented native kinetic devices by role; relocation is not a logic rewrite."""
from pathlib import Path
import hashlib
import json
import re
from migrate_java_namespace import PROJECT, ROOTS, LEXEMES

FORMER_PACKAGE = 'ic2ce.core.block.kineticgenerator.tileentity'
PACKAGE = 'ic2ce.neoforge.kinetic'
NAMES = {'TileEntityManualKineticGenerator': 'ManualCrank', 'TileEntityElectricKineticGenerator': 'ElectricMotor', 'TileEntityStirlingKineticGenerator': 'HeatDrivenCogenerator', 'TileEntitySteamKineticGenerator': 'SteamTurbine', 'TileEntityWindKineticGenerator': 'WindTurbine', 'TileEntityWaterKineticGenerator': 'WaterTurbine'}

def rewrite_kinetic(text):
    def token(match):
        value = match.group()
        if value.startswith(('"', "'")):
            if FORMER_PACKAGE not in value:
                return value
            for before, after in NAMES.items():
                value = re.sub(r'\b' + before + r'\b', after, value)
            return value.replace(FORMER_PACKAGE, PACKAGE)
        if value.startswith(('//', '/*')):
            return value
        return NAMES.get(value, value)
    return LEXEMES.sub(token, text).replace(FORMER_PACKAGE, PACKAGE)


def main():
    report = PROJECT / 'validation/kinetic-layout-migration.json'
    if report.exists():
        print(json.dumps({'alreadyReorganized': True}))
        return
    prior = json.loads((PROJECT / 'validation/namespace-migration.json').read_text('utf-8'))
    original_paths = {row['after']: row['before'] for row in prior['sources']}
    ledger = PROJECT / 'validation/rewrite-ownership.json'
    ownership = json.loads(ledger.read_text('utf-8'))
    changes = []
    for root_name in ROOTS:
        root = PROJECT / root_name
        for source in list(root.rglob('*.java')):
            before = source.read_bytes()
            after = rewrite_kinetic(before.decode('utf-8-sig')).encode('utf-8')
            if before == after:
                continue
            package = re.search(rb'^package\s+([\w.]+);', after, re.M)[1].decode()
            target = root / Path(package.replace('.', '/')) / (NAMES.get(source.stem, source.stem) + '.java')
            assert target == source or not target.exists(), target
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(after)
            old_path, new_path = source.relative_to(PROJECT).as_posix(), target.relative_to(PROJECT).as_posix()
            if source != target:
                source.unlink()
                ownership['retargetedNamespaceSources'][new_path] = original_paths.get(old_path, old_path)
                for row in ownership['completedImplementations'] + ownership['integrationChanges']:
                    if row['file'] == old_path:
                        row['file'] = new_path
            changes.append({'before': old_path, 'after': new_path,
                'beforeSha256': hashlib.sha256(before).hexdigest(), 'afterSha256': hashlib.sha256(after).hexdigest(),
                'countsAsImplementationRewrite': False})
    ledger.write_text(json.dumps(ownership, indent=2) + '\n', encoding='utf-8')
    report.write_text(json.dumps({'domain': 'Native kinetic device protocols', 'package': PACKAGE,
        'countsAsImplementationRewrite': False, 'sources': changes}, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'changedSources': len(changes), 'kineticClasses': len(NAMES)}))


if __name__ == '__main__':
    main()
