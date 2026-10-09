#!/usr/bin/env python3
"""Check real QTI downloads on both retained, disposable runtime variants."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import re
import xml.etree.ElementTree as ET
import pilot


def compare_qti(instances,output):
    fixtures=[]
    def contract(element,path):
        attributes=dict(element.attrib)
        if element.tag in ['assessment','section','item'] and 'ident' in attributes:
            fixtures.append({'path':path,'tag':element.tag,'ident':attributes['ident']})
            attributes['ident']='<fixture-'+element.tag+'-at-'+path+'>'
        return {'tag':element.tag,'attributes':attributes,'text':(element.text or '').strip(),
            'children':[contract(child,path+'/'+str(index)) for index,child in enumerate(element)]}
    contracts=[];identities=[]
    for label in ['jsp','facelets']:
        fixtures.clear()
        file=Path(instances[label]['base'])/'browser-export/exportAssessment.xml'
        if not file.exists():return
        contracts.append(contract(ET.parse(file).getroot(),'root'));identities.append(list(fixtures))
    differences=pilot.compare.differences(*contracts)
    report={'passed':not differences,'differences':differences,'fixtureIdentities':dict(zip(['jsp','facelets'],identities)),
        'normalizations':['Assessment, section and item ident attributes created by the separate fixtures; XML text, all other attributes and element order remain exact.']}
    (output/'qti-report.json').write_text(json.dumps(report,indent=2))
    if differences:raise RuntimeError('QTI XML differs beyond fixture object identities')


def exercise(instance,output_root,label,security=False):
    base=Path(instance['base']);output=base/'browser-export';output.mkdir(exist_ok=True)
    env=dict(os.environ,JAVA_HOME=str(pilot.runtime.JAVA_HOME),PLAYWRIGHT_BASE_URL=instance['url'])
    command=['mvn','-f',str(pilot.runtime.REPO/'e2e-tests/pom.xml'),'-B','-ntp','-Dtest=SamigoViewEquivalenceTest#exportsAssessmentAsWellFormedQtiXml',
        '-Dsamigo.equivalence.export=true','-Dsamigo.equivalence.browserOutput='+str(output),'-Dsakai.test.reportsDirectory='+str(output_root.resolve()/(label+'-export-test-results')),
        '-Dsakai.test.artifacts='+str(output_root.resolve()/(label+'-export-playwright')),'test']
    if security:
        snapshot=json.loads((base/'browser/samigotest-cancreatequizfromscratch.json').read_text())
        site=re.search(r'/site/([^/]+)',snapshot['path'])[1]
        command[5]+=',SamigoImportAccessTest'
        command.insert(-1,'-Dsamigo.security.siteUrl='+instance['url']+'/portal/site/'+site)
    with (output_root/(label+'-export.log')).open('w') as log_output:result=subprocess.run(command,env=env,stdout=log_output,stderr=subprocess.STDOUT)
    if result.returncode:raise RuntimeError(label+' QTI export failed; inspect '+str(output_root/(label+'-export.log')))
    print(label+': QTI download PASS',flush=True)


def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('run',type=Path)
    parser.add_argument('--security',action='store_true')
    parser.add_argument('--candidate-only',action='store_true')
    args=parser.parse_args();instances=json.loads((args.run/'runtimes.json').read_text())
    for label in ['jsp','facelets']:
        if args.candidate_only and label=='jsp':continue
        instance=instances[label];base=Path(instance['base']);output=base/'browser-export';output.mkdir(exist_ok=True)
        process,log=pilot.runtime.start(instance)
        try:
            print(label+': export runtime booting',flush=True);pilot.runtime.ready(instance,process,timeout=600)
            exercise(instance,args.run,label,args.security)
        finally:pilot.runtime.stop(process,log)
    compare_qti(instances,args.run)


if __name__=='__main__':main()
