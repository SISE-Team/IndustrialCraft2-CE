"""Organize native runtime, audio, configuration, diagnostics and weather by owned responsibility; moves never count as rewrites."""
from reorganize_energy_devices import reorganize,rewrite_named_roles
import re
RULES={
 'IC2':('ic2ce.core','ic2ce.neoforge.runtime','ProjectRuntime'),
 'Ic2Player':('ic2ce.core','ic2ce.neoforge.runtime','AutomationActors'),
 'Ic2Achievements':('ic2ce.core','ic2ce.neoforge.runtime','ProgressionService'),
 'WindSim':('ic2ce.core','ic2ce.neoforge.weather','WorldWind'),
 'ItemGroupIconSupplier':('ic2ce.core','ic2ce.neoforge.runtime','CategoryIcon'),
 'Ic2ItemGroupType':('ic2ce.core','ic2ce.neoforge.runtime','CreativeCategory'),
 'MainConfig':('ic2ce.core.init','ic2ce.neoforge.config','ProjectConfiguration'),
 'ConfigUtil':('ic2ce.core.util','ic2ce.neoforge.config','NativeConfigValues'),
 'Localization':('ic2ce.core.init','ic2ce.neoforge.client.text','LocalizedText'),
 'BlocksItems':('ic2ce.core.init','ic2ce.neoforge.registry','ContentBootstrap'),
 'Rezepte':('ic2ce.core.init','ic2ce.neoforge.recipe','RecipeBootstrap'),
 'OreValues':('ic2ce.core.init','ic2ce.neoforge.terrain','MaterialValuation'),
 'Log':('ic2ce.core.util','ic2ce.neoforge.diagnostics','ProjectLog'),
 'LogOutputStream':('ic2ce.core.util','ic2ce.neoforge.diagnostics','Utf8LogStream'),
 'LogCategory':('ic2ce.core.util','ic2ce.neoforge.diagnostics','DiagnosticArea'),
 'Sound':('ic2ce.core.sound','ic2ce.neoforge.audio','PlaybackHandle'),
 'SoundManager':('ic2ce.core.sound','ic2ce.neoforge.audio','AudioService'),
 'SoundClient':('ic2ce.core.sound','ic2ce.neoforge.client.audio','NativePlayback'),
 'SoundManagerClient':('ic2ce.core.sound','ic2ce.neoforge.client.audio','ClientAudioService'),
 'ListenableSoundInstance':('ic2ce.core.sound','ic2ce.neoforge.client.audio','FinishAwareAudio'),
 'PositionedSoundInstance':('ic2ce.core.sound','ic2ce.neoforge.client.audio','PositionedPlayback'),
 'RepeatablePositionedSoundInstance':('ic2ce.core.sound','ic2ce.neoforge.client.audio','PositionedLoop'),
 'EntityTrackingSoundInstance':('ic2ce.core.sound','ic2ce.neoforge.client.audio','EntityPlayback')}
FIXTURES={
 'ConfigurationContracts':('ic2ce.core.util','ic2ce.neoforge.diagnostics'),
 'AudioContracts':('ic2ce.core.sound','ic2ce.neoforge.client.audio'),
 'WeatherContracts':('ic2ce.core','ic2ce.neoforge.weather'),
 'BootstrapContracts':('ic2ce.core','ic2ce.neoforge.runtime')}
def rewrite_runtime_services(text):
 # Fully qualified native SoundManager must remain the actual Minecraft class.
 text=text.replace('net.minecraft.client.sounds.SoundManager','NativeSoundManagerSentinel')
 result=rewrite_named_roles(text,RULES,FIXTURES,(r'^import\s+ic2ce\.core\.sound\.\*;\r?\n',),implicit_packages=('ic2ce.core','ic2ce.core.init','ic2ce.core.util'))
 return re.sub(r'^import\s+ic2ce\.core\.init\.\*;\r?\n','',result,flags=re.M).replace('NativeSoundManagerSentinel','net.minecraft.client.sounds.SoundManager')
def main():reorganize(RULES,rewrite_runtime_services,'runtime-service-layout-migration.json','Owned native runtime/configuration/diagnostics/weather and separate dedicated/client audio responsibilities')
if __name__=='__main__':main()
