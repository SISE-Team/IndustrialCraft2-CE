"""Name native transport controllers by their distinct routing policies; moves count zero rewrites."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.block.machine'
TRANSPORT = 'ic2ce.neoforge.transport'
FLUID = 'ic2ce.neoforge.processing.fluid'
RULES = {}
def roles(former, target, names):
    for before, after in names.items(): RULES[before] = (former, target, after)
roles(OLD + '.tileentity', TRANSPORT, {'TileEntityFluidDistributor': 'FairFluidRouter', 'TileEntityWeightedFluidDistributor': 'PriorityFluidRouter',
    'TileEntityFluidRegulator': 'MeteredFluidOutlet', 'TileEntityWeightedItemDistributor': 'PriorityItemRouter', 'IWeightedDistributor': 'OrderedOutlets'})
roles(OLD + '.tileentity', FLUID, {'TileEntityPump': 'FluidCollector'})
roles(OLD + '.container', TRANSPORT + '.menu', {'ContainerFluidDistributor': 'FairFluidMenu', 'ContainerWeightedFluidDistributor': 'PriorityFluidMenu',
    'ContainerFluidRegulator': 'MeteredFluidMenu', 'ContainerWeightedItemDistributor': 'PriorityItemMenu'})
roles(OLD + '.gui', TRANSPORT + '.client', {'GuiFluidDistributor': 'FairFluidScreen', 'GuiWeightedDistributor': 'OutletOrderScreen',
    'GuiWeightedFluidDistributor': 'PriorityFluidScreen', 'GuiFluidRegulator': 'MeteredFluidScreen', 'GuiWeightedItemDistributor': 'PriorityItemScreen'})
FIXTURES = {'PumpContracts': (OLD + '.tileentity', FLUID), 'TransportContracts': (OLD + '.tileentity', TRANSPORT)}
def rewrite_transport(text): return rewrite_named_roles(text, RULES, FIXTURES, implicit_packages=(OLD + '.tileentity', OLD + '.container', OLD + '.gui'))
def main(): reorganize(RULES, rewrite_transport, 'transport-layout-migration.json', 'Distinct native fluid collection, fair/priority routing and metered outlets')
if __name__ == '__main__': main()
