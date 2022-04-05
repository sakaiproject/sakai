package org.sakaiproject.archive.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.sakaiproject.util.Xml;

import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.stream.*;


public class SubPageWhacker {

    private static final class PageInfo {
        public String pageId;
        public String parentId;
        public String toolId;
        public int depth;
    }

    private static final class ItemInfo {
        public String itemId;
        public String toolId;
        public String parentPageId;
        public String referencesPageId;
        public int depth;
        public Node node;
    }


    private static String nullToEmptyString(String maybeNull) {
        if (maybeNull == null) {
            return "";
        }

        return maybeNull;
    }

    public boolean whack(String xmlPath) {
        try {
            Map<String, PageInfo> pageInfoByPageId = new HashMap<>();
            Map<String, ItemInfo> itemInfoByItemId = new HashMap<>();
            Map<String, List<ItemInfo>> pageReferenceMap = new HashMap<>();

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new File(xmlPath));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList pages = ((NodeList)xPath.evaluate("/archive/org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer/lessonbuilder/page",
                                                       doc,
                                                       XPathConstants.NODESET));

            // Round 1: Page hierarchy
            for (int i = 0; i < pages.getLength(); i++) {
                if (!(pages.item(i) instanceof Element)) {
                    continue;
                }

                Element page = (Element) pages.item(i);

                PageInfo pageInfo = new PageInfo();
                pageInfo.pageId = nullToEmptyString(page.getAttribute("pageid"));
                pageInfo.parentId = nullToEmptyString(page.getAttribute("parent"));
                pageInfo.toolId = nullToEmptyString(page.getAttribute("toolid"));
                pageInfo.depth = -1;

                pageInfoByPageId.put(pageInfo.pageId, pageInfo);
            }


            // Calculate page depth
            for (PageInfo pageInfo : pageInfoByPageId.values()) {
                int depth = 0;

                PageInfo current = pageInfo;
                while (current != null && !current.parentId.isEmpty()) {
                    depth += 1;

                    // Will return null if current.parentId is 0.  That's top-level, sorta.
                    current = pageInfoByPageId.get(current.parentId);
                }
                pageInfo.depth = depth;
            }

            // Round 2: item info
            for (int i = 0; i < pages.getLength(); i++) {
                if (!(pages.item(i) instanceof Element)) {
                    continue;
                }

                Element page = (Element) pages.item(i);

                NodeList children = page.getChildNodes();

                for (int j = 0; j < children.getLength(); j++) {
                    if (!(children.item(j) instanceof Element)) {
                        continue;
                    }

                    Element item = (Element) children.item(j);

                    if ("2".equals(item.getAttribute("type"))) {
                        ItemInfo itemInfo = new ItemInfo();
                        itemInfo.itemId = nullToEmptyString(item.getAttribute("id"));
                        itemInfo.toolId = nullToEmptyString(page.getAttribute("toolId"));
                        itemInfo.parentPageId = nullToEmptyString(page.getAttribute("id"));
                        itemInfo.referencesPageId = nullToEmptyString(item.getAttribute("sakaiid"));

                        itemInfo.depth = pageInfoByPageId.get(nullToEmptyString(page.getAttribute("pageid"))).depth;
                        itemInfo.node = item;

                        itemInfoByItemId.put(itemInfo.itemId, itemInfo);

                        if (!pageReferenceMap.containsKey(itemInfo.referencesPageId)) {
                            pageReferenceMap.put(itemInfo.referencesPageId, new ArrayList<>());
                        }

                        pageReferenceMap.get(itemInfo.referencesPageId).add(itemInfo);
                    }
                }

            }

            // Round 3: find shared pages and decide which referrers to keep
            for (List<ItemInfo> referrers : pageReferenceMap.values()) {
                String contendedPageId = referrers.get(0).referencesPageId;
                PageInfo pageInfo = pageInfoByPageId.get(contendedPageId);

                if (pageInfo.parentId.isEmpty()) {
                    // Top-level pages are implicitly referenced by the tool itself, and that wins
                    // over everything.
                    for (int i = 0; i < referrers.size(); i++) {
                        referrers.get(i).node.getParentNode().removeChild(referrers.get(i).node);
                    }
                } else {
                    if (referrers.size() == 1) {
                        // Fine: not a shared page
                        continue;
                    }

                    List<ItemInfo> candidateItems = referrers.stream().filter((itemInfo) -> itemInfo.toolId.equals(pageInfo.toolId)).collect(Collectors.toList());

                    if (candidateItems.isEmpty()) {
                        candidateItems = referrers;
                    }

                    Collections.sort(candidateItems, (a, b) -> a.depth - b.depth);

                    for (int i = 1; i < candidateItems.size(); i++) {
                        candidateItems.get(i).node.getParentNode().removeChild(candidateItems.get(i).node);
                    }
                }
            }

            Xml.writeDocument(doc, xmlPath);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new SubPageWhacker().whack(args[0]);
    }

}

