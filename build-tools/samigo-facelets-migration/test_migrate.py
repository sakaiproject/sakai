"""Checks for migration traps found in the real Samigo templates."""
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET
import migrate


class MigrationTest(unittest.TestCase):
    def convert(self, source, tags, name='example.jsp'):
        with tempfile.TemporaryDirectory() as directory:
            root=Path(directory).resolve();web=root/'web';web.mkdir();page=web/name;page.write_text(source)
            jasper=root/'jasper';java=migrate.java_path(jasper,Path(name));java.parent.mkdir(parents=True)
            java.write_text('\n'.join('_jspx_th_'+tag.replace(':','_005f')+'_005f'+str(index)+'.setJspId("jsp_123_'+str(index)+'");' for index,tag in enumerate(tags)))
            with patch.object(migrate,'WEB',web):return migrate.convert_page(page,jasper)

    def test_preserves_component_inside_html_attribute(self):
        text,metadata=self.convert('<f:view><script src="/bundle.js<h:outputText value="#{scores.CDNQuery}"/>"></script></f:view>', ['f:view','h:outputText'])
        root=ET.fromstring(text);nodes=list(root.iter())
        output=next(node for node in nodes if node.tag.endswith('}outputText'))
        self.assertEqual(output.attrib, {'value':'#{scores.CDNQuery}','id':'j_id_jsp_123_1'})
        literals=[node.text for node in nodes if node.tag.endswith('}verbatim')]
        self.assertIn('<script src="/bundle.js',literals)
        self.assertIn('"></script>',literals)

    def test_decodes_jsp_quote_escapes_without_changing_value(self):
        text,_=self.convert(r'''<f:view><h:outputText value="\"#{settings.title}\""/></f:view>''',['f:view','h:outputText'])
        output=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}outputText'))
        self.assertEqual(output.get('value'),'"#{settings.title}"')

    def test_encodes_literal_markup_without_leaking_cdata(self):
        html='<script>if (a < b && c) { alert("done"); }</script>&nbsp;'
        text,_=self.convert('<f:view>'+html+'</f:view>',['f:view'])
        literal=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}verbatim'))
        self.assertEqual(literal.text,html)
        self.assertNotIn('<![CDATA[',text)

    def test_preserves_existing_verbatim_entities_as_writer_output(self):
        text,_=self.convert('<f:view><f:verbatim><b>&amp;</b></f:verbatim></f:view>',['f:view','f:verbatim'])
        literal=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}verbatim'))
        self.assertEqual(literal.text,'<b>&amp;</b>')

    def test_preserves_literal_whitespace_with_clean_xml_source(self):
        original='line \n  \tcode  '
        text,_=self.convert('<f:view>'+original+'</f:view>',['f:view'])
        literal=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}verbatim'))
        self.assertEqual(literal.text,original)
        self.assertFalse(any(line.endswith((' ','\t')) for line in text.splitlines()))

    def test_retains_local_value_for_literal_input(self):
        text,metadata=self.convert('<f:view><h:inputHidden id="part" value=""/></f:view>',['f:view','h:inputHidden'])
        node=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}inputHidden'))
        self.assertNotIn('value',node.attrib)
        attribute=next(child for child in node if child.tag.endswith('}attribute'))
        self.assertEqual(attribute.attrib,{'name':'value','value':''})
        self.assertEqual(metadata['localLiteralValues'],[{'tag':'h:inputHidden','value':''}])

    def test_retains_unescaped_el_in_literal_writer_output(self):
        text,_=self.convert('<f:view><head><%= request.getAttribute("html.head") %></head></f:view>',['f:view'])
        literal=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}literal'))
        self.assertEqual(literal.get('value'),"#{requestScope['html.head'] == null ? 'null' : requestScope['html.head']}")

    def test_keeps_immediate_and_deferred_writer_expressions_separate(self):
        text,_=self.convert('<f:view><script>var x="${bean.x}"; var y="#{bean.y}";</script></f:view>',['f:view'])
        values=[node.get('value') for node in ET.fromstring(text).iter() if node.tag.endswith('}literal')]
        self.assertEqual(values,['${bean.x}','#{bean.y}'])

    def test_assigns_original_verbatim_id_once_when_splitting_expressions(self):
        text,_=self.convert('<f:view><f:verbatim>${bean.x} #{bean.y}</f:verbatim></f:view>',['f:view','f:verbatim'])
        nodes=[node for node in ET.fromstring(text).iter() if node.get('id')]
        self.assertEqual([node.get('id') for node in nodes],['j_id_jsp_123_1'])

    def test_keeps_facet_child_without_synthetic_whitespace_components(self):
        text,_=self.convert('<f:view><h:column><f:facet name="header">\n  <h:outputText value="Header"/>\n</f:facet></h:column></f:view>',['f:view','h:column','h:outputText'])
        facet=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}facet'))
        self.assertEqual(len(facet),1)
        self.assertTrue(facet[0].tag.endswith('}outputText'))

    def test_distinguishes_html_id_in_value_from_component_id(self):
        text,metadata=self.convert("""<f:view><h:outputText value="&lt;input id='hidden_#{row.number}' /&gt;" escape="false"/></f:view>""",['f:view','h:outputText'])
        output=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}outputText'))
        self.assertEqual(output.get('id'),'j_id_jsp_123_1')
        self.assertEqual(metadata['components'][1]['provenance'],'jasper')

    def test_preserves_entity_text_in_jsp_attribute_values(self):
        text,_=self.convert('<f:view><h:outputText value="#{message} &#160; &amp;"/></f:view>',['f:view','h:outputText'])
        output=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}outputText'))
        self.assertEqual(output.get('value'),'#{message} &#160; &amp;')

    def test_translates_date_tag_to_jsp_compatible_converter_handler(self):
        text,_=self.convert('<f:view><h:outputText value="#{bean.date}"><f:convertDateTime dateStyle="medium" timeStyle="short"/></h:outputText></f:view>',['f:view','h:outputText'])
        converter=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}convertDateTime'))
        self.assertEqual(converter.tag,'{http://www.sakaiproject.org/samigo}convertDateTime')
        self.assertEqual(converter.attrib,{'dateStyle':'medium','timeStyle':'short'})

    def test_translates_select_item_escape_to_facelets_property(self):
        text,_=self.convert('<f:view><f:selectItem itemValue="1" itemLabel="Select" escape="false"/></f:view>',['f:view','f:selectItem'])
        item=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}selectItem'))
        self.assertNotIn('escape',item.attrib)
        self.assertEqual(item.get('itemEscaped'),'false')

    def test_keeps_export_form_as_html(self):
        text,_=self.convert('<f:view><h:form id="exportAssessmentForm"/></f:view>',['f:view','h:form'],'exportAssessment.jsp')
        view=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}view'))
        self.assertNotIn('contentType',view.attrib)

    def test_sets_xml_content_type_only_for_download_view(self):
        text,_=self.convert('<f:view><h:outputText value="#{xml.xml}" escape="false"/></f:view>',['f:view','h:outputText'],'xmlDisplay.jsp')
        view=next(node for node in ET.fromstring(text).iter() if node.tag.endswith('}view'))
        self.assertEqual(view.attrib,{'contentType':'text/xml','encoding':'UTF-8'})

    def test_rejects_jasper_component_mismatch(self):
        with self.assertRaisesRegex(ValueError,'Jasper/source tag mismatch'):
            self.convert('<f:view><h:commandButton value="Save"/></f:view>',['f:view','h:inputText'])

    def test_rejects_unconverted_java(self):
        with self.assertRaisesRegex(ValueError,'Unconverted scriptlet'):
            self.convert('<f:view><% unknown(); %></f:view>',['f:view'])

    def test_rejects_malformed_el(self):
        with self.assertRaisesRegex(ValueError,'Unclosed EL'):
            self.convert('<f:view><h:panelGroup rendered="#{broken"/></f:view>',['f:view','h:panelGroup'])


if __name__=='__main__':unittest.main()
