"""Give personal trading and remaining item routing explicit native roles; moves are separate evidence."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.block.personal'
PERSONAL = 'ic2ce.neoforge.personal'
MACHINE = 'ic2ce.core.block.machine'
TRANSPORT = 'ic2ce.neoforge.transport'
RULES = {}
def roles(former, target, names):
    for before, after in names.items(): RULES[before] = (former, target, after)
roles(OLD, PERSONAL, {
    'IPersonalBlock': 'OwnedDevice', 'TileEntityPersonalChest': 'PrivateChest',
    'TileEntityTradeOMat': 'ItemTradeTerminal', 'TileEntityEnergyOMat': 'EnergyTradeTerminal',
    'TradingMarket': 'WirelessTradeMarket',
})
roles(OLD, PERSONAL + '.menu', {
    'ContainerTradeOMatOpen': 'OwnerItemTradeMenu', 'ContainerTradeOMatClosed': 'PublicItemTradeMenu',
    'ContainerEnergyOMatOpen': 'OwnerEnergyTradeMenu', 'ContainerEnergyOMatClosed': 'PublicEnergyTradeMenu',
})
roles(OLD, PERSONAL + '.client', {
    'GuiTradeOMatOpen': 'OwnerItemTradeScreen', 'GuiTradeOMatClosed': 'PublicItemTradeScreen',
    'GuiEnergyOMatOpen': 'OwnerEnergyTradeScreen', 'GuiEnergyOMatClosed': 'PublicEnergyTradeScreen',
})
roles(MACHINE + '.tileentity', TRANSPORT, {'TileEntitySortingMachine': 'BatchItemSorter', 'TileEntityItemBuffer': 'DualItemBuffer'})
roles(MACHINE + '.container', TRANSPORT + '.menu', {'ContainerSortingMachine': 'BatchItemMenu', 'ContainerItemBuffer': 'DualBankMenu'})
roles(MACHINE + '.gui', TRANSPORT + '.client', {'GuiSortingMachine': 'BatchItemScreen', 'GuiItemBuffer': 'DualBankScreen'})
FIXTURES = {
    **{name: (OLD, PERSONAL) for name in ('PersonalContracts', 'EnergySalesContracts', 'ItemSalesContracts')},
    'SortingContracts': (MACHINE + '.tileentity', TRANSPORT),
}
def rewrite_personal(text):
    return rewrite_named_roles(text, RULES, FIXTURES, implicit_packages=(MACHINE + '.tileentity', MACHINE + '.container', MACHINE + '.gui')).replace(
        'import ' + OLD + '.*;', 'import ' + PERSONAL + '.*;').replace(
        '   final boolean canToggleInfinite;', '   public final boolean canToggleInfinite;')
def main(): reorganize(RULES, rewrite_personal, 'personal-layout-migration.json', 'Independent personal ownership, energy/item invoices and filtered/dual item routing')
if __name__ == '__main__': main()
