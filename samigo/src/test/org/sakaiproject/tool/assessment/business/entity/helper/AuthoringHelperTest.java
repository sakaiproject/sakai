package test.org.sakaiproject.tool.assessment.business.entity.helper;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.sakaiproject.tool.assessment.business.entity.asi.Assessment;
import org.sakaiproject.tool.assessment.business.entity.asi.Item;
import org.sakaiproject.tool.assessment.business.entity.asi.Section;
import org.sakaiproject.tool.assessment.business.entity.constants.QTIVersion;
import org.sakaiproject.tool.assessment.business.entity.helper.ExtractionHelper;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.util.XmlUtil;
import org.w3c.dom.Document;

/**
 * <p> Test program</p>
 * <p> </p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class AuthoringHelperTest {
  private static final org.apache.log4j.Logger LOG =
    org.apache.log4j.Logger.getLogger(AuthoringHelperTest.class);

  private final static String xslPath =
        "C:\\Documents and Settings\\Ed Smiley\\jbproject\\sam\\webapp.war\\xml\\xsl\\dataTransform\\import\\v1p2";
  private final static String myPath =
      "c:\\Documents and Settings\\Ed Smiley\\My Documents\\xml\\output";
  public AuthoringHelperTest() {
  }

  public static void main(String args[]) {
   String items[] =
   {
        "i1080.xml",
        "i1089.xml",
        "i1090.xml",
        "i1091.xml",
        "i1092.xml",
        "i1093.xml",
        "i1094.xml",
        "i1096.xml",
    };
   testItems(myPath, items);
   String assessments[] =
   {
       "..\\respondus\\respondus_IMS_QTI.xml",
       "a1083.xml",
       "a1117.xml",
       "a24.xml",
       "a25.xml",
       "a26.xml",
   };
  testAssessments(myPath, assessments);
  }

  public static void testAssessments(String myPath, String[] myDocs) {
    AuthoringHelperTest ah = new AuthoringHelperTest();
    String sep = "\\";
    for (int i = 0; i < myDocs.length; i++) {
      String path = myPath + File.separator + myDocs[i];
      System.out.println("Testing XML file:" + path);
      Document document =
          XmlUtil.readDocument(myPath + File.separator + myDocs[i]);
      System.out.println("Created doc.");
      AssessmentFacade a = ah.createImportedAssessment(document);
      System.out.println("Created assessment title: " + a.getTitle());
      System.out.println("Created assessment comments: " + a.getComments());
      System.out.println("Created assessment desc: " + a.getDescription());
      System.out.println("Created assessment mod: " + a.getLastModifiedBy());
      System.out.println("Created assessment date: " + a.getLastModifiedDate());
    }
  }

  public static void testItems(String myPath, String[] myDocs) {
    AuthoringHelperTest ah = new AuthoringHelperTest();
    String sep = "\\";
    for (int i = 0; i < myDocs.length; i++) {
      String path = myPath + File.separator + myDocs[i];
      System.out.println("Testing XML file:" + path);
      Document document =
          XmlUtil.readDocument(myPath + File.separator + myDocs[i]);
      if (document == null) System.out.println("DOCUMENT IS NULL");
      if (document != null) System.out.println("DOCUMENT EXISTS.");
      System.out.println("Created doc.");
      ItemFacade it = ah.createImportedItem(document);
//      System.out.println("Created item: " + it.getItemTextArray());
    }
  }

  /**
   * copy of method in AuthoringHelper with persistence turned off.
   * Import an XML document in QTI format, extract and persist the data.
   * @param document the document
   * @return a persisted assessment
   */
  public AssessmentFacade createImportedAssessment(Document document) {
//    if(LOG.isDebugEnabled())
//    {
    System.out.println(
//      LOG.debug(
        document==null?
        "DOCUMENT IS NULL IN createPublishedAssessment(  Document)":
        "createPublishedAssessment(Document)");
//    }
//    AssessmentFacade assessment = null;
    AssessmentFacade assessment = new AssessmentFacade();

    try {
      // create the assessment, later we'll add tests of 2.0
      ExtractionHelper exHelper = new ExtractionHelper(QTIVersion.VERSION_1_2);
      exHelper.setOverridePath(xslPath);
      // we need to know who we are
      String me = "admin";//AgentFacade.getAgentString();
//      AssessmentService assessmentService = new AssessmentService();
//      ItemService itemService = new ItemService();
      Assessment assessmentXml = new Assessment(document);
      Map assessmentMap = exHelper.mapAssessment(assessmentXml);
      assessment = new AssessmentFacade();//exHelper.createAssessment(assessmentMap);

      // update the remaining assessment properties
      exHelper.updateAssessment(assessment, assessmentMap);

      // make sure required fields are set
      assessment.setCreatedBy(me);
      assessment.setCreatedDate(assessment.getCreatedDate());
      assessment.setLastModifiedBy(me);
      assessment.setLastModifiedDate(assessment.getCreatedDate());
      assessment.setTypeId(TypeIfc.QUIZ);
      assessment.setStatus(new Integer(1));

      // process each section and each item within each section
      List sectionList = exHelper.getSectionXmlList(assessmentXml);
//      LOG.debug("found: " + sectionList.size() + "sections");
      LOG.debug("sections=" + sectionList.size());

      for (int sec = 0; sec < sectionList.size(); sec++)// for each section...
      {
        Section sectionXml =(Section) sectionList.get(sec);
        Map sectionMap = exHelper.mapSection(sectionXml);
        LOG.debug("SECTION MAP=" + sectionMap);
        // create the assessment section
        SectionFacade section =
            new SectionFacade();
//            assessmentService.addSection("" + assessment.getAssessmentId());
        exHelper.updateSection(section, sectionMap);
        // make sure we are the creator
        LOG.debug("section " + section.getTitle() +
          "created by '" + me+ "'.");
        section.setCreatedBy(me);
        section.setCreatedDate(assessment.getCreatedDate());
        section.setLastModifiedBy(me);
        section.setLastModifiedDate(assessment.getCreatedDate());
        section.setTypeId(TypeIfc.DEFAULT_SECTION);
        section.setStatus(new Integer(1));
        // set the sequence
        section.setSequence(new Integer(sec + 1));
//        // add the section to the assessment
//        section.setAssessmentId(assessment.getAssessmentId());//many to one
//        section.setAssessment(assessment);
//        assessment.getSectionArray().add(section);// one to many

        List itemList = exHelper.getItemXmlList(sectionXml);
        for (int itm = 0; itm < itemList.size(); itm++)// for each item
        {
          LOG.debug("items=" + itemList.size());
          Item itemXml = (Item) itemList.get(itm);
          Map itemMap = exHelper.mapItem(itemXml);
          LOG.debug("ITEM MAP=" + itemMap);

          ItemFacade item = new ItemFacade();
          exHelper.updateItem(item, itemMap);
          // make sure required fields are set
          item.setCreatedBy(me);
          item.setCreatedDate(assessment.getCreatedDate());
          item.setLastModifiedBy(me);
          item.setLastModifiedDate(assessment.getCreatedDate());
          LOG.debug("ITEM TYPE IS: " +item.getTypeId());
          item.setStatus(ItemDataIfc.ACTIVE_STATUS);
          // assign the next sequence number
          item.setSequence(new Integer(itm + 1));
          // add item to section
          item.setSection(section);// one to many
          section.addItem(item);// many to one
//          itemService.saveItem(item);
          // debugging
//          Set metaSet = item.getItemMetaDataSet();
//          Iterator iter = metaSet.iterator();
//          if (LOG.isDebugEnabled())
//          {
//            while (iter.hasNext())
//            {
//              ItemMetaData meta = (ItemMetaData) iter.next();
//              LOG.debug("ITEM DEBUG meta " + meta.getLabel() +
//                "=" + meta.getEntry());
//            }
//          }
          LOG.debug("ITEM:  ans key" + item.getAnswerKey() );
          LOG.debug("ITEM:  correct feed" + item.getCorrectItemFeedback() );
          LOG.debug("ITEM:  incorrect feed " + item.getInCorrectItemFeedback() );
          LOG.debug("ITEM:  by " + item.getCreatedBy() );
          LOG.debug("ITEM:  date" + item.getCreatedDate() );
          LOG.debug("ITEM:  desc " + item.getDescription() );
          LOG.debug("ITEM:  duration" + item.getDuration() );
          LOG.debug("ITEM:  general feed " + item.getGeneralItemFeedback() );
          LOG.debug("ITEM:  incorrect " + item.getInCorrectItemFeedback() );
          LOG.debug("ITEM:  is true " + item.getIsTrue() );
          LOG.debug("ITEM DEBUG item text" + item.getText() );
          LOG.debug("ITEM:  item text" + item.getText() );
        }// ... end for each item
      }// ... end for each section

      LOG.debug("assessment created by '" + assessment.getCreatedBy() + "'.");
//      assessmentService.update(assessment);
      // debugging
      LOG.debug("ASSESSMENT:  meta " + assessment.getAssessmentMetaDataMap());
      LOG.debug("ASSESSMENT:  feed " + assessment.getAssessmentFeedback());
      LOG.debug("ASSESSMENT:  comments  " + assessment.getComments());
      LOG.debug("ASSESSMENT:  by " + assessment.getCreatedBy());
      LOG.debug("ASSESSMENT:  by date " + assessment.getCreatedDate());
      LOG.debug("ASSESSMENT:  desc" + assessment.getDescription());
      LOG.debug("ASSESSMENT:  disp " + assessment.getDisplayName());
      LOG.debug("ASSESSMENT:  last by " + assessment.getLastModifiedBy());
      LOG.debug("ASSESSMENT:  last date" + assessment.getLastModifiedDate());
      LOG.debug("ASSESSMENT:  mult " + assessment.getMultipartAllowed());
      LOG.debug("ASSESSMENT:  title " + assessment.getTitle());
      LOG.debug("ASSESSMENT DEBUG title " + assessment.getTitle());
//      assessmentService.saveAssessment(assessment);
    }
    catch(Exception e)
    {
      LOG.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return assessment;
  }

  /**
   * Version of method in AuthoringHelper, but with persistence disabled.
   * Import an item XML document in QTI format, extract & persist the data.
   * @param document the item XML document in QTI format
   * @return a persisted assessment
   */
  public ItemFacade createImportedItem(Document document)
  {
    LOG.debug(
        document==null?
        "DOCUMENT IS NULL IN createImportedItem(Document)":
        "createImportedItem(Document)");
    ItemFacade item = new ItemFacade();

    try
    {
      // create the item
      ExtractionHelper exHelper = new ExtractionHelper(QTIVersion.VERSION_1_2);
      exHelper.setOverridePath(xslPath);
      System.out.println("XSLT Path: " + exHelper.getTransformPath());
      Item itemXml = new Item(document, QTIVersion.VERSION_1_2);
      Map itemMap = exHelper.mapItem(itemXml);
//      LOG.debug("ITEM MAP=" + itemMap);
      exHelper.updateItem(item, itemMap);
      ItemService itemService = new ItemService();
      System.out.println("updating item");
//      itemService.saveItem(item);
    }
    catch(Exception e)
    {
//      LOG.error(e.getMessage(), e);
      e.printStackTrace();
    }

    return item;
  }

}