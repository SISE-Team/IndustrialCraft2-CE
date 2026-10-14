"""Organize independent native block/entity lifetime adapters; names alone count zero rewritten bodies."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

RULES={
    'Ic2TileEntityBlock':('ic2ce.core.block.tileentity','ic2ce.neoforge.block','MachineBlockType'),
    'Ic2TileEntity':('ic2ce.core.block.tileentity','ic2ce.neoforge.block','NativeBlockEntity'),
    'TileEntityInventory':('ic2ce.core.block.tileentity','ic2ce.neoforge.block','InventoryBlockEntity'),
    'TileEntityBase':('ic2ce.core.block.tileentity','ic2ce.neoforge.block','ActiveBlockEntity'),
    'TileEntityElectricMachine':('ic2ce.core.block.machine.tileentity','ic2ce.neoforge.block','PoweredBlockEntity')}

def rewrite_native_blocks(text):
    return rewrite_named_roles(text,RULES,{},implicit_packages=('ic2ce.core.block.tileentity','ic2ce.core.block.machine.tileentity'))

def main():
    reorganize(RULES,rewrite_native_blocks,'native-block-layout-migration.json','Native block states, entity lifetimes, physical inventories, active audio and electrical admission')

if __name__=='__main__':main()
