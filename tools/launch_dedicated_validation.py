"""Run the isolated official NeoForge server with a controllable stdin pipe."""
from pathlib import Path
import argparse
import json
import subprocess
import time
import zipfile
from project_artifacts import installed_ic2_jars

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--server-directory',type=Path,required=True)
parser.add_argument('--verification',action='store_true')
parser.add_argument('--host-report',type=Path)
parser.add_argument('--guest-report',type=Path)
parser.add_argument('--log-name',default='extended-r1')
parser.add_argument('--resume-base',type=Path)
parser.add_argument('--resume-extended',type=Path)
args=parser.parse_args()
workspace=Path(__file__).resolve().parents[2]
server=args.server_directory.resolve()
assert server.is_relative_to(workspace) and server.name.startswith('test-ic2-')
assert 'eula=true' in (server/'eula.txt').read_text()
jars=installed_ic2_jars(server/'mods');assert len(jars)==1,jars
with zipfile.ZipFile(jars[0]) as jar:
    assert any(name in jar.namelist() for name in ('ic2/verification/AllMachinesVerification.class', 'ic2ce/verification/AllMachinesVerification.class'))==args.verification
command=[str(workspace/'devtools/jdk-25/bin/java.exe'),'-Xms1G','-Xmx6G','-Djava.awt.headless=true']
if args.verification:
    assert args.host_report and args.guest_report
    command+=['-Dic2.verifyMigration=true','-Dic2.verifyAllMachines=true','-Dic2.verifyDedicated=true','-Dic2.verifyExtended=true',
              '-Dic2.remoteHostReport='+str(args.host_report.resolve()),'-Dic2.remoteGuestReport='+str(args.guest_report.resolve()),
              '-Dic2.longRunSeconds=7200']
    if args.resume_base or args.resume_extended:
        assert args.resume_base and args.resume_extended and args.resume_base.is_file() and args.resume_extended.is_file()
        command+=['-Dic2.resumeExtendedAfterMatrix=true','-Dic2.previousBaseReport='+str(args.resume_base.resolve()),'-Dic2.previousExtendedReport='+str(args.resume_extended.resolve())]
command+=['@libraries/net/neoforged/neoforge/26.1.2.114/win_args.txt','nogui']
control=server/'console-commands.txt';control.write_text('',encoding='utf-8')
metadata={'command':command,'cwd':str(server),'artifact':jars[0].name,'startedAt':time.time()}
log=server/(args.log_name+'.log')
with log.open('wb') as output:
    process=subprocess.Popen(command,cwd=server,stdin=subprocess.PIPE,stdout=output,stderr=subprocess.STDOUT,
                             text=True,encoding='utf-8',creationflags=getattr(subprocess,'CREATE_NO_WINDOW',0))
    metadata['pid']=process.pid
    (server/'running-server.json').write_text(json.dumps(metadata,indent=2)+'\n',encoding='utf-8')
    offset=0
    while process.poll() is None:
        with control.open(encoding='utf-8') as source:
            source.seek(offset)
            commands=source.readlines();offset=source.tell()
        for line in commands:
            if line.strip():process.stdin.write(line.rstrip('\r\n')+'\n');process.stdin.flush()
        time.sleep(0.25)
    metadata['exitCode']=process.returncode;metadata['finishedAt']=time.time()
    (server/'running-server.json').write_text(json.dumps(metadata,indent=2)+'\n',encoding='utf-8')
raise SystemExit(process.returncode)
