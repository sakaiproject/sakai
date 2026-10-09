#!/usr/bin/env python3
"""Retry a fresh candidate against an unchanged, previously measured JSP baseline."""
import argparse
import json
from pathlib import Path
import sys
import pilot


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--baseline-run',type=Path,required=True)
    parser.add_argument('--source-tomcat',type=Path,required=True);parser.add_argument('--output',type=Path,required=True)
    parser.add_argument('--calibrate',action='store_true')
    parser.add_argument('--converted',type=Path,default=pilot.ROOT/'target/converted-fast')
    args=parser.parse_args();output=args.output.resolve();output.mkdir(parents=True)
    source=args.source_tomcat.resolve()
    previous=json.loads((args.baseline_run/'runtimes.json').read_text());baseline=previous['jsp']
    probe,access=pilot.runtime.compile_probe(source,output)
    candidate=pilot.runtime.prepare(source,output,'facelets',18087,probe,access,source_templates=False)
    base=Path(candidate['base']);app=base/'webapps/samigo-app'
    pilot.install_baseline(app)
    helpers=Path('org/sakaiproject/tool/assessment/ui/facelets')
    pilot.shutil.copytree(pilot.runtime.REPO/'samigo/samigo-app/target/classes'/helpers,app/'WEB-INF/classes'/helpers,dirs_exist_ok=True)
    pilot.runtime.instrument(app,probe,access,copy_source=False);pilot.configure_candidate(app,args.converted)
    setenv=base/'bin/setenv.sh';setenv.write_text(setenv.read_text().replace('-Dsamigo.equivalence.output=','-Dsamigo.equivalence.compileViews=true -Dsamigo.equivalence.output='))
    instances={'jsp':baseline,'facelets':candidate};(output/'runtimes.json').write_text(json.dumps(instances,indent=2))
    process,log=pilot.runtime.start(candidate)
    try:
        print('Fresh Facelets candidate: booting',flush=True);pilot.runtime.ready(candidate,process,timeout=600)
        print('Fresh Facelets candidate: browser flow',flush=True);pilot.flow.exercise(candidate,output,'facelets')
        if args.calibrate:pilot.flow.calibrate(candidate,output)
    finally:pilot.runtime.stop(process,log)
    manifest=json.loads((args.converted/'manifest.json').read_text())
    mapping={'/'+page['target']:'/'+page['source'] for page in manifest['converted']}
    original=pilot.compare.normalize
    pilot.compare.normalize=lambda value,identities:original(value,{**identities,**mapping})
    report=pilot.compare.compare(Path(baseline['base']),base,output)
    report['normalizations'].pop('permittedViewChange',None)
    report['normalizations']['permittedViewChanges']=mapping
    (output/'report.json').write_text(json.dumps(report,indent=2))
    for point in report['checkpoints']:print(point['step'],point['passed'],len(point['browserDifferences']),len(point['treeDifferences']),len(point['lifecycleDifferences']))
    return 0 if report['passed'] else 1


if __name__=='__main__':raise SystemExit(main())
