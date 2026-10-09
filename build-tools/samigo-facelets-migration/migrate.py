#!/usr/bin/env python3
"""Reproducible Samigo conversion, with Jasper-derived component ID provenance."""
import argparse
from collections import Counter
import html
import hashlib
from html.entities import name2codepoint
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET

REPO = Path(__file__).resolve().parents[2]
WEB = REPO / 'samigo/samigo-app/src/webapp'
NS = {'ui':'http://java.sun.com/jsf/facelets','h':'http://java.sun.com/jsf/html','f':'http://java.sun.com/jsf/core',
      't':'http://myfaces.apache.org/tomahawk','samigo':'http://www.sakaiproject.org/samigo','sakai':'http://sakaiproject.org/jsf2/sakai',
      'corejsf':'http://java.sun.com/upload','p':'http://java.sun.com/jsf/passthrough'}
SERVER_TAG = re.compile(r"""<(?P<close>/)?(?P<name>(?:h|f|t|samigo|sakai|corejsf|ui):[\w.-]+)\b(?P<attrs>(?:[^'"<>]|'(?:\\.|[^'\\])*'|"(?:\\.|[^"\\])*")*?)(?P<end>/?>)""", re.S)
COMPONENT_CORE = {'view','subview','verbatim','selectItem','selectItems','param'}
VOID = {'area','base','br','col','embed','hr','img','input','link','meta','param','source','track','wbr'}
TAG = re.compile(r'<(?P<close>/)?(?P<name>[A-Za-z][\w:.-]*)\b(?P<attrs>(?:[^\'"<>]|\'[^\']*\'|"[^"]*")*?)(?P<end>/?>)',re.S)
ATTR = re.compile(r"""([\w:.-]+)\s*=\s*('(?:\\.|[^'\\])*'|"(?:\\.|[^"\\])*")""", re.S)


def expand(path, sources, active=()):
    path=path.resolve()
    if path in active: raise ValueError('Recursive static include: '+str(path))
    sources.add(str(path.relative_to(WEB)))
    with path.open(newline="") as source:text=source.read()
    text=re.sub(r'<%--.*?--%>','',text,flags=re.S)
    return re.sub(r'<%@\s*include\s+file\s*=\s*([\'"])(.*?)\1\s*%>',lambda m:expand(WEB/m[2].lstrip('/') if m[2].startswith('/') else path.parent/m[2],sources,(*active,path)),text)


def jasper_ids(java):
    if not java.exists(): return []
    matches=re.findall(r'(_jspx_th_\w+)\.setJspId\("(jsp_\d+_(\d+))"\)',java.read_text())
    rows=[]
    for variable,jsp_id,order in matches:
        decoded=variable.removeprefix('_jspx_th_').replace('_005f','_')
        prefix,local=decoded.split('_',1); local=local.rsplit('_',1)[0]
        rows.append({'tag':prefix+':'+local,'id':'j_id_'+jsp_id,'order':int(order)})
    return sorted(rows,key=lambda row:row['order'])


def java_path(jasper, rel):
    path=Path(rel)
    encoded=re.sub(r'[^a-zA-Z0-9]',lambda m:'_' + format(ord(m[0]),'04x'),path.stem)
    roots=jasper if isinstance(jasper,tuple) else (jasper,)
    candidates=[root/'org/apache/jsp'/path.parent/(encoded+'_jsp.java') for root in roots]
    return next((candidate for candidate in candidates if candidate.exists()),candidates[0])


def protect_el(text):
    values=[];output=[];index=0
    while index<len(text):
        if text[index:index+2] in ['#{','${']:
            start=index; index+=2; depth=1;quote=None
            while index<len(text) and depth:
                char=text[index]
                if quote:
                    if char=='\\':index+=2;continue
                    if char==quote:quote=None
                elif char in ['\'', '"']:quote=char
                elif char=='{':depth+=1
                elif char=='}':depth-=1
                index+=1
            if depth:raise ValueError('Unclosed EL expression')
            token='__SAMIGO_EL_'+str(len(values))+'__';values.append(text[start:index]);output.append(token)
        else:output.append(text[index]);index+=1
    return ''.join(output),values


def xml_escape(value):
    # JSP attributes retain entity text in their Java values. Double-encode
    # those ampersands so XML parsing does not change the component contract.
    return html.escape(value,quote=True).replace('\t','&#9;').replace('\n','&#10;').replace('\r','&#13;')


def xml_literal(value):
    # Keep the JSP writer's whitespace while producing source without trailing
    # blanks or indentation tabs. XML character references decode identically.
    escaped=html.escape(value,quote=False).replace('\t','&#9;')
    return re.sub(r' +(?=\r?$)',lambda match:'&#32;'*len(match[0]),escaped,flags=re.M)


