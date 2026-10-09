"""Deliberately remove the real creation listener and require behavioral detection."""
import json
import os
from pathlib import Path
import subprocess
import shutil
import xml.etree.ElementTree as ET
from compare import choose_trace, walk
from runtime import JAVA_HOME, REPO


def calibrate(instance, output):
    base = Path(instance['base'])
    template = base / 'webapps/samigo-app/jsf/author/createAssessment_title.xhtml'
    original = template.read_text()
    listener = '<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorAssessmentListener" />'
    if original.count(listener) == 0:
        include = ET.fromstring(original).find('{http://java.sun.com/jsf/facelets}include')
        if include is not None:
            template = base / 'webapps/samigo-app' / include.get('src').lstrip('/')
            original = template.read_text()
    if original.count(listener) != 1: raise ValueError('Expected exactly one creation listener')
    browser = base / 'browser-negative'
    command = ['mvn','-f',str(REPO / 'e2e-tests/pom.xml'),'-B','-ntp','-Dtest=SamigoViewEquivalenceTest#rejectsEmptyTitle','-Dsamigo.equivalence.calibration=true','-Dsamigo.equivalence.browserOutput=' + str(browser),'-Dsakai.test.reportsDirectory='+str(output/'negative-control-test-results'),'-Dsakai.test.artifacts='+str(output/'negative-control-playwright'),'test']
    env = dict(os.environ,JAVA_HOME=str(JAVA_HOME),PLAYWRIGHT_BASE_URL=instance['url'])
    try:
        template.write_text(original.replace(listener,''))
        with (output / 'negative-control-e2e.log').open('w') as log:
            result = subprocess.run(command,env=env,stdout=log,stderr=subprocess.STDOUT)
        opened_path, opened = choose_trace(base,'calibration-opened','authorIndexForm')
        submitted_path, submitted = choose_trace(base,'calibration-empty-title','authorIndexForm')
        create = next(node for node in walk(opened[-1]['tree']) if node['id'] == 'createnew')
        rendered = json.loads((browser / 'calibration-empty-title.json').read_text())
        report = {
            'mutation':'Remove AuthorAssessmentListener from the disposable Facelets creation template',
            'expectedTestFailure':result.returncode != 0,
            'creationListeners':create['actionListeners'],
            'serverMessages':submitted[-1]['messages'],
            'browserMessages':rendered['messages'],
            'rawTraces':[str(opened_path),str(submitted_path)],
        }
        report['detected'] = report['expectedTestFailure'] and not report['creationListeners'] and not report['serverMessages'] and not report['browserMessages']
        (output / 'negative-control.json').write_text(json.dumps(report,indent=2))
        if not report['detected']: raise RuntimeError('Negative control did not produce the expected listener/message mismatch')
        return report
    finally:
        template.write_text(original)


if __name__ == '__main__':
    import argparse
    parser=argparse.ArgumentParser();parser.add_argument('output',type=Path)
    args=parser.parse_args()
    instances=json.loads((args.output / 'runtimes.json').read_text())
    print(json.dumps(calibrate(instances['facelets'],args.output),indent=2))
