"""Organize independent paired canning registries and actual fluid-container recipes."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
OLD='ic2ce.core.block.machine'
NEW='ic2ce.neoforge.recipe.processing'
RULES={before:(OLD,NEW,after) for before,after in {
 'CannerBottleRecipeManager':'SolidCanningRecipes','CannerEnrichRecipeManager':'EnrichmentRecipes',
 'CanningRecipeTable':'CanningRecipeCatalog','FillFluidContainerRecipeManager':'ContainerFillingRecipes',
 'EmptyFluidContainerRecipeManager':'ContainerDrainingRecipes'}.items()}
FIXTURES={'CanningRegistryContracts':(OLD,NEW)}
def rewrite_canning_registries(text):return rewrite_named_roles(text,RULES,FIXTURES)
def main():reorganize(RULES,rewrite_canning_registries,'canning-registry-layout-migration.json','Independent positive physical billing, immutable atomic canning generations and native fluid containers')
if __name__=='__main__':main()
