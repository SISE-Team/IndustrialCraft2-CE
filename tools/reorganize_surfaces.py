"""Organize independent native surface, signage, state-text and mining-column roles."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
RULES={
 'Ic2GlassBlock':('ic2ce.core.block.inherit','ic2ce.neoforge.world.block','StructuralGlass'),
 'Ic2SheetBlock':('ic2ce.core.block.inherit','ic2ce.neoforge.world.block','ReactiveSheet'),
 'Ic2SignBlock':('ic2ce.core.block.inherit','ic2ce.neoforge.world.block','RubberStandingSign'),
 'Ic2WallSignBlock':('ic2ce.core.block.inherit','ic2ce.neoforge.world.block','RubberWallSign'),
 'Ic2SignBlockEntity':('ic2ce.core.block.tileentity','ic2ce.neoforge.world.block','RubberSignEntity'),
 'MiningPipeBlock':('ic2ce.core.block.machine','ic2ce.neoforge.mining','MiningColumn'),
 'BlockStateUtil':('ic2ce.core.block.state','ic2ce.neoforge.world.surface','StateVariants')}
FIXTURES={'SurfaceContracts':('ic2ce.core.block.inherit','ic2ce.neoforge.world.block')}
def rewrite_surfaces(text):
    return rewrite_named_roles(text,RULES,FIXTURES,(r'^import\s+ic2ce\.core\.block\.(?:tileentity|inherit)\.\*;\r?\n',))
def main():reorganize(RULES,rewrite_surfaces,'surface-layout-migration.json','Distinct resin/rubber/wool physics, native signage ownership, strict state projection and mining column shape')
if __name__=='__main__':main()
