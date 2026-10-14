"""Name native menu transfer and distinct physical/template slot roles; moves count zero rewrites."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

RULES={
    'ContainerBase':('ic2ce.core','ic2ce.neoforge.menu','OwnedMenu'),
    'SlotInvSlot':('ic2ce.core.slot','ic2ce.neoforge.menu.slot','MachineSlot'),
    'SlotInvSlotReadOnly':('ic2ce.core.slot','ic2ce.neoforge.menu.slot','DisplaySlot'),
    'SlotDischarge':('ic2ce.core.slot','ic2ce.neoforge.menu.slot','EnergyItemSlot'),
    'SlotBoxable':('ic2ce.core.slot','ic2ce.neoforge.menu.slot','StorageItemSlot'),
    'SlotRadioactive':('ic2ce.core.slot','ic2ce.neoforge.menu.slot','ReactorFuelSlot'),
    'ArmorSlot':('ic2ce.core.slot','ic2ce.neoforge.menu.slot','EquipmentOrder'),
    'SlotArmor':('ic2ce.core.slot','ic2ce.neoforge.menu.slot','EquipmentItemSlot'),
    'SlotHologramSlot':('ic2ce.core.slot','ic2ce.neoforge.menu.slot','TemplateSlot')}
FIXTURES={'MenuContracts':('ic2ce.core','ic2ce.neoforge.menu')}

def rewrite_menus(text):
    return rewrite_named_roles(text,RULES,FIXTURES,(r'^import\s+ic2ce\.core\.slot\.\*;\r?\n',),implicit_packages=('ic2ce.core',))

def main():
    reorganize(RULES,rewrite_menus,'menu-layout-migration.json','Native owned menus, component-aware physical slots, equipment and detached templates')

if __name__=='__main__':main()
