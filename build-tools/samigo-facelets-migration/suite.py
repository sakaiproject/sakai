#!/usr/bin/env python3
"""Run existing Samigo author, student, grading and pool flows on both engines."""
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import pilot


def exercise(instance, output, label):
    base=Path(instance['base'])
    env=dict(os.environ,JAVA_HOME=str(pilot.runtime.JAVA_HOME),PLAYWRIGHT_BASE_URL=instance['url'])
    command=['mvn','-f',str(pilot.runtime.REPO/'e2e-tests/pom.xml'),'-B','-ntp','-Dtest=SamigoTest,SamigoViewEquivalenceTest#rendersAndCancelsEveryAvailableQuestionType',
        '-Dsamigo.equivalence.allQuestionTypes=true','-Dsamigo.equivalence.browserOutput='+str(base/'browser-types'),
        '-Dsakai.test.samigoTags=true','-Dsamigo.test.runId=FaceletsEquivalence',
        '-Dsamigo.test.fixtureTime=2026-10-09T12:00:00','-Dsamigo.equivalence.suiteOutput='+str(base/'browser'),'-Dsakai.test.reportsDirectory='+str(output/(label+'-test-results')),'-Dsakai.test.artifacts='+str(output/(label+'-playwright')),'test']
    with (output/(label+'-e2e.log')).open('w') as log:
        result=subprocess.run(command,env=env,stdout=log,stderr=subprocess.STDOUT)
    print(label+': browser suite exit status '+str(result.returncode)+' (all results retained)',flush=True)
    return result.returncode


def prepare(source,output,label,port,probe,access,source_templates=False):
    instance=pilot.runtime.prepare(source,output,label,port,probe,access,source_templates=False)
    base=Path(instance['base']);app=base/'webapps/samigo-app'
    pilot.install_baseline(app)
    helpers=Path('org/sakaiproject/tool/assessment/ui/facelets')
    shutil.copytree(pilot.runtime.REPO/'samigo/samigo-app/target/classes'/helpers,app/'WEB-INF/classes'/helpers,dirs_exist_ok=True)
    pilot.runtime.instrument(app,probe,access,copy_source=False)
    setenv=base/'bin/setenv.sh';setenv.write_text(setenv.read_text().replace('-Dsamigo.equivalence.output=',
        '-Dsamigo.equivalence.finalTreeOnly=true -Dsamigo.equivalence.output='))
    return instance


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-tomcat',type=Path,required=True);parser.add_argument('--output',type=Path,required=True)
    parser.add_argument('--jasper-fallback',type=Path)
    args=parser.parse_args();output=args.output.resolve();output.mkdir(parents=True)
    source=args.source_tomcat.resolve();probe,access=pilot.runtime.compile_probe(source,output)
    instances={}
    for label,port in [('jsp',18088),('facelets',18089)]:
        instance=prepare(source,output,label,port,probe,access,source_templates=False);instances[label]=instance
        (output/'runtimes.json').write_text(json.dumps(instances,indent=2))
        base=Path(instance['base']);app=base/'webapps/samigo-app'
        if label=='facelets':
            # The actual running Jasper compiler defines the comparison IDs.
            command=['python3',str(pilot.ROOT/'modular.py'),'--source-web',str(pilot.ROOT/'target/baseline-app'),'--jasper',
                str(Path(instances['jsp']['base'])/'work/Catalina/localhost/samigo-app'),
                '--output',str(output/'converted')]
            if args.jasper_fallback:command+=['--jasper-fallback',str(args.jasper_fallback)]
            result=subprocess.run(command)
            manifest=json.loads((output/'converted/manifest.json').read_text())
            shutil.copy2(output/'converted/manifest.json',output/'manifest.json')
            # Unresolved views remain explicitly listed; never count them as migrated.
            if result.returncode and not manifest['failures']:raise RuntimeError('Converter failed without manifest')
            pilot.configure_candidate(app,output/'converted')
            setenv=base/'bin/setenv.sh';setenv.write_text(setenv.read_text().replace('-Dsamigo.equivalence.output=',
                '-Dsamigo.equivalence.compileViews=true -Dsamigo.equivalence.output='))
        process,log=pilot.runtime.start(instance)
        try:
            print(label+': booting',flush=True);pilot.runtime.ready(instance,process,timeout=600)
            print(label+': existing Samigo suite and every available question authoring type',flush=True);exercise(instance,output,label)
        finally:pilot.runtime.stop(process,log)
    print('Both browser suites completed; raw captures ready for comparison: '+str(output),flush=True)


if __name__=='__main__':main()
