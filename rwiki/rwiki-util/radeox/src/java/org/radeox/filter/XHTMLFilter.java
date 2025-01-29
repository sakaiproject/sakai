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

import lombok.Setter;
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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/*
 * The paragraph filter finds any text between two empty lines and inserts a
 * <p/> @author stephan @team sonicteam
 *
 * @version $Id: ParagraphFilter.java 4158 2005-11-25 23:25:19Z
 *          ian@caret.cam.ac.uk $
 */
@Slf4j
public class XHTMLFilter implements Filter, CacheFilter {

    @Setter private InitialRenderContext initialContext;

    private final Map<String, List<String>> blockElements = new HashMap<>();
    private final Map<String, String> emptyTag = new HashMap<>();
    private final Map<String, String> ignoreEmpty = new HashMap<>();
    private final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

    public XHTMLFilter() {
        saxParserFactory.setNamespaceAware(true);
        List<String> p = List.of("p");
        blockElements.put("hr", p); // hr cant be nested inside p
        blockElements.put("h1", p);
        blockElements.put("h2", p);
        blockElements.put("h3", p);
        blockElements.put("h4", p);
        blockElements.put("h5", p);
        blockElements.put("h6", p);
        blockElements.put("h7", p);
        blockElements.put("ul", p);
        blockElements.put("ol", p);
        blockElements.put("div", p);
        blockElements.put("blockquote", p);

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
        ignoreEmpty.put("p", "p");
    }

    public String filter(String input, FilterContext context) {
        String finalOutput = input;
        try {
            DeBlockFilter deblockFilter = new DeBlockFilter();
            EmptyFilter emptyFilter = new EmptyFilter();

            deblockFilter.setBlockElements(blockElements);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ToXMLStream xmlStream = new ToXMLStream();
            xmlStream.setOutputStream(outputStream);
            xmlStream.setIndent(false);
            xmlStream.setEncoding("UTF-8");
            xmlStream.setIndentAmount(4);
            deblockFilter.setContentHandler(emptyFilter);
            emptyFilter.setContentHandler(xmlStream.asContentHandler());

            SAXParser parser = saxParserFactory.newSAXParser();

            XMLReader xmlReader = parser.getXMLReader();

            xmlReader.setContentHandler(deblockFilter);
            xmlReader.parse(new InputSource(new StringReader("<sr>" + input + "</sr>")));

            String output = outputStream.toString(StandardCharsets.UTF_8);
            int startBlock = output.indexOf("<sr>");
            int endBlock = output.indexOf("</sr>");
            if (startBlock >= 0 && endBlock >= 0) {
                finalOutput = output.substring(startBlock + 4, endBlock);
            }
            log.debug("Output is {}", finalOutput);
        } catch (Exception e) {
            log.error("Failed to XHTML check {}\n Input======\n{}\n=======", e, input);
            return input;
        }

        return finalOutput;
    }

    public String[] replaces() {
        return FilterPipe.NO_REPLACES;
    }

    public String[] before() {
        return FilterPipe.EMPTY_BEFORE;
    }

    public String getDescription() {
        return "Hand Coded XHTML filter";
    }

    public static class DeBlockFilter implements ContentHandler {

        @Setter private Map<String, List<String>> blockElements = new HashMap<>();
        @Setter private ContentHandler contentHandler;
        private final Stack<EStack> stack = new Stack<>();

        public void addElement(String blockElement, String unnested) {
            List<String> element = blockElements.computeIfAbsent(blockElement, k -> new ArrayList<>());
            element.add(unnested);
        }

        /**
         * Unwind the xpath stack back to the first instance of the requested element
         */
        private Stack<EStack> closeTo(List<String> deBlockElements) throws SAXException {
            int firstIndex = stack.size();
            for (int i = 0; i < stack.size(); i++) {
                EStack es = stack.get(i);
                if (deBlockElements.contains(es.lname)) {
                    firstIndex = i;
                }
            }
            EStack es;
            Stack<EStack> sb = new Stack<>();
            while (stack.size() > firstIndex) {
                es = stack.pop();
                contentHandler.endElement(es.ns, es.qname, es.lname);
                sb.push(es);
            }
            return sb;
        }

