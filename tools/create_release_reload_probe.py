"""Create a vanilla-command probe in the isolated validation world only.

The probe reads real loaded IC2 block entities; its only writes are its own
command-storage results. It never modifies machines, inventories, or energy.
"""
import argparse
import json
from pathlib import Path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--world', type=Path, required=True)
parser.add_argument('--report', type=Path, required=True)
args = parser.parse_args()
world = args.world.resolve()
workspace = Path(__file__).resolve().parents[2]
assert world.is_relative_to(workspace) and (world.parent.name == 'saves' or (world.name == 'world' and world.parent.name.startswith('test-ic2-')))
runtime = json.loads(args.report.read_text(encoding='utf-8'))
assert runtime['complete'] and runtime['registeredMachineTypes'] == 104
pack = world / 'datapacks/ic2-release-reload-probe'
function = pack / 'data/ic2_verification/function/read_saved_machines.mcfunction'
function.parent.mkdir(parents=True, exist_ok=True)
(pack / 'pack.mcmeta').write_text(json.dumps({'pack': {
    'description': 'IC2 isolated world: read loaded machines after release reload',
    'min_format': [101, 1], 'max_format': [101, 1]
}}, indent=2)+'\n', encoding='utf-8')
storage = 'ic2_verification:reload'
lines = [f'data modify storage {storage} summary set value {{passed:[],failures:[],persistedChecks:[]}}']
for name, row in runtime['machines'].items():
    pos = ' '.join(map(str, row['position']))
    valid = f'if loaded {pos} if block {pos} ic2:{name} if data block {pos} {{id:"ic2:{name}",ic2:{{}}}}'
    lines.append(f'execute {valid} run data modify storage {storage} summary.passed append value "{name}"')
    lines.append(f'execute unless loaded {pos} run data modify storage {storage} summary.failures append value "{name}: unloaded"')
    lines.append(f'execute if loaded {pos} unless block {pos} ic2:{name} run data modify storage {storage} summary.failures append value "{name}: wrong block"')
    lines.append(f'execute if loaded {pos} if block {pos} ic2:{name} unless data block {pos} {{id:"ic2:{name}",ic2:{{}}}} run data modify storage {storage} summary.failures append value "{name}: missing IC2 entity state"')

def persisted(name, predicate, detail):
    pos = ' '.join(map(str, runtime['machines'][name]['position']))
    lines.append(f'execute if loaded {pos} if data block {pos} {predicate} run data modify storage {storage} summary.persistedChecks append value "{detail}"')
    lines.append(f'execute if loaded {pos} unless data block {pos} {predicate} run data modify storage {storage} summary.failures append value "{detail}: not restored"')

for name in ['wooden_storage_box','iron_storage_box','bronze_storage_box','steel_storage_box','iridium_storage_box']:
    persisted(name,'{ic2:{InvSlots:{inventory:{Contents:[{id:"minecraft:iron_ingot",count:13}]}}}}',name+': 13 ingots')
for name in ['bronze_tank','iron_tank','steel_tank','iridium_tank']:
    persisted(name,'{ic2:{components:{fluid:{contents:{FluidName:"ic2:coolant",Amount:2000}}}}}',name+': 2000 mB coolant')
for name in ['lv_transformer','mv_transformer','hv_transformer','ev_transformer']:
    persisted(name,'{ic2:{mode:1}}',name+': manual step-down')
persisted('fluid_regulator','{ic2:{mode:1,outputmb:100,components:{fluid:{fluidTank:{FluidName:"ic2:coolant",Amount:63}}}}}','fluid regulator: per-tick 100 mB, 63 mB remaining')
persisted('batch_crafter','{ic2:{InvSlots:{"ingredient[4]":{Contents:[{id:"ic2:forge_hammer",components:{"minecraft:damage":1}}]},output:{Contents:[{id:"ic2:iron_plate",count:1}]}}}}','batch crafter: iron plate and damaged offset hammer')
persisted('personal_chest','{ic2:{ownerGameProfile:{name:"IC2Test"},InvSlots:{content:{Contents:[{id:"minecraft:diamond",count:10}]}}}}','personal chest: ownership and ten diamonds')
for key in ['passed','failures','persistedChecks']:
    lines.append(f'execute store result storage {storage} summary.{key}Count int 1 run data get storage {storage} summary.{key}')
lines.append('tellraw @a [{"text":"IC2 release reload: "},{"nbt":"summary.passedCount","storage":"'+storage+'"},{"text":"/104 machines; failures="},{"nbt":"summary.failuresCount","storage":"'+storage+'"},{"text":"; persisted checks="},{"nbt":"summary.persistedChecksCount","storage":"'+storage+'"}]')
lines.append(f'data modify storage {storage} completed set value 1b')
function.write_text('\n'.join(lines)+'\n', encoding='utf-8')
# The copied world has cheats disabled. A vanilla tick function runs this once
# after the real player has joined and every tested chunk is actually loaded.
loaded=' '.join('if loaded '+' '.join(map(str,row['position'])) for row in runtime['machines'].values())
(function.parent/'once_when_loaded.mcfunction').write_text(
    f'execute if entity @a[name=IC2Test] unless data storage {storage} {{completed:1b}} {loaded} run function ic2_verification:read_saved_machines\n', encoding='utf-8')
tick_tag=pack/'data/minecraft/tags/function/tick.json'
tick_tag.parent.mkdir(parents=True,exist_ok=True)
tick_tag.write_text(json.dumps({'values':['ic2_verification:once_when_loaded']})+'\n',encoding='utf-8')
print(function)
