"""Place independent wearable policies and propulsion contracts in the native equipment domain."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.item.armor'
PACKAGE = 'ic2ce.neoforge.item.armor'
RULES = {before: (OLD, PACKAGE, after) for before, after in {
    'ItemArmorIC2': 'EquipmentItem', 'ItemArmorUtility': 'UtilityGarment',
    'ItemArmorElectric': 'ChargedArmor', 'ItemArmorHazmat': 'SealedGarment',
    'ItemArmorNanoSuit': 'NanoArmor', 'ItemArmorQuantumSuit': 'QuantumArmor',
    'ItemArmorNightVisionGoggles': 'NightVisionGoggles', 'ItemArmorFluidTank': 'WearableFluidReservoir',
    'ItemArmorJetpack': 'BiogasJetpack', 'ItemArmorCFPack': 'FoamReservoir',
}.items()}
RULES['IJetpack'] = (OLD + '.jetpack', 'ic2ce.api.item', 'PropulsionEquipment')

def rewrite_armor(text):
    return rewrite_named_roles(text, RULES, {}, (r'^import\s+ic2ce\.core\.item\.armor(?:\.jetpack)?\.\*;\r?\n',))

def main():
    reorganize(RULES, rewrite_armor, 'armor-layout-migration.json', 'Independent native equipment, sealed protection, nano/quantum, optical and fluid/foam propulsion policies')
if __name__ == '__main__': main()
