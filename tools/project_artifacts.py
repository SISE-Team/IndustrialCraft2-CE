"""Read artifact names from the project build configuration."""
from pathlib import Path

PROJECT = Path(__file__).resolve().parents[1]
JAVA_ROOTS = tuple(PROJECT / path for path in ('platform/neoforge/src/main/java', 'api/src/main/java', 'engine/src/main/java'))
RESOURCE_ROOT = PROJECT / 'assets/src/main/resources'
VERIFICATION_ROOT = PROJECT / 'verification/src/main'
PROPERTIES = {}
for line in (PROJECT / 'gradle.properties').read_text(encoding='utf-8').splitlines():
    if line.strip() and not line.lstrip().startswith('#') and '=' in line:
        key, value = line.split('=', 1)
        PROPERTIES[key.strip()] = value.strip()

def artifact_name(classifier=None):
    base = PROPERTIES['mod_archive_name']
    version = PROPERTIES['minecraft_version'] + '-' + PROPERTIES['mod_version']
    return base + '-' + version + ('-' + classifier if classifier else '') + '.jar'

def artifact_path(classifier=None):
    return PROJECT / 'build/libs' / artifact_name(classifier)

def installed_ic2_jars(mods):
    import zipfile
    result = []
    for path in Path(mods).glob('*.jar'):
        with zipfile.ZipFile(path) as jar:
            if any(entry in jar.namelist() for entry in ('ic2ce/neoforge/runtime/ProjectRuntime.class', 'ic2ce/core/IC2.class', 'ic2/core/IC2.class')):
                result.append(path)
    return result
