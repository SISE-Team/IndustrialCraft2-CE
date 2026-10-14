"""Separate native component charge ledgers, manager routing and physical handheld supplies."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.item'
PACKAGE = 'ic2ce.neoforge.item.energy'
RULES = {before: (OLD, PACKAGE, after) for before, after in {
    'ElectricItemManager': 'ComponentChargeLedger', 'GatewayElectricItemManager': 'ElectricManagerRouter',
    'InfiniteElectricItemManager': 'CreativeChargeSource', 'BaseElectricItem': 'ElectricItemBase',
    'ItemBattery': 'RechargeableSupply', 'ItemBatterySU': 'DisposableSupply',
}.items()}

def rewrite_item_energy(text):
    return rewrite_named_roles(text, RULES, {}, implicit_packages=(OLD,))

def main():
    reorganize(RULES, rewrite_item_energy, 'item-energy-layout-migration.json', 'Independent component ledger, dynamic manager routing, creative source and distinct rechargeable/disposable policies')
if __name__ == '__main__': main()
