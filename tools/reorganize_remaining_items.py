"""Name independently replaced observation tools, portable menus, consumables, machining and hull policies."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

RULES = {}
def group(package, target, names):
    for former, name in names.items(): RULES[former] = (package, target, name)
group('ic2ce.core.item.tool', 'ic2ce.neoforge.item.tool', {
    'ItemFrequencyTransmitter':'TeleporterLinkCard', 'ItemWindMeter':'WindProbe', 'ItemScanner':'OreScanner',
    'ItemScannerAdv':'AdvancedOreScanner', 'ItemToolMeter':'EnergyProbe', 'ItemToolCrowbar':'CoverCrowbar',
    'ItemToolMiningLaser':'MiningLaser', 'ItemObscurator':'SurfaceImprinter'})
group('ic2ce.core.item.tool', 'ic2ce.neoforge.handheld', {
    'HandHeldToolbox':'ToolboxStorage','HandHeldScanner':'ScannerSession','HandHeldMeter':'MeterSession'})
RULES['ItemToolbox'] = ('ic2ce.core.item.tool','ic2ce.neoforge.item.container','PortableToolbox')
group('ic2ce.core.item.tool', 'ic2ce.neoforge.handheld.menu', {
    'ContainerToolbox':'ToolboxMenu','ContainerToolScanner':'ScannerMenu','ContainerMeter':'EnergyMeterMenu'})
RULES['ContainerHandHeldInventory'] = ('ic2ce.core.item','ic2ce.neoforge.handheld.menu','HandheldMenu')
group('ic2ce.core.item.tool', 'ic2ce.neoforge.handheld.client', {
    'GuiToolbox':'ToolboxScreen','GuiToolScanner':'ScannerScreen','GuiToolMeter':'EnergyMeterScreen'})
group('ic2ce.core.item', 'ic2ce.neoforge.item.consumable', {
    'ItemMug':'CoffeeCup','ItemTinCan':'CannedFood','ItemTerraWart':'MedicinalWart','ItemNuclearResource':'RadioactiveMaterial'})
RULES['DamageHandler'] = ('ic2ce.core.item','ic2ce.neoforge.item','PhysicalWear')
RULES['ItemWindRotor'] = ('ic2ce.core.item.resources','ic2ce.neoforge.kinetic','RotorItem')
RULES['BlockCuttingBlade'] = ('ic2ce.core.item.crafting','ic2ce.neoforge.item.machining','CuttingBlade')
RULES['BlockCuttingBladeType'] = ('ic2ce.core.item.type','ic2ce.neoforge.item.machining','BladeMaterial')
RULES['BoatItem'] = ('ic2ce.core.item.boat','ic2ce.neoforge.vehicle','HullItem')
group('ic2ce.core.entity.boat', 'ic2ce.neoforge.vehicle', {
    'RubberBoatEntity':'RubberHull','CarbonBoatEntity':'CarbonHull','ElectricBoatEntity':'ElectricHull'})
RULES['LaserBulletEntity'] = ('ic2ce.core.entity','ic2ce.neoforge.projectile','MiningBeam')
FIXTURES = {name:('ic2ce.core.item.tool','ic2ce.neoforge.item.tool') for name in ('ObservationContracts','LaserContracts','ObscuratorContracts')}
FIXTURES['ConsumableContracts'] = ('ic2ce.core.item','ic2ce.neoforge.item.consumable')

def rewrite_remaining_items(text):
    return rewrite_named_roles(text, RULES, FIXTURES, (r'^import\s+ic2ce\.core\.entity(?:\.boat)?\.\*;\r?\n',), implicit_packages=('ic2ce.core.item.tool','ic2ce.core.item'))
def main():
    reorganize(RULES, rewrite_remaining_items, 'remaining-item-layout-migration.json', 'Independent observation, imprinting, beam, portable inventory, consumable, machining and hull roles')
if __name__ == '__main__': main()
