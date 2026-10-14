"""Name independent tool policies, native payment/action services and electrical tooltip adapters."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.item.tool'
PACKAGE = 'ic2ce.neoforge.item.tool'
RULES = {before: (OLD, PACKAGE, after) for before, after in {
    'ItemToolCrafting': 'DurableCraftingTool', 'ItemToolPainter': 'PigmentBrush',
    'ItemToolCutter': 'InsulationCutter', 'ItemToolWrench': 'MechanicalWrench',
    'ItemToolWrenchElectric': 'ElectricWrench', 'ItemElectricTool': 'PoweredTool',
    'ItemDrill': 'MiningDrill', 'ItemDrillIridium': 'IridiumDrill',
    'ItemElectricToolChainsaw': 'PoweredSaw', 'AbstractItemNanoSaber': 'NanoBladeBase',
    'ItemNanoSaber': 'NanoBlade', 'ItemTreetap': 'ManualResinTap', 'ItemTreetapElectric': 'ElectricResinTap',
}.items()}
RULES['ElectricItemTooltipHandler'] = ('ic2ce.core.item', 'ic2ce.neoforge.item.tooltip', 'ElectricTooltip')

def rewrite_tools(text):
    return rewrite_named_roles(text, RULES, {}, implicit_packages=(OLD, 'ic2ce.core.item'))

def main():
    reorganize(RULES, rewrite_tools, 'tool-layout-migration.json', 'Independent mechanical/electrical/pigment/insulation/resin/mining/shearing/blade policies and native tooltip adapters')
if __name__ == '__main__': main()
