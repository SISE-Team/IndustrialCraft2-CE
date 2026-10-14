"""Name distinct terrain programs and linked teleportation; file moves are not rewrites."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.block.machine.tileentity'
FORMER_PROGRAMS = 'ic2ce.core.item.tfbp'
TERRAIN = 'ic2ce.neoforge.terrain'
RULES = {
    'TileEntityTeleporter': (OLD, 'ic2ce.neoforge.utility', 'LinkedTeleporter'),
    'TileEntityTerra': (OLD, TERRAIN, 'TerrainController'),
    'Tfbp': (FORMER_PROGRAMS, TERRAIN, 'TerrainBlueprint'),
    **{before: (FORMER_PROGRAMS, TERRAIN + '.program', after) for before, after in {
        'TerraformerBase': 'TerrainProgram', 'Chilling': 'FrostProgram', 'Cultivation': 'CultivationProgram',
        'Desertification': 'AridProgram', 'Flatification': 'LevelingProgram',
        'Irrigation': 'IrrigationProgram', 'Mushroom': 'FungalProgram',
    }.items()},
}
FIXTURES = {'TerrainTeleportContracts': (OLD, TERRAIN)}

def rewrite_terrain(text):
    return rewrite_named_roles(text, RULES, FIXTURES, implicit_packages=(OLD,)).replace(
        'import ' + FORMER_PROGRAMS + '.*;', 'import ' + TERRAIN + '.program.*;')

def main():
    reorganize(RULES, rewrite_terrain, 'terrain-layout-migration.json', 'Independent terrain programs and linked teleporter endpoints')

if __name__ == '__main__': main()
