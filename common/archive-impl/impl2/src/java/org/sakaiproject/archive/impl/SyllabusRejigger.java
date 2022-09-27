package org.sakaiproject.archive.impl;

import org.jsoup.Jsoup;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;

import org.sakaiproject.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyllabusRejigger {

    public boolean rewriteSyllabus(String path) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new File(path));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList syllabusData = ((NodeList)xPath.evaluate("/archive/org.sakaiproject.api.app.syllabus.SyllabusService/siteArchive/syllabus/syllabus_data",
                                                              doc,
                                                              XPathConstants.NODESET));

            if (syllabusData.getLength() == 0) {
                // Nothing to do here
                return true;
            }

            StringBuilder content = new StringBuilder();

            // Build up content
            for (int i = 0; i < syllabusData.getLength(); i++) {
                Element mergeMe = (syllabusData.item(i) instanceof Element) ? (Element) syllabusData.item(i) : null;

                if (mergeMe == null) {
                    continue;
                }

                String title = mergeMe.getAttribute("title");

                content.append(String.format("<h2>%s</h2>\n\n", StringEscapeUtils.escapeHtml4(title)));

                NodeList children = mergeMe.getChildNodes();
                List<String> attachmentPaths = new ArrayList<>();
                for (int j = 0; j < children.getLength(); j++) {
                    Element child = (children.item(j) instanceof Element) ? (Element) children.item(j) : null;

                    if (child == null) {
                        continue;
                    }

                    if ("asset".equals(child.getTagName())) {
                        // Get HTML content
                        if (child.getAttribute("syllabus_body-html") != null) {
                            String decoded = new String(Base64.getDecoder().decode(child.getAttribute("syllabus_body-html")),
                                                        "UTF-8");

                            content.append(decoded);
                        }
                    } else if ("attachment".equals(child.getTagName())) {
                        // Load attachment
                        if (child.getAttribute("relative-url") != null) {
                            attachmentPaths.add(String.format("%s", child.getAttribute("relative-url").replace("/content","")));
                        }
                    }
                }

                if (attachmentPaths.size() > 0) {
                    content.append("\n\n<h3>Attachments</h3>\n");
                    content.append("\n<ul>\n");
                    for (String attachment : attachmentPaths) {
                        attachment = StringEscapeUtils.escapeHtml4(attachment);

                        content.append(String.format("<li><a href=\"%s\">%s</a></li>\n",
                                                     attachment,
                                                     attachment.replaceAll("^.*/", "")));
                    }
                    content.append("</ul>\n");
                }
            }

            // Delete merged nodes
            for (int i = syllabusData.getLength() - 1; i >= 1; i--) {
                Node victim = syllabusData.item(i);
                victim.getParentNode().removeChild(victim);
            }

            Element primary = (Element) syllabusData.item(0);
            NodeList primaryNodes = primary.getChildNodes();

            for (int i = 0; i < primaryNodes.getLength(); i++) {
                if (!(primaryNodes.item(i) instanceof Element)) {
                    continue;
                }

                Element childNode = (Element) primaryNodes.item(i);

                if ("attachment".equals(childNode.getTagName())) {
                    childNode.getParentNode().removeChild(childNode);
                    i -= 1;
                } else if ("asset".equals(childNode.getTagName())) {
                    childNode.setAttribute("syllabus_body-html",
                                           new String(Base64.getEncoder().encode(content.toString().getBytes("UTF-8")),
                                                      "UTF-8"));
                }
            }

            // Page title
            primary.setAttribute("title", "Course Outline");

            Xml.writeDocument(doc, path + ".rewritten");

            // Keep the original copy for reference
            new File(path).renameTo(new File(path + ".pre_rewrite"));

            // Overwrite the original path
            new File(path + ".rewritten").renameTo(new File(path));

            return true;
        } catch (Exception e) {
            log.error("Error rewriting syllabus", e);
        }

        return false;
    }

    public static void main(String[] args) {
        new SyllabusRejigger().rewriteSyllabus(args[0]);
    }
}
