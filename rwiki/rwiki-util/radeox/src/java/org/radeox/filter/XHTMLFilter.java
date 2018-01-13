/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */

package org.radeox.filter;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;
import org.apache.xml.serializer.ToXMLStream;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.filter.context.FilterContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;



/*
 * The paragraph filter finds any text between two empty lines and inserts a
 * <p/> @author stephan @team sonicteam
 * 
 * @version $Id: ParagraphFilter.java 4158 2005-11-25 23:25:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public class XHTMLFilter implements Filter, CacheFilter
{

	private static SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
	private static final Map blockElements = new HashMap();
	static
	{
		saxParserFactory.setNamespaceAware(true);
		List l = new ArrayList();
		l.add("p");
		blockElements.put("hr", l); // hr cant be nested inside p
		blockElements.put("h1", l);
		blockElements.put("h2", l);
		blockElements.put("h3", l);
		blockElements.put("h4", l);
		blockElements.put("h5", l);
		blockElements.put("h6", l);
		blockElements.put("h7", l);
		blockElements.put("ul", l);
		blockElements.put("ol", l);
		blockElements.put("div", l);
		blockElements.put("blockquote", l);
	}

	private static HashMap emptyTag = new HashMap();

	static
	{
		// inclusion els
		emptyTag.put("img", "img");
		emptyTag.put("area", "area");
		emptyTag.put("frame", "frame");
		// non-standard inclusion els
		emptyTag.put("layer", "layer");
		emptyTag.put("embed", "embed");
		// form el
		emptyTag.put("input", "input");
		// default els
		emptyTag.put("base", "base");
		// styling els
		emptyTag.put("col", "col");
		emptyTag.put("basefont", "basefont");
		// hidden els
		emptyTag.put("link", "link");
		emptyTag.put("meta", "meta");
		// separator els
		emptyTag.put("br", "br");
		emptyTag.put("hr", "hr");
		// here because our current p implementation is broken
		// emptyTag.put("p", "p");
	}

	private static HashMap ignoreEmpty = new HashMap();

	static
	{
		ignoreEmpty.put("p", "p");
	}

	private InitialRenderContext initialContext;

	public String filter(String input, FilterContext context)
	{
		String finalOutput = input;
		try
		{
			DeblockFilter dbf = new DeblockFilter();
			EmptyFilter epf = new EmptyFilter();
			
			dbf.setBlockElements(blockElements);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			SpecialXHTMLSerializer xser = new SpecialXHTMLSerializer();
			xser.setOutputStream(baos);
			xser.setIndent(false);
			xser.setEncoding("UTF-8");
			xser.setIndentAmount(4);
			dbf.setContentHandler(epf);
			epf.setContentHander(xser.asContentHandler());
			
			SAXParser parser = saxParserFactory.newSAXParser();
		
			XMLReader xmlr = parser.getXMLReader();
			
			xmlr.setContentHandler(dbf);
			// log.warn("Input is "+input);
			xmlr.parse(new InputSource(new StringReader("<sr>" + input
					+ "</sr>")));

			String output = new String(baos.toByteArray(), "UTF-8");
			int startBlock = output.indexOf("<sr>");
			int endBlock = output.indexOf("</sr>");
			if(startBlock >= 0 && endBlock >= 0)
			{
				finalOutput = output.substring(startBlock + 4, endBlock);
			}
			log.debug("Output is "+finalOutput);
		}
		catch (Throwable t)
		{
			log.error("Failed to XHTML check " + t.getMessage()
					+ "\n Input======\n" + input + "\n=======");
			return input;
		}

		return finalOutput;
	}

	public String[] replaces()
	{
		return FilterPipe.NO_REPLACES;
	}

	public String[] before()
	{
		return FilterPipe.EMPTY_BEFORE;
	}

	public void setInitialContext(InitialRenderContext context)
	{
		initialContext = context;

	}

	public String getDescription()
	{
		return "Hand Coded XHTML filter";
	}

	public class DeblockFilter implements ContentHandler
	{

		private Stack s = new Stack();

		private ContentHandler ch;

		private Map blockElements = new HashMap();

		public void setContentHandler(ContentHandler ch)
		{
			this.ch = ch;
		}

		public void setBlockElements(Map blockElements)
		{
			this.blockElements = blockElements;

		}

		public void addElement(String blockElement, String unnested)
		{
			List l = (List) blockElements.get(blockElement);
			if (l == null)
			{
				l = new ArrayList();
				blockElements.put(blockElement, l);
			}
			l.add(unnested);
		}

		/**
		 * Unwind the xpath stack back to the first instance of the requested
		 * emement
		 * 
		 * @param deblockElement
		 */
		private Stack closeTo(List deblockElements) throws SAXException
		{
			int firstIndex = s.size();
			for (int i = 0; i < s.size(); i++)
			{
				EStack es = (EStack) s.get(i);
				if (deblockElements.contains(es.lname))
				{
					firstIndex = i;
				}
			}
			EStack es = null;
			Stack sb = new Stack();
			while (s.size() > firstIndex)
			{
				es = (EStack) s.pop();
				// log.warn("Closing "+es.qname);
				ch.endElement(es.ns, es.qname, es.lname);
				sb.push(es);
			}
			// log.warn("End Close");
			return sb;
		}

		/**
		 * Check each element to see if its in a list of elements which is
		 * should not be inside If it is one of these elements, get a list of
		 * elements, and unwind to that it is not inside the stack
		 * 
		 * @{inheritDoc}
		 */
		public void startElement(String ns, String qname, String lname,
				Attributes atts) throws SAXException
		{
			if (blockElements.get(lname) != null)
			{
				s.push(new EStack(ns, qname, lname, atts,
						closeTo((List) blockElements.get(lname))));
			}
			else
			{
				s.push(new EStack(ns, qname, lname, atts, null));
			}
			ch.startElement(ns, qname, lname, atts);
		}

		/**
		 * When we get to the end element, pop the Stack element off the stack.
		 * If there is arestore path, restore the path back in place by emitting
		 * start elements
		 * 
		 * @{inheritDoc}
		 */
		public void endElement(String arg0, String arg1, String arg2)
				throws SAXException
		{
			ch.endElement(arg0, arg1, arg2);
			EStack es = (EStack) s.pop();
			if (es.restore != null)
			{
				while (es.restore.size() > 0)
				{
					EStack esr = (EStack) es.restore.pop();
					// log.warn("Restore "+esr.lname);
					ch.startElement(esr.ns, esr.qname, esr.lname, esr.atts);
					s.push(esr);
				}
			}
		}

		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException
		{
			ch.characters(arg0, arg1, arg2);
		}

		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException
		{
			ch.ignorableWhitespace(arg0, arg1, arg2);
		}

		public void processingInstruction(String arg0, String arg1)
				throws SAXException
		{
			ch.processingInstruction(arg0, arg1);
		}

		public void skippedEntity(String arg0) throws SAXException
		{
			ch.skippedEntity(arg0);
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

	}

	public class EmptyFilter implements ContentHandler
	{

		
		private ContentHandler next = null;

		private EStack lastElement = null;

		public EmptyFilter()
		{
		}

		public void setContentHander(ContentHandler handler)
		{
			next = handler;
		}

		public void setDocumentLocator(Locator arg0)
		{
			next.setDocumentLocator(arg0);
		}

		public void startDocument() throws SAXException
		{
			emitLast();
			next.startDocument();
		}

		public void endDocument() throws SAXException
		{
			emitLast();
			next.endDocument();
		}

		public void startPrefixMapping(String arg0, String arg1)
				throws SAXException
		{
			emitLast();
			next.startPrefixMapping(arg0, arg1);
		}

		public void endPrefixMapping(String arg0) throws SAXException
		{
			emitLast();
			next.endPrefixMapping(arg0);
		}

		public void emitLast() throws SAXException
		{
			if (lastElement != null)
			{
				// this means that there was a startElement, startElement,
				// so the lastElement MUST be emited
				next.startElement(lastElement.ns, lastElement.qname,
						lastElement.lname, lastElement.atts);
				lastElement = null;
			}
		}

		public void startElement(String ns, String qname, String lname,
				Attributes atts) throws SAXException
		{
			emitLast();
			if (ignoreEmpty.get(lname.toLowerCase()) != null)
			{
				lastElement = new EStack(ns, qname, lname, atts, null);
			}
			else
			{
				next.startElement(ns, qname, lname, atts);
			}
		}

		public void endElement(String arg0, String arg1, String arg2)
				throws SAXException
		{
			if (lastElement != null)
			{
				// there was a start, then an end with nothing in between
				// so ignore alltogether
				lastElement = null;
			}
			else
			{
				next.endElement(arg0, arg1, arg2);
			}
		}

		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException
		{
			emitLast();
			next.characters(arg0, arg1, arg2);
		}

		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException
		{
			emitLast();
			next.ignorableWhitespace(arg0, arg1, arg2);
		}

		public void processingInstruction(String arg0, String arg1)
				throws SAXException
		{
			emitLast();
			next.processingInstruction(arg0, arg1);
		}

		public void skippedEntity(String arg0) throws SAXException
		{
			emitLast();
			next.skippedEntity(arg0);
		}


	}

	public class EStack
	{
		public EStack(String ns, String qname, String lname, Attributes atts,
				Stack restore)
		{
			this.ns = ns;
			this.qname = qname;
			this.lname = lname;
			this.atts = new AttributesImpl(atts);
			this.restore = restore;
		}

		public EStack(EStack es)
		{
			this.ns = es.ns;
			this.qname = es.qname;
			this.lname = es.lname;
			this.atts = new AttributesImpl(es.atts);
			this.restore = es.restore;
		}

		Stack restore = null;

		String ns;

		String qname;

		String lname;

		Attributes atts;
	}

	/**
	 * @author andrew
	 */
	public class SpecialXHTMLSerializer extends ToXMLStream
	{

		private static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

		public void endElement(String namespaceURI, String localName,
				String name) throws SAXException
		{
			if ((namespaceURI != null && !"".equals(namespaceURI) && !namespaceURI
					.equals(XHTML_NAMESPACE))
					|| emptyTag.containsKey(localName.toLowerCase()))
			{
				super.endElement(namespaceURI, localName, name);
				return;
			}

			this.characters("");

			super.endElement(namespaceURI, localName, name);

		}

	}

}
