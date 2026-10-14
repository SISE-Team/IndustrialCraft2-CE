"""Organize independent document parsing, native menu projection and live text contracts."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
OLD='ic2ce.core.gui.dynamic'
NEW='ic2ce.neoforge.menu.document'
RULES={name:(OLD,NEW,renamed) for name,renamed in {
 'GuiParser':'ScreenDocument','TextProvider':'LiveText','DynamicContainer':'DocumentMenu',
 'DynamicHandHeldContainer':'HandheldDocumentMenu','DynamicGui':'DocumentScreen',
 'GuiEnvironment':'ScreenProjection','IGuiConditionProvider':'ScreenConditions',
 'IGuiValueProvider':'ScreenValues','IHolographicSlotProvider':'TemplateStorage'}.items()}
FIXTURES={name:(OLD,NEW) for name in ('GuiDocumentContracts','TextContracts')}
def rewrite_documents(text):
 return rewrite_named_roles(text,RULES,FIXTURES,remove_import_patterns=(r"^import\s+ic2ce\.core\.gui\.dynamic\.\*;\r?\n",))
def main():
 reorganize(RULES,rewrite_documents,'document-layout-migration.json','Bounded immutable documents, live text and distinct native/handheld menu projections')
if __name__=='__main__':main()
