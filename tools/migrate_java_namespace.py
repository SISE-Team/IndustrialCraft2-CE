"""Move project Java namespaces without changing retained resource identifiers.

This is a naming migration only. It does not count as an implementation rewrite.
The lexer leaves translation keys, resource locations and configuration keys
alone, while moving executable references and reflective class names together.
"""
from pathlib import Path
import hashlib
import json
import re

PROJECT = Path(__file__).resolve().parents[1]
ROOTS = (
    "platform/neoforge/src/main/java", "api/src/main/java",
    "engine/src/main/java", "engine/src/test/java", "verification/src/main/java",
)
CLASS_NAMES = {
    "FmlMod": "IndustrialCraft2CE",
    "ForgeNetworkHandler": "NeoForgeNetworkHandler",
    "HandlerForge": "NativeInventoryHandle",
    **{name: name.replace("Forge", "NeoForge") for name in (
        "EnvProxyForge", "ClientEnvProxyForge", "EnvFluidHandlerForge",
        "ClientEnvFluidHandlerForge", "EnvItemHandlerForge", "EventHandlerForge",
        "ClientEventHandlerForge", "ClientModEventHandlerForge",
    )},
}
# Strings/comments are recognized before Java identifiers. No external parser
# or generated legacy implementation is involved in this migration.
LEXEMES = re.compile(r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\r\n]*|/\*[\s\S]*?\*/|[A-Za-z_$][\w$]*')
ENGINE = "org.siseteam.industrialcraft2ce.engine"
REFLECTIVE = re.compile(r'\bic2\.(?:api|core|forge|fabric|verification|integration)\.')


def qualified(text):
    text = text.replace(ENGINE, "ic2ce.engine")
    text = re.sub(r'\bic2\.forge\b', "ic2ce.neoforge", text)
    text = re.sub(r'\bic2\.', "ic2ce.", text)
    for before, after in CLASS_NAMES.items():
        text = re.sub(r'\b' + before + r'\b', after, text)
    return text


