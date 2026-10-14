"""Check the final closed world after intentional repeated output collection."""
from pathlib import Path
import argparse,json,math
import audit_saved_fixtures as nbt

parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--world',type=Path,required=True);parser.add_argument('--base',type=Path,required=True);parser.add_argument('--extended',type=Path,required=True);parser.add_argument('--output',type=Path,required=True);parser.add_argument('--allow-short-run',action='store_true',help='Audit a controlled short run without claiming two-hour acceptance');args=parser.parse_args()
base=json.loads(args.base.read_text(encoding='utf-8'));extended=json.loads(args.extended.read_text(encoding='utf-8'))
assert base['complete'] and not base['failures'] and not extended['failures']
if args.allow_short_run:
    assert extended['phase'] in ('REAL_TIME_SOAK', 'COMPLETE')
    assert extended['details']['soakElapsedSeconds'] > 0
else:
    assert extended['complete'] and extended['details']['soakElapsedSeconds'] >= 7200
nbt.WORLD=args.world.resolve();chunks={};checks=[];saved={}
for name,row in base['machines'].items():
    pos=tuple(row['position']);key=(pos[0]>>4,pos[2]>>4)
    if key not in chunks:chunks[key]=nbt.chunk(*key)
    entity=next(e for e in chunks[key]['block_entities'] if (e['x'],e['y'],e['z'])==pos)
    assert entity['id']=='ic2:'+name and 'ic2' in entity,name
    energy=entity['ic2'].get('components',{}).get('energy')
    if energy is not None:assert math.isfinite(energy['storage']) and energy['storage']>=0,name
    for slot in entity['ic2'].get('InvSlots',{}).values():
        for item in slot.get('Contents',[]):assert item.get('count',1)>0,(name,item)
    checks.append(name);saved[name]=entity
assert len(saved)==104
for name in ('wooden_storage_box','iron_storage_box','bronze_storage_box','steel_storage_box','iridium_storage_box'):
    items=saved[name]['ic2']['InvSlots']['inventory']['Contents'];assert len(items)==1 and items[0]['id']=='minecraft:iron_ingot' and items[0]['count']==13,name
for name in ('bronze_tank','iron_tank','steel_tank','iridium_tank'):
    fluid=saved[name]['ic2']['components']['fluid']['contents'];assert fluid['FluidName']=='ic2:coolant' and fluid['Amount']==2000,name
result={'complete':True,'savedMachineTypes':len(saved),'readChunks':len(chunks),'finiteStoredEnergy':True,'inventoryCountsValid':True,'retainedBoxes':5,'retainedTanks':4,'world':str(nbt.WORLD),'failures':[],'runScope':'controlled_short_run' if args.allow_short_run else 'two_hour_acceptance','actualSeconds':extended['details']['soakElapsedSeconds']}
args.output.write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
args.output.with_name('saved-machine-states-after-soak.json').write_text(json.dumps(saved,indent=2)+'\n',encoding='utf-8')
print(json.dumps(result))