def convert_page(path, jasper, modular_source=None):
    rel=path.relative_to(WEB);sources=set()
    if modular_source is None:text=expand(path,sources)
    else:text=modular_source["text"];sources.update(modular_source["sources"])
    license_comments=[comment for comment in re.findall(r'<%--(.*?)--%>',path.read_text(),re.S) if 'Copyright' in comment and 'License' in comment]
    namespaces=dict(NS)
    for uri,prefix in re.findall(r'<%@\s*taglib\s+uri="([^"]+)"\s+prefix="([^"]+)"\s*%>',text):namespaces[prefix]=uri
    text=re.sub(r'<%@.*?%>','',text,flags=re.S)
    if rel.name in ['xmlDisplay.jsp','xmlPoolDisplay.jsp']:
        text=text.replace('<f:view>', '<f:view contentType="text/xml" encoding="UTF-8">')
    # Java comments in scriptlet delimiters carry no behavior.
    text=re.sub(r'<%\s*/\*.*?\*/\s*%>','',text,flags=re.S)
    prepared,expressions=protect_el(text)
    metadata=jasper_ids(java_path(jasper,rel));cursor=iter(metadata);components=[]
    def pin(match):
        name=match['name']
        if match['close'] or ':' not in name:return match[0]
        if modular_source is not None and name=='ui:include':
            token=next(attribute[2][1:-1] for attribute in ATTR.finditer(match['attrs']) if attribute[1]=='migration-token')
            included=modular_source['includes'][token]
            offset=len(components)
            for child in included['components']:
                row=next(cursor,None) if metadata else None
                if metadata and (row is None or row['tag']!=child['tag'] or row['order']!=len(components)):
                    raise ValueError('Included Jasper/source tag mismatch: '+str(child)+' vs '+str(row))
                components.append({'tag':child['tag'],'id':child.get('explicit') or (row['id'] if row else 'source-position-'+str(len(components))),
                    'provenance':'explicit' if child.get('explicit') else 'jasper' if row else 'source-position'})
            return '<ui:include src="'+included['target']+'"><ui:param name="_samigoLegacyPrefix" value="#{_samigoLegacyPrefix}"/>' \
                +'<ui:param name="_samigoLegacyOffset" value="#{_samigoLegacyOffset + '+str(offset)+'}"/></ui:include>'
        prefix,local=name.split(':',1)
        if prefix not in ['h','t','samigo','sakai','corejsf'] and not(prefix=='f' and local in COMPONENT_CORE):return match[0]
        attrs=match['attrs'];explicit=next((attribute for attribute in ATTR.finditer(attrs) if attribute[1]=='id'),None)
        row=next(cursor,None) if metadata else None
        if metadata and (row is None or row['tag']!=name):raise ValueError('Jasper/source tag mismatch: '+name+' vs '+str(row))
        if row:identifier=row['id']
        else:identifier='samigo-'+re.sub('[^a-zA-Z0-9_-]','-',str(rel.with_suffix('')))+'-'+str(len(components))
        if modular_source is not None and row and row['order']!=len(components):raise ValueError('Noncontiguous Jasper component ordinals')
        components.append({'tag':name,'id':explicit[2][1:-1] if explicit else identifier,'provenance':'explicit' if explicit else 'jasper' if row else 'source-position'})
        if explicit or name=='f:view':return match[0]
        assigned='#{_samigoLegacyPrefix}_#{_samigoLegacyOffset + '+str(len(components)-1)+'}' if modular_source is not None else identifier
        return '<'+name+attrs+' id="'+assigned+'"'+match['end']
    prepared=SERVER_TAG.sub(pin,prepared)
    if next(cursor,None):raise ValueError('Unused Jasper component IDs')
    for index,expression in enumerate(expressions):prepared=prepared.replace('__SAMIGO_EL_'+str(index)+'__',expression)
    text=prepared
    init=[]
    def scriptlet(match):
        code=match[1]
        if 'String thisId = request.getParameter("panel")' in code or 'String thisId= request.getParameter("panel")' in code:return ''
        if 'AuthorizationBean authorization =' in code and 'getAdminPrivilege' in code:init.append('index');return ''
        if 'SelectActionListener listener = new SelectActionListener()' in code and 'SamigoJsfPermissions' not in code:init.append('review');return ''
        if 'SamigoJsfPermissions.get(request' in code:init.append('role');return ''
        if 'lookupParam("publishedAssessmentId")' in code:init.append('edit-link');return ''
        if 'response.setHeader' in code:
            init.append('export-pool' if 'exportPool.xml' in code else 'export-assessment');return ''
        if 'exception.printStackTrace' in code:return '<h:outputText value="#{samigoViewSupport.errorStack}"/>'
        raise ValueError('Unconverted scriptlet: '+code.strip()[:150])
    text=re.sub(r'<%(?![=@])(.*?)%>',scriptlet,text,flags=re.S)
    def expression(match):
        code=match[1].strip()
        request=re.fullmatch(r'request.getAttribute\("([^"]+)"\)',code)
        if request:
            name=request[1]
            return "#{requestScope['"+name+"'] == null ? 'null' : requestScope['"+name+"']}"
        if code=='thisId':return '#{samigoViewSupport.panelId}'
        if code=='org.sakaiproject.util.Web.escapeJavascript(thisId)':return '#{samigoViewSupport.panelIdEscaped}'
        if code in ['selectId','selectIdFixed']:
            form='modifyPartForm' if rel.name=='editPart.jsp' else 'itemForm'
            return form+':assignToPool'+('Fixed' if code=='selectIdFixed' else '')
        if code=='session.getAttribute("selectedItems")':return '#{sessionScope.selectedItems}'
        if code=='exception.getMessage()':return '#{samigoViewSupport.errorMessage}'
        emi=re.fullmatch(r'"([^"]+)".equals\(request.getParameter\("item"\)\)\s*\?\s*"lightgrey"\s*:\s*"white"',code)
        if emi:return "#{param.item eq '"+emi[1]+"' ? 'lightgrey' : 'white'}"
        raise ValueError('Unconverted JSP expression: '+code)
    text=re.sub(r'<%=(.*?)%>',expression,text,flags=re.S)
    if '<%' in text:raise ValueError('Unconverted JSP syntax')
    text=re.sub(r'<logic:iterate\b[^>]*>', '<ui:repeat var="qpool" value="#{sessionScope.selectedItems}">',text)
    text=text.replace('</logic:iterate>','</ui:repeat>').replace('<bean:write name="qpool" property="itemText" />','<h:outputText value="#{qpool.itemText}"/>')
    def equivalent_tag(match):
        name=match['name'];attrs=match['attrs']
        if name=='f:convertDateTime':name='samigo:convertDateTime'
        if name=='f:selectItem':
            attrs=ATTR.sub(lambda attribute:'itemEscaped='+attribute[2] if attribute[1]=='escape' else attribute[0],attrs)
        return '<'+('/' if match['close'] else '')+name+attrs+match['end']
    text=SERVER_TAG.sub(equivalent_tag,text)
    local_values=[]
    def local_value(match):
        name=match['name']
        if match['close'] or not name.startswith(('h:','t:')):return match[0]
        value=next((attribute for attribute in ATTR.finditer(match['attrs']) if attribute[1]=='value'),None)
        if value is None or '#{' in value[2] or '${' in value[2]:return match[0]
        # JSP's UIComponentELTag stores a literal value locally. Facelets'
        # ValueHolderRule instead creates a literal ValueExpression, which is
        # not writable during UPDATE_MODEL_VALUES. Use the standard attribute
        # handler to retain the JSP's local-value semantics.
        attrs=match['attrs'][:value.start()]+match['attrs'][value.end():]
        local_values.append({'tag':name,'value':value[2][1:-1]})
        tag='<'+name+attrs+'>'
        tag+='<f:attribute name="value" value='+value[2]+'/>'
        return tag+('</'+name+'>' if match['end']=='/>' else '')
    text=SERVER_TAG.sub(local_value,text)
    # JSP treats ordinary HTML as writer output, including incomplete tags and
    # JSF tags embedded in HTML attributes. Encode those bytes as XML text, so VerbatimHandler receives the original HTML.
    # Verbatim components are transient, as are JSP's literal output components.
    protected,els=protect_el(text)
    chunks=[];offset=0;literal_chunks=0
    def literal(value):
        nonlocal literal_chunks
        if not value:return ''
        literal_chunks+=1
        # Evaluate each expression independently: JSP can mix immediate ${}
        # and deferred #{} output, while a single EL value cannot mix both.
        result=[]
        for part in re.split(r'(__SAMIGO_EL_\d+__)',value):
            if not part:continue
            if re.fullmatch(r'__SAMIGO_EL_\d+__',part):
                result.append('<samigo:literal value="'+part+'"/>')
            else:result.append('<f:verbatim>'+xml_literal(part)+'</f:verbatim>')
        return ''.join(result)
    matches=list(SERVER_TAG.finditer(protected));index=0
    while index<len(matches):
        match=matches[index]
        if match.start()<offset:index+=1;continue
        chunks.append(literal(protected[offset:match.start()]))
        if match['name']=='f:verbatim' and not match['close'] and match['end']!='/>':
            closing=next((other for other in matches[index+1:] if other['name']=='f:verbatim' and other['close']),None)
            if closing is None:raise ValueError('Unclosed f:verbatim')
            body=protected[match.end():closing.start()]
            if '__SAMIGO_EL_' in body:
                converted=literal(body)
                original_id=re.search(r'\bid="([^"]+)"',match['attrs'])
                if original_id:converted=converted.replace('/>', ' id="'+original_id[1]+'"/>',1)
                chunks.append(converted)
            else:chunks.append(match[0]+xml_literal(body)+closing[0])
            offset=closing.end();index+=1;continue
        attrs=ATTR.sub(lambda m:m[1]+'="'+xml_escape(re.sub(r'''\\([\\'"])''', lambda escaped:escaped[1], m[2][1:-1]))+'"',match['attrs'])
        chunks.append('<'+('/' if match['close'] else '')+match['name']+attrs+match['end'])
        offset=match.end();index+=1
    chunks.append(literal(protected[offset:]));text=''.join(chunks)
    # JSP assigns the facet's actual component directly. Generated writer
    # whitespace must not make Facelets wrap it in an extra UIPanel.
    text=re.sub(r'(<f:facet\b[^>]*>)(.*?)(</f:facet>)',
        lambda match:match[1]+re.sub(r'<f:verbatim>(?:\s|&#32;|&#9;)*</f:verbatim>','',match[2])+match[3],text,flags=re.S)
    # Restore each EL expression in its actual XML context. All expressions require XML escaping of quotes and comparisons.
    text=re.sub(r'__SAMIGO_EL_(\d+)__',lambda match:xml_escape(els[int(match[1])]),text)
    initialization='' # Render-phase preparation is performed by SamigoViewPhaseListener.
    declarations=' '.join('xmlns:'+prefix+'="'+uri+'"' for prefix,uri in namespaces.items() if prefix not in ['logic','bean'])
    text='<ui:composition xmlns="http://www.w3.org/1999/xhtml" '+declarations+'>\n'+initialization+'\n'+text+'\n</ui:composition>\n'
    if license_comments:
        license_text='\n'.join(line.rstrip() for line in license_comments[0].replace('--','- -').splitlines())
        text='<!--'+license_text+'-->\n'+text
    # XML already normalizes raw CR/CRLF. Attribute values have explicit
    # character references above, so their meaningful line endings stay exact.
    text=text.replace('\r\n','\n').replace('\r','\n')
    # Remaining tabs/blanks are XML tag formatting; writer payloads and
    # attribute whitespace are already represented by character references.
    text='\n'.join(line.rstrip(' \t') for line in text.replace('\t','    ').split('\n')).rstrip('\n')+'\n'
    ET.fromstring(text)
    java=java_path(jasper,rel)
    provenance={'java':str(java.resolve()),'sha256':hashlib.sha256(java.read_bytes()).hexdigest()} if java.exists() else None
    return text,{'jasper':provenance,'source':str(rel),'target':str(rel.with_suffix('.xhtml')),'includedSources':sorted(sources),'components':components,'initializers':list(dict.fromkeys(init)),'literalChunks':literal_chunks,'localLiteralValues':local_values}


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--jasper',type=Path,required=True);parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args();args.output.mkdir(parents=True,exist_ok=True)
    pages=[];failures=[]
    for path in sorted([*WEB.rglob('*.jsp'),*WEB.rglob('*.jspf')]):
        try:
            text,metadata=convert_page(path,args.jasper)
            target=args.output/metadata['target'];target.parent.mkdir(parents=True,exist_ok=True);target.write_text(text);pages.append(metadata)
        except (ValueError,ET.ParseError,OSError) as exception:failures.append({'source':str(path.relative_to(WEB)),'error':str(exception)})
    manifest={'converted':pages,'failures':failures,'removedViews':[{'source':'jsf/author/previewAssessment.jsp','reason':'Unused legacy preview outcome; malformed EL. Current preview uses the shared delivery view.'},{'source':'jsf/delivery/reviewAssessment.jsp','reason':'Unused legacy review outcome; nonexistent itemreview includes. Current review uses the shared delivery view.'}]}
    (args.output/'manifest.json').write_text(json.dumps(manifest,indent=2))
    print('Converted',len(pages),'unresolved',len(failures))
    for failure in failures:print(failure['source'],failure['error'])
    return 1 if failures else 0


if __name__=='__main__':raise SystemExit(main())
