"""Expose native species roles, native cultivar metadata and physical supports; naming itself counts no rewrites."""
import re
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.crop'
PACKAGE = 'ic2ce.neoforge.agriculture'
RULES = {}
for before, after in {
    'Ic2CropCard': 'MaterialSpecies', 'CropBase': 'StoredYieldSpecies',
    'CropVanilla': 'VanillaProduceSpecies', 'CropVanillaStem': 'FruitStemSpecies',
}.items(): RULES[before] = (OLD, PACKAGE + '.species', after)
for before, after in {
    'CropWheat': 'WheatSpecies', 'CropCarrots': 'CarrotSpecies', 'CropBeetroots': 'BeetSpecies',
    'CropPotato': 'PotatoSpecies', 'CropPumpkin': 'PumpkinSpecies', 'CropMelon': 'MelonSpecies',
    'CropReed': 'ReedSpecies', 'CropStickyReed': 'ResinReedSpecies', 'CropCocoa': 'CocoaSpecies',
    'CropCoffee': 'CoffeeSpecies', 'CropHops': 'HopSpecies', 'CropFlax': 'FlaxSpecies',
    'CropColorFlower': 'PigmentSpecies', 'CropBaseMushroom': 'MushroomSpecies',
    'CropBaseSapling': 'TreeSpecies', 'CropBaseMetalCommon': 'MetalSpecies',
    'CropBaseMetalUncommon': 'PreciousMetalSpecies', 'CropRedWheat': 'RedstoneGrainSpecies',
    'CropNetherWart': 'SoulWartSpecies', 'CropTerraWart': 'FrostWartSpecies',
    'CropVenomilia': 'VenomSpecies', 'CropEating': 'CarnivorousSpecies', 'CropWeed': 'WeedSpecies',
}.items(): RULES[before] = (OLD + '.cropcard', PACKAGE + '.species', after)
RULES['Ic2CropType'] = (OLD, PACKAGE, 'Cultivar')
RULES['ItemCrop'] = (OLD, PACKAGE + '.item', 'CropSupportItem')
CONSTANTS = {name: re.sub(r'(?<!^)(?=[A-Z])', '_', name).upper() for name in (
    'none', 'weed', 'wheat', 'carrots', 'potato', 'beetroots', 'pumpkin', 'melon', 'dandelion', 'poppy',
    'blackthorn', 'tulip', 'cyazint', 'venomilia', 'reed', 'stickyReed', 'cocoa', 'flax', 'redMushroom',
    'brownMushroom', 'netherWart', 'terraWart', 'oakSapling', 'spruceSapling', 'birchSapling', 'jungleSapling',
    'acaciaSapling', 'darkOakSapling', 'ferru', 'cyprium', 'stagnium', 'plumbiscus', 'aurelia', 'shining',
    'redWheat', 'coffee', 'hops', 'eatingPlant')}

def rewrite_crop_species(text):
    result = rewrite_named_roles(text, RULES, {}, (
        r'^import\s+ic2ce\.core\.crop(?:\.cropcard)?\.\*;\r?\n',), implicit_packages=())
    for before, after in CONSTANTS.items(): result = re.sub(r'\bCultivar\.' + before + r'\b', 'Cultivar.' + after, result)
    return result

def main(): reorganize(RULES, rewrite_crop_species, 'crop-species-layout-migration.json', 'Distinct independently implemented native species protocols, cultivar metadata and physical supports')
if __name__ == '__main__': main()
