"""Read the closed dedicated-server world and verify machine and processing NBT."""
import argparse, json
from pathlib import Path
import audit_saved_fixtures as reader

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--server-directory',type=Path,required=True)
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    server=args.server_directory.resolve()
    report=json.loads((server/'reload-report.json').read_text())
    assert report['finished'] and not report['failures']
    reader.WORLD=server/'machine-world'
    chunks={}; entities={}; saved={}; failures=[]
    def entity(pos):
        key=(pos[0]>>4,pos[2]>>4)
        if key not in chunks: chunks[key]=reader.chunk(*key)
        return next((e for e in chunks[key].get('block_entities',[]) if (e['x'],e['y'],e['z'])==tuple(pos)),None)
    for name,row in report['machines'].items():
        value=entity(row['position'])
        if value is None or value.get('id')!='ic2:'+name or 'ic2' not in value: failures.append(name+': missing persisted machine state')
        else: saved[name]=value
    for name,pos in {'macerator':(34,101,31),'electric_furnace':(35,101,31),'compressor':(36,101,31),'extractor':(37,101,31)}.items():
        value=entity(pos)
        contents=value.get('ic2',{}).get('InvSlots',{}).get('output',{}).get('Contents',[]) if value else []
        if not contents or any(v.get('count',0)<=0 for v in contents): failures.append(name+': missing saved processing output')
        entities[name]=contents
    assert len(saved)==104
    result={'complete':not failures,'saved_machine_types':len(saved),'checked_chunks':len(chunks),
            'saved_processing_outputs':entities,'failures':failures,'world':str(reader.WORLD)}
    args.output.parent.mkdir(parents=True,exist_ok=True)
    args.output.write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps(result,indent=2))
    assert not failures,failures

if __name__=='__main__': main()
