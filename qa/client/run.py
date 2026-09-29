#!/usr/bin/env python3
"""Local cached official 26.3 client/server smoke; no production edits or agents."""
import importlib.util
import json
import os
from pathlib import Path
import shutil
import subprocess
import time
import zipfile

HERE = Path(__file__).resolve().parent
PROJECT = HERE.parents[1]
TEMPLATE = PROJECT.parent / 'mapstitch-polymer-compat-26.3/qa/launch.py'
spec = importlib.util.spec_from_file_location('launch', TEMPLATE)
launch = importlib.util.module_from_spec(spec)
spec.loader.exec_module(launch)
MODS = [PROJECT / 'versions/26.3-fabric/build/libs/mapstitch-fabric-1.1.6+26.3.jar',
        PROJECT.parent / 'toolpouch/versions/26.3-fabric/build/libs/toolpouch-fabric-1.1.10+26.3.jar',
        *launch.deps()[1:]]


def build():
    classes = HERE / 'classes'
    classes.mkdir(exist_ok=True)
    server, vanilla = launch.audits()
    paths = launch.cp(server['command']) + launch.cp(vanilla['command']) + list(map(str, MODS))
    for jar in MODS:
        with zipfile.ZipFile(jar) as archive:
            for member in archive.namelist():
                if member.endswith('.jar'):
                    path = HERE / 'nested' / Path(member).name
                    path.parent.mkdir(exist_ok=True)
                    path.write_bytes(archive.read(member))
                    paths.append(str(path))
    subprocess.run(['javac', '--release', '25', '-proc:none', '-cp', os.pathsep.join(dict.fromkeys(paths)),
                    '-d', str(classes), *map(str, HERE.glob('*.java'))], check=True)
    for side in ('server', 'client'):
        with zipfile.ZipFile(HERE / f'{side}-qa.jar', 'w') as archive:
            archive.writestr('fabric.mod.json', json.dumps({
                'schemaVersion': 1, 'id': 'pouch_qa_' + side, 'version': '1', 'environment': side,
                'entrypoints': {'main' if side == 'server' else 'client': ['pouchqa.Pouch' + side.title() + 'Qa']},
                'depends': {'mapstitch': '*', 'toolpouch': '*', 'fabric-api': '*'}}))
            for path in classes.rglob('*' + side.title() + '*.class'):
                archive.write(path, path.relative_to(classes))


def main():
    build()
    control = HERE / 'control'
    control.mkdir(exist_ok=True)
    for path in control.glob('*'):
        path.unlink()
    children = []
    env = os.environ.copy()
    env.update(SDL_VIDEODRIVER='x11', SDL_VIDEO_X11_XINPUT2='0', LP_NUM_THREADS='4')
    hashes = {str(path): launch.sha(path) for path in MODS}
    try:
        for mode in ('server', 'native'):
            run = HERE / 'runs' / mode
            run.mkdir(parents=True, exist_ok=True)
            mods = run / 'mods'
            mods.mkdir(exist_ok=True)
            for path in mods.glob('*.jar'):
                path.unlink()
            side = 'server' if mode == 'server' else 'client'
            selected = MODS + [HERE / f'{side}-qa.jar']
            for path in selected:
                shutil.copy2(path, mods / path.name)
            if mode == 'server':
                (run / 'eula.txt').write_text('eula=true\n')
                (run / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25666\nonline-mode=false\nwhite-list=false\nenforce-secure-profile=false\nview-distance=2\nsimulation-distance=2\nlevel-name=qa-world\nlevel-seed=927436\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\ndifficulty=peaceful\n')
            else:
                (run / 'options.txt').write_text('graphicsMode:0\nrenderDistance:3\nsimulationDistance:5\nmaxFps:30\nmaxFpsInactive:30\nsoundCategory_master:0.0\njoinedFirstServer:true\n')
            command = launch.base_command(mode, run, 25666)
            command.insert(1, '-Dpouch.qa.control=' + str(control))
            (run / 'launch-audit.json').write_text(json.dumps({'command': command, 'mods': [{'path': str(p), 'sha256': launch.sha(p)} for p in selected], 'qa_fixture': True}, indent=2))
            log = (run / 'console.log').open('w')
            child = subprocess.Popen(command, cwd=run, env=env, stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT, text=True)
            children.append((child, log))
            if mode == 'server':
                for _ in range(120):
                    if child.poll() is not None:
                        raise RuntimeError('Server exited: ' + str(run / 'console.log'))
                    if 'Done (' in (run / 'console.log').read_text():
                        break
                    time.sleep(1)
                else:
                    raise TimeoutError('Server startup')
        for _ in range(180):
            result = control / 'result.txt'
            if result.exists():
                message = result.read_text()
                print(message, flush=True)
                if not message.startswith('PASS'):
                    raise RuntimeError('Client smoke failed')
                return
            if any(child.poll() is not None for child, _ in children):
                raise RuntimeError('Process exited; inspect console logs')
            time.sleep(1)
        raise TimeoutError('Client result')
    finally:
        for child, log in reversed(children):
            if child.poll() is None:
                if child == children[0][0]:
                    child.stdin.write('stop\n')
                    child.stdin.flush()
                else:
                    child.terminate()
                try:
                    child.wait(timeout=30)
                except subprocess.TimeoutExpired:
                    child.kill()
                    child.wait()
            log.close()
        after = {str(path): launch.sha(path) for path in MODS}
        (HERE / 'integrity.json').write_text(json.dumps({'before': hashes, 'after': after, 'unchanged': hashes == after}, indent=2))
        assert hashes == after, 'Production JAR modified during run'


if __name__ == '__main__':
    main()
