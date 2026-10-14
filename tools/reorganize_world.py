"""Name independently rewritten native world blocks and foliage roles; moves count zero logic rewrites."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
RULES={
    'RubberLogBlock':('ic2ce.core.block.misc','ic2ce.neoforge.world.block','ResinLog'),
    'RubberWoodBlock':('ic2ce.core.block.misc','ic2ce.neoforge.world.block','BarkWood'),
    'FoamBlock':('ic2ce.core.block.misc','ic2ce.neoforge.world.block','CuringFoam'),
    'WallBlock':('ic2ce.core.block.misc','ic2ce.neoforge.world.block','ConstructionWall'),
    'ScaffoldBlock':('ic2ce.core.block.misc','ic2ce.neoforge.world.block','StructuralScaffold'),
    'TileEntityWall':('ic2ce.core.block.tileentity','ic2ce.neoforge.world.block','ImprintedWall'),
    'Obscuration':('ic2ce.core.block.comp','ic2ce.neoforge.world.surface','SurfaceMap'),
    'Ic2WorldGen':('ic2ce.core.world','ic2ce.neoforge.world.generation','WorldFeatures'),
    'RubberTreeFoliagePlacer':('ic2ce.core.world','ic2ce.neoforge.world.generation','ResinCanopy')}
FIXTURES={
    'ConstructionContracts':('ic2ce.core.block.misc','ic2ce.neoforge.world.block'),
    'ScaffoldContracts':('ic2ce.core.block.misc','ic2ce.neoforge.world.block'),
    'WorldGenerationContracts':('ic2ce.core.world','ic2ce.neoforge.world.generation')}
def rewrite_world(text):
    return rewrite_named_roles(text,RULES,FIXTURES,(r'^import\s+ic2ce\.core\.block\.misc\.\*;\r?\n',r'^import\s+ic2ce\.core\.world\.\*;\r?\n'),implicit_packages=('ic2ce.core.block.tileentity','ic2ce.core.block.comp'))
def main():reorganize(RULES,rewrite_world,'world-layout-migration.json','Independent resin/bark, curing foam, construction walls, sparse scaffolds, surface persistence and native foliage roles')
if __name__=='__main__':main()
