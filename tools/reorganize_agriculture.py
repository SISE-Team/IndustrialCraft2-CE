"""Separate harvesting, tank-fed and cell-fed care, retaining move-only accounting."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.block.machine'
PACKAGE = 'ic2ce.neoforge.agriculture'
RULES = {}
def roles(former, target, names):
    for before, after in names.items(): RULES[before] = (former, target, after)
roles(OLD + '.tileentity', PACKAGE, {'TileEntityCropHarvester': 'AutoHarvester', 'TileEntityCropmatron': 'FluidCropCaretaker', 'TileEntityClassicCropmatron': 'CellCropCaretaker'})
roles(OLD + '.container', PACKAGE + '.menu', {'ContainerCropHarvester': 'HarvesterMenu', 'ContainerCropmatron': 'FluidCareMenu', 'ContainerClassicCropmatron': 'CellCareMenu'})
roles(OLD + '.gui', PACKAGE + '.client', {'GuiCropHarvester': 'HarvesterScreen', 'GuiCropmatron': 'FluidCareScreen', 'GuiClassicCropmatron': 'CellCareScreen'})
roles('ic2ce.core.item', 'ic2ce.neoforge.item.container', {'ItemClassicCell': 'FixedCell'})
FIXTURES = {'AgricultureContracts': (OLD + '.tileentity', PACKAGE)}
def rewrite_agriculture(text):
    return rewrite_named_roles(text, RULES, FIXTURES, implicit_packages=(OLD + '.tileentity', OLD + '.container', OLD + '.gui', 'ic2ce.core.item'))
def main(): reorganize(RULES, rewrite_agriculture, 'agriculture-layout-migration.json', 'Independent harvesting, distinct crop-care policies and finite fixed cell protocols')
if __name__ == '__main__': main()
