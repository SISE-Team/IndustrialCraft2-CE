"""Organize native electrical roles. File/package moves never count as rewritten behavior."""
from pathlib import Path
import hashlib
import json
import re
from migrate_java_namespace import PROJECT, ROOTS, LEXEMES

OLD = 'ic2ce.core.block.wiring'
STORAGE = 'ic2ce.neoforge.energy.storage'
TRANSFORMER = 'ic2ce.neoforge.energy.transformer'
CABLE = 'ic2ce.neoforge.cable'
MENU = 'ic2ce.neoforge.energy.menu'
CLIENT = 'ic2ce.neoforge.energy.client'
RULES = {}

def roles(former_package, destination, names):
    for before, after in names.items(): RULES[before] = (former_package, destination, after)

roles(OLD + '.tileentity', STORAGE, {
    'TileEntityElectricBlock': 'EnergyBank', 'TileEntityElectricBatBox': 'LowVoltageBank',
    'TileEntityElectricCESU': 'MediumVoltageBank', 'TileEntityElectricMFE': 'HighVoltageBank',
    'TileEntityElectricMFSU': 'ExtremeVoltageBank', 'TileEntityChargepadBlock': 'WirelessChargingPad',
    'TileEntityChargepadBatBox': 'LowVoltagePad', 'TileEntityChargepadCESU': 'MediumVoltagePad',
    'TileEntityChargepadMFE': 'HighVoltagePad', 'TileEntityChargepadMFSU': 'ExtremeVoltagePad'})
roles(OLD + '.tileentity', TRANSFORMER, {
    'TileEntityTransformer': 'VoltageTransformer', 'TileEntityTransformerLV': 'LowVoltageTransformer',
    'TileEntityTransformerMV': 'MediumVoltageTransformer', 'TileEntityTransformerHV': 'HighVoltageTransformer',
    'TileEntityTransformerEV': 'ExtremeVoltageTransformer'})
roles(OLD, CABLE, {
    'AbstractCableBlock': 'ConductorBlock', 'CableBlock': 'BareCableBlock', 'FoamCableBlock': 'FoamConductorBlock',
    'AbstractDetectorCableBlock': 'TrafficSensorBlock', 'DetectorCableBlock': 'BareTrafficSensorBlock',
    'DetectorFoamCableBlock': 'FoamedTrafficSensorBlock', 'AbstractSplitterCableBlock': 'RedstoneGateBlock',
    'SplitterCableBlock': 'BareRedstoneGateBlock', 'SplitterFoamCableBlock': 'FoamedRedstoneGateBlock',
    'CableType': 'ConductorMaterial', 'CableFoam': 'FoamFinish'})
roles(OLD, MENU, {'ContainerElectricBlock': 'BankMenu', 'ContainerChargepadBlock': 'ChargingPadMenu', 'ContainerTransformer': 'TransformerMenu'})
roles(OLD, CLIENT, {'GuiElectricBlock': 'BankScreen', 'GuiChargepadBlock': 'ChargingPadScreen', 'GuiTransformer': 'TransformerScreen'})
FIXTURES = {'StorageBankContracts': STORAGE, 'TransformerContracts': TRANSFORMER, 'CableContracts': CABLE}

def rewrite_energy_devices(text):
    fixtures = {name: (OLD if name == 'CableContracts' else OLD + '.tileentity', target) for name, target in FIXTURES.items()}
    return rewrite_named_roles(text, RULES, fixtures, (r'^import\s+' + re.escape(OLD) + r'(?:\.tileentity)?\.\*;\r?\n',))

