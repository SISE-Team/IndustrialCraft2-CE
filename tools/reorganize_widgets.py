"""Group native widgets and screen extraction by ownership; relocation counts as no logic rewrite."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
OLD='ic2ce.core.gui'
NEW='ic2ce.neoforge.client.widget'
NAMES={
 'GuiElement':'ScreenWidget','Button':'ActionButton','Area':'HoverRegion','AbstractScrollingList':'ScrollViewport',
 'ScrollableList':'ClickableRows','ScrollableSelectiveList':'SelectableRows','BasicButton':'MinerControl',
 'CustomButton':'AtlasButton','VanillaButton':'NativeButton','StickyVanillaButton':'ModeButton',
 'CycleHandler':'OptionCursor','EnumCycleHandler':'EnumCursor','Gauge':'FillGauge','CustomGauge':'SupplierGauge',
 'LinkedGauge':'DeviceGauge','EnergyGauge':'ElectricalGauge','ProgressGauge':'ProcessGauge',
 'TankGauge':'ReservoirGauge','AbstractFluidSlot':'FluidWidget','TankFluidSlot':'ReservoirSlot',
 'TextLabel':'LiveLabel','TextBox':'TextInput','Image':'ResourceImage','GlTexture':'ImageDimensions',
 'ItemImage':'ItemIcon','ItemStackImage':'StackIcon','PlayerHead':'ProfileIcon','SlotGrid':'InventoryGrid',
 'RecipeButton':'RecipeOverlay','UpgradesWidget':'UpgradeHint','ElectrolyzerTank':'ElectrolysisTanks',
 'GuiDefaultBackground':'BorderedScreen','GuiFullInv':'PlayerInventoryScreen',
 'IClickHandler':'ClickAction','IEnableHandler':'WidgetCondition','IKeyboardDependent':'KeyboardFocus',
 'INumericValueHandler':'OptionValue','IOverlaySupplier':'AtlasRegion','OverlaySupplier':'FixedAtlasRegion',
 'FixedSizeOverlaySupplier':'DynamicAtlasRegion','MouseButton':'PointerButton','ScrollDirection':'ContentScroll'}
RULES={name:(OLD,NEW,renamed) for name,renamed in NAMES.items()}
RULES['Ic2Gui']=('ic2ce.core','ic2ce.neoforge.client.screen','MachineScreen')
FIXTURES={'WidgetContracts':(OLD,NEW)}
def rewrite_widgets(text):
 text=text.replace('InputEvent.MouseButton','InputEvent.IC2CENativePointerEvent')
 text=text.replace('import '+OLD+'.*;','import '+NEW+'.*;')
 result=rewrite_named_roles(text,RULES,FIXTURES)
 if "class MachineScreen" in result and "import ic2ce.core.IC2;" not in result:
  result=result.replace("package ic2ce.neoforge.client.screen;","package ic2ce.neoforge.client.screen;\n\nimport ic2ce.core.IC2;")
 return result.replace("InputEvent.IC2CENativePointerEvent","InputEvent.MouseButton")
def main():
 reorganize(RULES,rewrite_widgets,'widget-layout-migration.json','Owned client widgets, native input and coherent machine screen extraction')
if __name__=='__main__':main()
