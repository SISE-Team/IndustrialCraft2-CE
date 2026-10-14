"""Separate automatic, exact-layout UU and manual crafting; name native transactional item services."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
from reorganize_inventory import LEXEMES

MACHINE = 'ic2ce.core.block.machine'
PACKAGE = 'ic2ce.neoforge.crafting'
RULES = {}
def roles(former, target, names):
    for before, after in names.items(): RULES[before] = (former, target, after)
roles('ic2ce.core.block', PACKAGE, {'SimpleCraftingInventory': 'CraftingGridView'})
roles(MACHINE + '.tileentity', PACKAGE, {'TileEntityBatchCrafter': 'AutomaticCrafter', 'TileEntityAssemblyBench': 'UuAssembler', 'TileEntityIndustrialWorkbench': 'IndustrialWorkbench'})
roles(MACHINE + '.container', PACKAGE + '.menu', {'ContainerBatchCrafter': 'AutomaticCraftingMenu', 'ContainerIndustrialWorkbench': 'WorkbenchMenu'})
roles(MACHINE + '.gui', PACKAGE + '.client', {'GuiBatchCrafter': 'AutomaticCraftingScreen', 'GuiIndustrialWorkbench': 'WorkbenchScreen'})
roles('ic2ce.neoforge', 'ic2ce.neoforge.inventory.transfer', {'EnvItemHandlerNeoForge': 'NativeItemTransfers', 'ContainerItemHandler': 'TransactionalItemAdapter'})
FIXTURES = {name: (MACHINE + '.tileentity', PACKAGE) for name in ('AutomaticCraftingContracts', 'WorkbenchContracts')}
NESTED = {'ArrayCraftingInventory': 'ArrayGrid', 'InvSlotCraftingInventory': 'SlotGrid', 'InvSlotCraftingCombo': 'ToolStation'}
def rewrite_crafting(text):
    result = rewrite_named_roles(text, RULES, FIXTURES, implicit_packages=(MACHINE + '.tileentity', MACHINE + '.container', MACHINE + '.gui'))
    result = LEXEMES.sub(lambda match: NESTED.get(match[0], match[0]), result)
    return result
def main(): reorganize(RULES, rewrite_crafting, 'crafting-layout-migration.json', 'Independent automatic, UU-layout and manual crafting plus transactional item capability services')
if __name__ == '__main__': main()
