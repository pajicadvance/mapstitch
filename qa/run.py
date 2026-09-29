#!/usr/bin/env python3
"""Run a disposable Fabric 26.3 engine regression using an existing official-JAR runtime audit."""
import argparse, importlib.util, json, os, shutil, subprocess, zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--runtime-reference', type=Path, required=True, help='Existing official-JAR QA launcher module')
parser.add_argument('--mapstitch', type=Path, default=HERE.parent / 'versions/26.3-fabric/build/libs/mapstitch-fabric-1.1.6+26.3.jar')
parser.add_argument('--toolpouch', type=Path, default=HERE.parents[1] / 'toolpouch/versions/26.3-fabric/build/libs/toolpouch-fabric-1.1.10+26.3.jar')
parser.add_argument('--without-toolpouch', action='store_true')
args = parser.parse_args()
spec = importlib.util.spec_from_file_location('runtime', args.runtime_reference)
runtime = importlib.util.module_from_spec(spec)
spec.loader.exec_module(runtime)
build = HERE / 'build'
classes = build / 'classes'
classes.mkdir(parents=True, exist_ok=True)
deps = [args.mapstitch.resolve(), *runtime.deps()[1:]]
if not args.without_toolpouch:
    deps.append(args.toolpouch.resolve())
server, vanilla = runtime.audits()
paths = runtime.cp(server['command']) + runtime.cp(vanilla['command']) + list(map(str, deps))
for jar in deps:
    with zipfile.ZipFile(jar) as z:
        for member in z.namelist():
            if member.endswith('.jar'):
                dest = build / 'nested' / Path(member).name
                dest.parent.mkdir(exist_ok=True)
                dest.write_bytes(z.read(member))
                paths.append(str(dest))
fixture_class = 'SmokeQa' if args.without_toolpouch else 'ToolPouchQa'
subprocess.run(['/usr/bin/javac', '--release', '25', '-proc:none', '-cp', os.pathsep.join(dict.fromkeys(paths)), '-d', str(classes), str(HERE / (fixture_class + '.java'))], check=True)
fixture = build / (fixture_class + '.jar')
with zipfile.ZipFile(fixture, 'w', zipfile.ZIP_DEFLATED) as z:
    z.writestr('fabric.mod.json', json.dumps({'schemaVersion': 1, 'id': 'pouch_qa', 'version': '1', 'environment': 'server', 'entrypoints': {'main': ['qa.' + fixture_class]}, 'depends': {'mapstitch': '*', 'fabric-api': '*'}}))
    for p in classes.rglob(fixture_class + '*.class'):
        z.write(p, p.relative_to(classes))
deps.append(fixture)
run = build / ('without-toolpouch' if args.without_toolpouch else 'with-toolpouch')
if run.exists():
    shutil.rmtree(run)
mods = run / 'mods'
mods.mkdir(parents=True)
for p in deps:
    shutil.copy2(p, mods / p.name)
(run / 'eula.txt').write_text('eula=true\n')
(run / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25659\nonline-mode=false\nenforce-secure-profile=false\nview-distance=2\nsimulation-distance=2\nlevel-name=qa-world\nlevel-seed=927436\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\ndifficulty=peaceful\n')
command = runtime.base_command('server', run, 25659)
before = {str(p): runtime.sha(p) for p in deps}
(run / 'launch-audit.json').write_text(json.dumps({'command': command, 'mods': before}, indent=2))
with (run / 'console.log').open('w') as log:
    result = subprocess.run(command, cwd=run, stdout=log, stderr=subprocess.STDOUT, timeout=180)
after = {str(p): runtime.sha(p) for p in deps}
(run / 'integrity.json').write_text(json.dumps({'before': before, 'after': after, 'unchanged': before == after}, indent=2))
result_path = run / 'result.txt'
print('exit', result.returncode, flush=True)
print(result_path.read_text() if result_path.exists() else (run / 'console.log').read_text()[-8000:], flush=True)
assert before == after, 'Input JAR changed during test'
assert result.returncode == 0 and result_path.exists() and result_path.read_text().startswith('PASS '), 'Regression failed; inspect result and console.log'
