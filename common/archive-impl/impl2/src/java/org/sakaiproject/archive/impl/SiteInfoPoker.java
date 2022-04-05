package org.sakaiproject.archive.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.Xml;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class SiteInfoPoker {
    public boolean pokeAsResource(String siteId, String exportPath) {
        Site site;
        try {
            site = SiteService.getSite(siteId);
        } catch (IdUnusedException e) {
            return false;
        }

        String content = site.getDescription();

        if (StringUtils.trimToNull(content) == null) {
            return false;
        }

        String contentXmlPath = exportPath + "/content.xml";
        if (!new File(contentXmlPath).exists()) {
            // FIXME does this happen?
            return false;
        }

        DocumentBuilder builder = null;
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new File(contentXmlPath));

            Node chsEl = ((NodeList)xPath.evaluate("/archive/org.sakaiproject.content.api.ContentHostingService", doc, XPathConstants.NODESET)).item(0);

            if (chsEl == null) {
                throw new RuntimeException("No ContentHostingService element in content.xml");
            }

            String uuid = UUID.randomUUID().toString();

            String siteInfoResourcePath = exportPath + "/" + uuid;
            BufferedWriter writer = new BufferedWriter(new FileWriter(siteInfoResourcePath));
            writer.write("<html><head><title>Site Information</title></head><body>");
            writer.write(content);
            writer.write("</body></html>");
            writer.close();

            Element siteInfoResource = doc.createElement("resource");
            siteInfoResource.setAttribute("body-location", uuid);
            siteInfoResource.setAttribute("content-length", String.valueOf(new File(siteInfoResourcePath).length()));
            siteInfoResource.setAttribute("content-type", "text/html");
            siteInfoResource.setAttribute("file-path", "/tmp/Site Information.html");
            siteInfoResource.setAttribute("id", String.format("/group/%s/Site Information.html", siteId));
            siteInfoResource.setAttribute("rel-id", "Site Information.html");
            siteInfoResource.setAttribute("resource-type", "org.sakaiproject.content.types.fileUpload");
            siteInfoResource.setAttribute("sakai:access_mode", "inherited");
            siteInfoResource.setAttribute("sakai:hidden", "false");

            String timeNow = TimeService.newTime().toString();

            Element properties = doc.createElement("properties");

            Element property = doc.createElement("property");
            property.setAttribute("enc", "BASE64");
            property.setAttribute("name", "CHEF:creator");
            property.setAttribute("value", new String(Base64.getEncoder().encode("admin".getBytes())));
            properties.appendChild(property);

            property = doc.createElement("property");
            property.setAttribute("enc", "BASE64");
            property.setAttribute("name", "CHEF:modifiedby");
            property.setAttribute("value", new String(Base64.getEncoder().encode("admin".getBytes())));
            properties.appendChild(property);

            property = doc.createElement("property");
            property.setAttribute("enc", "BASE64");
            property.setAttribute("name", "CHEF:description");
            property.setAttribute("value", new String(Base64.getEncoder().encode("".getBytes())));
            properties.appendChild(property);

            property = doc.createElement("property");
            property.setAttribute("enc", "BASE64");
            property.setAttribute("name", "CHEF:is-collection");
            property.setAttribute("value", new String(Base64.getEncoder().encode("false".getBytes())));
            properties.appendChild(property);

            property = doc.createElement("property");
            property.setAttribute("enc", "BASE64");
            property.setAttribute("name", "DAV:getlastmodified");
            property.setAttribute("value", new String(Base64.getEncoder().encode(timeNow.getBytes())));
            properties.appendChild(property);

            property = doc.createElement("property");
            property.setAttribute("enc", "BASE64");
            property.setAttribute("name", "SAKAI:content_priority");
            property.setAttribute("value", new String(Base64.getEncoder().encode("2".getBytes())));
            properties.appendChild(property);

            property = doc.createElement("property");
            property.setAttribute("enc", "BASE64");
            property.setAttribute("name", "SAKAI:conditionalrelease");
            property.setAttribute("value", new String(Base64.getEncoder().encode("false".getBytes())));
            properties.appendChild(property);

            property = doc.createElement("property");
            property.setAttribute("enc", "BASE64");
            property.setAttribute("name", "DAV:creationdate");
            property.setAttribute("value", new String(Base64.getEncoder().encode(timeNow.getBytes())));
            properties.appendChild(property);

            property = doc.createElement("property");
            property.setAttribute("enc", "BASE64");
            property.setAttribute("name", "SAKAI:conditionalNotificationId");
            property.setAttribute("value", new String(Base64.getEncoder().encode("".getBytes())));
            properties.appendChild(property);


            property = doc.createElement("property");
            property.setAttribute("enc", "BASE64");
            property.setAttribute("name", "DAV:displayname");
            property.setAttribute("value", new String(Base64.getEncoder().encode("Site Information".getBytes())));
            properties.appendChild(property);

            siteInfoResource.appendChild(properties);

            chsEl.appendChild(siteInfoResource);

            Xml.writeDocument(doc, contentXmlPath);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public boolean pokeAway(String siteId, String path, String nowString, String fromSystem) {
        Site site;
        try {
            site = SiteService.getSite(siteId);
        } catch (IdUnusedException e) {
            return false;
        }

        String content = site.getDescription();

        if (StringUtils.trimToNull(content) == null) {
            return false;
        }

        if (!new File(path).exists()) {
            // create a lessonsbuilder.xml
            Document doc = Xml.createDocument();
            bootstrapXML(doc, siteId, nowString, fromSystem);
            Xml.writeDocument(doc, path);
        }

        DocumentBuilder builder = null;
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new File(path));
            Node lessonbuilder = ((NodeList)xPath.evaluate("/archive/org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer/lessonbuilder", doc, XPathConstants.NODESET)).item(0);

            if (lessonbuilder == null) {
                lessonbuilder = bootstrapXML(doc, siteId, nowString, fromSystem);
            }

            Element page = doc.createElement("page");
            page.setAttribute("folder", "SiteInfo/");
            page.setAttribute("hidden", "false");
            page.setAttribute("pageid", "");
            page.setAttribute("siteid", siteId);
            page.setAttribute("title", "Site Information");
            page.setAttribute("toolid", siteId);
            if (lessonbuilder.getChildNodes().getLength() == 0) {
                lessonbuilder.appendChild(page);
            } else {
                lessonbuilder.insertBefore(page, lessonbuilder.getFirstChild());
            }
            Element item = doc.createElement("item");
            item.setAttribute("alt", "");
            item.setAttribute("altPoints", "null");
            item.setAttribute("anonymous", "false");
            item.setAttribute("description", "");
            item.setAttribute("forcedCommentsAnonymous", "false");
            item.setAttribute("gradebookPoints", "null");
            item.setAttribute("groupOwned", "false");
            item.setAttribute("html", content);
            item.setAttribute("id", siteId);
            item.setAttribute("name", "Site Information");
            item.setAttribute("nextpage", "false");
            item.setAttribute("pageId", siteId);
            item.setAttribute("prerequisite", "false");
            item.setAttribute("required", "false");
            item.setAttribute("sakaiid", "");
            item.setAttribute("samewindow", "false");
            item.setAttribute("sequence", "1");
            item.setAttribute("showComments", "false");
            item.setAttribute("subrequirement", "false");
            item.setAttribute("type", "5");
            page.appendChild(item);
            Element itemAttributes = doc.createElement("attributes");
            itemAttributes.setTextContent("{}");
            item.appendChild(itemAttributes);

            Xml.writeDocument(doc, path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private Element bootstrapXML(Document doc, String siteId, String nowString, String fromSystem) {
        Element root = doc.getDocumentElement();
        if (root == null) {
            root = doc.createElement("archive");
            root.setAttribute("site", siteId);
            root.setAttribute("date", nowString);
            root.setAttribute("system", fromSystem);
            doc.appendChild(root);
        }

        Element subRoot = doc.createElement("org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer");
        subRoot.setAttribute("version", "2.4");
        root.appendChild(subRoot);
        Element lessonbuilder = doc.createElement("lessonbuilder");
        subRoot.appendChild(lessonbuilder);

        return lessonbuilder; 
    }
}
