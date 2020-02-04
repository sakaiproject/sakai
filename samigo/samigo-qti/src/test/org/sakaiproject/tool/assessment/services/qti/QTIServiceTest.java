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
import static org.mockito.Mockito.*;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.qti.asi.Item;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.ExtractionHelper;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.w3c.dom.Document;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ComponentManager.class})
public class QTIServiceTest {

	@Before
	public void setUp() {
        PowerMockito.mockStatic(ComponentManager.class);
        // A mock component manager.
        when(ComponentManager.get(any(Class.class))).then(new Answer<Object>() {
            private Map<Class, Object> mocks = new HashMap<>();
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Class classToMock = (Class) invocation.getArguments()[0];
                return mocks.computeIfAbsent(classToMock, k -> mock(classToMock));
            }
        });
	}

	@Test
	public void testImportExport() throws Exception {
		Document document = getDocument("exportAssessment.xml");
		ItemFacade item = extractItem(document);
//		String output = PrintUtil.printItem(item);
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
