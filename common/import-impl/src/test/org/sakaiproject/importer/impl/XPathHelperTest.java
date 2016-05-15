package org.sakaiproject.importer.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathHelperTest {

	private Document document;
	
	@Before
	public void setUp() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			String testDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<foo>\r\n\t<bar baz=\"abc\" bang=\"first\">\r\n\t\t<a>0</a>\r\n\t\t<b>1</b>\r\n\t\t<c>2</c>\r\n\t</bar>\r\n\t<bar baz=\"def\" bang=\"second\">\r\n\t\t<a>0</a>\r\n\t\t<b>1</b>\r\n\t\t<c>2</c>\r\n\t</bar>\r\n</foo>";
			this.document = builder.parse(new InputSource(new StringReader(testDoc)));
		} catch (ParserConfigurationException e) {
			// won't happen
		} catch (SAXException e) {
			// won't happen
		} catch (IOException e) {
			// won't happen
		}
	}
	
	@Test
	public void testSelectNodes() {
		List nodes = XPathHelper.selectNodes("//bar", document);
		assertEquals(2, nodes.size());
	}
	
	@Test
	public void testSelectNode() {
		Node node = XPathHelper.selectNode("//bar[@baz='abc']", document);
		assertEquals("first", node.getAttributes().getNamedItem("bang").getNodeValue());
	}
	
	@Test
	public void testGetNodeValue() {
		String val = XPathHelper.getNodeValue("//bar[@baz='abc']/a", document);
		assertEquals("0", val);
	}

}