def rewrite_named_roles(text, rules, fixtures, remove_import_patterns=(), implicit_packages=()):
    original_package = re.search(r'^package\s+([\w.]+);', text, re.M)
    destination = original_package[1] if original_package else None
    for name, (package, target, renamed) in rules.items():
        if destination == package and re.search(r'\b(?:class|enum|interface|record)\s+' + name + r'\b', text): destination = target; break
    for name, (_, target) in fixtures.items():
        if re.search(r'\bclass\s+' + name + r'\b', text): destination = target
    def token(match):
        value = match.group()
        if value.startswith(('//', '/*')): return value
        if value.startswith(('"', "'")):
            for name, (package, target, renamed) in rules.items(): value = value.replace(package + '.' + name, target + '.' + renamed)
            return value
        return rules[value][2] if value in rules else value
    result = LEXEMES.sub(token, text)
    for name, (package, target, renamed) in rules.items():
        result = re.sub(r'\b' + re.escape(package + '.' + renamed) + r'\b', target + '.' + renamed, result)
    for name, (former, target) in fixtures.items():
        result = result.replace(former + '.' + name, target + '.' + name)
    if original_package and destination != original_package[1]: result = result.replace(original_package[0], 'package ' + destination + ';', 1)
    for pattern in remove_import_patterns: result = re.sub(pattern, '', result, flags=re.M)
    if original_package and destination != original_package[1] and original_package[1] in implicit_packages:
        result = result.replace('package ' + destination + ';', 'package ' + destination + ';\n\nimport ' + original_package[1] + '.*;', 1)
    for _, (_, target, renamed) in rules.items():
        qualified = target + '.' + renamed
        if destination != target and re.search(r'\b' + renamed + r'\b', result) and qualified not in result and 'import ' + target + '.*;' not in result:
            result = re.sub(r'(^package [\w.]+;)', lambda m: m[0] + '\n\nimport ' + qualified + ';', result, count=1, flags=re.M)
    return result

def main():
    reorganize(RULES, rewrite_energy_devices, 'energy-device-layout-migration.json', 'Native energy storage, transformers and conductors')

def reorganize(rules, rewrite, report_name, domain):
    report = PROJECT / 'validation' / report_name
    if report.exists(): print(json.dumps({'alreadyReorganized': True})); return
    prior = json.loads((PROJECT / 'validation/namespace-migration.json').read_text('utf-8'))
    original_paths = {row['after']: row['before'] for row in prior['sources']}
    ledger = PROJECT / 'validation/rewrite-ownership.json'; ownership = json.loads(ledger.read_text('utf-8'))
    changes = []
    for root_name in ROOTS:
        root = PROJECT / root_name
        for source in list(root.rglob('*.java')):
            before = source.read_bytes(); after = rewrite(before.decode('utf-8-sig')).encode('utf-8')
            if before == after: continue
            package = re.search(rb'^package\s+([\w.]+);', after, re.M)[1].decode()
            target = root / Path(package.replace('.', '/')) / ((rules[source.stem][2] if source.stem in rules else source.stem) + '.java')
            assert target == source or not target.exists(), target
            target.parent.mkdir(parents=True, exist_ok=True); target.write_bytes(after)
            old_path, new_path = source.relative_to(PROJECT).as_posix(), target.relative_to(PROJECT).as_posix()
            if source != target:
                source.unlink()
                ownership['retargetedNamespaceSources'][new_path] = ownership['retargetedNamespaceSources'].get(old_path, original_paths.get(old_path, old_path))
                for row in ownership['completedImplementations'] + ownership['integrationChanges']:
                    if row['file'] == old_path: row['file'] = new_path
            changes.append({'before': old_path, 'after': new_path, 'beforeSha256': hashlib.sha256(before).hexdigest(), 'afterSha256': hashlib.sha256(after).hexdigest(), 'countsAsImplementationRewrite': False})
    ledger.write_text(json.dumps(ownership, indent=2) + '\n', encoding='utf-8')
    report.write_text(json.dumps({'domain': domain, 'countsAsImplementationRewrite': False, 'sources': changes}, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'changedSources': len(changes), 'namedRoleClasses': len(rules)}))

if __name__ == '__main__': main()
