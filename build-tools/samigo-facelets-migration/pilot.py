#!/usr/bin/env python3
"""Disposable whole-tool candidate, followed by a real browser migration check."""
import argparse
import json
import re
from pathlib import Path
import shutil
import sys
import time

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT.parent / 'samigo-facelets-flow'))
import runtime
import run as flow
import compare
import migrate


def install_baseline(app):
    baseline = ROOT / 'target/baseline-app'
    if not (baseline / 'WEB-INF/faces-config.xml').is_file() or not any(baseline.rglob('*.jsp')):
        raise RuntimeError('Provide a complete retained JSP application in ' + str(baseline))
    # app is an owned copy created by runtime.prepare, never the source deployment.
    shutil.rmtree(app)
    shutil.copytree(baseline, app)


def configure_candidate(app, converted=None):
    converted = converted or ROOT / 'target/converted-fast'
    faces = app / 'WEB-INF/faces-config.xml'
    text = faces.read_text()
    manifest = json.loads((converted / 'manifest.json').read_text())
    for page in manifest.get('removedViews', []):
        source = page['source']
        text = re.sub(r'\s*<navigation-case>\s*<from-outcome>[^<]+</from-outcome>\s*<to-view-id>/'
            + re.escape(source) + r'</to-view-id>\s*</navigation-case>', '', text)
        (app / source).unlink(missing_ok=True)
    for page in manifest['converted']:
        text = text.replace('/' + page['source'], '/' + page['target'])
        target = app / page['target'];target.parent.mkdir(parents=True,exist_ok=True)
        shutil.copy2(converted / page['target'], target)
        if 'template' in page:
            template = app / page['template'];template.parent.mkdir(parents=True,exist_ok=True)
            shutil.copy2(converted / page['template'], template)
        (app / page['source']).unlink()
    for fragment in manifest.get('fragments', []):
        target = app / fragment['template'];target.parent.mkdir(parents=True,exist_ok=True)
        shutil.copy2(converted / fragment['template'], target)
    text = text.replace('</lifecycle>', '<phase-listener>org.sakaiproject.tool.assessment.ui.facelets.SamigoViewPhaseListener</phase-listener><phase-listener>org.sakaiproject.equivalence.SamigoFaceletCompiler</phase-listener></lifecycle>')
    text = text.replace('</faces-config>', '<managed-bean><managed-bean-name>samigoViewSupport</managed-bean-name><managed-bean-class>org.sakaiproject.tool.assessment.ui.facelets.SamigoViewSupport</managed-bean-class><managed-bean-scope>request</managed-bean-scope></managed-bean></faces-config>')
    faces.write_text(text)
    descriptor = app / 'WEB-INF/web.xml'
    text = descriptor.read_text().replace('<web-resource-name>JSF templates</web-resource-name>', '<web-resource-name>JSF templates</web-resource-name><url-pattern>*.xhtml</url-pattern>')
    text = text.replace('</web-app>', '''<context-param><param-name>jakarta.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR</param-name><param-value>true</param-value></context-param>
<context-param><param-name>jakarta.faces.FACELETS_LIBRARIES</param-name><param-value>/WEB-INF/samigo-facelets.taglib.xml;/WEB-INF/upload-facelets.taglib.xml</param-value></context-param>
<filter><filter-name>samigo-header</filter-name><filter-class>org.sakaiproject.tool.assessment.ui.facelets.SamigoHeaderFilter</filter-class></filter>
<filter-mapping><filter-name>samigo-header</filter-name><servlet-name>Faces Servlet</servlet-name><dispatcher>REQUEST</dispatcher><dispatcher>FORWARD</dispatcher></filter-mapping>
</web-app>''')
    descriptor.write_text(text)
    for file in ['samigo-facelets.taglib.xml','upload-facelets.taglib.xml']:
        shutil.copy2(migrate.WEB / 'WEB-INF' / file, app / 'WEB-INF' / file)


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--output',type=Path,required=True);parser.add_argument('--source-tomcat',type=Path,required=True)
    parser.add_argument('--converted',type=Path,default=ROOT/'target/converted-fast')
    parser.add_argument('--jasper-cache',type=Path,default=ROOT/'target/jasper-compiled/org/apache/jsp')
    parser.add_argument('--calibrate',action='store_true')
    args=parser.parse_args();output=args.output.resolve();output.mkdir(parents=True)
    source=args.source_tomcat.resolve()
    probe,access=runtime.compile_probe(source,output)
    instances={label:runtime.prepare(source,output,label,port,probe,access,source_templates=False) for label,port in [('jsp',18086),('facelets',18087)]}
    for label,instance in instances.items():
        base=Path(instance['base']);app=base / 'webapps/samigo-app'
        install_baseline(app)
        helpers=Path('org/sakaiproject/tool/assessment/ui/facelets')
        shutil.copytree(runtime.REPO / 'samigo/samigo-app/target/classes' / helpers,app / 'WEB-INF/classes' / helpers,dirs_exist_ok=True)
        runtime.instrument(app,probe,access,copy_source=False)
        if label=='facelets':
            configure_candidate(app,args.converted)
            setenv=base / 'bin/setenv.sh'
            setenv.write_text(setenv.read_text().replace('-Dsamigo.equivalence.output=', '-Dsamigo.equivalence.compileViews=true -Dsamigo.equivalence.output='))
        else:
            cache=base / 'work/Catalina/localhost/samigo-app/org/apache/jsp'
            shutil.copytree(args.jasper_cache,cache,dirs_exist_ok=True)
    (output / 'runtimes.json').write_text(json.dumps(instances,indent=2))
    for label,instance in instances.items():
        process,log=runtime.start(instance)
        try:
            print(label+': booting',flush=True);runtime.ready(instance,process,timeout=600)
            print(label+': browser flow',flush=True);flow.exercise(instance,output,label)
            if label=='facelets' and args.calibrate:flow.calibrate(instance,output)
        finally:runtime.stop(process,log)
    original_normalize=compare.normalize
    manifest=json.loads((args.converted / 'manifest.json').read_text())
    mappings={'/'+page['target']:'/'+page['source'] for page in manifest['converted']}
    def normalize(value,identities):return original_normalize(value,{**identities,**mappings})
    compare.normalize=normalize
    report=compare.compare(*[Path(instances[label]['base']) for label in ['jsp','facelets']],output)
    for point in report['checkpoints']:print(point['step'],point['passed'],len(point['browserDifferences']),len(point['treeDifferences']),len(point['lifecycleDifferences']))
    return 0 if report['passed'] else 1


if __name__=='__main__':raise SystemExit(main())
