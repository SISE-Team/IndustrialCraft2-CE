"""Name independent native component lifetimes and signal/resource roles; all moves count zero rewrites."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
import re

NAMES={
    'TileEntityComponent':'NativeComponent',
    'Components':'ComponentTypes',
    'BasicRedstoneComponent':'SignalOutput',
    'RedstoneEmitter':'DirectSignalOutput',
    'ComparatorEmitter':'AnalogSignalOutput',
    'Redstone':'SignalInput',
    'Process':'AddonProcessor',
    'Energy':'ElectricalBuffer',
    'Fluids':'FluidPorts'}
RULES={name:('ic2ce.core.block.comp','ic2ce.neoforge.component',renamed) for name,renamed in NAMES.items()}
FIXTURES={name:('ic2ce.core.block.comp','ic2ce.neoforge.component') for name in ('ProcessingComponentContracts','SignalContracts')}

def rewrite_components(text):
    # Native fluid constants share the former component's simple name. Preserve
    # their explicit/wildcard imports and qualified constants before Java role moves.
    text=text.replace('net.minecraft.world.level.material.Fluids','net.minecraft.world.level.material.IC2CENativeFluidConstants')
    if re.search(r'^import net\.minecraft\.world\.level\.material\.(?:\*|IC2CENativeFluidConstants);',text,re.M):
        text=re.sub(r'\bFluids\.(EMPTY|WATER|LAVA|FLOWING_WATER|FLOWING_LAVA)\b',r'IC2CENativeFluidConstants.\1',text)
    result=rewrite_named_roles(text,RULES,FIXTURES,(r'^import\s+ic2ce\.core\.block\.comp\.\*;\r?\n',))
    return result.replace('IC2CENativeFluidConstants','Fluids')

def main():
    reorganize(RULES,rewrite_components,'component-layout-migration.json','Explicit native component lifetime, analog/direct signals, electrical buffer, fluid ports and addon processing')

if __name__=='__main__':main()
