/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.qti.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolItemData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolItemIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.data.model.Tree;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.integration.helper.integrated.AgentHelperImpl;
import org.sakaiproject.tool.assessment.qti.asi.Assessment;
import org.sakaiproject.tool.assessment.qti.asi.Item;
import org.sakaiproject.tool.assessment.qti.asi.Section;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.exception.RespondusMatchingException;
import org.sakaiproject.tool.assessment.qti.helper.assessment.AssessmentHelperIfc;
import org.sakaiproject.tool.assessment.qti.helper.item.ItemHelperIfc;
import org.sakaiproject.tool.assessment.qti.helper.section.SectionHelperIfc;
import org.sakaiproject.tool.assessment.qti.util.XmlStringBuffer;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @author Shastri, Rashmi <rshastri@iupui.edu>
 * @version $Id$
 */
@Slf4j
public class AuthoringHelper
{
//  private static final AuthoringXml ax = new AuthoringXml(QTIVersion.VERSION_1_2);
  private AuthoringXml ax;

  private int qtiVersion;
  private static final String VALIDATE_XSD_PATH ="xml/xsd/";


  private AuthoringHelper()
  {

  }

  /**
   * QTI versioned constructor
   * @param qtiVersion either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
   */
  public AuthoringHelper(int qtiVersion)
  {
    this.qtiVersion = qtiVersion;
    if (!QTIVersion.isValid(qtiVersion))
    {
      throw new IllegalArgumentException(
        "Version Codes supported: QTIVersion.VERSION_1_2, QTIVersion.VERSION_2_0");
    }
    ax = new AuthoringXml(qtiVersion);
  }

  /**
   * Get a published assessment in Document form.
   *
   * @param assessmentId the published assessment's Id
   * @return the Document with the published assessment data
   */
  public Document getAssessment(String assessmentId)
  {

    InputStream is =
      ax.getTemplateInputStream(ax.ASSESSMENT);

    return getAssessment(assessmentId, is);
  }

