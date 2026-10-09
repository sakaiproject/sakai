"""Narrow conversion for the static Samigo creation form, with captured ID provenance."""
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET
from runtime import REPO

TAG = re.compile(r'<(?P<tag>[ht]:[A-Za-z]+|f:selectItem|f:selectItems|f:param)\b(?P<attrs>(?:[^\'"<>]|\'[^\']*\'|"[^"]*")*?)(?P<end>/?>)')


def walk(node):
    if node:
        yield node
        for facet in node.get('facets', {}).values(): yield from walk(facet)
        for child in node.get('children', []): yield from walk(child)


def literal_markup(node):
    return bool(node.get('literalMarkup') or (
        node['class'] == 'jakarta.faces.component.UIOutput'
        and node.get('transient') and node.get('renderer') == 'jakarta.faces.Text'
        and 'value' not in node.get('expressions', {})))


def meaningful(node):
    # Conditional verbatim output still has a contract: retain its rendering
    # expression and ID. Unconditional writer text is checked in the browser.
    conditional = any(name in node.get('expressions', {}) for name in ['rendered', 'binding'])
    return (not literal_markup(node) or conditional) and not node['class'].endswith('UIInstructions')


def convert(baseline, candidate, output):
    web = REPO / 'samigo/samigo-app/src/webapp'
    sources = []
    def expand(path):
        sources.append(str(path.relative_to(web)))
        text = path.read_text()
        return re.sub(r'<%@\s*include\s+file="([^"]+)"\s*%>', lambda m: expand(web / m[1].lstrip('/')) if m[1].startswith('/') else expand(path.parent / m[1]), text)
    text = expand(web / 'jsf/author/createAssessment_title.jsp')
    text = re.sub(r'<%--.*?--%>', '', text, flags=re.S)
    text = re.sub(r'<%@.*?%>', '', text, flags=re.S)
    text = text.replace('<%= request.getAttribute("html.head") %>', '<h:outputText value="#{requestScope[\'html.head\']}" escape="false"/>')
    text = text.replace('<%= request.getAttribute("html.body.onload") %>', "#{requestScope['html.body.onload']}")
    text = text.replace('<f:view>', '<f:view xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:t="http://myfaces.apache.org/tomahawk">')
    if '<%' in text: raise ValueError('Unconverted JSP construct')
    ET.fromstring(text)
    captures = list((baseline / 'captures/create-opened').glob('*.json'))
    trace = json.loads(max(captures, key=lambda p: p.stat().st_mtime).read_text())
    form = next(n for n in walk(trace[-1]['tree']) if n['id'] == 'authorIndexForm')
    nodes = [n for n in walk(form) if meaningful(n)]
    start = text.index('<h:form')
    end = text.index('</h:form>', start) + len('</h:form>')
    matches = list(TAG.finditer(text[start:end]))
    if len(matches) != len(nodes): raise ValueError(f'Template/capture count mismatch: {len(matches)} vs {len(nodes)}')
    mapping = []
    cursor = iter(nodes)
    def assign(match):
        node = next(cursor)
        tag = match['tag']
        expected = {'f:selectItem':'UISelectItem', 'f:selectItems':'UISelectItems', 'f:param':'UIParameter', 't:radio':'HtmlRadio', 't:selectOneRadio':'HtmlSelectOneRadio'}.get(tag, 'Html' + tag.split(':')[1][0].upper() + tag.split(':')[1][1:])
        if not node['class'].endswith('.' + expected): raise ValueError(f'Unexpected class for {tag}: {node["class"]}')
        explicit = re.search(r'\bid\s*=\s*([\'"])(.*?)\1', match['attrs'])
        if explicit and explicit[2] != node['id']: raise ValueError('Explicit ID mismatch')
        mapping.append({'tag':tag, 'id':node['id'], 'clientId':node['clientId'], 'source':'explicit' if explicit else 'captured-jsp-id'})
        return match[0] if explicit else '<' + tag + match['attrs'] + ' id="' + node['id'] + '"' + match['end']
    text = text[:start] + TAG.sub(assign, text[start:end]) + text[end:]
    ET.fromstring(text)
    app = candidate / 'webapps/samigo-app'
    target = app / 'jsf/author/createAssessment_title.xhtml'
    target.write_text(text)
    faces = app / 'WEB-INF/faces-config.xml'
    faces.write_text(faces.read_text().replace('/jsf/author/createAssessment_title.jsp', '/jsf/author/createAssessment_title.xhtml'))
    descriptor = app / 'WEB-INF/web.xml'
    descriptor.write_text(descriptor.read_text().replace('<web-resource-name>JSF templates</web-resource-name>', '<web-resource-name>JSF templates</web-resource-name><url-pattern>*.xhtml</url-pattern>'))
    manifest = {'sourceFiles':sources, 'candidate':str(target), 'componentIds':mapping}
    (output / 'conversion.json').write_text(json.dumps(manifest, indent=2))
    return manifest
