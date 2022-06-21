package org.sakaiproject.archive.impl;

import org.jsoup.Jsoup;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.util.regex.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class LessonsRejigger {

    private final AtomicBoolean insidePage = new AtomicBoolean(false);
    private final List<BufferedSAXEvent> bufferedItems = new LinkedList<>();

    private static String IMAGE_EXTENSIONS = "(jpg|jpeg|png|gif|bmp|svg|jfif|pjpeg|pjp|ico|cur|tif|tiff|webp)";

    private static final char REPLACEMENT_CHAR = ' ';

    private static String stripControlChars(String s) {
        if (s == null) {
            return s;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int ch = (int)s.charAt(i);

            // 9 = tab, 10 = LF, 13 = CR
            if (ch < 32) {
                if (ch != 9 && ch != 10 && ch != 13) {
                    System.err.println(String.format("Replacing 0x%02x with 0x%02x", ch, (int)REPLACEMENT_CHAR));
                    ch = (int)REPLACEMENT_CHAR;
                }
            }

            result.append((char)ch);
        }

        return result.toString();
    }


    public boolean rewriteLessons(String path) {
        try {
            XMLReader xr = new XMLFilterImpl(XMLReaderFactory.createXMLReader()) {
                final AtomicBoolean hasKaltura = new AtomicBoolean(false);

                public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                    if ("page".equals(qName)) {
                        insidePage.set(true);
                    } else if (insidePage.get()) {
                        bufferedItems.add(BufferedSAXEvent.startElement(uri, localName, qName, atts));
                        return;
                    }

                    super.startElement(uri, localName, qName, atts);
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if ("page".equals(qName)) {
                        if (!insidePage.get()) {
                            throw new RuntimeException("Assertion failed: did not expect to find nested pages here.");
                        }

                        insidePage.set(false);
                        emitBufferedItems();
                    } else if (insidePage.get()) {
                        bufferedItems.add(BufferedSAXEvent.endElement(uri, localName, qName));
                        return;
                    }

                    super.endElement(uri, localName, qName);
                }

                public void characters(char[] ch, int start, int length) throws SAXException {
                    if (insidePage.get()) {
                        bufferedItems.add(BufferedSAXEvent.characters(ch.clone(), start, length));
                        return;
                    }

                    super.characters(ch, start, length);
                }

                private void emitBufferedItems() throws SAXException {
                    LinkedList<Item> items = groupEvents();
                    bufferedItems.clear();

                    // Multimedia items linking to Sakai resources can be rewritten as rich text
                    // TEXT items.
                    for (Item item : items) {
                        if (item.type.equals(ItemType.MULTIMEDIA) &&
                            item.events.get(0).atts.getValue("sakaiid").toLowerCase(Locale.ROOT).matches("^/.*\\." + IMAGE_EXTENSIONS + "$")) {

			    try {
				    // Transmogrify into rich text.  That's right: transmogrify.
				    BufferedSAXEvent elt = item.events.get(0);
				    AttributesImpl updatedAttributes = new AttributesImpl(elt.atts);

				    String contentPath = updatedAttributes.getValue("sakaiid");
				    String altText = updatedAttributes.getValue("alt");

				    updatedAttributes.setValue(updatedAttributes.getIndex("sakaiid"), "");
				    updatedAttributes.setValue(updatedAttributes.getIndex("type"), "5"); // text
				    updatedAttributes.setValue(updatedAttributes.getIndex("name"), altText);
				    updatedAttributes.setValue(updatedAttributes.getIndex("html"),
							       String.format("<p><img style=\"max-width: 100%%\" alt=\"%s\" src=\"https://vula.uct.ac.za/access/content%s\"></p>",
									     altText,
									     contentPath));

				    elt.atts = updatedAttributes;
				    item.type = ItemType.TEXT;
			    } catch (Exception e) {
				log.warn("Exception converting Lessons item to rich text (NYU code) for path {}: {}", path, e.getMessage());
			    }
                        }
                    }

                    // <break><break> => <break>
                    for (int i = 0; i < (items.size() - 1); i++) {
                        if (items.get(i).type.equals(ItemType.BREAK) &&
                            items.get(i + 1).type.equals(ItemType.BREAK)) {
                            // Too many breaks!
                            items.remove(i);
                            i -= 1;
                        }
                    }

                    // <text><break><text> => <text w/ hr>
                    for (int i = 1; i < (items.size() - 1); i++) {
                        if (items.get(i - 1).type.equals(ItemType.TEXT) &&
                            items.get(i).type.equals(ItemType.BREAK) &&
                            items.get(i + 1).type.equals(ItemType.TEXT)) {
                            // Mergey mergey
                            Item victim = items.remove(i + 1);
                            items.remove(i); // drop the break

                            Item assimilator = items.get(i - 1);

                            BufferedSAXEvent startNode = assimilator.events.get(0);
                            AttributesImpl mergedAttributes = new AttributesImpl(startNode.atts);
                            int attIdx = mergedAttributes.getIndex("html");
                            mergedAttributes.setValue(attIdx,
                                                      mergedAttributes.getValue(attIdx) +
                                                      "<hr>" +
                                                      victim.events.get(0).atts.getValue("html"));

                            startNode.atts = mergedAttributes;

                            // compensate for the nodes we just removed to pick up runs of
                            // <text><break><text><break>...
                            i -= 1;
                        }
                    }

                    // Adjacent text items can be merged too
                    // <text><text> => <text>
                    for (int i = 0; i < (items.size() - 1); i++) {
                        if (items.get(i).type.equals(ItemType.TEXT) &&
                            items.get(i + 1).type.equals(ItemType.TEXT)) {
                            // Mergey mergey
                            Item victim = items.remove(i + 1);
                            Item assimilator = items.get(i);

                            BufferedSAXEvent startNode = assimilator.events.get(0);
                            AttributesImpl mergedAttributes = new AttributesImpl(startNode.atts);
                            int attIdx = mergedAttributes.getIndex("html");
                            mergedAttributes.setValue(attIdx,
                                                      mergedAttributes.getValue(attIdx) +
                                                      victim.events.get(0).atts.getValue("html"));

                            startNode.atts = mergedAttributes;

                            i -= 1;
                        }
                    }

                    // Any remaining breaks are not required
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).type.equals(ItemType.BREAK)) {
                            items.remove(i);
                            i -= 1;
                        }
                    }

                    // Nameless text items get a name
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).type.equals(ItemType.TEXT)) {
                            Item text = items.get(i);
                            BufferedSAXEvent elt = text.events.get(0);

                            if (elt.atts.getValue("name").isEmpty() ||
                                (i == 0 && elt.atts.getValue("name").toLowerCase(Locale.ROOT).matches(".*\\." + IMAGE_EXTENSIONS + "$"))) {
                                // If our text item has no name, or if it's the first item on the page but it's
                                // really just a header image... do better.

                                org.jsoup.nodes.Element document = Jsoup.parse(elt.atts.getValue("html"));
                                org.jsoup.nodes.Element header = document.selectFirst("h1");

                                String generatedName = "";

                                if (header != null) {
                                    // If we got a header, take it wholesale.
                                    generatedName = header.text();
                                } else {
                                    // Otherwise, cobble a snippet together.
                                    generatedName = document.text();

                                    if (generatedName.length() > 30) {
                                        generatedName = generatedName.substring(0, 30) + "...";
                                    }
                                }

                                // Sometimes the content we've used to derive a name will have contained a
                                // backspace character (or any other control character).  Drop them out before
                                // they cause problems downstream.
                                generatedName = stripControlChars(generatedName);

                                if (generatedName.isEmpty()) {
                                    generatedName = "Embedded Item";
                                }

                                AttributesImpl updatedAttributes = new AttributesImpl(elt.atts);
                                updatedAttributes.setValue(updatedAttributes.getIndex("name"), generatedName);
                                elt.atts = updatedAttributes;
                            }
                        }
                    }

                    for (Item item : items) {
                        for (BufferedSAXEvent event : item.events) {
                            if (event.type.equals(EventType.START)) {
                                super.startElement(event.uri, event.localName, event.qName, event.atts);
                            } else if (event.type.equals(EventType.END)) {
                                super.endElement(event.uri, event.localName, event.qName);
                            } else if (event.type.equals(EventType.CHARS)) {
                                super.characters(event.ch, event.start, event.length);
                            }
                        }
                    }
                }

                // Group our SAX events into top-level <item> elements
                private LinkedList<Item> groupEvents() {
                    LinkedList<BufferedSAXEvent> queue = new LinkedList(bufferedItems);
                    LinkedList<Item> result = new LinkedList<Item>();

                    while (!queue.isEmpty()) {
                        BufferedSAXEvent head = queue.removeFirst();

                        if (EventType.START.equals(head.type) && "item".equals(head.qName)) {
                            Item item = null;

                            if ("14".equals(head.atts.getValue("type"))) {
                                item = new Item(ItemType.BREAK);
                            } else if ("5".equals(head.atts.getValue("type"))) {
                                item = new Item(ItemType.TEXT);
                            } else if ("7".equals(head.atts.getValue("type"))) {
                                item = new Item(ItemType.MULTIMEDIA);
                            } else {
                                item = new Item(ItemType.OTHER);
                            }

                            item.events.add(head);

                            // Read up until the closing item event
                            boolean foundClose = false;
                            while (!queue.isEmpty()) {
                                BufferedSAXEvent elt = queue.removeFirst();
                                if (EventType.START.equals(elt.type) && "item".equals(elt.qName)) {
                                    throw new RuntimeException("Assertion failed: did not expect nested items");
                                }

                                item.events.add(elt);

                                if (EventType.END.equals(elt.type) && "item".equals(elt.qName)) {
                                    // All done
                                    foundClose = true;
                                    break;
                                }
                            }

                            if (!foundClose) {
                                throw new RuntimeException("Assertion failed: Ran out of input before finding our closing item");
                            }

                            result.add(item);
                        } else {
                            throw new RuntimeException("Assertion failed: expected a list of top-level items here");
                        }
                    }

                    return result;
                }
            };

            // Pass through invalid 'sakai:' prefixes
            xr.setFeature("http://xml.org/sax/features/namespaces", false);

            Source src = new SAXSource(xr, new InputSource(path));
            Result res = new StreamResult(new FileOutputStream(path + ".rewritten"));
            TransformerFactory.newInstance().newTransformer().transform(src, res);

            // Keep the original copy for reference
            new File(path).renameTo(new File(path + ".pre_rewrite"));

            // Overwrite the original path
            new File(path + ".rewritten").renameTo(new File(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private enum EventType {
        START,
        END,
        CHARS,
    }

    private static class BufferedSAXEvent {
        public EventType type;

        // elements
        public String uri;
        public String localName;
        public String qName;
        public Attributes atts;

        // char data
        public char[] ch;
        public int start;
        public int length;

        private BufferedSAXEvent(EventType type) {
            this.type = type;
        }

        public static BufferedSAXEvent startElement(String uri, String localName, String qName, Attributes atts) {
            BufferedSAXEvent result = new BufferedSAXEvent(EventType.START);

            result.uri = uri;
            result.localName = localName;
            result.qName = qName;
            result.atts = new AttributesImpl(atts);

            return result;
        }

        public static BufferedSAXEvent endElement(String uri, String localName, String qName) {
            BufferedSAXEvent result = new BufferedSAXEvent(EventType.END);

            result.uri = uri;
            result.localName = localName;
            result.qName = qName;

            return result;
        }

        public static BufferedSAXEvent characters(char[] ch, int start, int length) {
            BufferedSAXEvent result = new BufferedSAXEvent(EventType.CHARS);

            result.ch = ch;
            result.start = start;
            result.length = length;

            return result;
        }
    }

    private enum ItemType {
        TEXT,
        BREAK,
        MULTIMEDIA,
        OTHER,
    }

    private static class Item {
        public ItemType type;
        public List<BufferedSAXEvent> events;

        public Item(ItemType type) {
            this.type = type;
            this.events = new ArrayList<>();
        }
    }


    public static void main(String[] args) {
        new LessonsRejigger().rewriteLessons(args[0]);
    }
}
