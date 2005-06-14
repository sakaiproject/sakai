package test.org.sakaiproject.tool.assessment.business.entity.helper;

import java.io.InputStream;
import java.util.ArrayList;

import javax.faces.context.FacesContext;

import org.w3c.dom.Document;

import org.sakaiproject.tool.assessment.business.entity.constants.QTIVersion;
import org.sakaiproject.tool.assessment.business.entity.helper.AuthoringHelper;
import org.sakaiproject.tool.assessment.business.entity.helper.AuthoringXml;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.util.XmlUtil;

/**
 * <p>Test bed for QTI utilities.</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id: QTITester.java,v 1.8 2005/02/28 18:55:44 esmiley.stanford.edu Exp $
 */

public class QTITester {
//  private static boolean useContextPath = true;
  private static boolean useContextPath = false;
//  private static int version = QTIVersion.VERSION_1_2;
  private static int version = QTIVersion.VERSION_2_0;

  public static void main(String[] args) {
//    System.out.println("testing: AuthoringHelper");
//    testAuthoringHelper();
//    System.out.println("<!--testing: AuthoringXml templates-->");
//    testAuthoringXmlTemplates();
    System.out.println("<!--testing: AuthoringXml routines-->");
    testAuthoringXmlRoutines();

    }

  private static void testAuthoringHelper()
  {
    AuthoringHelper authHelper = new AuthoringHelper(QTIVersion.VERSION_1_2);
    AssessmentService aService = new AssessmentService();
    ArrayList list = aService.getAllAssessments(1,1,"title");
    AuthoringXml ax = new AuthoringXml(version);

    for (int i = 0; i < list.size(); i++)
    {
      PublishedAssessmentFacade pub = (PublishedAssessmentFacade) list.get(
        i);
      String pubid = pub.getAssessmentId().toString();
      System.out.println("testing: " + pubid);
      System.out.println(
        "=======================================================");
      InputStream is = ax.getTemplateInputStream(AuthoringXml.ASSESSMENT);
      Document doc = authHelper.getAssessment(pubid, is);
      System.out.println(doc.toString());
      System.out.println(
        "=======================================================");
    }

  }
  private static void testAuthoringXmlTemplates()
  {
    AuthoringXml ax = new AuthoringXml(version);
    String[] template =
      {
      ax.ASSESSMENT, //   "assessmentTemplate.xml";
      ax.SECTION, //   "sectionTemplate.xml";
      ax.ITEM_AUDIO, //   "audioRecordingTemplate.xml";
      ax.ITEM_ESSAY, //   "essayTemplate.xml";
      ax.ITEM_FIB, //   "fibTemplate.xml";
      ax.ITEM_FILE, //   "fileUploadTemplate.xml";
      ax.ITEM_MATCH, //   "matchTemplate.xml";
      ax.ITEM_MCMC, //   "mcMCTemplate.xml";
      ax.ITEM_MCSC, //   "mcSCTemplate.xml";
      ax.ITEM_SURVEY, //   "mcSurveyTemplate.xml";
      ax.ITEM_TF, //   "trueFalseTemplate.xml";
      ax.SURVEY_10, //   SURVEY_PATH + "10.xml";
      ax.SURVEY_5, //   SURVEY_PATH + "5.xml";
      ax.SURVEY_AGREE, //   SURVEY_PATH + "AGREE.xml";
      ax.SURVEY_AVERAGE, //   SURVEY_PATH + "AVERAGE.xml";
      ax.SURVEY_EXCELLENT, //   SURVEY_PATH +        "EXCELLENT.xml";
      ax.SURVEY_STRONGLY, //   SURVEY_PATH +        "STRONGLY_AGREE.xml";
      ax.SURVEY_UNDECIDED, //   SURVEY_PATH +        "UNDECIDED.xml";
      ax.SURVEY_YES, //   SURVEY_PATH + "YES.xml";
      };

    for (int i = 0; i < template.length; i++)
    {
      System.out.println("<!--=======================================================");
      System.out.println("testing: " + template[i]);
      System.out.println("=======================================================-->");
      InputStream is = null;

      if (useContextPath)
      {
        is = ax.getTemplateInputStream(template[i],FacesContext.getCurrentInstance());
      }
      else
      {
      is = ax.getTemplateInputStream(template[i]);
      }
      System.out.println("<!--=======================================================-->");
      System.out.println(ax.getTemplateAsString(is));
      System.out.println("<!--=======================================================-->");

    }

  }

  public static void testAuthoringXmlRoutines()
  {
    AuthoringXml ax = new AuthoringXml(version);
    Document assessmentXml = null;
    Document sectionXml = null;
    InputStream is = null;

    is = ax.getTemplateInputStream(ax.ASSESSMENT);

    assessmentXml = ax.readXMLDocument(is);
    System.out.println("<!--============= assessment ==============================-->");
    System.out.println(XmlUtil.getDOMString(assessmentXml));
    System.out.println("<!--=======================================================-->");

    is = ax.getTemplateInputStream(ax.SECTION);

    sectionXml = ax.readXMLDocument(is);
    System.out.println("<!--============= section ================================-->");
    System.out.println(XmlUtil.getDOMString(sectionXml));
    System.out.println("<!--=======================================================-->");
    try
    {
      assessmentXml = ax.update(assessmentXml, "questestinterop/assessment/@ident",
            "test_ident");
      assessmentXml = ax.update(assessmentXml, "questestinterop/assessment/@title",
            "this is a title");
    }
    catch (Exception ex)
    {
      System.out.println("oops: " + ex);
    }
    System.out.println("<!--============= modified assessment =====================-->");
    System.out.println(XmlUtil.getDOMString(assessmentXml));
    System.out.println("<!--=======================================================-->");
    try
    {
      sectionXml = ax.update(sectionXml, "section/@ident",
            "test_section_ident");
      sectionXml = ax.update(sectionXml, "section/@title",
            "this is a section title");
    }
    catch (Exception ex)
    {
      System.out.println("oops: " + ex);
    }
    System.out.println("<!--============= modified section ===============-->");
    System.out.println(XmlUtil.getDOMString(sectionXml));
    System.out.println("<!--=======================================================-->");

    try {
      ax.addElement(assessmentXml, "questestinterop/assessment",
                    sectionXml.getDocumentElement());
      ax.addAttribute(assessmentXml, "questestinterop/assessment",
                      "custom_attribute");
      sectionXml = ax.update(assessmentXml,
                             "questestinterop/assessment/@custom_attribute",
                             "custom_value");
    }
    catch (Exception ex) {
      System.out.println("oops: " + ex);
    }
    System.out.println("<!--============= modified assessment and section ===============-->");
    System.out.println(XmlUtil.getDOMString(sectionXml));
    System.out.println("<!--=======================================================-->");


  }

}