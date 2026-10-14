"""Name independently implemented native utility roles; move accounting stays separate."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.block.machine'
PACKAGE = 'ic2ce.neoforge.utility'
RULES = {}
def roles(former, target, names):
    for before, after in names.items(): RULES[before] = (former, target, after)
roles(OLD + '.tileentity', PACKAGE, {'TileEntityTesla': 'ArcCoil', 'TileEntityMagnetizer': 'PoleDrive', 'TileEntityChunkloader': 'ChunkAnchor'})
roles('ic2ce.core', 'ic2ce.neoforge.world', {'ChunkLoaderLogic': 'ChunkLeases'})
roles('ic2ce.core.block.inherit', PACKAGE, {'Ic2FenceBlock': 'IndustrialFence'})
roles(OLD + '.container', PACKAGE + '.menu', {'ContainerChunkLoader': 'ChunkAnchorMenu', 'ContainerMagnetizer': 'PoleDriveMenu'})
roles(OLD + '.gui', PACKAGE + '.client', {'GuiChunkLoader': 'ChunkAnchorScreen', 'GuiMagnetizer': 'PoleDriveScreen'})
FIXTURES = {name: (OLD + '.tileentity', PACKAGE) for name in ('UtilityContracts', 'ChunkLeaseContracts')}
def rewrite_utility(text): return rewrite_named_roles(text, RULES, FIXTURES, implicit_packages=('ic2ce.core', 'ic2ce.core.block.inherit', OLD + '.tileentity', OLD + '.container', OLD + '.gui'))
def main(): reorganize(RULES, rewrite_utility, 'utility-layout-migration.json', 'Independent arc, magnetic-pole and chunk-lease protocols')
if __name__ == '__main__': main()
