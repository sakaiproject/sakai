/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
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

import java.text.MessageFormat;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
/**
 * Proxyis a content handler providing a debug output to stdout, 
 * not for production use
 * @author ieb
 *
 */
public class DebugContentHandler implements ContentHandler {

	private ContentHandler ch = null;
	public DebugContentHandler(ContentHandler ch ) {
		this.ch = ch;
	}
	public void setDocumentLocator(Locator arg0) {
		ch.setDocumentLocator(arg0);
	}

	public void startDocument() throws SAXException {
		ch.startDocument();
	}

	public void endDocument() throws SAXException {
		ch.endDocument();
	}

	public void startPrefixMapping(String arg0, String arg1) throws SAXException {
		ch.startPrefixMapping(arg0,arg1);
	}

	public void endPrefixMapping(String arg0) throws SAXException {
		ch.endPrefixMapping(arg0);
	}

	public void startElement(String arg0, String arg1, String arg2, Attributes arg3) throws SAXException {
		System.out.println(MessageFormat.format("<{0} {1} {2} >", new Object[] { arg0, arg1, arg2 }));
		ch.startElement(arg0,arg1,arg2,arg3);
	}

	public void endElement(String arg0, String arg1, String arg2) throws SAXException {
		System.out.println(MessageFormat.format("</{0} {1} {2} >", new Object[] { arg0, arg1, arg2 }));
		ch.endElement(arg0,arg1,arg2);
	}

	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		System.out.print(String.valueOf(arg0,arg1,arg2));
		ch.characters(arg0,arg1,arg2);
	}

	public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
		System.out.print(String.valueOf(arg0,arg1,arg2));
		ch.ignorableWhitespace(arg0,arg1,arg2);
	}

	public void processingInstruction(String arg0, String arg1) throws SAXException {
		System.out.println(MessageFormat.format("<? {0} {1} ?>", new Object[] {arg0,arg1}));
		ch.processingInstruction(arg0,arg1);
	}

	public void skippedEntity(String arg0) throws SAXException {
		ch.skippedEntity(arg0);
	}

}
