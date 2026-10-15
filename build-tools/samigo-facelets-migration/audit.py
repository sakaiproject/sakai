#!/usr/bin/env python3
"""Static declaration audit; does not apply Facelets or prove runtime equivalence."""
import argparse,json,re
from pathlib import Path
import xml.etree.ElementTree as ET
import migrate,modular
parser=argparse.ArgumentParser(description="Check source tag order, include offsets, IDs and navigation without starting a server.")
parser.add_argument('--source-web',type=Path,required=True)
parser.add_argument('--web-root',type=Path,default=migrate.WEB)
parser.add_argument('--output',type=Path,required=True)
parser.add_argument('--baseline',default='provided-source')
args=parser.parse_args()
source=args.source_web.resolve();web=args.web_root.resolve()
migrate.WEB=source
namespaces={uri:prefix for prefix,uri in migrate.NS.items()}
ui='{'+migrate.NS['ui']+'}'
def offset_value(value,offset):
 if value=='#{0}':return 0
 m=re.fullmatch(r'#\{_samigoLegacyOffset \+ (\d+)\}',value)
 if m:return offset+int(m[1])
 raise ValueError('Unexpected include offset: '+value)
def actual(path,prefix='',offset=0,active=()):
 if path in active:raise ValueError('Include cycle: '+str(path))
 root=ET.parse(path).getroot();result=[]
 def visit(node):
  if node.tag==ui+'include':
   params={p.get('name'):p.get('value') for p in node if p.tag==ui+'param'}
   selected=params['_samigoLegacyPrefix'];selected=prefix if selected=='#{_samigoLegacyPrefix}' else selected
   start=offset_value(params['_samigoLegacyOffset'],offset)
   child=web/node.get('src').lstrip('/');assert child.is_relative_to(web),child
   result.extend(actual(child,selected,start,(*active,path)));return
  uri,local=node.tag[1:].split('}',1);prefix_name=namespaces.get(uri)
  name=str(prefix_name)+':'+local
  component=prefix_name in ['h','t','samigo','sakai','corejsf'] or (prefix_name=='f' and local in migrate.COMPONENT_CORE)
  if name in ['samigo:convertDateTime','samigo:literal']:component=name=='samigo:literal' and node.get('id') is not None;name='f:verbatim'
  if name=='f:verbatim' and node.get('id') is None:component=False
  if component:
   identifier=node.get('id')
   m=re.fullmatch(r'#\{_samigoLegacyPrefix\}_#\{_samigoLegacyOffset \+ (\d+)\}',identifier or '')
   if m:identifier=prefix+'_'+str(offset+int(m[1]))
   result.append((name,identifier))
  for child in node:visit(child)
 visit(root);return result
rows=[]
for path in sorted([*source.rglob('*.jsp'),*source.rglob('*.jspf')]):
 rel=path.relative_to(source)
 if str(rel) in modular.REMOVED:continue
 native=web/rel.with_suffix('.xhtml')
 wrapper=ET.parse(native).getroot();prefix=next(p.get('value') for p in wrapper.iter(ui+'param') if p.get('name')=='_samigoLegacyPrefix')
 definitions=modular.component_definitions(migrate.expand(path,set()))
 expected=[(d['tag'],d['explicit'] or (None if d['tag']=='f:view' else prefix+'_'+str(index))) for index,d in enumerate(definitions)]
 observed=actual(native)
 if expected!=observed:
  for index,(left,right) in enumerate(zip(expected,observed)):
   if left!=right:raise ValueError(str(rel)+' component '+str(index)+': '+str(left)+' != '+str(right))
  raise ValueError(str(rel)+' component counts '+str(len(expected))+' != '+str(len(observed)))
 rows.append({'view':str(rel),'components':len(expected)})
for path in web.rglob('*.xhtml'):ET.parse(path)
assert not list(web.rglob('*.jsp')) and not list(web.rglob('*.jspf'))
assert not (web/'jsf/evaluation/downloadFileSubmissions.xhtml').exists()
faces=ET.parse(web/'WEB-INF/faces-config.xml').getroot()
for node in faces.iter():
 if node.tag.endswith('}to-view-id'):
  view=node.text.strip()
  if view.startswith('/jsf/'):assert (web/view.lstrip('/')).is_file(),view
result={'baseline':args.baseline,'views':len(rows),'templates':len(list(web.rglob('*.xhtml'))),'componentsChecked':sum(r['components'] for r in rows),'passed':True,'details':rows}
args.output.parent.mkdir(parents=True,exist_ok=True)
args.output.write_text(json.dumps(result,indent=2))
print({key:value for key,value in result.items() if key!='details'})
