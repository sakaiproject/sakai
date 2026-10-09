#!/usr/bin/env python3
"""Apply a fully converted manifest to the Samigo source tree."""
import argparse
import json
from pathlib import Path
import re
import shutil
import migrate


def apply(converted):
    manifest=json.loads((converted/'manifest.json').read_text())
    if manifest['failures']:raise ValueError('Resolve every conversion failure before applying')
    sources={str(path.relative_to(migrate.WEB)) for pattern in ['*.jsp','*.jspf'] for path in migrate.WEB.rglob(pattern)}
    expected={page['source'] for page in manifest['converted']}
    if sources!=expected:raise ValueError('Manifest/source mismatch: '+str(sorted(sources^expected)))
    faces=migrate.WEB/'WEB-INF/faces-config.xml';text=faces.read_text()
    for page in manifest['converted']:
        text=text.replace('/'+page['source'],'/'+page['target'])
    text=text.replace('</lifecycle>', '  <phase-listener>org.sakaiproject.tool.assessment.ui.facelets.SamigoViewPhaseListener</phase-listener>\n  </lifecycle>')
    text=text.replace('</faces-config>', '''  <managed-bean>
    <managed-bean-name>samigoViewSupport</managed-bean-name>
    <managed-bean-class>org.sakaiproject.tool.assessment.ui.facelets.SamigoViewSupport</managed-bean-class>
    <managed-bean-scope>request</managed-bean-scope>
  </managed-bean>
</faces-config>''')
    descriptor=migrate.WEB/'WEB-INF/web.xml';web=descriptor.read_text()
    web=re.sub(r'\s*<!-- JSF TAGLIBS -->\s*<jsp-config>.*?</jsp-config>','',web,flags=re.S)
    web=web.replace('<!-- JSPs are JSF view templates. External requests must enter through a JSF mapping. -->',
        '<!-- View templates must be reached through a JSF mapping. -->')
    web=web.replace('<web-resource-name>JSF templates</web-resource-name>', '<web-resource-name>JSF templates</web-resource-name>\n      <url-pattern>*.xhtml</url-pattern>')
    web=web.replace('</web-app>', '''  <!-- JSP did not add an implicit BeanValidator to every input. -->
  <context-param>
    <param-name>jakarta.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR</param-name>
    <param-value>true</param-value>
  </context-param>
  <context-param>
    <param-name>jakarta.faces.FACELETS_LIBRARIES</param-name>
    <param-value>/WEB-INF/samigo-facelets.taglib.xml;/WEB-INF/upload-facelets.taglib.xml</param-value>
  </context-param>
  <filter>
    <filter-name>samigo-header</filter-name>
    <filter-class>org.sakaiproject.tool.assessment.ui.facelets.SamigoHeaderFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>samigo-header</filter-name>
    <servlet-name>Faces Servlet</servlet-name>
    <dispatcher>REQUEST</dispatcher>
    <dispatcher>FORWARD</dispatcher>
  </filter-mapping>
</web-app>''')
    # All checks above are read-only. Copy before removing the old templates.
    for page in manifest['converted']+manifest.get('fragments',[]):
        for key in ['target','template'] if page in manifest['converted'] else ['template']:
            destination=migrate.WEB/page[key];destination.parent.mkdir(parents=True,exist_ok=True)
            shutil.copy2(converted/page[key],destination)
    faces.write_text(text);descriptor.write_text(web)
    for page in manifest['converted']:(migrate.WEB/page['source']).unlink()
    (migrate.WEB/'include/header.inc').unlink()
    print('Applied',len(manifest['converted']),'views/fragments; removed every JSP/JSPF and the JSP prelude.')


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--converted',type=Path,required=True)
    apply(parser.parse_args().converted)
