"""Name native crop plots, registration and encoded genetics independently of role implementations."""
from reorganize_energy_devices import reorganize, rewrite_named_roles

OLD = 'ic2ce.core.crop'
PACKAGE = 'ic2ce.neoforge.agriculture'
RULES = {
    'TileEntityCrop': (OLD, PACKAGE, 'CropPlot'),
    'Ic2Crops': (OLD, PACKAGE, 'CropCatalog'),
    'ItemCropSeed': ('ic2ce.core.item', PACKAGE + '.item', 'EncodedSeed'),
}
FIXTURES = {'CropControllerContracts': (OLD, PACKAGE)}

def rewrite_crop_domain(text):
    return rewrite_named_roles(text, RULES, FIXTURES, implicit_packages=(OLD, 'ic2ce.core.item'))

def main():
    reorganize(RULES, rewrite_crop_domain, 'crop-domain-layout-migration.json', 'Native crop plots, immutable crop catalog generations and owned encoded seeds')

if __name__ == '__main__': main()
