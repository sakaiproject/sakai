/**
 * Copyright (c) 2005-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.services.qti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.qti.asi.Item;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.ExtractionHelper;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.sakaiproject.util.api.FormattedText;
import org.w3c.dom.Document;

@RunWith(MockitoJUnitRunner.class)
public class QTIServiceTest {

    private MockedStatic<ComponentManager> componentManagerMock;

    @Mock private FormattedText formattedText;

    @Before
    public void setUp() throws Exception {
        componentManagerMock = Mockito.mockStatic(ComponentManager.class);
        componentManagerMock.when(() -> ComponentManager.get(FormattedText.class)).thenReturn(formattedText);
        Mockito.when(formattedText.processFormattedText(ArgumentMatchers.anyString(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @After
    public void tearDown() {
        if (componentManagerMock != null) {
            componentManagerMock.close();
        }
    }

	@Test
	public void testImportExport() throws Exception {
		Document document = getDocument("exportAssessment.xml");
		ItemFacade item = extractItem(document);
		assertEquals("Extended Matching Items", item.getDescription());
		assertEquals("Grading A", item.getThemeText());
		assertEquals("The Leadin Text.", item.getLeadInText());
		assertEquals(Double.valueOf(48.0), item.getScore());
		assertEquals(Double.valueOf(0.0), item.getDiscount());
		assertEquals("1:AB 2:AB 3:AB 4:AB 5:AB 6:AB 7:AB 8:AB 9:AB 10:AB 11:AB 12:AB ", item.getAnswerKey());
		assertEquals("ABCDEF", item.getEmiAnswerOptionLabels());
	}

	@Test
	public void testCogneroXmlFormat() throws Exception {
		Document document = getDocument("SAK-34033.xml");
        ItemFacade item = extractItem(document);
		assertTrue(item.getDescription().contains("database systems"));
		assertEquals(Double.valueOf(5.0), item.getScore());
		assertEquals(Double.valueOf(0.0), item.getDiscount());
	}

	@Test
	public void testOnlyOneCorrectScoreIsFound() throws Exception {
		Document document = getDocument("SAK-42728.xml");
		ItemFacade item = extractItem(document);
		assertEquals("Multiple Choice", item.getDescription());
		assertEquals(Double.valueOf(1.0), item.getScore());
		assertEquals(Double.valueOf(0.0), item.getDiscount());
	}

	public static void printDocument(Document doc, OutputStream out) throws Exception {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}
	
	private Document getDocument(String fileName) throws Exception {
		URL url = QTIServiceTest.class.getClassLoader().getResource(fileName);
		if(url == null){
			throw new IllegalArgumentException("Could not find the test file, " + fileName + "! Stopping test.");
		}
		String file = url.getPath();
		ComponentManager.testingMode = true;
		return XmlUtil.readDocument(file, true);
	}

	private ItemFacade extractItem(Document document) {
		ExtractionHelper exHelper = new ExtractionHelper(QTIVersion.VERSION_1_2);
		ItemFacade item = new ItemFacade();
		Item itemXml = new Item(document, QTIVersion.VERSION_1_2);
		exHelper.updateItem(item, itemXml);
		return item;
	}

}
