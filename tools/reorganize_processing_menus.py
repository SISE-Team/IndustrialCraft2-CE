"""Organize independently implemented native processing menus; relocation contributes zero implementation rewrites."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

RULES={'ContainerFullInv':('ic2ce.core','ic2ce.neoforge.menu','PhysicalInventoryMenu')}
NAMES={
    'ContainerElectricMachine':'PoweredMenu',
    'ContainerStandardMachine':'ProcessingMenu',
    'ContainerCanner':'CanningMenu',
    'ContainerClassicCanner':'LegacyCanningMenu',
    'ContainerCondenser':'CondensationMenu',
    'ContainerElectrolyzer':'ElectrolysisMenu',
    'ContainerFermenter':'FermentationMenu',
    'ContainerFluidBottler':'FluidPackagingMenu',
    'ContainerLiquidHeatExchanger':'HeatExchangeMenu',
    'ContainerMatter':'MatterMenu',
    'ContainerMetalFormer':'MetalFormingMenu',
    'ContainerPatternStorage':'PatternLibraryMenu',
    'ContainerReplicator':'ReplicationMenu',
    'ContainerScanner':'ScanningMenu',
    'ContainerSolarDestiller':'DistillationMenu',
    'ContainerSteamGenerator':'BoilerMenu'}
RULES.update({name:('ic2ce.core.block.machine.container','ic2ce.neoforge.processing.menu',target) for name,target in NAMES.items()})

def rewrite_processing_menus(text):
    return rewrite_named_roles(text,RULES,{},(r'^import\s+ic2ce\.core\.block\.machine\.container\.\*;\r?\n',),implicit_packages=('ic2ce.core',))

def main():
    reorganize(RULES,rewrite_processing_menus,'processing-menu-layout-migration.json','Distinct native processing menu bindings and physical inventory layouts')

if __name__=='__main__':main()
