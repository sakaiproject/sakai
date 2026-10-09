"""Compare observed contracts, retaining every raw capture and listing explicit normalizations."""
import argparse
import copy
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET
from convert import walk, meaningful, literal_markup

STEPS = ['create-opened', 'empty-title', 'assessment-created', 'first-question-saved', 'second-question-saved', 'second-question-opened', 'second-question-edited', 'assessment-reopened']


def differences(left, right, path='$'):
    if type(left) is not type(right): return [{'path':path, 'jsp':left, 'facelets':right}]
    if isinstance(left, dict):
        result = []
        for key in sorted(left.keys() | right.keys()):
            if key not in left or key not in right: result.append({'path':path + '.' + key, 'jsp':left.get(key), 'facelets':right.get(key)})
            else: result.extend(differences(left[key], right[key], path + '.' + key))
        return result
    if isinstance(left, list):
        if len(left) != len(right): return [{'path':path, 'jspLength':len(left), 'faceletsLength':len(right), 'jsp':left, 'facelets':right}]
        return [d for index, pair in enumerate(zip(left, right)) for d in differences(*pair, path + f'[{index}]')]
    return [] if left == right else [{'path':path, 'jsp':left, 'facelets':right}]


def fixture_map(browser, label, base=None):
    path = browser['path']
    match = re.search(r'/site/([^/]+)/tool/([^/]+)', path)
    if not match: raise ValueError('Expected fixture site/tool in ' + label + ' path')
    mapping = {match[1]:'<fixture-site>', match[2]:'<fixture-tool>'}
    if base is not None:
        connector = next(c for c in ET.parse(base / 'conf/server.xml').getroot().iter('Connector') if c.get('protocol', '').startswith('HTTP'))
        mapping['http://127.0.0.1:' + connector.get('port')] = '<runtime-origin>'
    return mapping


def normalize(value, identities):
    if isinstance(value, dict): return {key:normalize(item, identities) for key, item in value.items()}
    if isinstance(value, list): return [normalize(item, identities) for item in value]
    if isinstance(value, str):
        for source, target in identities.items(): value = value.replace(source, target)
        # Only the explicitly migrated view has a permitted suffix change.
        value = value.replace('/jsf/author/createAssessment_title.xhtml', '/jsf/author/createAssessment_title.jsp')
        return value
    return value


def contract(node):
    data = {key:value for key, value in node.items() if key not in ['literalMarkup', 'children', 'facets', 'actionListenerImplementations']}
    if literal_markup(node): data['class'] = 'jakarta.faces.component.UIOutput'
    data['facets'] = {name:contract(facet) for name, facet in node.get('facets', {}).items() if meaningful(facet)}
    data['children'] = [contract(child) for child in node.get('children', []) if meaningful(child)]
    return data


def choose_trace(base, step, form_id):
    files = sorted((base / 'captures' / step).glob('*.json'), key=lambda p:p.stat().st_mtime, reverse=True)
    for path in files:
        trace = json.loads(path.read_text())
        if trace and any(node['id'] == form_id for node in walk(trace[-1].get('tree'))): return path, trace
    raise ValueError('No completed trace for ' + step + ' and ' + form_id)


def compare(jsp, facelets, output):
    results = []
    normalizations = {'jsp':{}, 'facelets':{}, 'permittedViewChange':'/jsf/author/createAssessment_title.jsp -> .xhtml',
                      'treeOnly':['Unconditional transient literal output; conditional expressions and IDs retained; literal renderer implementation classes canonicalized', 'Facelets UIInstructions', 'Lazy listener implementation classes; serialized delegate types remain exact'],
                      'browserOnly':['jakarta.faces.ViewState', 'sakai_csrf_token', 'Whitespace runs in form innerText; control values remain exact'],
                      'scope':'Final rendered form contract and lifecycle flags/messages/view transitions for its completing request; raw traces and HTML retained.'}
    for step in STEPS:
        browsers = [json.loads((base / 'browser' / (step + '.json')).read_text()) for base in [jsp,facelets]]
        identities = [fixture_map(browser, label, base) for browser, label, base in zip(browsers, ['jsp','facelets'], [jsp, facelets])]
        for label, mapping in zip(['jsp', 'facelets'], identities): normalizations[label].update(mapping)
        dom = [normalize(browser, mapping) for browser,mapping in zip(browsers,identities)]
        for browser in dom:
            browser['text'] = re.sub(r'\s+', ' ', browser['text']).strip()
        dom_diff = differences(*dom)
        traces = [choose_trace(base, step, browser['formId']) for base,browser in zip([jsp,facelets],browsers)]
        lifecycle = [[{key:frame[key] for key in ['phase','postback','validationFailed','viewId','messages']} for frame in trace] for _,trace in traces]
        phase_diff = differences(*[normalize(data, mapping) for data,mapping in zip(lifecycle,identities)])
        forms = [next(node for node in walk(trace[-1]['tree']) if node['id'] == browser['formId']) for (_,trace),browser in zip(traces,browsers)]
        tree_diff = differences(*[normalize(contract(form), mapping) for form,mapping in zip(forms,identities)])
        result = {'step':step, 'passed':not (dom_diff or phase_diff or tree_diff), 'browserDifferences':dom_diff, 'lifecycleDifferences':phase_diff, 'treeDifferences':tree_diff, 'rawTraces':[str(path) for path,_ in traces]}
        results.append(result)
    report = {'passed':all(result['passed'] for result in results), 'normalizations':normalizations, 'checkpoints':results}
    output.mkdir(parents=True,exist_ok=True)
    (output / 'report.json').write_text(json.dumps(report,indent=2))
    return report


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('jsp',type=Path); parser.add_argument('facelets',type=Path); parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    report=compare(args.jsp,args.facelets,args.output)
    for checkpoint in report['checkpoints']:
        print(checkpoint['step'], 'PASS' if checkpoint['passed'] else 'DIFF', len(checkpoint['browserDifferences']),len(checkpoint['treeDifferences']),len(checkpoint['lifecycleDifferences']))
    raise SystemExit(0 if report['passed'] else 1)
