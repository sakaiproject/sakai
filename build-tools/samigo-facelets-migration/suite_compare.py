#!/usr/bin/env python3
"""Compare full-suite observations, reporting every remaining difference."""
import argparse
from datetime import datetime
from functools import lru_cache
from zoneinfo import ZoneInfo
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET
import pilot


def observations(base,step):
    result=[]
    for path in sorted((base/'captures'/step).glob('*.json'),key=lambda path:path.stat().st_mtime_ns):
        frames=json.loads(path.read_text())
        if not frames:continue
        result.append({'phases':[{name:frame[name] for name in ['phase','postback','validationFailed','viewId','messages']} for frame in frames],
            'forms':[pilot.compare.contract(node) for node in pilot.compare.walk(frames[-1].get('tree'))
                     if node['class'].endswith('.HtmlForm')], 'rawTrace':str(path)})
    return result


def identity_map(left,right):
    mappings=[{},{}]
    for browsers in zip(left,right):
        for pattern,label in [(r'/site/([^/]+)','fixture-site'),(r'/tool/([^/]+)','fixture-tool')]:
            matches=[re.search(pattern,browser['path']) for browser in browsers]
            if all(matches):
                for mapping,match in zip(mappings,matches):
                    mapping.setdefault(match[1],'<'+label+'-'+str(len(mapping))+'>')
    return mappings


DATE=re.compile(r"\b(?:[A-Z][a-z]{2} \d{1,2}, \d{4}, \d{1,2}:\d{2}(?::\d{2})? (?:AM|PM)|\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})\b")


@lru_cache(maxsize=8)
def fixture_context(base):
    zones=set()
    def collect(node):
        if isinstance(node,dict):
            options=node.get('converterOptions',{})
            if options.get('timezone'):zones.add(options['timezone'])
            for value in node.values():collect(value)
        elif isinstance(node,list):
            for value in node:collect(value)
    for trace in (base/'captures').rglob('*.json'):collect(json.loads(trace.read_text()))
    if len(zones)!=1:raise ValueError('A single centrally resolved fixture timezone is required: '+str(zones))
    files=list((base/'browser').glob('*.json'))+list((base/'browser-types').glob('*.json'))
    bounds=(min(file.stat().st_mtime for file in files)-300,max(file.stat().st_mtime for file in files)+60)
    return bounds,ZoneInfo(next(iter(zones)))


def fixture_dates(value,base):
    bounds,zone=fixture_context(base)
    observed=[]
    def date(match):
        text=match[0]
        format=('%Y-%m-%d %H:%M:%S' if text[0].isdigit() else '%b %d, %Y, %I:%M:%S %p' if text.count(':')==2 else '%b %d, %Y, %I:%M %p')
        try:timestamp=datetime.strptime(text,format).replace(tzinfo=zone).timestamp()
        except ValueError:return text
        if not bounds[0]<=timestamp<=bounds[1]:return text
        observed.append(text)
        return '<runtime-generated-date:'+format+'>'
    return DATE.sub(date,value),observed


def separate_fixture_differences(differences,bases,control_paths,external=False):
    remaining=[];fixtures=[]
    for difference in differences:
        left=difference.get('jsp');right=difference.get('facelets');path=difference['path']
        if external:
            fixtures.append({**difference,'reason':'Final snapshot belongs to an unchanged external tool; its test status is compared separately.'});continue
        reason=None
        if isinstance(left,str) and isinstance(right,str):
            if path.endswith('.text') or path in control_paths.get('editform:createdDate',[]):
                normalized=[fixture_dates(value,base) for value,base in zip([left,right],bases)]
                if normalized[0][0]==normalized[1][0] and any(row[1] for row in normalized):
                    reason='Times generated within each disposable fixture run; date formats and converter settings are retained.'
            if path in control_paths.get('questionpool:selectedTags',[]) and all(re.fullmatch(r'[a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12}',value) for value in [left,right]):
                reason='Selected tag identity created independently by the tag fixture; control binding and submitted filtering behavior are compared.'
        (fixtures if reason else remaining).append({**difference,'reason':reason} if reason else difference)
    return remaining,fixtures


