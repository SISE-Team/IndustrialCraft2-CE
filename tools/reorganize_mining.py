"""Separate native pipe and area mining roles. File moves never count as rewritten implementation."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.block.machine'
PACKAGE = 'ic2ce.neoforge.mining'
RULES = {}
def roles(former, target, names):
    for before, after in names.items(): RULES[before] = (former, target, after)
roles(OLD + '.tileentity', PACKAGE, {'TileEntityMiner': 'PipeMiner', 'TileEntityAdvMiner': 'AreaMiner'})
roles(OLD + '.container', PACKAGE + '.menu', {'ContainerMiner': 'PipeMinerMenu', 'ContainerAdvMiner': 'AreaMinerMenu'})
roles(OLD + '.gui', PACKAGE + '.client', {'GuiMiner': 'PipeMinerScreen', 'GuiAdvMiner': 'AreaMinerScreen'})
FIXTURES = {name: (OLD + '.tileentity', PACKAGE) for name in ('MiningContracts', 'PipeMiningContracts')}
def rewrite_mining(text): return rewrite_named_roles(text, RULES, FIXTURES, implicit_packages=(OLD + '.tileentity', OLD + '.container', OLD + '.gui'))
def main(): reorganize(RULES, rewrite_mining, 'mining-layout-migration.json', 'Distinct native pipe and area mining protocols')
if __name__ == '__main__': main()
