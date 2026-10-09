#!/usr/bin/env python3
"""Preserve reusable static fragments with scoped Facelets include parameters."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET
import migrate

REMOVED={'jsf/author/previewAssessment.jsp','jsf/delivery/reviewAssessment.jsp'}
INCLUDE=re.compile(r'<%@\s*include\s+file\s*=\s*([\'"])(.*?)\1\s*%>')


def component_definitions(text):
    definitions=[]
    for match in migrate.SERVER_TAG.finditer(text):
        if match['close']:continue
        prefix,local=match['name'].split(':',1)
        if prefix not in ['h','t','samigo','sakai','corejsf'] and not(prefix=='f' and local in migrate.COMPONENT_CORE):continue
        explicit=next((attr[2][1:-1] for attr in migrate.ATTR.finditer(match['attrs']) if attr[1]=='id'),None)
        definitions.append({'tag':match['name'],'explicit':explicit})
    return definitions


def local_source(path):
    sources={str(path.relative_to(migrate.WEB))};includes={}
    with path.open(newline='') as source:text=source.read()
    text=re.sub(r'<%--.*?--%>','',text,flags=re.S)
    def include(match):
        child=(migrate.WEB/match[2].lstrip('/') if match[2].startswith('/') else path.parent/match[2]).resolve()
        body=migrate.expand(child,sources)
        token=str(len(includes));rel=child.relative_to(migrate.WEB).with_suffix('.xhtml')
        includes[token]={'target':'/WEB-INF/samigo-views/'+str(rel),'components':component_definitions(body)}
        return '<ui:include migration-token="'+token+'"/>'
    text=INCLUDE.sub(include,text)
    return {'text':text,'sources':sources,'includes':includes}


def convert(path,jasper):
    text,metadata=migrate.convert_page(path,jasper,local_source(path))
    rows=migrate.jasper_ids(migrate.java_path(jasper,path.relative_to(migrate.WEB)))
    prefix=rows[0]['id'].rsplit('_',1)[0] if rows else 'samigo-'+re.sub('[^a-zA-Z0-9_-]','-',str(path.relative_to(migrate.WEB).with_suffix('')))
    template='WEB-INF/samigo-views/'+metadata['target']
    wrapper='''<ui:composition xmlns:ui="http://java.sun.com/jsf/facelets">
  <ui:include src="/'''+template+'''">
    <ui:param name="_samigoLegacyPrefix" value="'''+prefix+'''"/>
    <ui:param name="_samigoLegacyOffset" value="#{0}"/>
  </ui:include>
</ui:composition>
'''
    # The public view and its reusable template both retain the source license.
    license_comment=re.match(r'<!--.*?-->\s*',text,re.S)
    if license_comment:wrapper=license_comment[0]+wrapper
    ET.fromstring(wrapper)
    metadata['template']=template
    metadata['includes']=[{'target':include['target'],'components':len(include['components'])} for include in local_source(path)['includes'].values()]
    return wrapper,text,metadata


def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--jasper',type=Path,required=True)
    parser.add_argument('--jasper-fallback',type=Path)
    parser.add_argument('--output',type=Path,required=True);parser.add_argument('--source-web',type=Path)
    args=parser.parse_args()
    if args.source_web:migrate.WEB=args.source_web.resolve()
    jasper=(args.jasper,args.jasper_fallback) if args.jasper_fallback else args.jasper
    args.output.mkdir(parents=True,exist_ok=True);pages=[];failures=[]
    for path in sorted([*migrate.WEB.rglob('*.jsp'),*migrate.WEB.rglob('*.jspf')]):
        if str(path.relative_to(migrate.WEB)) in REMOVED:continue
        try:
            wrapper,template,metadata=convert(path,jasper)
            for rel,text in [(metadata['target'],wrapper),(metadata['template'],template)]:
                target=args.output/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(text)
            pages.append(metadata)
        except (ValueError,ET.ParseError,OSError) as exception:failures.append({'source':str(path.relative_to(migrate.WEB)),'error':str(exception)})
    fragments=[]
    additional=sorted({source for page in pages for source in page['includedSources'] if Path(source).suffix not in ['.jsp','.jspf']})
    for source in additional:
        try:
            _,template,metadata=convert(migrate.WEB/source,jasper)
            target=args.output/metadata['template'];target.parent.mkdir(parents=True,exist_ok=True);target.write_text(template)
            fragments.append(metadata)
        except (ValueError,ET.ParseError,OSError) as exception:failures.append({'source':source,'error':str(exception)})
    manifest={'converted':pages,'failures':failures,'removedViews':[{'source':source,'reason':'Unused broken legacy view; current preview/review uses the shared delivery view.'} for source in sorted(REMOVED)],
        'fragments':fragments,
        'strategy':'Reusable Facelets includes; legacy component IDs use explicit parameters scoped to each include.'}
    (args.output/'manifest.json').write_text(json.dumps(manifest,indent=2))
    print('Converted',len(pages),'unresolved',len(failures))
    for failure in failures:print(failure['source'],failure['error'])
    return 1 if failures else 0


if __name__=='__main__':raise SystemExit(main())
