"""Organize the independently implemented processing policies, physical storage and demolition lifecycle."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
from migrate_java_namespace import LEXEMES
import re
OLD='ic2ce.core.block.machine.tileentity'
RULES={}
def role(target,names):
 for before,after in names.items():RULES[before]=(OLD,'ic2ce.neoforge.'+target,after)
role('processing',{'TileEntityStandardMachine':'PoweredProcessor'})
role('processing.solid',{'TileEntityMacerator':'OreGrinder','TileEntityExtractor':'MaterialExtractor',
 'TileEntityCompressor':'PressureCompressor','TileEntityCentrifuge':'ThermalCentrifuge','TileEntityBlockCutter':'PlateCutter',
 'TileEntityMetalFormer':'MetalFormer','TileEntityOreWashing':'OreWasher','TileEntityRecycler':'MaterialRecycler','TileEntitySolidCanner':'SolidPackager'})
role('processing.thermal',{'TileEntityBlastFurnace':'AlloyFurnace','TileEntityElectricFurnace':'ElectricSmelter',
 'TileEntityIronFurnace':'FuelSmelter','TileEntityInduction':'InductionSmelter'})
role('processing.fluid',{'TileEntityCanner':'MultiPolicyCanner','TileEntityClassicCanner':'LegacyCellCanner',
 'TileEntityFluidBottler':'LiquidPackager','TileEntityElectrolyzer':'FluidElectrolyzer','TileEntityClassicElectrolyzer':'CellElectrolyzer'})
role('processing.fabrication',{'TileEntityPatternStorage':'PatternLibrary','TileEntityScanner':'PatternScanner',
 'TileEntityReplicator':'PatternReplicator','TileEntityMatter':'LiquidMatterFabricator',
 'TileEntityMassFabricator':'AmplifiedMatterFabricator','TileEntityClassicMassFabricator':'ClassicMatterFabricator'})
role('storage',{'TileEntityTank':'StationaryFluidStore'})
role('explosion',{'TileEntityExplosive':'ExplosiveBlockEntity','TileEntityBridgeNuke':'NuclearBlockEntity',
 'TileEntityNuke':'ConfiguredNuclearCharge','TileEntityITnt':'DemolitionBlockEntity'})
RULES.update({'ITntEntity':('ic2ce.core.entity.block','ic2ce.neoforge.explosion','PrimedDemolitionCharge'),
 'NukeEntity':('ic2ce.core.entity.block','ic2ce.neoforge.explosion','PrimedNuclearCharge')})
FIXTURES={name:(OLD,'ic2ce.neoforge.processing') for name in ('CanningContracts','ElectrolysisContracts','FabricationContracts','UuContracts','LegacyCanningContracts','FluidPackagingContracts')}
FIXTURES['ExplosiveBlockContracts']=(OLD,'ic2ce.neoforge.explosion')
def rewrite_processing_devices(text):
 portable='ic2ce.core.block.storage.tank.TileEntityTank'
 imports_portable='import '+portable+';' in text
 belongs_portable=bool(re.search(r'^package\s+ic2ce\.core\.block\.storage\.tank;',text,re.M))
 text=text.replace(portable,'IC2CEPortableFluidReference')
 if imports_portable or belongs_portable:
  text=LEXEMES.sub(lambda m:'IC2CEPortableFluidSymbol' if m[0]=='TileEntityTank' else m[0],text)
 text=text.replace('import '+OLD+'.*;','').replace('import ic2ce.core.entity.block.*;','')
 result=rewrite_named_roles(text,RULES,FIXTURES)
 # The classic implementation remains a declared nested native type, not another top-level registry class.
 result=result.replace('TileEntityClassicNuke','ClassicNuclearCharge')
 return result.replace("IC2CEPortableFluidReference","ic2ce.core.block.storage.tank.FluidReservoir").replace("IC2CEPortableFluidSymbol","FluidReservoir")

def main():
 reorganize(RULES,rewrite_processing_devices,'processing-device-layout-migration.json','Distinct solid, thermal, fluid, fabrication, storage and demolition native policies')
if __name__=='__main__':main()
