#!/usr/bin/env python3
"""Exercise the clean production WAR on an already owned, stopped fixture runtime."""
import argparse
import json
from pathlib import Path
import shutil
import socket
from urllib.parse import urlparse
import zipfile
import pilot
import export_check


def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('run',type=Path)
    parser.add_argument('--war',type=Path,required=True);parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args();output=args.output.resolve();output.mkdir(parents=True)
    previous=json.loads((args.run/'runtimes.json').read_text());candidate=previous['facelets']
    base=Path(candidate['base']);app=base/'webapps/samigo-app'
    if not base.resolve().is_relative_to(args.run.resolve()) or base.name!='runtime-facelets':
        raise RuntimeError('Production check must use the candidate owned by this run')
    address=urlparse(candidate['url'])
    if address.hostname!='127.0.0.1':raise RuntimeError('Expected a loopback fixture runtime')
    try:
        connection=socket.create_connection(('127.0.0.1',address.port),timeout=1)
    except ConnectionRefusedError:
        pass
    else:
        connection.close();raise RuntimeError('Stop the owned runtime before replacing its app')
    if not (base/'sakai/sakai.properties').is_file() or not (base/'captures').is_dir():raise RuntimeError('Expected an owned fixture runtime')
    with zipfile.ZipFile(args.war) as archive:
        names=archive.namelist()
        if any(name.endswith(('.jsp','.jspf')) for name in names):raise RuntimeError('Production WAR still has JSPs')
        if any(name.startswith('/') or '..' in Path(name).parts for name in names):raise RuntimeError('Invalid WAR entry')
        shutil.rmtree(app);app.mkdir();archive.extractall(app)
    source=pilot.ROOT/'target/current-ui-deployment';probe,access=pilot.runtime.compile_probe(source,output)
    pilot.runtime.instrument(app,probe,access,copy_source=False)
    faces=app/'WEB-INF/faces-config.xml';faces.write_text(faces.read_text().replace('</lifecycle>',
        '<phase-listener>org.sakaiproject.equivalence.SamigoFaceletCompiler</phase-listener></lifecycle>'))
    (output/'runtimes.json').write_text(json.dumps({'jsp':previous['jsp'],'facelets':candidate},indent=2))
    browser=output/'browser';browser.mkdir()
    process,log=pilot.runtime.start(candidate)
    try:
        print('Production WAR: booting',flush=True);pilot.runtime.ready(candidate,process,timeout=600)
        pilot.flow.exercise(candidate,output,'facelets',browser_output=browser)
        print('Production WAR: persistence flow PASS',flush=True)
        export_check.exercise(candidate,output,'facelets',security=True)
        export_check.compare_qti(previous,output)
    finally:pilot.runtime.stop(process,log)


if __name__=='__main__':main()
