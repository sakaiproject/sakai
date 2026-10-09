#!/usr/bin/env python3
"""Measure a fresh full-suite candidate against a retained JSP observation."""
import argparse
import json
from pathlib import Path
import shutil
import pilot
import suite


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--baseline-run',type=Path,required=True)
    parser.add_argument('--source-tomcat',type=Path,required=True)
    parser.add_argument('--converted',type=Path,required=True)
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args();output=args.output.resolve();output.mkdir(parents=True)
    baseline=json.loads((args.baseline_run/'runtimes.json').read_text())['jsp']
    source=args.source_tomcat.resolve();probe,access=pilot.runtime.compile_probe(source,output)
    candidate=suite.prepare(source,output,'facelets',18089,probe,access)
    base=Path(candidate['base']);pilot.configure_candidate(base/'webapps/samigo-app',args.converted)
    setenv=base/'bin/setenv.sh';setenv.write_text(setenv.read_text().replace('-Dsamigo.equivalence.output=',
        '-Dsamigo.equivalence.compileViews=true -Dsamigo.equivalence.output='))
    shutil.copy2(args.converted/'manifest.json',output/'manifest.json')
    shutil.copytree(args.baseline_run/'jsp-test-results',output/'jsp-test-results')
    (output/'runtimes.json').write_text(json.dumps({'jsp':baseline,'facelets':candidate},indent=2))
    process,log=pilot.runtime.start(candidate)
    try:
        print('Fresh full-suite candidate: booting',flush=True);pilot.runtime.ready(candidate,process,timeout=600)
        suite.exercise(candidate,output,'facelets')
    finally:pilot.runtime.stop(process,log)


if __name__=='__main__':main()
