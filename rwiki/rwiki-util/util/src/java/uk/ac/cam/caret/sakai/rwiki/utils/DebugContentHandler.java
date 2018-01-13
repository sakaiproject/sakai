/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package uk.ac.cam.caret.sakai.rwiki.utils;

import java.text.MessageFormat;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Proxyis a content handler providing a debug output to stdout, not for
 * production use
 * 
 * @author ieb
 */
@Slf4j
public class DebugContentHandler implements ContentHandler
{

	private ContentHandler ch = null;

	public DebugContentHandler(ContentHandler ch)
	{
		this.ch = ch;
	}

	public void setDocumentLocator(Locator arg0)
	{
		ch.setDocumentLocator(arg0);
	}

	public void startDocument() throws SAXException
	{
		ch.startDocument();
	}

	public void endDocument() throws SAXException
	{
		ch.endDocument();
	}

	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException
	{
		ch.startPrefixMapping(arg0, arg1);
	}

	public void endPrefixMapping(String arg0) throws SAXException
	{
		ch.endPrefixMapping(arg0);
	}

	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException
	{
		log.info(MessageFormat.format("<{0} {1} {2} >", new Object[] {
				arg0, arg1, arg2 }));
		ch.startElement(arg0, arg1, arg2, arg3);
	}

	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException
	{
		log.info(MessageFormat.format("</{0} {1} {2} >",
				new Object[] { arg0, arg1, arg2 }));
		ch.endElement(arg0, arg1, arg2);
	}

	public void characters(char[] arg0, int arg1, int arg2) throws SAXException
	{
		log.info(String.valueOf(arg0, arg1, arg2));
		ch.characters(arg0, arg1, arg2);
	}

	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException
	{
		log.info(String.valueOf(arg0, arg1, arg2));
		ch.ignorableWhitespace(arg0, arg1, arg2);
	}

	public void processingInstruction(String arg0, String arg1)
			throws SAXException
	{
		log.info(MessageFormat.format("<? {0} {1} ?>", new Object[] {
				arg0, arg1 }));
		ch.processingInstruction(arg0, arg1);
	}

	public void skippedEntity(String arg0) throws SAXException
	{
		ch.skippedEntity(arg0);
	}

}
