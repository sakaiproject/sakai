#!/usr/bin/env python3
"""Compile the preserved JSP application with the same Jasper used by Tomcat."""
import argparse
import json
import os
from pathlib import Path
import subprocess

JAVA_HOME = Path('/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home')


def compile_jsp(source, app, output):
    output.mkdir(parents=True, exist_ok=True)
    jars = [*source.joinpath('lib').glob('*.jar'), *source.joinpath('bin').glob('*.jar'),
            *app.joinpath('WEB-INF/lib').glob('*.jar'), *source.joinpath('components').rglob('*.jar')]
    for artifact in ['ant', 'ant-launcher']:
        jars.extend(Path.home().joinpath('.m2/repository/org/apache/ant', artifact).glob('*/*.jar'))
    classpath = os.pathsep.join(map(str, [app / 'WEB-INF/classes', *jars]))
    command = [str(JAVA_HOME / 'bin/java'), '-cp', classpath, 'org.apache.jasper.JspC',
               '-uriroot', str(app.resolve()), '-d', str(output.resolve()), '-p', 'org.apache.jsp',
               '-webapp', str(app.resolve()), '-l', '-die', '-compile']
    with (output / 'compile.log').open('w') as log:
        result = subprocess.run(command, stdout=log, stderr=subprocess.STDOUT)
    report = {'exitCode':result.returncode, 'generatedJava':len(list(output.rglob('*.java'))),
              'compiledClasses':len(list(output.rglob('*.class'))), 'log':str(output / 'compile.log')}
    (output / 'report.json').write_text(json.dumps(report, indent=2))
    return report


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-tomcat',type=Path,required=True)
    parser.add_argument('--app',type=Path,required=True)
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    print(json.dumps(compile_jsp(args.source_tomcat,args.app,args.output)))
