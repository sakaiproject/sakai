#!/usr/bin/env python3
"""Run the real Samigo migration experiment against a complete local Sakai deployment."""
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import time
import runtime
from convert import convert
from compare import compare
from calibrate import calibrate


def exercise(instance, output, label, browser_output=None):
    base = Path(instance['base'])
    env = dict(os.environ, JAVA_HOME=str(runtime.JAVA_HOME), PLAYWRIGHT_BASE_URL=instance['url'])
    command = ['mvn', '-f', str(runtime.REPO / 'e2e-tests/pom.xml'), '-B', '-ntp', '-Dtest=SamigoViewEquivalenceTest', '-Dsamigo.equivalence.browserOutput=' + str(browser_output or base / 'browser'), '-Dsamigo.equivalence.title=Samigo Facelets Flow', '-Dsakai.test.reportsDirectory='+str(output/(label+'-test-results')),'-Dsakai.test.artifacts='+str(output/(label+'-playwright')),'test']
    with (output / (label + '-e2e.log')).open('w') as log:
        result = subprocess.run(command, env=env, stdout=log, stderr=subprocess.STDOUT)
    if result.returncode: raise RuntimeError('Browser flow failed; inspect ' + str(output / (label + '-e2e.log')))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-tomcat', required=True, type=Path, help='Complete disposable demo Sakai deployment; copied/symlinked read-only')
    parser.add_argument('--output', type=Path, default=runtime.ROOT / 'target' / ('run-' + str(int(time.time()))))
    parser.add_argument('--jsp-port', type=int, default=18084)
    parser.add_argument('--facelets-port', type=int, default=18085)
    parser.add_argument('--calibrate', action='store_true', help='Also remove the creation listener and require the empty-title check to fail')
    parser.add_argument('--mysql-admin-config', type=Path, help='Private MySQL admin option file; defaults to local root TCP with no password')
    args = parser.parse_args()
    if args.jsp_port == args.facelets_port or args.jsp_port % 100 == args.facelets_port % 100:
        parser.error('Use distinct HTTP ports with distinct final two digits for isolated Ignite ports')
    output = args.output.resolve()
    if output.exists(): raise RuntimeError('Output must be new: ' + str(output))
    output.mkdir(parents=True)
    source = args.source_tomcat.resolve()
    probe, access = runtime.compile_probe(source, output)
    instances = {label:runtime.prepare(source, output, label, port, probe, access, args.mysql_admin_config) for label,port in [('jsp',args.jsp_port),('facelets',args.facelets_port)]}
    (output / 'runtimes.json').write_text(json.dumps(instances,indent=2))
    for label in ['jsp','facelets']:
        if label == 'facelets':
            convert(Path(instances['jsp']['base']),Path(instances['facelets']['base']),output)
            alignment = runtime.align_unchanged_jsp_cache(instances['jsp'], instances['facelets'])
            (output / 'jsp-cache-alignment.json').write_text(json.dumps(alignment,indent=2))
        process, log = runtime.start(instances[label])
        try:
            print(label + ': starting full Sakai runtime', flush=True)
            runtime.ready(instances[label], process, timeout=600)
            print(label + ': exercising create, two questions, second-row edit and fresh-login reopen', flush=True)
            exercise(instances[label],output,label)
            if label == 'facelets' and args.calibrate:
                print('facelets: calibrating missing-listener detection',flush=True)
                calibrate(instances[label],output)
        finally:
            runtime.stop(process,log)
    report = compare(Path(instances['jsp']['base']),Path(instances['facelets']['base']),output)
    print('Comparison ' + ('PASS' if report['passed'] else 'DIFF') + ': ' + str(output / 'report.json'),flush=True)
    return 0 if report['passed'] else 1


if __name__ == '__main__': raise SystemExit(main())
