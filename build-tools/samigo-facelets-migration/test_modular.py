"""Reusable includes must preserve the containing JSP's component ordinals."""
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET
import migrate
import modular


class ModularMigrationTest(unittest.TestCase):
    def test_nested_include_offsets_follow_jasper_order(self):
        with tempfile.TemporaryDirectory() as directory:
            root=Path(directory).resolve();web=root/'web';web.mkdir()
            (web/'outer.jsp').write_text('<f:view><h:form id="form"><h:outputText value="Before"/><%@ include file="fragment.jsp" %><h:commandButton value="After"/></h:form></f:view>')
            (web/'fragment.jsp').write_text('<h:panelGroup><%@ include file="inner.jsp" %></h:panelGroup>')
            (web/'inner.jsp').write_text('<h:inputText value="#{bean.value}"/>')
            jasper=root/'jasper';java=jasper/'org/apache/jsp/outer_jsp.java';java.parent.mkdir(parents=True)
            tags=['f:view','h:form','h:outputText','h:panelGroup','h:inputText','h:commandButton']
            java.write_text('\n'.join('_jspx_th_'+tag.replace(':','_005f')+'_005f'+str(index)+'.setJspId("jsp_123_'+str(index)+'");' for index,tag in enumerate(tags)))
            with patch.object(migrate,'WEB',web):
                wrapper,template,metadata=modular.convert(web/'outer.jsp',jasper)
                _,fragment,_=modular.convert(web/'fragment.jsp',jasper)
            outer=ET.fromstring(template)
            include=next(node for node in outer.iter() if node.tag.endswith('}include'))
            self.assertEqual(include.get('src'),'/WEB-INF/samigo-views/fragment.xhtml')
            self.assertEqual([node.get('value') for node in include],['#{_samigoLegacyPrefix}','#{_samigoLegacyOffset + 3}'])
            button=next(node for node in outer.iter() if node.tag.endswith('}commandButton'))
            self.assertEqual(button.get('id'),'#{_samigoLegacyPrefix}_#{_samigoLegacyOffset + 5}')
            nested=next(node for node in ET.fromstring(fragment).iter() if node.tag.endswith('}include'))
            self.assertEqual(list(nested)[1].get('value'),'#{_samigoLegacyOffset + 1}')
            self.assertIn('value="j_id_jsp_123"',wrapper)
            self.assertEqual([component['id'] for component in metadata['components']],['j_id_jsp_123_0','form','j_id_jsp_123_2','j_id_jsp_123_3','j_id_jsp_123_4','j_id_jsp_123_5'])


if __name__=='__main__':unittest.main()
