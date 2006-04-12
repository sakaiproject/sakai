/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.utils;

import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author ieb
 * 
 */
public class Digester implements ContentHandler {

	private Stack stack = new Stack();

	private StringBuffer buffer = new StringBuffer();

	private HashMap tags = new HashMap();

	public static final String[] tagList = { "p", "\n", "div", "\n", "a", "\n",
			"span", " ", "td", "\n", "th", "\n", "li", "\n","content",""

	};

	public Digester() {
		for (int i = 0; i < tagList.length; i += 2) {
			tags.put(tagList[i], tagList[i + 1]);
		}
	}
	
	public String toString() {
		return buffer.toString();
	}

	public void setDocumentLocator(Locator arg0) {
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
	}

	public void endPrefixMapping(String arg0) throws SAXException {
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		stack.push(tags.get(localName.toLowerCase()));
	}

	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		String marker = (String) stack.pop();
		if (marker != null) {
			buffer.append(marker);
		}
	}

	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		if (stack.peek() != null) {
			String s = new String(arg0, arg1, arg2);
			buffer.append(s.trim()).append(" ");
		}

	}

	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {

	}

	public void processingInstruction(String arg0, String arg1)
			throws SAXException {

	}

	public void skippedEntity(String arg0) throws SAXException {

	}
}
