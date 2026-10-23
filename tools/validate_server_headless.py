"""Run isolated, real dedicated-server tests without launching a game client."""
from pathlib import Path
import argparse
import hashlib
import json
import shutil
import subprocess
import time
from project_artifacts import PROJECT, artifact_path, installed_ic2_jars


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--server-directory', type=Path, required=True)
    parser.add_argument('--phase', choices=('fixtures', 'reload', 'release'), required=True)
    args = parser.parse_args()
    workspace = Path(__file__).resolve().parents[2]
    server = args.server_directory.resolve()
    assert server.is_relative_to(workspace) and server.name.startswith('test-ic2-')
    assert 'eula=true' in (server / 'eula.txt').read_text()
    phase = args.phase
    verification = phase != 'release'
    if verification and not (server / 'machine-ids.json').exists():
        baseline = json.loads((PROJECT / 'validation/registry-id-baseline.json').read_text())
        excluded = {'crop_stick', 'sign', 'wall', 'itnt', 'nuke', 'classic_nuke'}
        ids = sorted(name for name in baseline['blockEntityBlocks'] if name not in excluded and not name.endswith('_crop'))
        assert len(ids) == 104, ids
        (server / 'machine-ids.json').write_text(json.dumps(ids) + '\n')
    artifact = artifact_path('verification' if verification else None)
    assert artifact.is_file(), artifact
    mods = server / 'mods'
    mods.mkdir(exist_ok=True)
    for old in installed_ic2_jars(mods):
        assert old.parent == mods
        old.unlink()
    shutil.copy2(artifact, mods / artifact.name)
    report = server / (phase + '-report.json')
    migration = server / 'migration-report.json'
    command = [str(workspace / 'devtools/jdk-25/bin/java.exe'), '-Xms512m', '-Xmx3G', '-Djava.awt.headless=true']
    if verification:
        command += ['-Dic2.verifyMigration=true', '-Dic2.verifyAllMachines=true', '-Dic2.verifyServerOnly=true',
                    '-Dic2.serverMachineIds=' + str(server / 'machine-ids.json'),
                    '-Dic2.serverOnlyReport=' + str(report), '-Dic2.verifyReport=' + str(migration)]
        if phase == 'reload': command += ['-Dic2.serverReload=true']
    command += ['@libraries/net/neoforged/neoforge/26.1.2.114/win_args.txt', 'nogui']
    metadata = {'phase': phase, 'artifact': artifact.name, 'sha256': hashlib.sha256(artifact.read_bytes()).hexdigest(),
                'started_at': time.time(), 'command': command, 'scope': 'headless dedicated server; no client rendering or multiplayer assertion'}
    log = server / (phase + '.log')
    stop_sent = False
    with log.open('wb') as output:
        process = subprocess.Popen(command, cwd=server, stdin=subprocess.PIPE, stdout=output,
                                   stderr=subprocess.STDOUT, text=True, encoding='utf-8',
                                   creationflags=getattr(subprocess, 'CREATE_NO_WINDOW', 0))
        metadata['pid'] = process.pid
        (server / 'running-server.json').write_text(json.dumps(metadata, indent=2))
        while process.poll() is None:
            elapsed = time.time() - metadata['started_at']
            if elapsed > 900:
                process.stdin.write('stop\n'); process.stdin.flush()
                try: process.wait(timeout=30)
                except subprocess.TimeoutExpired: process.terminate()
                raise RuntimeError('Dedicated server test exceeded 15 minutes; see ' + str(log))
            if phase == 'release' and not stop_sent and log.exists():
                text = log.read_text(encoding='utf-8', errors='replace')
                if 'Done (' in text:
                    process.stdin.write('tick sprint 200\nsave-all flush\nstop\n'); process.stdin.flush()
                    stop_sent = True
            time.sleep(0.5)
    metadata.update(exit_code=process.returncode, finished_at=time.time())
    metadata['elapsed_seconds'] = metadata['finished_at'] - metadata['started_at']
    (server / (phase + '-process.json')).write_text(json.dumps(metadata, indent=2) + '\n')
    assert process.returncode == 0, metadata
    text = log.read_text(encoding='utf-8', errors='replace')
    assert 'Done (' in text and 'Saving chunks' in text, 'Missing startup or clean world save'
    if verification:
        result = json.loads(report.read_text())
        assert result['finished'] and not result['failures'], result.get('failures')
        assert len(result['machines']) == 104, 'Incomplete machine catalog'
        if phase == 'fixtures':
            result = json.loads(migration.read_text())
            assert result['finished'] and not result['failures'], result.get('failures')
    print(json.dumps({'phase': phase, 'exit_code': process.returncode, 'elapsed_seconds': metadata['elapsed_seconds'], 'log': str(log), 'report': str(report) if verification else None}))


if __name__ == '__main__': main()