def compare(output):
    instances=json.loads((output/'runtimes.json').read_text())
    bases=[Path(instances[label]['base']) for label in ['jsp','facelets']]
    steps=sorted(set(path.stem for base in bases for path in (base/'browser').glob('*.json') if not path.stem.endswith('-errors')))
    browsers=[[json.loads((base/'browser'/(step+'.json')).read_text()) for step in steps] for base in bases]
    identities=identity_map(*browsers)
    manifest=json.loads((output/'manifest.json').read_text())
    view_map={'/'+page['target']:'/'+page['source'] for page in manifest['converted']}
    for label,mapping in zip(['jsp','facelets'],identities):
        mapping.update(view_map);mapping[instances[label]['url']]='<runtime-origin>'
    results=[]
    for index,step in enumerate(steps):
        snapshots=[pilot.compare.normalize(rows[index],mapping) for rows,mapping in zip(browsers,identities)]
        for snapshot in snapshots:
            for form in snapshot['forms']:
                form['text']=re.sub(r'\s+', ' ', form['text']).strip()
        observations_both=[observations(base,step) for base in bases]
        raw=[[row.pop('rawTrace') for row in observations] for observations in observations_both]
        traces=[pilot.compare.normalize(rows,mapping) for rows,mapping in zip(observations_both,identities)]
        errors=[pilot.compare.normalize(json.loads((base/'browser'/(step+'-errors.json')).read_text()),mapping) for base,mapping in zip(bases,identities)]
        control_paths={}
        for form_index,form in enumerate(snapshots[0]['forms']):
            for control_index,control in enumerate(form['controls']):
                if control['id'] in ['editform:createdDate','questionpool:selectedTags']:
                    control_paths.setdefault(control['id'],[]).append('$.forms['+str(form_index)+'].controls['+str(control_index)+'].value')
        browser_differences,fixture_differences=separate_fixture_differences(pilot.compare.differences(*snapshots),bases,control_paths,step in ['samigotest-cancreatenewcourse','samigotest-deletedquizishiddenfromstudentsinlessons'])
        result={'step':step,'browserDifferences':browser_differences,'fixtureDifferences':fixture_differences,
            'requestDifferences':pilot.compare.differences(*traces),
            'pageErrorDifferences':pilot.compare.differences(*errors),'rawTraces':raw}
        result['passed']=not any(result[key] for key in ['browserDifferences','requestDifferences','pageErrorDifferences'])
        results.append(result)
    type_steps=sorted({path.stem for base in bases for path in (base/'browser-types').glob('*.json')})
    for step in type_steps:
        snapshots=[json.loads((base/'browser-types'/(step+'.json')).read_text()) for base in bases]
        mappings=[{**pilot.compare.fixture_map(snapshot,label,base),**view_map} for snapshot,label,base in zip(snapshots,['jsp','facelets'],bases)]
        normalized=[pilot.compare.normalize(snapshot,mapping) for snapshot,mapping in zip(snapshots,mappings)]
        for snapshot in normalized:snapshot['text']=re.sub(r'\s+',' ',snapshot['text']).strip()
        traces=[pilot.compare.choose_trace(base,step,snapshot['formId']) for base,snapshot in zip(bases,snapshots)]
        forms=[next(node for node in pilot.compare.walk(trace[-1]['tree']) if node['id']==snapshot['formId']) for (_,trace),snapshot in zip(traces,snapshots)]
        lifecycle=[[{name:frame[name] for name in ['phase','postback','validationFailed','viewId','messages']} for frame in trace] for _,trace in traces]
        result={'step':step,'browserDifferences':pilot.compare.differences(*normalized),
            'requestDifferences':pilot.compare.differences(*[pilot.compare.normalize({'form':pilot.compare.contract(form),'phases':phases},mapping) for form,phases,mapping in zip(forms,lifecycle,mappings)]),
            'pageErrorDifferences':[],'rawTraces':[str(path) for path,_ in traces]}
        result['passed']=not any(result[key] for key in ['browserDifferences','requestDifferences','pageErrorDifferences']);results.append(result)
    compilation=json.loads((bases[1]/'captures/facelet-compilation.json').read_text())
    visited=set()
    for file in (bases[1]/'captures').rglob('*.json'):
        if file.name=='facelet-compilation.json':continue
        for frame in json.loads(file.read_text()):visited.add(frame['viewId'])
    statuses=[]
    for label in ['jsp','facelets']:
        tests={}
        for file in sorted((output/(label+'-test-results')).glob('TEST-*.xml')):
            for test in ET.parse(file).getroot().iter('testcase'):
                tests[test.get('classname')+'.'+test.get('name')]=next((status for status in ['failure','error','skipped'] if test.find(status) is not None),'passed')
        statuses.append(tests)
    status_differences=pilot.compare.differences(*statuses)
    report={'passed':all(result['passed'] for result in results) and not status_differences and all(row['status']=='compiled' for row in compilation),'compilation':compilation,'visitedViews':sorted(visited),'testStatuses':dict(zip(['jsp','facelets'],statuses)),'testStatusDifferences':status_differences,'unresolvedViews':manifest['failures'],
        'normalizations':{'identities':identities,'treeOnly':['Transient literal text renderers and Facelets UIInstructions','Lazy listener wrapper classes; delegate types retained'],
            'browserOnly':['Opaque ViewState and CSRF fields','Whitespace runs in form innerText; control values remain exact'],
            'fixturePolicy':'Separately record generated dates only within each runtime window and using its centrally resolved converter timezone; retain formats. Selected tag identity is fixture-specific. Preserve raw differences for final snapshots of unchanged external tools.',
            'scope':'Final browser forms plus completing-request lifecycle and all final JSF form trees, in request order; exact page-error comparison.'},'tests':results}
    (output/'report.json').write_text(json.dumps(report,indent=2))
    for result in results:print(result['step'],'PASS' if result['passed'] else 'DIFF',*[len(result[key]) for key in ['browserDifferences','requestDifferences','pageErrorDifferences']])
    return report


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('output',type=Path);args=parser.parse_args()
    raise SystemExit(0 if compare(args.output)['passed'] else 1)
