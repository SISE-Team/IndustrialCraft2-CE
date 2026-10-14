"""Organize independently implemented native recipe, diagnostic and rendering roles; moves count zero rewrites."""
from pathlib import Path
import json
import re
from reorganize_energy_devices import reorganize, rewrite_named_roles

RULES = {}
def group(package,target,names):
    for former,name in names.items(): RULES[former]=(package,target,name)
group('ic2ce.core.recipe.input','ic2ce.neoforge.recipe.input',{
    'RecipeInputBase':'InputDescriptor','RecipeInputItemStack':'ExactStackInput','RecipeInputIngredient':'NativeIngredientInput',
    'RecipeInputMultiple':'AlternativeInputs','RecipeInputFluidContainer':'FluidContentsInput','RecipeInputFactory':'InputFactory',
    'FluidContainerIngredient':'FluidContentsIngredient','MaterialTagIngredient':'MaterialTagIngredient'})
group('ic2ce.core.recipe.v2','ic2ce.neoforge.recipe.codec',{
    'RecipeIo':'RecipeValueCodec','LazyRecipeInput':'DeferredInput','LazyMachineRecipe':'DeferredMachineRecipe',
    'LazyMachineRecipeWeighted':'DeferredWeightedRecipe','Ic2RecipeCodecSource':'NativeRecipeCodec',
    'BasicMachineRecipeSerializer':'CollectionRecipeCodec','IntegerOutputRecipeSerializer':'QuantityRecipeCodec',
    'WeightedMachineRecipeSerializer':'WeightedRecipeCodec','CannerBottleRecipeSerializer':'SolidCanningCodec',
    'CannerEnrichRecipeSerializer':'EnrichmentCodec','RecipeManagerGetter':'RecipeGeneration'})
group('ic2ce.core.recipe','ic2ce.neoforge.recipe.processing',{
    'MachineRecipeHelper':'OrderedMachineRecipes','BasicMachineRecipeManager':'BasicMachineRecipes','BasicListRecipeManager':'ListedMachineInputs',
    'SmeltingRecipeManager':'SmeltingRecipes','FermenterRecipeManager':'FermentationRecipes','ElectrolyzerRecipeManager':'ElectrolysisRecipes',
    'MatterAmplifierRecipeManager':'AmplifierRecipes','ScrapboxRecipeManager':'ScrapboxDrops'})
group('ic2ce.core.recipe','ic2ce.neoforge.recipe.crafting',{
    'AdvRecipe':'PositionedCrafting','AdvShapelessRecipe':'UnorderedCrafting','CraftingRemainders':'CraftingRemainders'})
RULES['Ic2BucketItem']=('ic2ce.core.item','ic2ce.neoforge.item.container','WorldFluidCell')
RULES['ItemDebug']=('ic2ce.core.item.tool','ic2ce.neoforge.item.tool','DiagnosticTool')
RULES['LaserBulletEntityRenderer']=('ic2ce.core.entity.render','ic2ce.neoforge.client.render','MiningBeamRenderer')
RULES['BeamRenderer']=('ic2ce.core.block.beam','ic2ce.neoforge.client.render','AcceleratorParticleRenderer')
FIXTURES={
    'RecipeInputContracts':('ic2ce.core.recipe.input','ic2ce.neoforge.recipe.input'),
    'RecipeFamilyContracts':('ic2ce.core.recipe.v2','ic2ce.neoforge.recipe.codec'),
    'ScrapboxContracts':('ic2ce.core.recipe','ic2ce.neoforge.recipe.processing')}
OLD_HOLDER='ic2ce.core.recipe.v2.RecipeHolder'
NEW_HOLDER='ic2ce.neoforge.recipe.codec.RecipeHolder'
def rewrite_recipes(text):
    package=re.search(r'^package\s+([\w.]+);',text,re.M)
    result=text.replace(OLD_HOLDER,NEW_HOLDER)
    result=rewrite_named_roles(result,RULES,FIXTURES,(r'^import\s+ic2ce\.core\.recipe(?:\.(?:input|v2))?\.\*;\r?\n',r'^import\s+ic2ce\.core\.item\.tool\.\*;\r?\n'))
    if package and package[1]=='ic2ce.core.recipe.v2':
        if re.search(r'\brecord\s+RecipeHolder\b',text): result=result.replace('package ic2ce.core.recipe.v2;','package ic2ce.neoforge.recipe.codec;',1)
        elif 'RecipeHolder' in text and not re.search(r'^package ic2ce\.neoforge\.recipe\.codec;',result,re.M) and NEW_HOLDER not in result:
            result=re.sub(r'(^package [\w.]+;)',lambda m:m[0]+'\n\nimport '+NEW_HOLDER+';',result,count=1,flags=re.M)
    if 'class AcceleratorParticleRenderer ' in result and 'import ic2ce.core.block.beam.ParticleEntity;' not in result:
        result=result.replace('package ic2ce.neoforge.client.render;', 'package ic2ce.neoforge.client.render;\n\nimport ic2ce.core.block.beam.ParticleEntity;',1)
    return result

def main():
    reorganize(RULES,rewrite_recipes,'recipe-layout-migration.json','Distinct native crafting, processing, input, codecs, diagnostics and entity rendering')
    # RecipeHolder is also a native vanilla class; move only the actual IC2 document instead of globally renaming its symbol.
    project=Path(__file__).resolve().parents[1];old='platform/neoforge/src/main/java/ic2ce/core/recipe/v2/RecipeHolder.java';new='platform/neoforge/src/main/java/ic2ce/neoforge/recipe/codec/RecipeHolder.java'
    source=project/old;target=project/new
    if source.exists():
        target.parent.mkdir(parents=True,exist_ok=True);source.rename(target)
        p=project/'validation/rewrite-ownership.json';data=json.loads(p.read_text('utf-8'))
        for row in data['completedImplementations']+data['integrationChanges']:
            if row['file']==old:row['file']=new
        before=next(row['before'] for row in json.loads((project/'validation/namespace-migration.json').read_text('utf-8'))['sources'] if row['after']==old)
        data.setdefault('retargetedNamespaceSources',{})[new]=before;p.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':main()
