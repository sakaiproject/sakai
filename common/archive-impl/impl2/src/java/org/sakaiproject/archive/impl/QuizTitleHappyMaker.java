package org.sakaiproject.archive.impl;

import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;

public class QuizTitleHappyMaker {
    public void makeHappy(String lessonsExportPath) {
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new File(lessonsExportPath));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList pages = ((NodeList)xPath.evaluate("/archive/org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer/lessonbuilder/page",
                    doc,
                    XPathConstants.NODESET));

            for (int i = 0; i < pages.getLength(); i++) {
                if (!(pages.item(i) instanceof Element)) {
                    continue;
                }

                Element page = (Element) pages.item(i);

                NodeList questions = ((NodeList)xPath.evaluate("item[@type=11]",
                        page,
                        XPathConstants.NODESET));

                for (int j = 0; j < questions.getLength(); j++) {
                    if (!(questions.item(j) instanceof Element)) {
                        continue;
                    }

                    Element question = (Element) questions.item(j);

                    if (questions.getLength() == 1) {
                        question.setAttribute("name", "Question");
                    } else {
                        String newQuizName = String.format("Question %d", j+1);
                        question.setAttribute("name", newQuizName);
                    }
                }
            }

            Xml.writeDocument(doc, lessonsExportPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