def rewrite(text):
    def token(match):
        value = match.group()
        if value.startswith(('"', "'")):
            if REFLECTIVE.search(value) or ENGINE in value:
                return qualified(value)
            return value
        if value.startswith(("//", "/*")):
            return qualified(value)
        return CLASS_NAMES.get(value, {"ic2": "ic2ce", "forge": "neoforge",
                                      "isForgeEnv": "isNeoForgeEnv"}.get(value, value))
    result = LEXEMES.sub(token, text)
    # The engine prefix consists of separate identifier tokens.
    result = result.replace(ENGINE, "ic2ce.engine")
    if 'getClass().getName().startsWith("ic2.")' in result:
        result = result.replace('getClass().getName().startsWith("ic2.")',
                                'getClass().getName().startsWith("ic2ce.")')
    if 'loadClass("ic2." + name' in result:
        result = result.replace('loadClass("ic2." + name', 'loadClass("ic2ce." + name')
    result = result.replace('getClassName().startsWith("ic2.")', 'getClassName().startsWith("ic2ce.")')
    for name in ('Ic2FluidStack', 'Ic2FluidItem'):
        result = result.replace('ic2ce.core.fluid.' + name, 'ic2ce.api.fluid.' + name)
    if re.search(r'public interface Ic2Fluid(?:Item|Stack)\b', result):
        result = result.replace('package ic2ce.core.fluid;', 'package ic2ce.api.fluid;')
    elif 'package ic2ce.core.fluid;' in result:
        for name in ('Ic2FluidStack', 'Ic2FluidItem'):
            if name in result and 'import ic2ce.api.fluid.' + name + ';' not in result:
                result = result.replace('package ic2ce.core.fluid;', 'package ic2ce.core.fluid;\n\nimport ic2ce.api.fluid.' + name + ';')
    result = re.sub(r'\b(?:ic2ce\.core\.proxy\.)?EnvProxy\.BiomeType\b', 'ic2ce.api.crops.BiomeType', result)
    result = re.sub(r'   public static enum BiomeType \{[^}]+\}\r?\n', '', result)
    if 'EnvProxy' not in result.replace('import ic2ce.core.proxy.EnvProxy;', ''):
        result = re.sub(r'import ic2ce\.core\.proxy\.EnvProxy;\r?\n', '', result)
    result = result.replace('import ic2ce.core.fluid.*;', 'import ic2ce.core.fluid.*;\nimport ic2ce.api.fluid.*;')
    from reorganize_inventory import rewrite_domains
    from reorganize_power import rewrite_power
    from reorganize_thermal import rewrite_thermal
    from reorganize_kinetic import rewrite_kinetic
    from reorganize_reactor import rewrite_reactor
    from reorganize_reactor_system import rewrite_reactor_system
    from reorganize_fluid_processing import rewrite_fluid_processing
    from reorganize_energy_devices import rewrite_energy_devices
    from reorganize_transport import rewrite_transport
    from reorganize_mining import rewrite_mining
    from reorganize_utility import rewrite_utility
    from reorganize_terrain import rewrite_terrain
    from reorganize_personal import rewrite_personal
    from reorganize_crafting import rewrite_crafting
    from reorganize_agriculture import rewrite_agriculture
    from reorganize_crop_domain import rewrite_crop_domain
    from reorganize_crop_species import rewrite_crop_species
    from reorganize_tools import rewrite_tools
    from reorganize_item_energy import rewrite_item_energy
    from reorganize_armor import rewrite_armor
    from reorganize_upgrades import rewrite_upgrades
    from reorganize_remaining_items import rewrite_remaining_items
    from reorganize_recipes import rewrite_recipes
    from reorganize_world import rewrite_world
    from reorganize_menus import rewrite_menus
    from reorganize_processing_menus import rewrite_processing_menus
    from reorganize_components import rewrite_components
    from reorganize_native_blocks import rewrite_native_blocks
    from reorganize_documents import rewrite_documents
    from reorganize_widgets import rewrite_widgets
    from reorganize_device_screens import rewrite_device_screens
    from reorganize_processing_devices import rewrite_processing_devices
    from reorganize_canning_registries import rewrite_canning_registries
    from reorganize_damage import rewrite_damage
    from reorganize_surfaces import rewrite_surfaces
    from reorganize_runtime_services import rewrite_runtime_services
    from reorganize_registry_definitions import rewrite_registry_definitions
    result = rewrite_item_energy(rewrite_tools(rewrite_crop_species(rewrite_crop_domain(rewrite_agriculture(rewrite_crafting(rewrite_personal(rewrite_terrain(rewrite_utility(rewrite_mining(rewrite_transport(rewrite_energy_devices(rewrite_fluid_processing(rewrite_reactor_system(rewrite_reactor(rewrite_kinetic(rewrite_thermal(rewrite_power(rewrite_domains(result)))))))))))))))))))
    return rewrite_registry_definitions(rewrite_runtime_services(rewrite_surfaces(rewrite_damage(rewrite_canning_registries(rewrite_processing_devices(rewrite_device_screens(rewrite_widgets(rewrite_documents(rewrite_native_blocks(rewrite_components(rewrite_processing_menus(rewrite_menus(rewrite_world(rewrite_recipes(rewrite_remaining_items(rewrite_upgrades(rewrite_armor(result))))))))))))))))))


def main():
    if (PROJECT / 'validation/namespace-migration.json').exists():
        print(json.dumps({'alreadyMigrated': True, 'javaRoot': 'ic2ce'}))
        return
    changes = []
    for root_name in ROOTS:
        root = PROJECT / root_name
        for source in list(root.rglob("*.java")):
            before = source.read_bytes()
            text = rewrite(before.decode("utf-8-sig"))
            package = re.search(r'^package\s+([\w.]+);', text, re.M)
            assert package, source
            target = root / Path(package[1].replace(".", "/")) / (CLASS_NAMES.get(source.stem, source.stem) + ".java")
            assert target == source or not target.exists(), target
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(text.encode("utf-8"))
            if source != target:
                source.unlink()
            changes.append({"before": source.relative_to(PROJECT).as_posix(),
                            "after": target.relative_to(PROJECT).as_posix(),
                            "beforeSha256": hashlib.sha256(before).hexdigest(),
                            "afterSha256": hashlib.sha256(target.read_bytes()).hexdigest(),
                            "countsAsImplementationRewrite": False})
    output = PROJECT / "validation/namespace-migration.json"
    output.write_text(json.dumps({"javaRoot": "ic2ce", "platformPackage": "ic2ce.neoforge",
                                 "resourceAndRegistryNamespace": "ic2", "sources": changes}, indent=2) + "\n", "utf-8")
    print(json.dumps({"migratedSources": len(changes), "report": str(output)}))


if __name__ == "__main__":
    main()