        /**
         * Check each element to see if it's in a list of elements which is
         * should not be inside If it is one of these elements, get a list of
         * elements, and unwind to that it is not inside the stack
         */
        public void startElement(String ns, String qname, String lname, Attributes atts) throws SAXException {
            if (blockElements.get(lname) != null) {
                stack.push(new EStack(ns, qname, lname, atts, closeTo(blockElements.get(lname))));
            } else {
                stack.push(new EStack(ns, qname, lname, atts, null));
            }
            contentHandler.startElement(ns, qname, lname, atts);
        }

        /**
         * When we get to the end element, pop the Stack element off the stack.
         * If there is a restore path, restore the path back in place by emitting
         * start elements
         */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            contentHandler.endElement(uri, localName, qName);
            EStack es = stack.pop();
            if (es.restore != null) {
                while (!es.restore.isEmpty()) {
                    EStack esr = es.restore.pop();
                    contentHandler.startElement(esr.ns, esr.qname, esr.lname, esr.atts);
                    stack.push(esr);
                }
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            contentHandler.characters(ch, start, length);
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            contentHandler.ignorableWhitespace(ch, start, length);
        }

        public void processingInstruction(String target, String data) throws SAXException {
            contentHandler.processingInstruction(target, data);
        }

        public void skippedEntity(String name) throws SAXException {
            contentHandler.skippedEntity(name);
        }

        public void setDocumentLocator(Locator locator) {
            contentHandler.setDocumentLocator(locator);
        }

        public void startDocument() throws SAXException {
            contentHandler.startDocument();
        }

        public void endDocument() throws SAXException {
            contentHandler.endDocument();
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            contentHandler.startPrefixMapping(prefix, uri);
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            contentHandler.endPrefixMapping(prefix);
        }

    }

    public class EmptyFilter implements ContentHandler {
        @Setter private ContentHandler contentHandler = null;
        private EStack lastElement = null;

        public EmptyFilter() {
        }

        public void setDocumentLocator(Locator locator) {
            contentHandler.setDocumentLocator(locator);
        }

        public void startDocument() throws SAXException {
            emitLast();
            contentHandler.startDocument();
        }

        public void endDocument() throws SAXException {
            emitLast();
            contentHandler.endDocument();
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            emitLast();
            contentHandler.startPrefixMapping(prefix, uri);
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            emitLast();
            contentHandler.endPrefixMapping(prefix);
        }

        public void emitLast() throws SAXException {
            if (lastElement != null) {
                // this means that there was a startElement, startElement,
                // so the lastElement MUST be emitted
                contentHandler.startElement(lastElement.ns, lastElement.qname, lastElement.lname, lastElement.atts);
                lastElement = null;
            }
        }

        public void startElement(String ns, String qname, String lname, Attributes atts) throws SAXException {
            emitLast();
            if (ignoreEmpty.get(lname.toLowerCase()) != null) {
                lastElement = new EStack(ns, qname, lname, atts, null);
            } else {
                contentHandler.startElement(ns, qname, lname, atts);
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (lastElement != null) {
                // there was a start, then an end with nothing in between
                // so ignore all together
                lastElement = null;
            } else {
                contentHandler.endElement(uri, localName, qName);
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            emitLast();
            contentHandler.characters(ch, start, length);
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            emitLast();
            contentHandler.ignorableWhitespace(ch, start, length);
        }

        public void processingInstruction(String target, String data) throws SAXException {
            emitLast();
            contentHandler.processingInstruction(target, data);
        }

        public void skippedEntity(String name) throws SAXException {
            emitLast();
            contentHandler.skippedEntity(name);
        }


    }

    public static class EStack {
        Attributes atts;
        String lname;
        String ns;
        String qname;
        Stack<EStack> restore;

        public EStack(String ns, String qname, String lname, Attributes atts, Stack<EStack> restore) {
            this.ns = ns;
            this.qname = qname;
            this.lname = lname;
            this.atts = new AttributesImpl(atts);
            this.restore = restore;
        }

        public EStack(EStack es) {
            this.ns = es.ns;
            this.qname = es.qname;
            this.lname = es.lname;
            this.atts = new AttributesImpl(es.atts);
            this.restore = es.restore;
        }
    }
}
