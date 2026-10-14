"""Name independently implemented native reactor systems by role; relocation is not a logic rewrite."""
from pathlib import Path
import hashlib
import json
import re
from migrate_java_namespace import PROJECT, ROOTS, LEXEMES

FORMER_PACKAGE = 'ic2ce.core.block.reactor.tileentity'
PACKAGE = 'ic2ce.neoforge.reactor'
NAMES = {'TileEntityNuclearReactorElectric': 'NuclearCore', 'TileEntityReactorChamberElectric': 'ReactorChamber', 'TileEntityReactorVessel': 'VesselEndpoint', 'TileEntityReactorAccessHatch': 'VesselHatch', 'TileEntityReactorFluidPort': 'CoolantPort', 'TileEntityReactorRedstonePort': 'SignalPort', 'TileEntityAbstractRCI': 'CondensatorService', 'TileEntityRCI_RSH': 'RedstoneCondensatorService', 'TileEntityRCI_LZH': 'LapisCondensatorService', 'FluidReactorLookup': 'FluidCoreLookup', 'ContainerNuclearReactor': 'ReactorMenu', 'GuiNuclearReactor': 'ReactorScreen'}

def rewrite_reactor_system(text):
    def token(match):
        value = match.group()
        if value.startswith(('"', "'")):
            if not any(prefix in value for prefix in (FORMER_PACKAGE, 'ic2ce.core.block.reactor.container', 'ic2ce.core.block.reactor.gui', 'ic2ce.core.block.comp.FluidReactorLookup')):
                return value
            for before, after in NAMES.items():
                value = re.sub(r'\b' + before + r'\b', after, value)
            return value.replace(FORMER_PACKAGE, PACKAGE)
        if value.startswith(('//', '/*')):
            return value
        return NAMES.get(value, value)
    result = LEXEMES.sub(token, text).replace(FORMER_PACKAGE, PACKAGE)
    result = result.replace('ic2ce.core.block.reactor.container', PACKAGE + '.menu').replace('ic2ce.core.block.reactor.gui', PACKAGE + '.client')
    result = result.replace('ic2ce.core.block.comp.FluidCoreLookup', PACKAGE + '.FluidCoreLookup')
    if re.search(r'\bclass FluidCoreLookup\b', result): result = result.replace('package ic2ce.core.block.comp;', 'package ' + PACKAGE + ';\n\nimport ic2ce.core.block.comp.TileEntityComponent;')
    if re.search(r'\bFluidCoreLookup\b', result) and 'package ' + PACKAGE + ';' not in result and PACKAGE + '.FluidCoreLookup' not in result and 'import ' + PACKAGE + '.*;' not in result:
        result = re.sub(r'(^package [\w.]+;)', lambda match: match.group() + '\n\nimport ' + PACKAGE + '.FluidCoreLookup;', result, count=1, flags=re.M)
    return result


def main():
    report = PROJECT / 'validation/reactor-system-layout-migration.json'
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
            after = rewrite_reactor_system(before.decode('utf-8-sig')).encode('utf-8')
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
    report.write_text(json.dumps({'domain': 'Native reactor system protocols', 'package': PACKAGE,
        'countsAsImplementationRewrite': False, 'sources': changes}, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'changedSources': len(changes), 'reactorSystemClasses': len(NAMES)}))


if __name__ == '__main__':
    main()
