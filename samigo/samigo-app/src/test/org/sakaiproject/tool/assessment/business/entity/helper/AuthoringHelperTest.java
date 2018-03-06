/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.business.entity.helper;
import java.io.File;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.qti.asi.Assessment;
import org.sakaiproject.tool.assessment.qti.asi.Item;
import org.sakaiproject.tool.assessment.qti.asi.Section;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.ExtractionHelper;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.w3c.dom.Document;

/**
 * <p> Test program</p>
 * <p> </p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

 @Slf4j
 public class AuthoringHelperTest {

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
    //String sep = "\\";
    for (int i = 0; i < myDocs.length; i++) {
      String path = myPath + File.separator + myDocs[i];
      log.info("Testing XML file:" + path);
      Document document =
          XmlUtil.readDocument(myPath + File.separator + myDocs[i]);
      log.info("Created doc.");
      AssessmentFacade a = ah.createImportedAssessment(document);
      log.info("Created assessment title: " + a.getTitle());
      log.info("Created assessment comments: " + a.getComments());
      log.info("Created assessment desc: " + a.getDescription());
      log.info("Created assessment mod: " + a.getLastModifiedBy());
      log.info("Created assessment date: " + a.getLastModifiedDate());
    }
  }

  public static void testItems(String myPath, String[] myDocs) {
    AuthoringHelperTest ah = new AuthoringHelperTest();
    //String sep = "\\";
    for (int i = 0; i < myDocs.length; i++) {
      String path = myPath + File.separator + myDocs[i];
      log.info("Testing XML file:" + path);
      Document document =
          XmlUtil.readDocument(myPath + File.separator + myDocs[i]);
      if (document == null) log.info("DOCUMENT IS NULL");
      if (document != null) log.info("DOCUMENT EXISTS.");
      log.info("Created doc.");
      ItemFacade it = ah.createImportedItem(document);
      log.info("Created item: " + it.getItemTextArray());
    }
  }

  /**
   * copy of method in AuthoringHelper with persistence turned off.
   * Import an XML document in QTI format, extract and persist the data.
   * @param document the document
   * @return a persisted assessment
   */
  public AssessmentFacade createImportedAssessment(Document document) {
//    if(log.isDebugEnabled())
//    {
    log.info(
//      log.debug(
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
      assessment.setStatus(Integer.valueOf(1));

      // process each section and each item within each section
      List sectionList = exHelper.getSectionXmlList(assessmentXml);
//      log.debug("found: " + sectionList.size() + "sections");
      log.debug("sections=" + sectionList.size());

      for (int sec = 0; sec < sectionList.size(); sec++)// for each section...
      {
        Section sectionXml =(Section) sectionList.get(sec);
        Map sectionMap = exHelper.mapSection(sectionXml);
        log.debug("SECTION MAP=" + sectionMap);
        // create the assessment section
        SectionFacade section =
            new SectionFacade();
//            assessmentService.addSection("" + assessment.getAssessmentId());
        exHelper.updateSection(section, sectionMap);
        // make sure we are the creator
        log.debug("section " + section.getTitle() +
          "created by '" + me+ "'.");
        section.setCreatedBy(me);
        section.setCreatedDate(assessment.getCreatedDate());
        section.setLastModifiedBy(me);
        section.setLastModifiedDate(assessment.getCreatedDate());
        section.setTypeId(TypeIfc.DEFAULT_SECTION);
        section.setStatus(Integer.valueOf(1));
        // set the sequence
        section.setSequence(Integer.valueOf(sec + 1));
//        // add the section to the assessment
//        section.setAssessmentId(assessment.getAssessmentId());//many to one
//        section.setAssessment(assessment);
//        assessment.getSectionArray().add(section);// one to many

        List itemList = exHelper.getItemXmlList(sectionXml);
        for (int itm = 0; itm < itemList.size(); itm++)// for each item
        {
          log.debug("items=" + itemList.size());
          Item itemXml = (Item) itemList.get(itm);
          Map itemMap = exHelper.mapItem(itemXml);
          log.debug("ITEM MAP=" + itemMap);

          ItemFacade item = new ItemFacade();
          exHelper.updateItem(item, itemXml, itemMap);
          // make sure required fields are set
          item.setCreatedBy(me);
          item.setCreatedDate(assessment.getCreatedDate());
          item.setLastModifiedBy(me);
          item.setLastModifiedDate(assessment.getCreatedDate());
          log.debug("ITEM TYPE IS: " +item.getTypeId());
          item.setStatus(ItemDataIfc.ACTIVE_STATUS);
          // assign the next sequence number
          item.setSequence(Integer.valueOf(itm + 1));
          // add item to section
          item.setSection(section);// one to many
          section.addItem(item);// many to one
//          itemService.saveItem(item);
          // debugging
//          Set metaSet = item.getItemMetaDataSet();
//          Iterator iter = metaSet.iterator();
//          if (log.isDebugEnabled())
//          {
//            while (iter.hasNext())
//            {
//              ItemMetaData meta = (ItemMetaData) iter.next();
//              log.debug("ITEM DEBUG meta " + meta.getLabel() +
//                "=" + meta.getEntry());
//            }
//          }
          log.debug("ITEM:  ans key" + item.getAnswerKey() );
          log.debug("ITEM:  correct feed" + item.getCorrectItemFeedback() );
          log.debug("ITEM:  incorrect feed " + item.getInCorrectItemFeedback() );
          log.debug("ITEM:  by " + item.getCreatedBy() );
          log.debug("ITEM:  date" + item.getCreatedDate() );
          log.debug("ITEM:  desc " + item.getDescription() );
          log.debug("ITEM:  duration" + item.getDuration() );
          log.debug("ITEM:  general feed " + item.getGeneralItemFeedback() );
          log.debug("ITEM:  incorrect " + item.getInCorrectItemFeedback() );
          log.debug("ITEM:  is true " + item.getIsTrue() );
          log.debug("ITEM DEBUG item text" + item.getText() );
          log.debug("ITEM:  item text" + item.getText() );
        }// ... end for each item
      }// ... end for each section

      log.debug("assessment created by '" + assessment.getCreatedBy() + "'.");
//      assessmentService.update(assessment);
      // debugging
      log.debug("ASSESSMENT:  meta " + assessment.getAssessmentMetaDataMap());
      log.debug("ASSESSMENT:  feed " + assessment.getAssessmentFeedback());
      log.debug("ASSESSMENT:  comments  " + assessment.getComments());
      log.debug("ASSESSMENT:  by " + assessment.getCreatedBy());
      log.debug("ASSESSMENT:  by date " + assessment.getCreatedDate());
      log.debug("ASSESSMENT:  desc" + assessment.getDescription());
      log.debug("ASSESSMENT:  disp " + assessment.getDisplayName());
      log.debug("ASSESSMENT:  last by " + assessment.getLastModifiedBy());
      log.debug("ASSESSMENT:  last date" + assessment.getLastModifiedDate());
      log.debug("ASSESSMENT:  mult " + assessment.getMultipartAllowed());
      log.debug("ASSESSMENT:  title " + assessment.getTitle());
      log.debug("ASSESSMENT DEBUG title " + assessment.getTitle());
//      assessmentService.saveAssessment(assessment);
    }
    catch(RuntimeException e)
    {
      log.error(e.getMessage(), e);
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
    log.debug(
        document==null?
        "DOCUMENT IS NULL IN createImportedItem(Document)":
        "createImportedItem(Document)");
    ItemFacade item = new ItemFacade();

    try
    {
      // create the item
      ExtractionHelper exHelper = new ExtractionHelper(QTIVersion.VERSION_1_2);
      exHelper.setOverridePath(xslPath);
      log.info("XSLT Path: " + exHelper.getTransformPath());
      Item itemXml = new Item(document, QTIVersion.VERSION_1_2);
      Map itemMap = exHelper.mapItem(itemXml);
//      log.debug("ITEM MAP=" + itemMap);
      exHelper.updateItem(item, itemXml, itemMap);
      //ItemService itemService = new ItemService();
      log.info("updating item");
//      itemService.saveItem(item);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
    }

    return item;
  }

}
