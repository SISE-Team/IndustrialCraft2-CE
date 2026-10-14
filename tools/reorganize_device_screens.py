"""Organize independent device-specific screen policies and their physical menu adapters."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
RULES={}
def group(domain,names):
 former='ic2ce.core.block.'+domain[0]+'.'+domain[1]
 target='ic2ce.neoforge.'+domain[2]+'.'+domain[3]
 for name,new in names.items():RULES[name]=(former,target,new)
group(('machine','gui','processing','client'),{
 'GuiCanner':'CanningScreen','GuiClassicCanner':'LegacyCanningScreen','GuiCondenser':'CondensationScreen',
 'GuiElectrolyzer':'ElectrolysisScreen','GuiFermenter':'FermentationScreen','GuiFluidBottler':'FluidPackagingScreen',
 'GuiLiquidHeatExchanger':'HeatExchangeScreen','GuiMatter':'MatterScreen','GuiMetalFormer':'MetalFormingScreen',
 'GuiPatternStorage':'PatternLibraryScreen','GuiReplicator':'ReplicationScreen','GuiScanner':'ScanningScreen',
 'GuiSolarDestiller':'DistillationScreen','GuiSteamGenerator':'BoilerScreen'})
group(('kineticgenerator','gui','kinetic','client'),{'GuiElectricKineticGenertor':'MotorScreen',
 'GuiSteamKineticGenerator':'SteamTurbineScreen','GuiStirlingKineticGenerator':'StirlingEngineScreen',
 'GuiWaterKineticGenerator':'WaterTurbineScreen','GuiWindKineticGenerator':'WindTurbineScreen'})
group(('heatgenerator','gui','thermal','client'),{'GuiElectricHeatGenerator':'ElectricHeaterScreen',
 'GuiFluidHeatGenerator':'FluidHeaterScreen','GuiRTHeatGenerator':'IsotopeHeaterScreen'})
group(('generator','gui','power','client'),{'GuiSolarGenerator':'SolarCollectorScreen'})
group(('kineticgenerator','container','kinetic','menu'),{'ContainerElectricKineticGenerator':'MotorMenu',
 'ContainerSteamKineticGenerator':'SteamTurbineMenu','ContainerStirlingKineticGenerator':'StirlingEngineMenu',
 'ContainerWaterKineticGenerator':'WaterTurbineMenu','ContainerWindKineticGenerator':'WindTurbineMenu'})
group(('heatgenerator','container','thermal','menu'),{'ContainerElectricHeatGenerator':'ElectricHeaterMenu',
 'ContainerFluidHeatGenerator':'FluidHeaterMenu','ContainerRTHeatGenerator':'IsotopeHeaterMenu'})
group(('generator','container','power','menu'),{'ContainerSolarGenerator':'SolarCollectorMenu'})
def rewrite_device_screens(text):
 result=rewrite_named_roles(text,RULES,{})
 for area in ['machine.gui','kineticgenerator.gui','heatgenerator.gui','generator.gui','kineticgenerator.container','heatgenerator.container','generator.container']:
  result=result.replace('import ic2ce.core.block.'+area+'.*;','')
 return result
def main():
 reorganize(RULES,rewrite_device_screens,'device-screen-layout-migration.json','Distinct processing, kinetic, thermal and solar native screen/menu policies')
if __name__=='__main__':main()
