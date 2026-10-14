"""Name independent upgrade policies and ghost menus, with actual-stack storage in its own domain."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.item.upgrade'
PACKAGE = 'ic2ce.neoforge.item.upgrade'
RULES = {before: (OLD, PACKAGE, after) for before, after in {
    'ItemUpgradeModule': 'MachineUpgrade', 'ComparisonSettings': 'IntegerRelation',
    'ComparisonType': 'FilterMode', 'NbtSettings': 'MetadataMatch', 'UpgradeSettings': 'NumericFilter',
    'HandHeldAdvancedUpgrade': 'AdvancedFilterInventory', 'HandHeldUpgradeOption': 'FilterSubmenu',
    'HandHeldValueConfig': 'NumericFilterEditor', 'HandHeldOre': 'MaterialTagEditor',
    'AdvancedUpgradeScreenFactory': 'AdvancedFilterScreenFactory',
}.items()}
RULES['HandHeldInventory'] = ('ic2ce.core.item.tool', 'ic2ce.neoforge.handheld', 'HandheldStorage')
FIXTURES = {'UpgradeMenuContracts': (OLD, PACKAGE)}

def rewrite_upgrades(text):
    return rewrite_named_roles(text, RULES, FIXTURES, (r'^import\s+ic2ce\.core\.item\.upgrade\.\*;\r?\n',), implicit_packages=('ic2ce.core.item.tool',))

def main():
    reorganize(RULES, rewrite_upgrades, 'upgrade-layout-migration.json', 'Independent numerical/metadata/material filters, native transfer policies and count-preserving handheld storage')
if __name__ == '__main__': main()
