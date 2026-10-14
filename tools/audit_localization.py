"""Audit source keys, original-key retention, locale parity, and format arguments."""
from pathlib import Path
import argparse, base64, json, re, zipfile
from contextlib import nullcontext
from project_artifacts import JAVA_ROOTS, RESOURCE_ROOT, VERIFICATION_ROOT

ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--output',type=Path,required=True)
parser.add_argument('--original',type=Path,help='Optional original JAR for key-retention checks')
args=parser.parse_args();out=args.output.resolve();out.mkdir(parents=True,exist_ok=True)
duplicates={}
def load(path):
    seen=[]
    def pairs(values):
        result={}
        for key,value in values:
            if key in result: seen.append(key)
            result[key]=value
        return result
    result=json.loads(path.read_text(encoding='utf-8-sig'),object_pairs_hook=pairs)
    duplicates[path.stem]=seen
    return result
data={p.stem:load(p) for p in (RESOURCE_ROOT/'assets/ic2/lang').glob('*.json')}
en=data['en_us'];refs=set();locations={}
def project_key(key):
    return 'ic2' in key.split('.')

def json_text_keys(value, location):
    if isinstance(value,dict):
        key=value.get('translate')
        if isinstance(key,str) and project_key(key):
            refs.add(key);locations.setdefault(key,[]).append(location)
        for child in value.values():json_text_keys(child,location)
    elif isinstance(value,list):
        for child in value:json_text_keys(child,location)

for path in RESOURCE_ROOT.rglob('*.json'):
    if path.parent.name!='lang':json_text_keys(json.loads(path.read_text(encoding='utf-8-sig')),str(path.relative_to(ROOT)))
pattern=re.compile(r'(?:translate|translatable|withText|withTooltip)\(\s*"([^"\n]+)"')
for p in (p for root in (*JAVA_ROOTS, RESOURCE_ROOT) for p in root.rglob('*')):
    if p.suffix not in ('.java','.xml'):continue
    text=p.read_text(encoding='utf-8-sig')
    for match in pattern.finditer(text):
        key=match.group(1)
        if not project_key(key):continue
        if key.endswith('.') or re.match(r'\s*\+',text[match.end():]):continue
        for line in key.split('\\n'):
            refs.add(line);locations.setdefault(line,[]).append(str(p.relative_to(ROOT)))
    if p.suffix=='.xml':refs.update(re.findall(r'\{((?:ic2|item\.ic2|block\.ic2|container\.ic2)\.[\w.]+)\}',text))
for key in json.loads((VERIFICATION_ROOT/'resources/ic2-verification-language-keys.json').read_text(encoding='utf-8')):
    refs.update(key.split('\\n'))
for group,names in {
 'ic2.upgrade.advancedGUI.':['ignored','direct','comparison','range','fuzzy','exact','less_or_equal','less','greater','greater_or_equal'],
 'ic2.crop.':['unknown','invalid','seeds','acorns','pine_cones','catkins','seedling'],
}.items(): refs.update(group+name for name in names)
def arguments(value):
    tokens=re.findall(r'%(?:(\d+)\$)?(?:[-#+ 0,(]*\d*(?:\.\d+)?)?([sdf])',value.replace('%%',''))
    position=0;result=[]
    for number,kind in tokens:
        if number: result.append(int(number))
        else:position+=1;result.append(position)
    return sorted(result)
failures=[];reports={}
original_path=args.original or ROOT/'original/industrialcraft-2-2.9.40+ex119-1.19.2-forge.jar'
with (zipfile.ZipFile(original_path) if original_path.is_file() else nullcontext(None)) as jar:
    for lang,values in data.items():
        original=json.loads(jar.read(f'assets/ic2/lang/{lang}.json')) if jar else {}
        retained=set(original)-set(values)
        invalid=[k for k,v in values.items() if not isinstance(v,str) or not v.strip() or '\ufffd' in v]
        missing=sorted(set(en)-set(values));raw=sorted(refs-set(values))
        mismatch=[k for k in set(en)&set(values) if arguments(en[k])!=arguments(values[k])]
        report={'entries':len(values),'originalEntries':len(original),'removedOriginalKeys':sorted(retained),'duplicates':duplicates[lang],
                'blankOrCorrupt':invalid,'missingEnglishKeys':missing,'missingSourceKeys':raw,'placeholderMismatches':mismatch,
                'effectiveMissingSourceKeys':sorted(refs-set(values)-set(en))}
        reports[lang]=report
        if retained or invalid or duplicates[lang]:failures.append(lang+': original retention, duplicate, or corrupt text')
        if lang in ('en_us','en_gb','en_ic','zh_cn','zh_tw') and (missing or raw or mismatch):failures.append(lang+': incomplete primary catalog or mismatched arguments')
        if report['effectiveMissingSourceKeys']:failures.append(lang+': missing fallback keys')
manifest=VERIFICATION_ROOT/'resources/ic2-verification-language-keys.json'
result={'languages':reports,'sourceKeys':len(refs),'originalRetentionChecked':jar is not None,'failures':failures,'complete':not failures}
(out/'localization-audit.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
with (out/'localization-format-input.tsv').open('w',encoding='utf-8',newline='\n') as f:
    for lang,values in data.items():
        for key,value in values.items():f.write(lang+'\t'+key+'\t'+base64.b64encode(value.encode('utf-8')).decode()+'\n')
print(json.dumps({'sourceKeys':len(refs),'entries':sum(len(v) for v in data.values()),'failures':failures,'missingSourceKeys':reports['en_us']['missingSourceKeys'],'argumentMismatch':{k:v['placeholderMismatches'] for k,v in reports.items() if k in ('zh_cn','zh_tw','en_gb','en_ic')}},ensure_ascii=False))
raise SystemExit(bool(failures))