  /**
   * Get an assessment in Document form.
   *
   * @param assessmentId the assessment's Id
   * @param is a stream containing the unpopulated XML document
   * @return the Document with the published assessment data
   */
  public Document getAssessment(String assessmentId, InputStream is)
  {
    try
    {
      String authors;
      String objectives;
      String keywords;
      String rubrics;
      String bgColor;
      String bgImage;

      AssessmentService assessmentService = new AssessmentService();
      QTIHelperFactory factory = new QTIHelperFactory();

      AssessmentFacade assessment =
        assessmentService.getAssessment(assessmentId);
      // convert assessment to document
      AssessmentHelperIfc assessmentHelper =
        factory.getAssessmentHelperInstance(this.qtiVersion);
      Assessment assessmentXml = assessmentHelper.readXMLDocument(is);
      assessmentXml.setIdent(assessmentId);
      assessmentXml.setTitle(FormattedText.convertFormattedTextToPlaintext(assessment.getTitle()));
      assessmentHelper.setDescriptiveText(assessment.getDescription(),
                                          assessmentXml);

      authors =
        assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.AUTHORS);
      objectives = assessment.getAssessmentMetaDataByLabel(
        AssessmentMetaDataIfc.OBJECTIVES);
      keywords = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
        KEYWORDS);
      rubrics = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
        RUBRICS);
      bgColor = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
        BGCOLOR);
      bgImage = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.
        BGIMAGE);

      if (authors != null)
      {
        assessmentXml.setFieldentry("AUTHORS", authors);
      }
      else
      {
    	AgentHelperImpl helper = new AgentHelperImpl();
        String createdBy = assessment.getCreatedBy();
        String eid = helper.getEidById(createdBy);
        if (eid != null)
        {
          assessmentXml.setFieldentry("AUTHORS", eid);
        }
        else
        {
          log.debug("\n\nNO AUTHORS");
        }
      }

      if (objectives != null)
      {
        assessmentXml.setFieldentry("ASSESSMENT_OBJECTIVES", objectives);
      }
      if (keywords != null)
      {
        assessmentXml.setFieldentry("ASSESSMENT_KEYWORDS", keywords);
      }
      if (rubrics != null)
      {
        assessmentXml.setFieldentry("ASSESSMENT_RUBRICS", rubrics);
      }
      if (bgColor != null)
      {
        assessmentXml.setFieldentry("BGCOLOR", bgColor);
      }
      if (bgImage != null)
      {
        assessmentXml.setFieldentry("BGIMG", bgImage);
      }

      // fieldentry properties
      EvaluationModelIfc evaluationModel = assessment.getEvaluationModel();
      if (evaluationModel != null)
      {
        assessmentHelper.updateEvaluationModel(assessmentXml,
                                               evaluationModel);
      }
      AssessmentFeedbackIfc assessmentFeedback = assessment.
        getAssessmentFeedback();
      if (assessmentFeedback != null)
      {
        assessmentHelper.updateFeedbackModel(assessmentXml, assessmentFeedback);
      }
      AssessmentAccessControlIfc assessmentAccessControl = assessment.
        getAssessmentAccessControl();
      if (assessmentAccessControl != null)
      {
        assessmentHelper.updateAccessControl(assessmentXml,
                                             assessmentAccessControl);
      }
      
      // Respondus Locked Browser
      // To-do: To retain the value, need to re-organize SamigoApiFactory to samigo-api. 
      /*
      if (assessment.getAssessmentMetaDataByLabel(SecureDeliveryServiceAPI.MODULE_KEY) == null ||
    		  SecureDeliveryServiceAPI.NONE_ID.equals(assessment.getAssessmentMetaDataByLabel(SecureDeliveryServiceAPI.MODULE_KEY))) {

    	  assessmentXml.setFieldentry("REQUIRE_LOCKED_BROWSER", "True");

    	  if (assessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.EXITPWD_KEY ) != null)
    	  {
    		  SecureDeliveryServiceAPI secureDeliveryService = SamigoApiFactory.getInstance().getSecureDeliveryServiceAPI();
    		  String exitPassword = secureDeliveryService.decryptPassword((String) assessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.MODULE_KEY ), 
    				  (String) assessment.getAssessmentMetaDataByLabel( SecureDeliveryServiceAPI.EXITPWD_KEY ) );
    		  assessmentXml.setFieldentry("EXIT_PASSWARD", exitPassword);
    	  }
      }
      */
      
      Set securedIPAddressSet = (Set) assessment.getSecuredIPAddressSet();
      if (securedIPAddressSet != null)
      {
        assessmentHelper.updateIPAddressSet(assessmentXml,
                                             securedIPAddressSet);
      }
      
      Set attachmentSet = (Set) assessment.getAssessmentAttachmentSet();
      
   	  if (attachmentSet != null && attachmentSet.size() != 0)    	  
      {
        assessmentHelper.updateAttachmentSet(assessmentXml, attachmentSet);
      }

      assessmentHelper.updateMetaData(assessmentXml, assessment);

      // sections
      factory = new QTIHelperFactory();
      SectionHelperIfc sectionHelper =
        factory.getSectionHelperInstance(this.qtiVersion);
      List sectionList = assessment.getSectionArraySorted();
      for (int i = 0; i < sectionList.size(); i++)
      {
        SectionDataIfc section = (SectionDataIfc) sectionList.get(i);
        InputStream sis =
          ax.getTemplateInputStream(ax.SECTION);
        Section sectionXml = sectionHelper.readXMLDocument(sis);
        sectionXml.update(section);
        addSection(assessmentXml, sectionXml);
      }

      return assessmentXml.getDocument();
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * Get an object bank of items in Document form.
   *
   * @param itemIds array of the the item ids
   * @return the Document with the item bank data
   */
  public Document getItemBank(String[] itemIds)
  {
    Document objectBank = XmlUtil.createDocument();

    Element root = objectBank.createElement("questestinterop");
    Element ob = objectBank.createElement("objectbank");
    String objectBankIdent = "object" + Math.random();
    ob.setAttribute("ident", objectBankIdent);
    for (int i = 0; i < itemIds.length; i++)
    {
      Document itemDoc = getItem(itemIds[i]);
      Element itemElement = itemDoc.getDocumentElement();
      Node itemImport = objectBank.importNode(itemElement, true);
      ob.appendChild(itemImport);
    }
    root.appendChild(ob);
    objectBank.appendChild(root);

    return objectBank;
  }

  /**
   * Get an object bank of assessments (asi) in Document form.
   *
   * @param assessmentIds array of the the assessment ids
   * @return the Document with the item bank data
   */
  public Document getAssessmentBank(String[] assessmentIds)
  {
    Document objectBank = XmlUtil.createDocument();

    Element root = objectBank.createElement("questestinterop");
    Element ob = objectBank.createElement("objectbank");
    String objectBankIdent = "object" + Math.random();
    ob.setAttribute("ident", objectBankIdent);
    for (int i = 0; i < assessmentIds.length; i++)
    {
      Document itemDoc = getAssessment(assessmentIds[i]);
      Element itemElement = itemDoc.getDocumentElement();
      Node itemImport = objectBank.importNode(itemElement, true);
      ob.appendChild(itemImport);
    }
    root.appendChild(ob);
    objectBank.appendChild(root);

    return objectBank;
  }

  /**
   * Get an item in Document form.
   * @param itemId the item id
   * @return the Document with the item data
   */
  public Document getItem(String itemId)
  {
    Item itemXml = new Item(this.qtiVersion);
    try
    {
      ItemService itemService = new ItemService();
      QTIHelperFactory factory = new QTIHelperFactory();
      ItemHelperIfc itemHelper =
        factory.getItemHelperInstance(this.qtiVersion);
      ItemDataIfc item = itemService.getItem(itemId);
      //TypeIfc type = item.getType();
      Long type = item.getTypeId();

      if ( (TypeIfc.MULTIPLE_CHOICE_SURVEY).equals(type))

      {
        String scale = item.getItemMetaDataByLabel(ItemMetaData.SCALENAME);
        itemXml = itemHelper.readTypeSurveyItem(scale);
      }
      else
      {
        itemXml = itemHelper.readTypeXMLItem(type);
      }
      itemXml.setIdent(item.getItemIdString());
      itemXml.update(item);
      return itemXml.getDocument();

    }
    catch (Exception ex)
    {
      log.error(ex.getMessage(), ex);
      return null;
    }
  }

  /**
   *
   * @param assessmentXml
   * @param sectionXml
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  private void addSection(Assessment assessmentXml,
                          Section sectionXml) throws IOException, SAXException,
    ParserConfigurationException
  {
    ax.addElement(assessmentXml.getDocument(), "questestinterop/assessment",
                  sectionXml.getDocument().getDocumentElement());
  }


  /**
   * Get an InputStream to an unpopulated section XML from file system.
   * @return InputStream to an unpopulated section XML
   */
  public InputStream getBlankSectionTemplateFileStream()
  {
    InputStream is = ax.getTemplateInputStream(ax.SECTION);
    return is;
  }

  /**
   * Pull apart object bank into multiple assessments
   * @param objectBank
   * @return an array of AssesmentFacades
   */
  public AssessmentFacade[] createMultipleImportedAssessments(Document
    objectBank)
  {
    Document[] docs = getDocumentsFromObjectBankDoc(objectBank, "assessment");
    return createMultipleImportedAssessments(docs);
  }

  /**
   * Pull apart object bank based on elementName
   * @param objectBank
   * @param elementName e.g. "assessment", "item", etc.
   * @return
   */
  private Document[] getDocumentsFromObjectBankDoc(Document objectBank,
    String elementName)
  {
    Document[] documents = null;
    // Get the matching elements
    NodeList nodeList = objectBank.getElementsByTagName("//" + elementName); 
    
    int numDocs = nodeList.getLength();
    if (numDocs == 0)
    {
      return null;
    }

    documents = new Document[numDocs];
    for (int i = 0; i < numDocs; i++)
    {
      Node node = (Node) nodeList.item(i);
      Document objectDoc = XmlUtil.createDocument();
      Node importNode = objectDoc.importNode(node, true);
      objectDoc.appendChild(importNode);
      documents[i] = objectDoc;
    }
    
    return documents;
  }

  /**
   * Import multiple assessment documents in QTI format, extract & persist.
   * @param objectBank
   * @return an array of AssesmentFacades
   */
  public AssessmentFacade[] createMultipleImportedAssessments(Document[]
    documents)
  {
    AssessmentFacade[] assessments = new AssessmentFacade[documents.length];
    for (int i = 0; i < documents.length; i++)
    {
      assessments[i] = createImportedAssessment(documents[i]);
    }
    return assessments;
  }

  /**
     * Import an assessment XML document in QTI format, extract & persist the data.
   * @param document the assessment XML document in QTI format
   * @return a persisted assessment
   */
  public AssessmentFacade createImportedAssessment(Document document)
  {
	  return createImportedAssessment(document, null);
  }

  public AssessmentFacade createImportedAssessment(Document document, String unzipLocation)
  {
	  return createImportedAssessment(document, unzipLocation, false, null);
  }
  
  public AssessmentFacade createImportedAssessment(Document document, String unzipLocation, boolean isRespondus, List failedMatchingQuestions)
  {
    return createImportedAssessment(document, unzipLocation, null, isRespondus, failedMatchingQuestions, null);
  }

	  /**
	     * Import an assessment XML document in QTI format, extract & persist the data.
	   * @param document the assessment XML document in QTI format
	   * @return a persisted assessment
	   */
  public AssessmentFacade createImportedAssessment(Document document, String unzipLocation, String templateId, String siteId)
  {
	  return createImportedAssessment(document, unzipLocation, templateId, false, null, siteId);
  }

  private boolean validateImportXml(Document doc) throws SAXException, IOException{
      // 1. Lookup a factory for the W3C XML Schema language
      SchemaFactory factory =
          SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

      // 2. Compile the schema.
      // Here the schema is loaded from a java.io.File, but you could use
      // a java.net.URL or a javax.xml.transform.Source instead.
      String schemaFile = VALIDATE_XSD_PATH + "qtiv1p2.xsd";
      log.debug("schemaFile = " + schemaFile);
      Schema schema = factory.newSchema(new StreamSource(AuthoringHelper.class.getClassLoader().getResourceAsStream(schemaFile)));

      // 3. Get a validator from the schema.
      Validator validator = schema.newValidator();

      // 4. Parse the document you want to check.
      Source source = new DOMSource(doc);

      // 5. Check the document
      try {
          validator.validate(source);
          log.debug("The xml is valid.");
          return true;
      }
      catch (SAXException ex) {
          log.debug("The xml is not valid QTI format.", ex);
      }
      return false;
  }

  protected String getAgentString(){
	  return AgentFacade.getAgentString();
  }

  public AssessmentFacade createImportedAssessment(Document document, String unzipLocation, String templateId, boolean isRespondus, List failedMatchingQuestions, String siteId)
  {
	AssessmentFacade assessment = null;

    AssessmentService assessmentService = new AssessmentService();
    try
    {
      // we need to know who we are
      String me = getAgentString();

      // create the assessment
      ExtractionHelper exHelper = new ExtractionHelper(this.qtiVersion);
      exHelper.setUnzipLocation(unzipLocation);
      ItemService itemService = new ItemService();
      // we need to remove a default namespace if present
      Document removeNamespace = exHelper.getTransformDocument(exHelper.REMOVE_NAMESPACE_TRANSFORM);
      Document flatNamespaceXml = XmlUtil.transformDocument(document, removeNamespace);
	
      // validate xml

      // validate xml first
      boolean success = validateImportXml(flatNamespaceXml);
      if (!success) {
      	throw( new RuntimeException("Invalid QTI XML format."));
      }
      // else continue;
 
      Assessment assessmentXml = new Assessment(flatNamespaceXml);
      Map assessmentMap = exHelper.mapAssessment(assessmentXml, isRespondus);
      String description = (String) assessmentMap.get("description");
      String title = TextFormat.convertPlaintextToFormattedTextNoHighUnicode((String) assessmentMap.get("title"));
      assessment = assessmentService.createAssessmentWithoutDefaultSection(
        title, exHelper.makeFCKAttachment(description), null, templateId, siteId);

      // now make sure we have a unique name for the assessment
      String baseId = assessment.getAssessmentBaseId().toString();
      boolean notUnique =
        !assessmentService.assessmentTitleIsUnique(baseId , title, false);

      if (notUnique)
      {
        synchronized (title)
        {
          log.debug("Assessment "+ title + " is not unique.");
          int count = 0; // alternate exit condition

          while (notUnique)
          {
            title = exHelper.renameDuplicate(title);
            log.debug("renameDuplicate(title): " + title);
            assessment.setTitle(title);
            notUnique =
               !assessmentService.assessmentTitleIsUnique(baseId , title, false);
            if (count++ > 99) break;// exit condition in case bug is introduced
          }
        }
      }

      // update the remaining assessment properties
      if (isRespondus) {
    	  AssessmentAccessControl control =
    		  (AssessmentAccessControl)assessment.getAssessmentAccessControl();
    	  if (control == null){
    		  control = new AssessmentAccessControl();
    		  control.setAssessmentBase(assessment.getData());
    	  }

    	  EvaluationModel evaluationModel =
    		  (EvaluationModel) assessment.getEvaluationModel();
    	  if (evaluationModel == null){
    		  evaluationModel = new EvaluationModel();
    		  evaluationModel.setAssessmentBase(assessment.getData());
    	  }

    	  AssessmentFeedback feedback =
    		  (AssessmentFeedback) assessment.getAssessmentFeedback();
    	  if (feedback == null){
    		  feedback = new AssessmentFeedback();
    		  feedback.setAssessmentBase(assessment.getData());
    	  }
      }
      else {
    	  exHelper.updateAssessment(assessment, assessmentMap);
      }
      // make sure required fields are set
      assessment.setCreatedBy(me);
      assessment.setCreatedDate(assessment.getCreatedDate());
      assessment.setLastModifiedBy(me);
      assessment.setLastModifiedDate(assessment.getCreatedDate());
      assessment.setTypeId(TypeIfc.QUIZ);
      assessment.setStatus( Integer.valueOf(1));
      // set comments
      String comments = (String) assessmentMap.get("comments");
      assessment.setComments(comments);


      // process each section and each item within each section
      List sectionList = exHelper.getSectionXmlList(assessmentXml);
      int sectionListSize = sectionList.size();
      log.debug("sections=" + sectionListSize);

      for (int sec = 0; sec < sectionListSize; sec++) // for each section...
      {
        Section sectionXml = (Section) sectionList.get(sec);
        Map sectionMap = exHelper.mapSection(sectionXml, isRespondus);
        // create the assessment section
        SectionFacade section =
          assessmentService.addSection("" + assessment.getAssessmentId());
        exHelper.updateSection(section, sectionMap);
        // make sure we are the creator
        section.setCreatedBy(me);
        section.setCreatedDate(assessment.getCreatedDate());
        section.setLastModifiedBy(me);
        section.setLastModifiedDate(assessment.getCreatedDate());
        section.setTypeId(TypeIfc.DEFAULT_SECTION);
        section.setStatus( Integer.valueOf(1));
        section.setSequence(Integer.valueOf(sec + 1));
        
        List itemList = exHelper.getItemXmlList(sectionXml);
        for (int itm = 0; itm < itemList.size(); itm++) // for each item
        {
          log.debug("items=" + itemList.size());
          Item itemXml = (Item) itemList.get(itm);

          ItemFacade item = new ItemFacade();
          try {
        	  exHelper.updateItem(item, itemXml, isRespondus);
          }
          catch (RespondusMatchingException rme) {
        	  if (failedMatchingQuestions != null) {
        		  failedMatchingQuestions.add(itm + 1);
        	  }
          }
          // make sure required fields are set
          item.setCreatedBy(me);
          item.setCreatedDate(assessment.getCreatedDate());
          item.setLastModifiedBy(me);
          item.setLastModifiedDate(assessment.getCreatedDate());
          item.setStatus(ItemDataIfc.ACTIVE_STATUS);
          // assign the next sequence number
          item.setSequence(  Integer.valueOf(itm + 1));
          // add item to section
          item.setSection(section); // one to many
          // update metadata with PARTID
          item.addItemMetaData(ItemMetaData.PARTID, section.getSectionId().toString());
           
          // Item Attachment
          exHelper.makeItemAttachmentSet(item);
          
          section.addItem(item); // many to one
          itemService.saveItem(item);
          EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/saved itemId=" + item.getItemId().toString(), true));
        } // ... end for each item
        
        // Section Attachment
      	exHelper.makeSectionAttachmentSet(section, sectionMap);

        assessmentService.saveOrUpdateSection(section);
      } // ... end for each section

      // and add ip address restriction, if any
      String allowIp = assessment.getAssessmentMetaDataByLabel("ALLOW_IP");

      if (allowIp !=null && !allowIp.trim().equalsIgnoreCase("null"))
      {
        exHelper.makeSecuredIPAddressSet(assessment, allowIp);
		//Clean unnecesary ip metadata that fail when an assessment  
		//with more than 256 charts in the field ALLOWED IPS is imported
		Set assessmentSet = assessment.getAssessmentMetaDataSet();
		Iterator assessmentSetIterator = assessmentSet.iterator();
		while(assessmentSetIterator.hasNext()){
			AssessmentMetaData assessmentMetaData = (AssessmentMetaData)assessmentSetIterator.next();
			if("ALLOW_IP".equals(assessmentMetaData.getLabel())) {
				assessmentSetIterator.remove();
				break;
			}
		}
      }
      
      // Assessment Attachment
      exHelper.makeAssessmentAttachmentSet(assessment);

      String siteTitle = SiteService.getSite(ToolManager.getCurrentPlacement().getContext()).getTitle();
      if(siteTitle != null && !siteTitle.equals(assessment.getAssessmentAccessControl().getReleaseTo())){
          assessment.getAssessmentAccessControl().setReleaseTo(siteTitle);
      }

      assessmentService.saveAssessment(assessment);
      return assessment;
    }
    catch (RespondusMatchingException rme)
    {
      log.error(rme.getMessage(), rme);
      assessmentService.removeAssessment(assessment.getAssessmentId().toString());
      throw new RespondusMatchingException(rme);
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
      assessmentService.removeAssessment(assessment.getAssessmentId().toString());
      throw new RuntimeException(e);
    }
  }
  
  /**
  * Import an assessment XML document in QTI format, extract & persist the data.
  * import process assumes assessment structure, not objectbank or itembank
  * @param document the assessment XML document in QTI format
  * @return a persisted assessment
  */
   public QuestionPoolFacade createImportedQuestionPool(Document document) 
   {
 	QuestionPoolFacade questionpool = new QuestionPoolFacade();
 	QuestionPoolService questionPoolService = new QuestionPoolService();
 	
 	try
 	{
       // identify user to assign as question pool owner
       String me = AgentFacade.getAgentString();

 	  // create the questionpool as an assessment
 	  ExtractionHelper exHelper = new ExtractionHelper(this.qtiVersion);
 	  ItemService itemService = new ItemService();
 	  // we need to remove a default namespace if present
  	  Document removeNamespace = exHelper.getTransformDocument(exHelper.REMOVE_NAMESPACE_TRANSFORM);
  	  Document flatNamespaceXml = XmlUtil.transformDocument(document, removeNamespace);
  	  Assessment assessmentXml = new Assessment(flatNamespaceXml);

  	  // validate xml first
  	  boolean success = validateImportXml(flatNamespaceXml);
  	  if (!success) {
  		  throw( new RuntimeException("Invalid QTI XML format."));
  	  }
  	  // else continue;

 	  Map assessmentMap = exHelper.mapAssessment(assessmentXml);
 	  String title = (String) assessmentMap.get("title");
 	  
 	  // save questionpool with required info only at this point
 	  questionpool.setOwnerId(me);
 	  questionpool.setTitle(title);
 	  questionpool.setLastModifiedById(me);
      questionpool.setAccessTypeId(QuestionPoolFacade.ACCESS_DENIED); // set as default
 	  questionpool = questionPoolService.savePool(questionpool);
      // update the remaining questionpool properties
      exHelper.updateQuestionPool(questionpool, assessmentMap);
 	  
 	  // now make sure we have a unique name for the question pool
      String baseId = questionpool.getQuestionPoolId().toString();
 	  boolean isUnique=questionPoolService.poolIsUnique(baseId,title,"0", me);
   
 	  // if the title is not unique, increment with a number per renameDuplicate()
 	  if (!isUnique) {
 		  synchronized (title)
 	        {
 	          log.debug("Questionpool "+ title + " is not unique.");
 	          int count = 0; // alternate exit condition

 	          while (!isUnique)
 	          {
 	        	title = exHelper.renameDuplicate(title);
 	            log.debug("renameDuplicate(title): " + title);
 	            questionpool.setTitle(title);
 	            //recheck to confirm that new title is not a dplicate too
 	            isUnique = questionPoolService.poolIsUnique(baseId,title,"0", me);	      	  
 	            if (count++ > 99) break;// exit condition in case bug is introduced
 	          }
 	      }		  
 	  }
 	  
 	  
      // process each section and each item within assessment each section
      List sectionList = exHelper.getSectionXmlList(assessmentXml);
      int sectionListSize = sectionList.size();
      log.debug("sections=" + sectionListSize);
             
      // initialize setQuestionPoolItems so items can be added
      Set itemSet = new HashSet();
      questionpool.setQuestionPoolItems(itemSet);
       
      for (int sec = 0; sec < sectionListSize; sec++) {
           Section sectionXml = (Section) sectionList.get(sec);
           Map sectionMap = exHelper.mapSection(sectionXml);
           // for single section, do not create subpool

           List itemList = exHelper.getItemXmlList(sectionXml);
           for (int itm = 0; itm < itemList.size(); itm++) // for each item
           {
               log.debug("items=" + itemList.size());
               Item itemXml = (Item) itemList.get(itm);

               ItemFacade item = new ItemFacade();
               exHelper.updateItem(item, itemXml);
               // make sure required fields are set
               item.setCreatedBy(me);
               item.setCreatedDate(questionpool.getLastModified());
               item.setLastModifiedBy(me);
               item.setLastModifiedDate(questionpool.getLastModified());
               item.setStatus(ItemDataIfc.ACTIVE_STATUS);
               itemService.saveItem(item);
               EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/saved itemId=" + item.getItemId().toString(), true));
               
               QuestionPoolItemData questionPoolItem = new QuestionPoolItemData();
               questionPoolItem.setQuestionPoolId(questionpool.getQuestionPoolId());
               questionPoolItem.setItemId(item.getItemId());         
               questionpool.addQuestionPoolItem((QuestionPoolItemIfc) questionPoolItem);
               
             } // ... end for each item
      }
      // need error message if more than one section, for now
       
      // update the questionpoool with all sections and items
      questionPoolService.savePool(questionpool);
       
 	  return questionpool;		
 	}
 	catch (Exception e)
 	{
 		log.error(e.getMessage(), e);
 		questionPoolService.deletePool(questionpool.getQuestionPoolId(), AgentFacade.getAgentString(), null);		
 		throw new RuntimeException(e);		
 	}
   }
 

  /**
   * @deprecated
   * Import an item XML document in QTI format, extract & persist the data.
   * @param document the item XML document in QTI format
   * @return a persisted item
   */
  public ItemFacade createImportedItem(Document document)
  {
    ItemFacade item = new ItemFacade();

    try
    {
      // create the item
      ExtractionHelper exHelper = new ExtractionHelper(this.qtiVersion);
      Item itemXml = new Item(document, QTIVersion.VERSION_1_2);
      exHelper.updateItem(item, itemXml);
      ItemService itemService = new ItemService();
      itemService.saveItem(item);
    }
    catch (Exception e)
    {
      log.error(e.getMessage(), e);
    }

    return item;
  }

  /**
   * Create an XmlStringBuffer (base class for A,S,I XML classes)
   *
   * @param inputStream the input stram
   *
   * @return an XmlStringBuffer (this is the base class for A,S,I XML classes)
   */
  public XmlStringBuffer readXMLDocument(InputStream inputStream)
  {
    Document document = null;
    DocumentBuilderFactory builderFactory =
      DocumentBuilderFactory.newInstance();
//    builderFactory.setNamespaceAware(true);
    try
    {
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      document = documentBuilder.parse(inputStream);
    }
    catch (ParserConfigurationException e)
    {
      log.error(e.getMessage(), e);
    }
    catch (SAXException e)
    {
      log.error(e.getMessage(), e);
    }
    catch (IOException e)
    {
      log.error(e.getMessage(), e);
    }

    return new XmlStringBuffer(document);
  }

  /**
   * Helper method.
   * @param inputStr
   * @param delimiter
   * @return
   */
  public ArrayList changeDelimitedStringtoArray(String inputStr,
                                                String delimiter)
  {
    ArrayList selectedList = new ArrayList();
    if (inputStr != null && inputStr.trim().length() > 0)
    {
      StringTokenizer st = new StringTokenizer(inputStr, delimiter);
      if (st != null)
      {

        while (st.hasMoreTokens())
        {
          selectedList.add(st.nextToken());
        }
      }
      else
      {
        selectedList.add(inputStr);
      }
    }
    return selectedList;
  }

  public int getQtiVersion()
  {
    return qtiVersion;
  }

  public void setQtiVersion(int qtiVersion)
  {
    this.qtiVersion = qtiVersion;
  }
}
