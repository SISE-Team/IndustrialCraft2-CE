"""Organize native definition tables; namespace moves never count as rewritten behavior."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
import re

OLD = 'ic2ce.core.ref'
TARGET = 'ic2ce.neoforge.registry'
RULES = {
    name: (OLD, TARGET, role) for name, role in {
        'Ic2ArmorMaterials': 'ArmorProfiles', 'Ic2BlockEntities': 'BlockEntityDefinitions',
        'Ic2Blocks': 'BlockDefinitions', 'Ic2BlockTags': 'BlockGroups',
        'Ic2BoatTypes': 'BoatDefinitions', 'Ic2Entities': 'EntityDefinitions',
        'Ic2EntityTags': 'EntityGroups', 'Ic2Fluids': 'FluidDefinitions',
        'Ic2FluidTags': 'FluidGroups', 'Ic2GameEvents': 'GameSignals',
        'Ic2Items': 'ItemDefinitions', 'Ic2ItemTags': 'ItemGroups',
        'IC2Material': 'BlockProfiles', 'Ic2RecipeSerializers': 'RecipeCodecs',
        'Ic2RecipeTypes': 'RecipeKinds', 'Ic2ScreenHandlers': 'MenuDefinitions',
        'Ic2SignType': 'SignDefinitions', 'Ic2SoundEvents': 'SoundDefinitions',
        'Ic2ToolMaterials': 'ToolProfiles', 'TabAssignments': 'CreativeContents',
    }.items()
}
RULES.update({
    'Tuple': ('ic2ce.core.util', 'ic2ce.engine.value', 'ValueTuples'),
    'Ic2Color': ('ic2ce.core.util', 'ic2ce.neoforge.item.paint', 'PigmentPalette'),
    'BlockMaterialChecks': ('ic2ce.core.util', 'ic2ce.neoforge.world.block', 'WorldMaterials'),
})
FIXTURES = {'CreativeContracts': (OLD, TARGET)}


def rewrite_registry_definitions(text):
    result = rewrite_named_roles(text, RULES, FIXTURES,
        (r'^import\s+ic2ce\.core\.ref\.\*;\r?\n',),
        implicit_packages=(OLD, 'ic2ce.core.util'))
    result = re.sub(r'^import\s+ic2ce\.core\.ref\.\*;\r?\n', '', result, flags=re.M)
    if re.search(r'^package\s+ic2ce\.engine\.value;', result, re.M):
        result = re.sub(r'^import\s+ic2ce\.core\.util\.\*;\r?\n', '', result, flags=re.M)
    return result


def main():
    reorganize(RULES, rewrite_registry_definitions, 'registry-definition-layout-migration.json',
        'Native definition tables, physical pigment policy and platform-independent value tuples; data moves do not count as behavior rewrites')


if __name__ == '__main__':
    main()
