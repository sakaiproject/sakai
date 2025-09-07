/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
 * limitations under the Licensee.
 */
package org.sakaiproject.lessonbuildertool.cc;

/***********
 * This code is based on a reference implementation done for the IMS Consortium.
 * The copyright notice for that implementation is included below. 
 * All modifications are covered by the following copyright notice.
 *
 * Copyright (c) 2011 Rutgers, the State University of New Jersey
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
 */

/**********************************************************************************
 * $URL: http://ims-dev.googlecode.com/svn/trunk/cc/IMS_CCParser_v1p0/src/main/java/org/imsglobal/cc/PrintHandler.java $
 * $Id: PrintHandler.java 227 2011-01-08 18:26:55Z drchuck $
 **********************************************************************************
 *
 * Copyright (c) 2010 IMS Global Learning Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. 
 *
 **********************************************************************************/

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filters;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathFactory;

import org.jsoup.Jsoup;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.AssignmentInterface;
import org.sakaiproject.lessonbuildertool.service.BltiInterface;
import org.sakaiproject.lessonbuildertool.service.ForumInterface;
import org.sakaiproject.lessonbuildertool.service.GroupPermissionsService;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.lessonbuildertool.service.QuizEntity;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;

/* PJN NOTE:
 * This class is an example of what an implementer might want to do as regards overloading DefaultHandler.
 * In this case, messages are written to the screen. If a method in default handler is not overridden, then it does
 * nothing.
 */
@Slf4j
public class PrintHandler extends DefaultHandler implements AssessmentHandler, DiscussionHandler, AuthorizationHandler,
                                       MetadataHandler, LearningApplicationResourceHandler, QuestionBankHandler,
                                       WebContentHandler, WebLinkHandler{
  private static final String HREF="href";
  private static final String TYPE="type";
  private static final String FILE="file";
  private static final String XML=".xml";
  private static final String URL="url";
  private static final String TITLE="title";
  private static final String ID="id";
  private static final String TEXT="text";
  private static final String TEXTTYPE="texttype";
  private static final String TEXTHTML="text/html";
  private static final String DESCRIPTION="description";
  private static final String GENERAL="general";
  private static final String STRING="string";
  private static final String LANGSTRING="langstring";
  private static final String ATTACHMENT="attachment";
  private static final String ATTACHMENTS="attachments";
  private static final String INTENDEDUSE="intendeduse";
  private static final String VARIANT="variant";
  private static final String IDENTIFIERREF="identifierref";
  private static final String IDENTIFIER="identifier";
  private static final String RESOURCES="resources";
  private static final String RESOURCE="resource";
    
  private static final String CC_ITEM_TITLE="title";
  private static final String CC_ITEM_METADATA="metadata";
  private static final String LOM_LOM="lom";
  private static final String LOM_GENERAL="general";
  private static final String LOM_STRUCTURE="structure";
  private static final String LOM_SOURCE="source";
  private static final String LOM_VALUE="value";
  private static final String CC_WEBCONTENT="webcontent";
  private static final String LAR="learning-application-resource";
  private static final String WEBLINK="webLink";
  private static final String TOPIC="topic";
  private static final String QUESTIONS="questestinterop";
  private static final String ASSESSMENT="assessment";
  private static final String ASSIGNMENT="assignment";
  private static final String CANVAS_ASSIGNMENT="canvas_assignment";
  private static final String QUESTION_BANK="question-bank";
  private static final String CART_LTI_LINK="cartridge_basiclti_link";
  private static final String BLTI="basiclti";
  private static final String UNKNOWN="unknown";
  private static final int MAX_ATTEMPTS = 100;
  private static final String CANVAS_MODULE_META_NAMESPACE = "http://canvas.instructure.com/xsd/cccv1p0";
  private static final String CANVAS_MODULE_NEW_TAB = "new_tab";
  private static final String CANVAS_MODULE_NEW_TAB_FALSE = "false";

  private List<SimplePage> pages = new ArrayList<SimplePage>();
    // list parallel to pages containing sequence of last item on the page
  private List<Integer> sequences= new ArrayList<Integer>();
  CartridgeLoader utils = null;
  SimplePageToolDao simplePageToolDao = null;

  private String title = null;
  private String description = null;
  private String baseName = null;
  private String baseUrl = null;
  private String siteId = null;
  private LessonEntity quiztool = null;
  private LessonEntity topictool = null;
  private LessonEntity bltitool = null;
  private LessonEntity assigntool = null;
  private SyllabusManager syllabusManager = null;
  private Set<String>roles = null;
  boolean usesRole = false;
  boolean usesPatternMatch = false;
  boolean usesCurriculum = false;
  boolean importtop = false;
  Integer assignmentNumber = 1;
  Element manifestXml = null;
  Element canvasModuleMeta = null;
  boolean forceInline;

  // this is the CC file name for all files added
  private Set<String> filesAdded = new HashSet<String>();
  // Track files that are part of Canvas entities and should not be imported as resources
  private Set<String> canvasEntityFiles = new HashSet<String>();
  // This keeps track of what files are added to what (possibly truncated) name, this is pre-populated
  private Map<String,String> fileNames = new HashMap<String,String>();
  // this is the CC file name (of the XML file) -> Sakaiid for non-file items
  private Map<String,String> itemsAdded = new HashMap<String,String>();
  private Map<String, Map<String, String>> itemsMetaDataAdded = new HashMap<>();
  private Map<String,String> assignsAdded = new HashMap<String,String>();
  private Set<String> badTypes = new HashSet<String>();
  static private Map<String, String> badTypeNames = null;

  private Map<String,String> getBadTypeNames() {
      Map<String,String> badNames = new HashMap<String, String>();
      
      badNames.put("imsapip_zipv1p0", simplePageBean.getMessageLocator().getMessage("simplepage.cc_apip"));
      badNames.put("imsiwb_iwbv1p0", simplePageBean.getMessageLocator().getMessage("simplepage.cc_iwb"));
      badNames.put("idpfepub_epubv3p0", simplePageBean.getMessageLocator().getMessage("simplepage.cc_epub3"));
      badNames.put("assignment_xmlv1p0", simplePageBean.getMessageLocator().getMessage("simplepage.cc_ext_assignment"));

      return badNames;
  }		      

  public PrintHandler(SimplePageBean bean, CartridgeLoader utils, SimplePageToolDao dao, LessonEntity q, LessonEntity l, LessonEntity b, LessonEntity a, boolean itop) {
      super();
      this.utils = utils;
      this.simplePageBean = bean;
      this.simplePageToolDao = dao;
      this.siteId = bean.getCurrentSiteId();
      if (q != null) {
    	  this.quiztool = q;
    	  this.quiztool.setSimplePageBean(bean);
      }
      if (l != null) {
    	  this.topictool = l;
    	  this.topictool.setSimplePageBean(bean);
      }
      if (b != null) {
    	  this.bltitool = b;
    	  this.bltitool.setSimplePageBean(bean);
      }
      if (a != null) {
    	  this.assigntool = a;
    	  this.assigntool.setSimplePageBean(bean);
      }
      // Initialize Syllabus and Forum services
      this.syllabusManager = (SyllabusManager) ComponentManager.get("org.sakaiproject.api.app.syllabus.SyllabusManager");
      this.importtop = itop;
	  this.forceInline = ServerConfigurationService.getBoolean("lessonbuilder.cc.import.forceinline", false);
  }

  public void setAssessmentDetails(String the_ident, String the_title) {
      if (log.isDebugEnabled())
	  log.debug("assessment ident: "+the_ident +" title: "+the_title);
  }

  public void endCCFolder() {
      if (log.isDebugEnabled())
	  log.debug("cc folder ends");
      int top = pages.size()-1;
      sequences.remove(top);
      pages.remove(top);
  }

  public void endCCItem() {
      if (log.isDebugEnabled())
	  log.debug("cc item ends");
  }

  public void startCCFolder(Element folder) {
      String title = this.title;
      if (folder != null)
	  title = folder.getChildText(TITLE, ns.getNs());

      // add top level pages to left margin
      SimplePage page = null;
      if (pages.size() == 0) {
	  page = simplePageBean.addPage(title, false);  // add new top level page
	  if (description != null && !description.trim().equals("")) {
	      SimplePageItem item = simplePageToolDao.makeItem(page.getPageId(), 1, SimplePageItem.TEXT, "", "");
	      item.setHtml(ComponentManager.get(FormattedText.class).escapeHtml(description));
	      simplePageBean.saveItem(item);
	      sequences.add(2);
	  } else
	      sequences.add(1);
      } else {
	  // we're adding a level. We're at sequence 1 in the new top level,
	  // but we continue the current sequence count at the old top level, which
	  // is where the new folder is added
	  page = simplePageToolDao.makePage("0", siteId, title, 0L, 0L);
	  simplePageBean.saveItem(page);
	  // index of old top level
	  int top = pages.size()-1;
	  SimplePage parent = pages.get(top);
	  int seq = sequences.get(top);

	  SimplePageItem item = simplePageToolDao.makeItem(parent.getPageId(), seq, SimplePageItem.PAGE, Long.toString(page.getPageId()), title);
	  simplePageBean.saveItem(item);
	  // increment sequence to after this folder
	  sequences.set(top, seq+1);
	  // inside the new folder we start at sequence 1
	  sequences.add(1);
      }
      pages.add(page);
  }

  public void startCCItem(String the_id, String the_title) {
      if (log.isDebugEnabled()) {
	  log.debug("cc item "+the_id+" begins");
	  log.debug("title: "+the_title);
      }
  }

  private ContentCollection makeBaseFolder(String name) {

      if (siteId == null) {
	  simplePageBean.setErrKey("simplepage.nosite", "");
	  return null;
      }

      if (importtop) {
	  try {
	      ContentCollection top = ContentHostingService.getCollection(ContentHostingService.getSiteCollection(siteId));
	      return top;
	  } catch (Exception e) {
	      simplePageBean.setErrKey("simplepage.create.resource.failed",name + " " +e);
	      return null;
	  }
      }

      if (name == null) 
	  name = "Common Cartridge";
      if (name.trim().length() == 0) 
	  name = "Common Cartridge";

      // we must reject certain characters that we cannot even escape and get into Tomcat via a URL                               

      StringBuffer newname = new StringBuffer(ContentHostingService.getSiteCollection(siteId));

      int length = name.length();
      for (int i = 0; i < length; i++) {
	  if (Validator.INVALID_CHARS_IN_RESOURCE_ID.indexOf(name.charAt(i)) != -1)
	      newname.append("_");
	  else
	      newname.append(name.charAt(i));
      }

      length = newname.length();
      if (length > (ContentHostingService.MAXIMUM_RESOURCE_ID_LENGTH - 5))
	  length = ContentHostingService.MAXIMUM_RESOURCE_ID_LENGTH - 5; // for trailing / and possible count
      newname.setLength(length);

      name = newname.toString() + "1";

      ContentCollectionEdit collection = null;
      int tries = 1;
      int olength = name.length();
      for (; tries <= MAX_ATTEMPTS; tries++) {
	  try {
	      collection = ContentHostingService.addCollection(name + "/");  // append / here because we may hack on the name

	      String display = name;
	      int main = name.lastIndexOf("/");
	      if (main >= 0)
		  display = display.substring(main+1);
	      collection.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, display);

	      ContentHostingService.commitCollection(collection);
	      break;   // got it
	  } catch (IdUsedException e) {
	      name = name.substring(0, olength) + "-" + tries;
	  } catch (Exception e) {
	      simplePageBean.setErrKey("simplepage.create.resource.failed",name + " " +e);
	      return null;
	  }
      }
      if (collection == null) {
	  simplePageBean.setErrKey("simplepage.resource100", name);
	  return null;
      }
      return collection;
  }

  //Get filename from a resource element
  //Return the href attribute, otherwise return the href value of a file child
  private String getFileName(Element resource) {
	  //The problem here is there might be multiple <file> elements, so should trust the href on the resource first
	  String href = resource.getAttributeValue(HREF);
	  if (href == null) {
		  Element file = resource.getChild(FILE, ns.getNs());
		  //This will return null if there is no HREF on the file
		  return file.getAttributeValue(HREF);
	  }
	  return href;
  }

  /**
   * Check if a resource represents a Canvas assignment
   */
  private boolean isCanvasAssignment(Element resource) {
      // Check if this is a Canvas learning application resource that contains assignment_settings.xml
      List<Element> files = resource.getChildren(FILE, ns.getNs());
      for (Element file : files) {
          String fileHref = file.getAttributeValue(HREF);
          if (fileHref != null && fileHref.endsWith("assignment_settings.xml")) {
              return true;
          }
      }
      return false;
  }
  
  /**
   * Check if a resource represents a Canvas QTI assessment or question bank
   */
  private boolean isCanvasQTI(Element resource) {
      // Check if this is a Canvas learning application resource that contains .xml.qti files
      List<Element> files = resource.getChildren(FILE, ns.getNs());
      for (Element file : files) {
          String fileHref = file.getAttributeValue(HREF);
          if (fileHref != null && fileHref.endsWith(".xml.qti")) {
              return true;
          }
      }
      return false;
  }
  
  /**
   * Check if a resource represents a Canvas syllabus
   */
  private boolean isCanvasSyllabus(Element resource) {
      // Check if this is a Canvas learning application resource with intendeduse="syllabus"
      String intendedUse = resource.getAttributeValue(INTENDEDUSE);
      return intendedUse != null && "syllabus".equals(intendedUse.toLowerCase());
  }

  /**
   * Handle Canvas QTI files from learning-application-resource types
   */
  private void handleCanvasQTI(Element resourceXml, SimplePage page, int seq, List<Integer> sequences, int top, boolean noPage, Set<String> roles) {
      try {
          // Find the .xml.qti file in this resource
          String qtiFileName = null;
          List<Element> files = resourceXml.getChildren(FILE, ns.getNs());
          for (Element file : files) {
              String fileHref = file.getAttributeValue(HREF);
              if (fileHref != null && fileHref.endsWith(".xml.qti")) {
                  qtiFileName = fileHref;
                  break;
              }
          }
          
          if (qtiFileName == null) {
              log.warn("Canvas QTI resource has no .xml.qti file");
              return;
          }
          
          String sakaiId = null;
          String base = baseUrl;
          boolean isBank = false;
          
          // Check if already added
          if (itemsAdded.get(qtiFileName) == null) {
              itemsAdded.put(qtiFileName, SimplePageItem.DUMMY); // don't add the same test more than once
              
              File qtitemp = null;
              try {
                  base = baseUrl + qtiFileName;
                  int slash = base.lastIndexOf("/");
                  if (slash >= 0) base = base.substring(0, slash + 1); // include trailing slash
                  
                  // Determine if this is a question bank or assessment by examining the QTI structure
                  try (InputStream instream = utils.getFile(qtiFileName)) {
                      if (instream != null) {
                          isBank = isQTIQuestionBank(instream);
                      }
                  }
                  
                  qtitemp = File.createTempFile("ccqti", "txt");
                  
                  // Process the QTI file
                  try (InputStream instream = utils.getFile(qtiFileName);
                       PrintWriter outwriter = new PrintWriter(qtitemp)) {
                      
                      if (instream != null) {
                          QtiImport imp = new QtiImport();
                          try {
                              boolean thisUsesPattern = imp.mainproc(instream, outwriter, isBank, base, siteId, simplePageBean, null);
                              if (thisUsesPattern) usesPatternMatch = true;
                              if (imp.getUsesCurriculum()) usesCurriculum = true;
                          } catch (Exception e) {
                              log.error("Error processing Canvas QTI file: {}", qtiFileName, e);
                          }
                      }
                  }
                  
                  // Parse the processed QTI
                  try (InputStream inputStream = new FileInputStream(qtitemp)) {
                      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                      builderFactory.setNamespaceAware(true);
                      builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                      builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
                      org.w3c.dom.Document document = documentBuilder.parse(inputStream);
                      
                      QuizEntity q = (QuizEntity) quiztool;
                      sakaiId = q.importObject(document, isBank, siteId, noPage);
                      if (sakaiId == null) sakaiId = SimplePageItem.DUMMY;
                      
                      itemsAdded.put(qtiFileName, sakaiId); // Update with actual sakaiId
                  } catch (Exception e) {
                      log.warn("Canvas QTI import could not create or parse QTI file: {}", qtiFileName, e);
                      simplePageBean.setErrKey("simplepage.create.object.failed", e.toString());
                  }
                  
              } finally {
                  // Clean up temp file
                  if (qtitemp != null) {
                      qtitemp.delete();
                  }
              }
          } else {
              sakaiId = itemsAdded.get(qtiFileName);
              // Need to determine isBank for existing items too
              try (InputStream instream = utils.getFile(qtiFileName)) {
                  if (instream != null) {
                      isBank = isQTIQuestionBank(instream);
                  }
              }
          }
          
          // Question banks don't appear on the page, only assessments do
          if (!isBank && !noPage) {
              String title = getQTITitle(resourceXml, qtiFileName);
              SimplePageItem item = simplePageToolDao.makeItem(page.getPageId(), seq, SimplePageItem.ASSESSMENT, 
                  (sakaiId == null ? SimplePageItem.DUMMY : sakaiId), title);
              simplePageBean.saveItem(item);
              if (!roles.isEmpty()) simplePageBean.setItemGroups(item, roles.toArray(new String[roles.size()]));
              sequences.set(top, seq + 1);
          }
          
      } catch (Exception e) {
          log.error("Error handling Canvas QTI resource", e);
      }
  }
  
  /**
   * Determine if a QTI file represents a question bank (objectbank) or an assessment
   */
  private boolean isQTIQuestionBank(InputStream qtiStream) {
      try {
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          factory.setNamespaceAware(true);
          DocumentBuilder builder = factory.newDocumentBuilder();
          org.w3c.dom.Document doc = builder.parse(qtiStream);
          
          // Check for objectbank element - Canvas exports question banks as objectbank
          org.w3c.dom.NodeList objectbanks = doc.getElementsByTagName("objectbank");
          return objectbanks.getLength() > 0;
          
      } catch (Exception e) {
          log.warn("Error determining QTI type, assuming assessment", e);
          return false; // Default to assessment
      }
  }
  
  /**
   * Extract title from QTI file or resource metadata
   */
  private String getQTITitle(Element resourceXml, String qtiFileName) {
      try {
          // Try to get title from QTI file itself
          try (InputStream qtiStream = utils.getFile(qtiFileName)) {
              if (qtiStream == null) {
                  log.debug("Could not get QTI file stream for: " + qtiFileName);
              } else {
                  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                  factory.setNamespaceAware(true);
                  DocumentBuilder builder = factory.newDocumentBuilder();
                  org.w3c.dom.Document doc = builder.parse(qtiStream);
                  
                  // Look for bank_title in QTI metadata
                  org.w3c.dom.NodeList metadataFields = doc.getElementsByTagName("qtimetadatafield");
                  for (int i = 0; i < metadataFields.getLength(); i++) {
                      org.w3c.dom.Element field = (org.w3c.dom.Element) metadataFields.item(i);
                      org.w3c.dom.NodeList labels = field.getElementsByTagName("fieldlabel");
                      org.w3c.dom.NodeList entries = field.getElementsByTagName("fieldentry");
                      
                      if (labels.getLength() > 0 && entries.getLength() > 0) {
                          String label = labels.item(0).getTextContent().trim();
                          if ("bank_title".equals(label)) {
                              return entries.item(0).getTextContent().trim();
                          }
                      }
                  }
                  
                  // Look for assessment title
                  org.w3c.dom.NodeList assessments = doc.getElementsByTagName("assessment");
                  if (assessments.getLength() > 0) {
                      org.w3c.dom.Element assessment = (org.w3c.dom.Element) assessments.item(0);
                      String title = assessment.getAttribute("title");
                      if (title != null && !title.isEmpty()) {
                          return title;
                      }
                  }
              }
          }
      } catch (Exception e) {
          log.debug("Could not extract title from QTI file", e);
      }
      
      // Fallback to filename
      String title = qtiFileName;
      if (title.contains("/")) {
          title = title.substring(title.lastIndexOf("/") + 1);
      }
      if (title.endsWith(".xml.qti")) {
          title = title.substring(0, title.length() - 8);
      }
      return title;
  }

  /**
   * Identify Canvas entity files early in the process so they can be excluded from resource processing
   */
  private void identifyCanvasEntityFiles(Element manifest) {
      try {
          Element resourcesElement = manifest.getChild(RESOURCES, ns.cc_ns());
          if (resourcesElement != null) {
              List<Element> resources = resourcesElement.getChildren(RESOURCE, ns.cc_ns());

              // First pass: collect all discussion dependency resource IDs
              Set<String> discussionDependencies = new HashSet<String>();
              for (Element resource : resources) {
                  String resourceType = resource.getAttributeValue("type");
                  if (resourceType != null && resourceType.startsWith("imsdt_xmlv1p")) {
                      // This is a Canvas discussion - find its dependencies
                      List<Element> dependencies = resource.getChildren("dependency", ns.cc_ns());
                      for (Element dependency : dependencies) {
                          String dependencyId = dependency.getAttributeValue("identifierref");
                          if (dependencyId != null) {
                              discussionDependencies.add(dependencyId);
                              log.debug("Found discussion dependency: {}", dependencyId);
                          }
                      }
                  }
              }

              // Second pass: mark Canvas entities for exclusion
              for (Element resource : resources) {
                  String resourceType = resource.getAttributeValue("type");
                  String intendedUse = resource.getAttributeValue("intendeduse");
                  String resourceId = resource.getAttributeValue("identifier");

                  boolean isCanvasEntity = false;

                  // Check for Canvas assignments (learning-application-resource with Canvas files)
                  if (resourceType != null && resourceType.contains("learning-application-resource")) {
                      List<Element> files = resource.getChildren("file", manifest.getNamespace());
                      for (Element file : files) {
                          String fileHref = file.getAttributeValue("href");
                          if (fileHref != null && isCanvasEntityFile(fileHref)) {
                              isCanvasEntity = true;
                              break;
                          }
                      }
                  }

                  // Check for Canvas syllabus (intendeduse="syllabus")
                  if (intendedUse != null && intendedUse.equals("syllabus")) {
                      isCanvasEntity = true;
                  }

                  // Check for Canvas wiki content (webcontent with wiki_content/)
                  String href = resource.getAttributeValue(HREF);
                  if (href != null && href.contains("wiki_content/")) {
                      isCanvasEntity = true;
                      log.debug("Marking Canvas wiki content as entity: {}", href);
                  }

                  // Check for Canvas discussion dependencies
                  if (resourceId != null && discussionDependencies.contains(resourceId)) {
                      isCanvasEntity = true;
                      log.debug("Marking discussion dependency as Canvas entity: {}", resourceId);
                  }

                  // Check for Canvas discussions (imsdt_xmlv1p1) - exclude XML files from Resources
                  if (resourceType != null && resourceType.startsWith("imsdt_xmlv1p")) {
                      isCanvasEntity = true;
                      log.debug("Marking Canvas discussion as entity: {}", resourceId);
                  }

                  // If this is a Canvas entity resource, mark all its files for exclusion
                  if (isCanvasEntity) {
                      List<Element> files = resource.getChildren(FILE, ns.cc_ns());
                      for (Element file : files) {
                          String fileHref = file.getAttributeValue(HREF);
                          if (fileHref != null) {
                              canvasEntityFiles.add(fileHref);
                              log.debug("Marking Canvas entity file for exclusion: {}", fileHref);
                          }
                      }
                  }
              }
          }
      } catch (Exception e) {
          log.warn("Error identifying Canvas entity files", e);
      }
  }

  /**
   * Check if a file is a Canvas entity file that should not be imported as a resource
   */
  private boolean isCanvasEntityFile(String fileHref) {
      return fileHref.endsWith("assignment_settings.xml") ||
             fileHref.endsWith("course_settings.xml") ||
             fileHref.endsWith("assignment_groups.xml") ||
             fileHref.endsWith("files_meta.xml") ||
             fileHref.endsWith("late_policy.xml") ||
             fileHref.endsWith("context.xml") ||
             fileHref.endsWith("media_tracks.xml") ||
             fileHref.endsWith("canvas_export.txt") ||
             fileHref.endsWith("module_meta.xml") ||
             fileHref.contains("discussion_settings.xml") ||
             fileHref.contains("assessment_settings.xml") ||
             fileHref.contains("quiz_settings.xml");
  }

  public String getGroupForRole(String role) {
      // if group already exists, this will return the existing one
      try {
	  String g = GroupPermissionsService.makeGroup(siteId, role, role, null, simplePageBean);
	  return g;
	  //	  return GroupPermissionsService.makeGroup(siteId, role);
      } catch (Exception e) {
	  log.debug("Unable to create group " + role);
	  return null;
      }
  }

  //Fix external references in an htmlString to point to the correct location
  public String fixupInlineReferences(String htmlString) {
	  // and fix relative URLs to absolute, since this is going to be inserted inline
	  // in a page that's not in resources.
	  // These fixups are inserted as identified by the lessons exporter
	  if (htmlString.startsWith("<!--fixups:")) {
		  int fixend = htmlString.indexOf("-->");
		  String fixString = htmlString.substring(11, fixend);
		  htmlString = htmlString.substring(fixend + 3);
		  String[] fixups = fixString.split(",");
		  // iterate backwards since once we fix something, offsets
		  // further in the string are bad
		  for (int i = (fixups.length-1); i >= 0; i--) {
			  String fixup = fixups[i];
			  // these are offsets of a URL. The URL is for a file in attachments, so we need
			  // to map it to a full URL. The file should be attachments/item-xx.html in the
			  // package. relFixup will have added ../ to it to get to the base.
			  try {
				  int offset = Integer.parseInt(fixup);
				  htmlString = htmlString.substring(0, offset) + baseUrl + htmlString.substring(offset+3);
			  } catch (Exception e) {
				  log.info("exception " + e);
			  }
		  }
	  //Otherwise try jsoup to do the fixups
	  } else {
		  /*
			Now we need to go through the string looking for other references that weren't identified by the fixups
			Using full class names here because of conflict in names
			I think the ideal here would be that this only updates resources that are in the manifest, but really any
			relative resources are going to be incorrect pulled out of a package and need an update.
		   */
		  org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(htmlString);
		  org.jsoup.select.Elements hrefs = doc.select("[href]");
		  org.jsoup.select.Elements srcs = doc.select("[src]");

		  log.debug("BaseURL is: {}", baseUrl);

		  // Have to look for both href and src tags
		  for (org.jsoup.nodes.Element element : srcs) {
			  String src = element.attr("src");
			  if (src != null && !src.startsWith("http")) {
				  for (Map.Entry<String,String> entry : fileNames.entrySet()) {
					  if (entry.getKey() != null && entry.getValue() != null && entry.getValue().contains(src)) {
						  // Found key, set it and stop looking
						  log.debug(String.format("Updating tag %s: <%s> to <%s>", element.tagName(), src, baseUrl+src));
						  element.attr("src",baseUrl+entry.getValue());
						  break;
					  }
				  }
			  }
		  }

		  for (org.jsoup.nodes.Element element : hrefs) {
			  String href = element.attr("href");
			  if(href != null && !href.startsWith("http")) {
				  for (Map.Entry<String,String> entry : fileNames.entrySet()) {
					  if (entry.getKey() != null && entry.getValue() != null && entry.getValue().contains(href)) {
						  // Found key, set it and stop looking
						  log.debug(String.format("Updating a: <%s> to <%s> (%s)", href, baseUrl+href, element.text()));
						  element.attr("href",baseUrl+entry.getValue());
						  break;
					  }
				  }
			  }
		  }
		  htmlString = doc.body().html();
	  }
	  
	  return htmlString;
  }

  public void setCCItemXml(Element itemXml, Element resourceXml, AbstractParser parser, CartridgeLoader loader, boolean noPage) {
      log.debug("Add item { xml: [{}], title: [{}], type: [{}], href: [{}] }, to page: [{}]",
              itemXml,
              itemXml != null ? itemXml.getChildText(CC_ITEM_TITLE, ns.getNs()) : null,
              resourceXml != null ? resourceXml.getAttributeValue(TYPE) : null,
              resourceXml != null ? resourceXml.getAttributeValue(HREF) : null,
              pages != null && !pages.isEmpty() ? pages.get(pages.size() - 1).getTitle() : null);

      String resourceType = ns.normType(resourceXml.getAttributeValue(TYPE));
      boolean isBank = resourceType.equals(QUESTION_BANK);

      // first question: is this the resource we want to use, or is there are preferable variant?
      Element variant = resourceXml.getChild(VARIANT, ns.cpx_ns());
      Set<String> seen = new HashSet<>();
      String variantId = null;
      while (variant != null) {
          variantId = variant.getAttributeValue(IDENTIFIERREF);
          // prevent loop. If we've seen it, exit
          if (seen.contains(variantId)) break;
          seen.add(variantId);
          variant = null; // to stop loop unless we find a valid next variant
          Element variantResource = null;
          if (variantId != null) {
              Element resourcesNode = manifestXml.getChild(RESOURCES, ns.cc_ns());
              if (resourcesNode != null) {
                  List<Element> resources = resourcesNode.getChildren(RESOURCE, ns.cc_ns());
                  if (resources != null) {
                      for (Element e : resources) {
                          if (variantId.equals(e.getAttributeValue(IDENTIFIER))) {
                              variantResource = e;
                              break;
                          }
                      }
                  }
              }
              if (variantResource == null) {
                  log.debug("variant resource is missing with id=[{}],", variantId);
                  // should be impossible. means there was a variant pointing to a non-existent resource
              } else {
                  // we now have the variant resource. Only use it if we recognize the type
                  String variantType = ns.normType(variantResource.getAttributeValue(TYPE));
                  // if we recognize the type, use the variant. By definition the variant is preferred, so we'll use
                  // it if we recognize it.
                  if (!UNKNOWN.equals(variantType)) {
                      log.debug("using variant {} of type {} for resource {}", variantResource, variantType, resourceXml);
                      resourceType = variantType;
                      resourceXml = variantResource;
                  } else {
                      log.debug("not using variant {} of type {} for resource {}", variantResource, variantType, resourceXml);
                  }
                  // next step for loop. want to check next one even if the source was unusable
                  variant = variantResource.getChild(VARIANT, ns.cpx_ns());
              }
          }
      }

      Set<String> roles = new HashSet<>();
      // version 1 and higher are different formats, hence a slightly weird test
      Iterator<Element> mdRoles = resourceXml.getDescendants(new ElementFilter("intendedEndUserRole", ns.lom_ns()));
      if (mdRoles != null) {
          while (mdRoles.hasNext()) {
              Element role = mdRoles.next();
              Iterator<Element> values = role.getDescendants(new ElementFilter("value", ns.lom_ns()));
              if (values != null) {
                  while (values.hasNext()) {
                      Element value = values.next();
                      String roleName = value.getTextTrim();
                      if (!"Learner".equals(roleName)) {
                          // roles currently only implemented for visible objects. We may want to fix that.
                          if (!noPage && !isBank) {
                              usesRole = true;
                          }
                      }
                      if ("Mentor".equals(roleName)) {
                          roles.add(getGroupForRole("Mentor"));
                      }
                      if ("Instructor".equals(roleName)) {
                          roles.add(getGroupForRole("Instructor"));
                      }
                  }
              }
          }
      }

      // for question banks we don't need a current page, as we don't put banks on a page
      if (pages.isEmpty() && !isBank && !noPage) startCCFolder(null);

      int top = pages.size() - 1;
      SimplePage page = (isBank || noPage) ? null : pages.get(top);

      int seq = (isBank || noPage) ? 0 : sequences.get(top);
      String title;
      String identifier = null;
      String identifierRef = null;

      if (itemXml == null)
          title = "Question Pool";
      else {
          identifier = itemXml.getAttributeValue(IDENTIFIER);
          identifierRef = itemXml.getAttributeValue(IDENTIFIERREF);
          title = itemXml.getChildText(CC_ITEM_TITLE, ns.getNs());
          // metadata is used for special Sakai data
          String id = StringUtils.defaultIfBlank(identifierRef, identifier); // prefer identifierref if exists on item
          if (StringUtils.isNotBlank(id)) {
              itemsMetaDataAdded.computeIfAbsent(id, f -> readItemMetaData(itemXml));
          }
      }

      boolean inline = false;
      String mmDisplayType = null;
      String resourceId = resourceXml.getAttributeValue(IDENTIFIER);
      log.debug("identifier {} identifierRef {} resourceId {} resourceXml {}", identifier, identifierRef, resourceId, resourceXml);

      // Check if there is any Canvas module metadata in the import
      Element canvas_module_item = null;
      boolean open_same_window = true;
      if ( identifier != null && canvasModuleMeta != null ) {

          Namespace namespace = Namespace.getNamespace("cccv1p0", CANVAS_MODULE_META_NAMESPACE);

          String xpathExpression = "//cccv1p0:item[@identifier='" + identifier + "']";

          List<Element> items = XPathFactory.instance().compile(xpathExpression, Filters.element(), null, namespace).evaluate(canvasModuleMeta);

          log.debug("Found {} items whilst looking for {}", items.size(), xpathExpression);
          if ( items.size() >= 1 ) {
              canvas_module_item = items.get(0);
              String new_tab = canvas_module_item.getChildText(CANVAS_MODULE_NEW_TAB, namespace);
              open_same_window = CANVAS_MODULE_NEW_TAB_FALSE.equals(new_tab);
              log.debug("new_tab {} open_same_window {}", new_tab, open_same_window);
          }
      }

      Map<String, String> itemsMetaData = itemsMetaDataAdded.get(resourceId);
      if (itemsMetaData != null) {
          inline = BooleanUtils.toBoolean(itemsMetaData.get("inline.lessonbuilder.sakaiproject.org"));
          mmDisplayType = itemsMetaData.get("mmDisplayType.lessonbuilder.sakaiproject.org");
      } else {
          log.debug("found a resource with no id");
      }

      try {
          if ((resourceType.equals(CC_WEBCONTENT) || (resourceType.equals(UNKNOWN))) && !noPage) {
              // note: when this code is called the actual sakai resource hasn't been created yet
              String href = resourceXml.getAttributeValue(HREF);
              // for unknown item types, may have a file with an HREF but no HREF in the actual resource
              // of course someone might define an extension resource without that.
              if (href == null) {
                  Element fileElement = resourceXml.getChild(FILE, ns.cc_ns());
                  href = fileElement.getAttributeValue(HREF);
              }

              String sakaiId = baseName + href;
              String extension = Validator.getFileExtension(sakaiId);
              String mime = ContentTypeImageService.getContentType(extension);
              String intendedUse = resourceXml.getAttributeValue(INTENDEDUSE);
              SimplePageItem item = simplePageToolDao.makeItem(page.getPageId(), seq, SimplePageItem.RESOURCE, sakaiId, title);
              item.setHtml(mime);
              item.setSameWindow(open_same_window);

              // only text type files can be inlined, see addFile
              if ((inline || forceInline) && StringUtils.startsWith(mime, "text/")) {
                  StringBuilder html = new StringBuilder();
                  String htmlString = null;

                  // type 3 is a link, so it's handled below
                  // get contents of file for types where we don't need a file in contents
                  if (mmDisplayType == null || "1".equals(mmDisplayType)) {

                      // read the file containing the HTML
                      String fileName = getFileName(resourceXml);
                      InputStream fileStream = null;

                      if (fileName != null) fileStream = utils.getFile(fileName);
                      if (fileStream != null) {
                          byte[] buffer = new byte[8096];
                          int n = 0;
                          while ((n = fileStream.read(buffer, 0, 8096)) >= 0) {
                              if (n > 0) html.append(new String(buffer, 0, n, StandardCharsets.UTF_8));
                          }
                      }

                      htmlString = ComponentManager.get(FormattedText.class).getHtmlBody(html.toString());
                      htmlString = fixupInlineReferences(htmlString);
                  }

                  // inline can be multimedia or text. If mmdisplaytype set, it's multimedia
                  if (mmDisplayType != null) {
                      // 	 1 -- embed code, 2 -- av type, 3 -- oembed, 4 -- iframe
                      // 3 is output as a link, so it's handled below
                      item.setType(SimplePageItem.MULTIMEDIA);
                      if ("1".equals(mmDisplayType)) {
                          item.setAttribute("multimediaEmbedCode", htmlString);
                      }
                      item.setAttribute("multimediaDisplayType", mmDisplayType);
                  } else {
                      // must be text item
                      item.setType(SimplePageItem.TEXT);
                      item.setHtml(htmlString);
                  }
              }

              if (intendedUse != null) {
                  intendedUse = intendedUse.toLowerCase();
                  if (intendedUse.equals("lessonplan"))
                      item.setDescription(simplePageBean.getMessageLocator().getMessage("simplepage.import_cc_lessonplan"));
                  else if (intendedUse.equals("syllabus"))
                      item.setDescription(simplePageBean.getMessageLocator().getMessage("simplepage.import_cc_syllabus"));
                  else if (assigntool != null && intendedUse.equals("assignment")) {
                      String fileName = getFileName(resourceXml);

                      if (itemsAdded.get(fileName) == null) {
                          // itemsAdded.put(fileName, SimplePageItem.DUMMY); // don't add the same test more than once
                          AssignmentInterface a = (AssignmentInterface) assigntool;
                          // file hasn't been written yet to contenthosting. A2 requires it to be there
                          addFile(href);
                          String assignmentId = a.importObject(title, sakaiId, mime, false); // sakaiid for assignment
                          if (assignmentId != null) {
                              item = simplePageToolDao.makeItem(page.getPageId(), seq, SimplePageItem.ASSIGNMENT, assignmentId, title);
                          }
                      }
                  }
              }
              simplePageBean.saveItem(item);
              if (!roles.isEmpty()) {  // has to be written already or we can't set groups
                  // file hasn't been written yet to contenthosting. setitemgroups requires it to be there
                  addFile(href);
                  simplePageBean.setItemGroups(item, roles.toArray(new String[0]));
              }
              sequences.set(top, seq + 1);
          } else if (resourceType.equals(CC_WEBCONTENT) || resourceType.equals(UNKNOWN)) { // i.e. hidden. if it's an assignment have to load it
              String intendedUse = resourceXml.getAttributeValue(INTENDEDUSE);
              if (assigntool != null && intendedUse != null && intendedUse.equals("assignment")) {
                  String fileName = getFileName(resourceXml);
                  if (itemsAdded.get(fileName) == null) {
                      itemsAdded.put(fileName, SimplePageItem.DUMMY); // don't add the same test more than once
                      String sakaiId = baseName + resourceXml.getAttributeValue(HREF);
                      String extension = Validator.getFileExtension(sakaiId);
                      String mime = ContentTypeImageService.getContentType(extension);
                      AssignmentInterface a = (AssignmentInterface) assigntool;
                      // file hasn't been written yet to contenthosting. A2 requires it to be there
                      addFile(resourceXml.getAttributeValue(HREF));
                      // in this case there's no item to take a title from
                      String atitle = simplePageBean.getMessageLocator().getMessage("simplepage.importcc-assigntitle").replace("{}", (assignmentNumber++).toString());
                      String assignmentId = a.importObject(atitle, sakaiId, mime, true); // sakaiid for assignment
                  }
              } else if ("syllabus".equals(intendedUse)) {
                  // Handle Canvas syllabus import
                  if (syllabusManager != null) {
                      try {
                          String syllabusResourceId = resourceXml.getAttributeValue(IDENTIFIER);
                          String syllabusTitle = itemXml != null ? itemXml.getChildText(CC_ITEM_TITLE, ns.getNs()) : "Canvas Syllabus";
                          if (syllabusTitle == null) syllabusTitle = "Canvas Syllabus";
                          
                          // Find HTML file in resource
                          String syllabusContent = "";
                          List<Element> files = resourceXml.getChildren(FILE, ns.getNs());
                          for (Element file : files) {
                              String fileHref = file.getAttributeValue(HREF);
                              if (fileHref != null && fileHref.endsWith(".html")) {
                                  try (InputStream htmlStream = loader.getFile(fileHref)) {
                                      if (htmlStream != null) {
                                          byte[] buffer = new byte[8096];
                                          StringBuilder sb = new StringBuilder();
                                          int n;
                                          while ((n = htmlStream.read(buffer)) > 0) {
                                              sb.append(new String(buffer, 0, n, StandardCharsets.UTF_8));
                                          }
                                          syllabusContent = sb.toString();
                                          break;
                                      }
                                  } catch (Exception e) {
                                      log.warn("Failed to load syllabus HTML: {}", fileHref, e);
                                  }
                              }
                          }
                          
                          // Create or get syllabus item for site
                          SyllabusItem syllabusItem = syllabusManager.getSyllabusItemByContextId(siteId);
                          if (syllabusItem == null) {
                              syllabusItem = syllabusManager.createSyllabusItem(simplePageBean.getCurrentUserId(), siteId, null);
                          }
                          
                          // Create syllabus data entry
                          int position = syllabusManager.findLargestSyllabusPosition(syllabusItem) + 1;
                          syllabusManager.createSyllabusDataObject(syllabusTitle, position,
                                  syllabusContent, "yes", SyllabusData.ITEM_POSTED, "none", null, null, false, null, null, syllabusItem);
                          
                          log.debug("Created Canvas syllabus entry: {}", syllabusTitle);
                          
                      } catch (Exception e) {
                          log.warn("Failed to import Canvas syllabus: {}", e.getMessage(), e);
                      }
                  }
              } else {
                  // Handle Canvas wiki content import as Lessons pages
                  String href = resourceXml.getAttributeValue(HREF);
                  log.debug("Canvas webcontent handler: href={}, noPage={}", href, noPage);
                  if (href != null && href.contains("wiki_content/")) {
                      log.debug("Processing Canvas wiki content import for: {}", href);
                      try {
                          // Extract page title from href (remove wiki_content/ prefix and .html suffix)
                          String pageTitle = href.substring(href.lastIndexOf("/") + 1);
                          if (pageTitle.endsWith(".html")) {
                              pageTitle = pageTitle.substring(0, pageTitle.length() - 5);
                          }
                          // Convert filename to readable title
                          pageTitle = pageTitle.replace("-", " ").replace("_", " ");
                          // Capitalize first letter of each word
                          StringBuilder titleBuilder = new StringBuilder();
                          boolean capitalizeNext = true;
                          for (char c : pageTitle.toCharArray()) {
                              if (Character.isWhitespace(c)) {
                                  capitalizeNext = true;
                                  titleBuilder.append(c);
                              } else if (capitalizeNext) {
                                  titleBuilder.append(Character.toUpperCase(c));
                                  capitalizeNext = false;
                              } else {
                                  titleBuilder.append(c);
                              }
                          }
                          pageTitle = titleBuilder.toString();
                          
                          // Load HTML content
                          String wikiContent = "";
                          try (InputStream htmlStream = loader.getFile(href)) {
                              if (htmlStream != null) {
                                  wikiContent = new String(htmlStream.readAllBytes(), StandardCharsets.UTF_8);
                              }
                          } catch (Exception e) {
                              log.warn("Failed to load Canvas wiki content: {}", href, e);
                          }
                          
                          if (!wikiContent.isEmpty()) {
                              // Create a new Lessons page for this wiki content
                              SimplePage wikiPage = simplePageBean.addPage(pageTitle, false);
                              
                              // Add the HTML content as a text item on the page
                              SimplePageItem textItem = simplePageToolDao.makeItem(wikiPage.getPageId(), 1, SimplePageItem.TEXT, null, pageTitle);
                              textItem.setHtml(wikiContent);
                              simplePageBean.saveItem(textItem);

                              log.debug("Created Canvas wiki page: {}", pageTitle);
                          }
                          
                      } catch (Exception e) {
                          log.warn("Failed to import Canvas wiki content: {}", e.getMessage(), e);
                      }
                  }
              }
          } else if (resourceType.equals(WEBLINK)) {
              Element linkXml = null;
              String filename = getFileName(resourceXml);
              if (filename != null) {
                  linkXml = parser.getXML(loader, filename);
              } else {
                  linkXml = resourceXml.getChild(WEBLINK, ns.link_ns());
                  filename = resourceXml.getAttributeValue(ID) + XML;
              }
              Namespace linkNs = ns.link_ns();
              Element urlElement = linkXml.getChild(URL, linkNs);
              String url = urlElement.getAttributeValue(HREF);

              // the name must end in XML, so we can just turn it into URL
              filename = filename.substring(0, filename.length() - 3) + "url";
              String sakaiId = baseName + filename;

              if (!inline && !filesAdded.contains(filename)) {
                  // we store the URL as a text/url resource
                  ContentResourceEdit edit = ContentHostingService.addResource(sakaiId);
                  edit.setContentType("text/url");
                  edit.setResourceType("org.sakaiproject.content.types.urlResource");
                  edit.setContent(url.getBytes(StandardCharsets.UTF_8));
                  edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, Validator.escapeResourceName(filename));
                  ContentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
                  filesAdded.add(filename);
              }

              if (inline && "3".equals(mmDisplayType)) {
                  // inline can be either oembed or youtube. Handle oembed here
                  SimplePageItem item = simplePageToolDao.makeItem(page.getPageId(), seq, SimplePageItem.MULTIMEDIA, sakaiId, title);
                  item.setAttribute("multimediaUrl", url);
                  item.setAttribute("multimediaDisplayType", "3");
                  simplePageBean.saveItem(item);

              } else if (!noPage) {
                  // now create the Sakai item
                  SimplePageItem item = simplePageToolDao.makeItem(page.getPageId(), seq, SimplePageItem.RESOURCE, sakaiId, title);
                  if (inline) {
                      // should just be youtube. null displaytype is right for that
                      item.setType(SimplePageItem.MULTIMEDIA);
                  } else {
                      item.setHtml(simplePageBean.getTypeOfUrl(url));  // checks the web site to see what it actually is
                      item.setSameWindow(open_same_window);
                  }
                  simplePageBean.saveItem(item);
                  if (!roles.isEmpty()) simplePageBean.setItemGroups(item, roles.toArray(new String[0]));
                  sequences.set(top, seq + 1);
              }

          } else if (resourceType.equals(TOPIC)) {
              if (topictool != null) {
                  Element topicXml = null;
                  String filename = getFileName(resourceXml);
                  if (filename != null) {
                      topicXml = parser.getXML(loader, filename);
                  } else {
                      topicXml = resourceXml.getChild(TOPIC, ns.topic_ns());
                  }
                  Namespace topicNs = ns.topic_ns();
                  String topicTitle = topicXml.getChildText(TITLE, topicNs);
                  if (topicTitle == null) topicTitle = simplePageBean.getMessageLocator().getMessage("simplepage.cc-defaulttopic");
                  String topicText = topicXml.getChildText(TEXT, topicNs);
                  boolean texthtml = false;
                  if (topicText != null) {
                      Element textNode = topicXml.getChild(TEXT, topicNs);
                      String textFormat = textNode.getAttributeValue(TEXTTYPE);
                      if (TEXTHTML.equalsIgnoreCase(textFormat)) texthtml = true;
                  }

                  String base = baseUrl;
                  if (filename != null) {
                      base = baseUrl + filename;
                      int slash = base.lastIndexOf("/");
                      if (slash >= 0) base = base.substring(0, slash + 1); // include trailing slash
                  }

                  // collection id rather than URL
                  String baseDir = baseName;
                  if (filename != null) {
                      baseDir = baseName + filename;
                      int slash = baseDir.lastIndexOf("/");
                      if (slash >= 0) baseDir = baseDir.substring(0, slash + 1); // include trailing slash
                  }

                  if (texthtml) {
                      topicText = topicText.replaceAll("\\$IMS-CC-FILEBASE\\$", base);
                  }

                  // I'm going to assume that URLs in the CC files are legal, but if
                  // I add to them I nneed to URLencode what I add

                  // filebase will be directory name for discussion.xml, since attachments are relative to that
                  String filebase = "";
                  if (filename != null) {
                      filebase = filename;
                      int slash = filebase.lastIndexOf("/");
                      if (slash >= 0) filebase = filebase.substring(0, slash + 1); // include trailing slash
                  }

                  Element attachmentlist = topicXml.getChild(ATTACHMENTS, topicNs);
                  List<Element> attachments = new ArrayList<>();
                  if (attachmentlist != null) attachments = attachmentlist.getChildren();
                  List<String> attachmentHrefs = new ArrayList<>();
                  for (Element a : attachments) {
                      // file has to be there for the forum attachment handling to work
                      addFile(removeDotDot(filebase + a.getAttributeValue(HREF)));
                      attachmentHrefs.add(a.getAttributeValue(HREF));
                  }

                  ForumInterface f = (ForumInterface) topictool;

                  if (noPage) title = simplePageBean.getMessageLocator().getMessage("simplepage.cc-defaultforum");

                  log.debug("about to call forum import base {}", base);
                  // title is for the cartridge. That will be used as the forum
                  // if already added, don't do it again
                  String sakaiId = itemsAdded.get(filename);
                  if (sakaiId == null) {
                      if (f != null) sakaiId = f.importObject(title, topicTitle, topicText, texthtml, base, baseDir, siteId, attachmentHrefs, noPage);
                      if (sakaiId != null) itemsAdded.put(filename, sakaiId);
                  }

                  if (!noPage) {
                      log.debug("about to add formum item");
                      SimplePageItem item = simplePageToolDao.makeItem(page.getPageId(), seq, SimplePageItem.FORUM, sakaiId, title);
                      simplePageBean.saveItem(item);
                      if (!roles.isEmpty()) simplePageBean.setItemGroups(item, roles.toArray(new String[0]));
                      sequences.set(top, seq + 1);
                      log.debug("finished with forum item");
                  }
              }
          } else if (resourceType.equals(ASSESSMENT) || resourceType.equals(QUESTION_BANK)) {
              if (quiztool != null) {
                  String fileName = getFileName(resourceXml);
                  String sakaiId = null;
                  String base = baseUrl;
                  org.w3c.dom.Document quizDoc = null;
                  InputStream instream = null;

                  // not already added
                  if (fileName == null || itemsAdded.get(fileName) == null) {

                      File qtitemp = File.createTempFile("ccqti", "txt");
                      PrintWriter outwriter = new PrintWriter(qtitemp);

                      // assessment in file
                      if (fileName != null) {

                          itemsAdded.put(fileName, SimplePageItem.DUMMY); // don't add the same test more than once

                          instream = utils.getFile(fileName);

                          // I'm going to assume that URLs in the CC files are legal, but if
                          // I add to them I nneed to URLencode what I add
                          base = baseUrl + fileName;
                          int slash = base.lastIndexOf("/");
                          if (slash >= 0) base = base.substring(0, slash + 1); // include trailing slash

                          // assessment inline
                      } else {
                          Element quizXml = resourceXml.getChild(QUESTIONS, ns.qticc_ns()).clone();
                          // we work in jdom. Qti parser needs w3c
                          quizDoc = new DOMOutputter().output(new org.jdom2.Document(quizXml));
                      }

                      QtiImport imp = new QtiImport();
                      try {
                          boolean thisUsesPattern = imp.mainproc(instream, outwriter, isBank, base, siteId, simplePageBean, quizDoc);
                          if (thisUsesPattern) usesPatternMatch = true;
                          if (imp.getUsesCurriculum()) usesCurriculum = true;
                      } catch (Exception e) {
                          log.error(e.getMessage(), e);
                      }

                      outwriter.close();
                      InputStream inputStream = new FileInputStream(qtitemp);

                      try {
                          DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                          builderFactory.setNamespaceAware(true);
                          builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                          builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                          DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
                          org.w3c.dom.Document document = documentBuilder.parse(inputStream);

                          QuizEntity q = (QuizEntity) quiztool;

                          sakaiId = q.importObject(document, isBank, siteId, noPage);
                          if (sakaiId == null) sakaiId = SimplePageItem.DUMMY;
                      } catch (Exception e) {
                          log.warn("cc import could not create or parse QTI file {}, {}", fileName, e.toString());
                          simplePageBean.setErrKey("simplepage.create.object.failed", e.toString());
                      }

                      inputStream.close();
                      qtitemp.delete();

                  }

                  // question banks don't appear on the page
                  if (!isBank && !noPage) {
                      SimplePageItem item = simplePageToolDao.makeItem(page.getPageId(), seq, SimplePageItem.ASSESSMENT, (sakaiId == null ? SimplePageItem.DUMMY : sakaiId), title);
                      simplePageBean.saveItem(item);
                      if (!roles.isEmpty()) simplePageBean.setItemGroups(item, roles.toArray(new String[0]));
                      sequences.set(top, seq + 1);
                  }
              }
          } else if (resourceType.equals(QUESTION_BANK)) {
              ; // handled elsewhere
              // current code seems to assume that BLTI tool is part of the page so skip if no page
          } else if (resourceType.equals(BLTI)) {
              if (!noPage) {
                  String filename = getFileName(resourceXml);
                  Element ltiXml = null;
                  if (filename != null)
                      ltiXml = parser.getXML(loader, filename);
                  else {
                      ltiXml = resourceXml.getChild(CART_LTI_LINK, ns.lticc_ns());
                  }
                  XMLOutputter outputter = new XMLOutputter();
                  String strXml = outputter.outputString(ltiXml);
                  Namespace bltiNs = ns.blti_ns();
                  String bltiTitle = ltiXml.getChildText(TITLE, bltiNs);

                  Element customElement = ltiXml.getChild("custom", bltiNs);
                  List<Element> customs = new ArrayList<>();
                  if (customElement != null) customs = customElement.getChildren();
                  StringBuffer sb = new StringBuffer();
                  String custom = null;
                  for (Element a : customs) {
                      String key = a.getAttributeValue("name");
                      String value = a.getText();
                      if (key == null) continue;
                      key = key.trim();
                      if (value == null) continue;
                      sb.append(key.trim());
                      sb.append("=");
                      sb.append(value.trim());
                      sb.append("\n");
                  }
                  if (sb.length() > 0) custom = sb.toString();

                  String launchUrl = ltiXml.getChildTextTrim("secure_launch_url", bltiNs);
                  if (launchUrl == null) launchUrl = ltiXml.getChildTextTrim("launch_url", bltiNs);

                  String sakaiId = null;
                  if (bltitool != null) {
                      sakaiId = ((BltiInterface) bltitool).doImportTool(launchUrl, bltiTitle, strXml, custom, open_same_window);
                  }

                  if (sakaiId != null) {
                      log.debug("adding imported LTI resource [{}] as lessons item [{}]", bltiTitle, sakaiId);
                      SimplePageItem item = simplePageToolDao.makeItem(page.getPageId(), seq, SimplePageItem.BLTI, sakaiId, title);
                      item.setHeight(""); // default depends upon format, so it's supplied at runtime
                      simplePageBean.saveItem(item);
                      if (!roles.isEmpty()) simplePageBean.setItemGroups(item, roles.toArray(new String[0]));
                      sequences.set(top, seq + 1);
                  } else {
                      log.warn("failure importing LTI resource [{}]", bltiTitle);
                  }
              }
          } else if (resourceType.equals(LAR) && isCanvasQTI(resourceXml)) {
              // Handle Canvas QTI files (learning-application-resource containing .xml.qti files)
              if (quiztool != null) {
                  handleCanvasQTI(resourceXml, page, seq, sequences, top, noPage, roles);
              }
          } else if (resourceType.equals(LAR) && isCanvasAssignment(resourceXml)) {
              // Handle Canvas assignments (learning-application-resource containing assignment_settings.xml)
              // Handle Canvas learning application resources with assignment_settings.xml
              if (assigntool != null) {
                  String canvasResourceId = resourceXml.getAttributeValue(IDENTIFIER);
                  String canvasAssignmentId = assignsAdded.get(canvasResourceId);
                  
                  if (canvasAssignmentId == null) {
                      try {
                          // Find assignment_settings.xml file in this resource
                          String assignmentSettingsFile = null;
                          String instructionsFile = null;
                          
                          List<Element> files = resourceXml.getChildren(FILE, ns.getNs());
                          for (Element file : files) {
                              String fileHref = file.getAttributeValue(HREF);
                              if (fileHref != null) {
                                  if (fileHref.endsWith("assignment_settings.xml")) {
                                      assignmentSettingsFile = fileHref;
                                  } else if (fileHref.endsWith(".html")) {
                                      instructionsFile = fileHref;
                                  }
                              }
                          }
                          
                          if (assignmentSettingsFile != null) {
                              // Load the Canvas assignment XML
                              Element canvasAssignmentXml = parser.getXML(loader, assignmentSettingsFile);
                              
                              // Load instructions from HTML file if available
                              String instructions = "";
                              if (instructionsFile != null) {
                                  try (InputStream instructionsStream = loader.getFile(instructionsFile)) {
                                      if (instructionsStream != null) {
                                          instructions = new String(instructionsStream.readAllBytes(), StandardCharsets.UTF_8);
                                      }
                                  } catch (Exception e) {
                                      log.warn("Failed to load Canvas assignment instructions: {}", instructionsFile);
                                  }
                              }
                              
                              AssignmentInterface a = (AssignmentInterface) assigntool;
                              canvasAssignmentId = a.importCanvasAssignment(canvasAssignmentXml, instructions, noPage);
                              if (canvasAssignmentId != null) {
                                  assignsAdded.put(canvasResourceId, canvasAssignmentId);
                                  log.debug("Created Canvas assignment from resource: {}", canvasResourceId);
                              }
                          }
                      } catch (Exception e) {
                          log.warn("Failed to process Canvas assignment resource: {}", canvasResourceId, e);
                      }
                  }
                  
                  if (canvasAssignmentId != null && !noPage) {
                      String canvasTitle = itemXml != null ? itemXml.getChildText(CC_ITEM_TITLE, ns.getNs()) : "Canvas Assignment";
                      if (canvasTitle == null) {
                          // Try to get title from the main HTML file name
                          String href = resourceXml.getAttributeValue(HREF);
                          if (href != null && href.contains("/")) {
                              canvasTitle = href.substring(href.lastIndexOf("/") + 1);
                              if (canvasTitle.endsWith(".html")) {
                                  canvasTitle = canvasTitle.substring(0, canvasTitle.length() - 5);
                              }
                              canvasTitle = canvasTitle.replace("-", " ").replace("_", " ");
                          }
                      }
                      if (canvasTitle == null) canvasTitle = "Canvas Assignment";
                      
                      SimplePageItem item = simplePageToolDao.makeItem(page.getPageId(), seq, SimplePageItem.ASSIGNMENT, canvasAssignmentId, canvasTitle);
                      simplePageBean.saveItem(item);
                      if (!roles.isEmpty()) simplePageBean.setItemGroups(item, roles.toArray(new String[0]));
                      sequences.set(top, seq + 1);
                  }
              }
          } else if (resourceType.equals(LAR) && isCanvasSyllabus(resourceXml)) {
              // Handle Canvas syllabus (learning-application-resource with intendeduse="syllabus")  
              if (syllabusManager != null) {
                  try {
                      String syllabusTitle = itemXml != null ? itemXml.getChildText(CC_ITEM_TITLE, ns.getNs()) : null;
                      if (syllabusTitle == null) syllabusTitle = "Canvas Syllabus";
                      
                      // Find HTML file in resource
                      String syllabusContent = "";
                      List<Element> files = resourceXml.getChildren(FILE, ns.getNs());
                      for (Element file : files) {
                          String fileHref = file.getAttributeValue(HREF);
                          if (fileHref != null && fileHref.endsWith(".html")) {
                              try (InputStream htmlStream = loader.getFile(fileHref)) {
                                  if (htmlStream != null) {
                                      syllabusContent = new String(htmlStream.readAllBytes(), StandardCharsets.UTF_8);
                                      break;
                                  }
                              } catch (Exception e) {
                                  log.warn("Failed to load Canvas syllabus HTML: {}", fileHref, e);
                              }
                          }
                      }
                      
                      // Create or get syllabus item for site
                      SyllabusItem syllabusItem = syllabusManager.getSyllabusItemByContextId(siteId);
                      if (syllabusItem == null) {
                          syllabusItem = syllabusManager.createSyllabusItem(simplePageBean.getCurrentUserId(), siteId, null);
                      }
                      
                      // Create syllabus data entry
                      int position = syllabusManager.findLargestSyllabusPosition(syllabusItem) + 1;
                      syllabusManager.createSyllabusDataObject(syllabusTitle, position,
                              syllabusContent, "yes", SyllabusData.ITEM_POSTED, "none", null, null, false, null, null, syllabusItem);
                      
                      log.debug("Created Canvas syllabus entry: {}", syllabusTitle);
                      
                  } catch (Exception e) {
                      log.warn("Failed to import Canvas syllabus: {}", e.getMessage(), e);
                  }
              }
          } else if (resourceType.equals(ASSIGNMENT)) {
              Element assignXml = null;
              String filename = getFileName(resourceXml);
              if (filename != null) {
                  assignXml = parser.getXML(loader, filename);
              } else {
                  assignXml = resourceXml.getChild(ASSIGNMENT, ns.assign_ns());
              }
              Namespace assignNs = ns.assign_ns();

              // filebase will be directory name for discussion.xml, since attachments are relative to that
              String filebase = "";
              if (filename != null) {
                  filebase = filename;
                  int slash = filebase.lastIndexOf("/");
                  if (slash >= 0) filebase = filebase.substring(0, slash + 1); // include trailing slash
              }

              String base = baseUrl;
              if (filename != null) {
                  base = baseUrl + filename;
                  int slash = base.lastIndexOf("/");
                  if (slash >= 0) base = base.substring(0, slash + 1); // include trailing slash
              }

              // collection id rather than URL
              String baseDir = baseName;
              if (filename != null) {
                  baseDir = baseName + filename;
                  int slash = baseDir.lastIndexOf("/");
                  if (slash >= 0) baseDir = baseDir.substring(0, slash + 1); // include trailing slash
              }

              // let importobject handle most of this, but we have to
              // process the attachments to make sure they're present

              Element attachmentlist = assignXml.getChild(ATTACHMENTS, assignNs);
              List<Element> attachments = new ArrayList<Element>();
              if (attachmentlist != null) attachments = attachmentlist.getChildren();
              List<String> attachmentHrefs = new ArrayList<String>();
              // note that we ignore the role attribute. No obvious way to implement it.
              for (Element a : attachments) {
                  // file has to be there
                  addFile(removeDotDot(filebase + a.getAttributeValue(HREF)));
                  attachmentHrefs.add(a.getAttributeValue(HREF));
              }

              // need to prevent duplicates, as we're likely to see the same resource more than once.
              // Remember that we've produced this resource ID.
              String assignmentId = assignsAdded.get(resourceId);
              if (assignmentId == null) {
                  AssignmentInterface a = (AssignmentInterface) assigntool;
                  assignmentId = a.importObject(assignXml, assignNs, base, baseDir, attachmentHrefs, noPage); // sakaiid for assignment
                  if (assignmentId != null) assignsAdded.put(resourceId, assignmentId);
              }

              if (assignmentId != null && !noPage) {
                  SimplePageItem item = simplePageToolDao.makeItem(page.getPageId(), seq, SimplePageItem.ASSIGNMENT, assignmentId, title);
                  simplePageBean.saveItem(item);
                  if (!roles.isEmpty()) simplePageBean.setItemGroups(item, roles.toArray(new String[0]));
                  sequences.set(top, seq + 1);
              }

          } else if (((resourceType.equals(CC_WEBCONTENT) || (resourceType.equals(UNKNOWN))) && noPage) || resourceType.equals(LAR)) {
              // handled elsewhere
          }
          if (resourceType.equals(UNKNOWN)) {
              badTypes.add(resourceXml.getAttributeValue(TYPE));
              log.debug("resource of an unknown type found: {}", resourceXml.getAttributeValue(TYPE));
          }
      } catch (Exception e) {
          log.warn("failure while processing resource with id=[{}] and type=[{}], {}", resourceId, resourceType, e.toString());
      }
  }

  public Map<String, String> readItemMetaData(Element metadata) {
	  Map<String, String> itemMetaData = new HashMap<>();
	  Element element;

	  try {
		  // This is the structure for item metadata
		  //           <metadata>
		  //               <lom:lom>
		  //                 <lom:general>
		  //                   <lom:structure>
		  //                     <lom:source></lom:source>
		  //                     <lom:value></lom:value>
		  //                   </lom:structure>
		  //                 </lom:general>
		  //               </lom:lom>
		  //             </metadata>
		  element = metadata.getChild(CC_ITEM_METADATA, ns.cc_ns())
				  .getChild(LOM_LOM, ns.lom_ns())
				  .getChild(LOM_GENERAL, ns.lom_ns())
				  .getChild(LOM_STRUCTURE, ns.lom_ns());

		  List<Element> children = element.getChildren();
		  Iterator<Element> iterator = children.iterator();
		  // read the source and value pairs and add them to a Map
		  while (iterator.hasNext()) {
			  Element source = iterator.next();
			  if (!iterator.hasNext()) break; // exit if missing value
			  Element value = iterator.next();
			  if (!"source".equals(source.getName())) {
				  log.warn("deserializing item metadata expected to find a source element but instead found: [{}]", source.getName());
				  break;
			  }
			  if (!"value".equals(value.getName())) {
				  log.warn("deserializing item metadata expected to find a value element but instead found: [{}]", value.getName());
				  break;
			  }
			  itemMetaData.put(source.getText(), value.getText());
		  }
	  } catch (Exception e) {
          // when an item doesn't have the structure above it is expected to end up here, so level is debug
		  log.debug("could not deserialize item metadata (this is expected if it is missing), {}", e.toString());
	  }

	  return itemMetaData;
  }

  public void addAttachment(String attachment_path) {
	  log.debug("adding an attachment: "+attachment_path);
  }

  public void endDiscussion() {
	  log.debug("end discussion");
  }

  public void startManifest() {
	  log.debug("start manifest");
  }

  public void checkCurriculum(Element the_xml) {
      Element md = the_xml.getChild("curriculumStandardsMetadataSet",ns.csmd_ns());
      if (md != null) {
	  if (md.getChild("curriculumStandardsMetadata",ns.csmd_ns()) != null) {
	      usesCurriculum = true;
	  }
      }
  }

  public void setManifestXml(Element the_xml) {
      manifestXml = the_xml;
      log.debug("manifest xml: {}", the_xml);

      // Identify Canvas entity files early so they can be excluded from resource processing
      identifyCanvasEntityFiles(the_xml);
  }

  public void setCanvasModuleMetaXml(Element the_xml) {
      canvasModuleMeta = the_xml;
      log.debug("canvas_meta_module xml: {}", the_xml);
  }


  public void endManifest() {
	  log.debug("end manifest");
      if (usesRole)
	  simplePageBean.setErrKey("simplepage.cc-uses-role", null);
      // the pattern match is restricted enough that we can actually do it
      // if (usesPatternMatch)
      //  simplePageBean.setErrKey("simplepage.import_cc_usespattern", null);
      if (usesCurriculum)
	  simplePageBean.setErrKey("simplepage.cc-uses-curriculum", null);
      if (badTypes.size() > 0) {
	  String typeList = "";
	  if (badTypeNames == null)
	      badTypeNames = getBadTypeNames();
	  for (String badType: badTypes) {
	      String typeName = badTypeNames.get(badType);
	      if (typeName == null)
		  typeName = badType;
	      typeList = typeList + ", " + typeName;
	  }
	  simplePageBean.setErrKey("simplepage.cc-has-badtypes", typeList.substring(2));
      }
  }

  public void startDiscussion(String topic_name, String text_type, String text, boolean isProtected) {
      if (log.isDebugEnabled()){
	  log.debug("start a discussion: "+topic_name);
	  log.debug("text type: "+text_type);
	  log.debug("text: "+text); 
	  log.debug("protected: "+isProtected);
      }
  }

  public void endWebLink() {
      if (log.isDebugEnabled())
	  log.debug("end weblink");
  }

  public void startWebLink(String the_title, String the_url, String the_target, String the_window_features, boolean isProtected) {
      if (log.isDebugEnabled()) {
	  log.debug("start weblink: "+the_title);
	  log.debug("link to: "+the_url);
	  log.debug("target window: "+the_target);
	  log.debug("window features: "+the_window_features);
	  log.debug("protected: "+isProtected);
      }
  }
 
  public void setWebLinkXml(Element the_link) {
      if (log.isDebugEnabled())
	  log.debug("weblink xml: "+the_link);
  }

  public void preProcessFile(String the_file_id) {
	  String original_file_id = the_file_id;
	  //Restrict file length to 250
	  if ((baseName + original_file_id).length() > 250) {
		 the_file_id = original_file_id.substring(0,250-baseName.length()); 
		 log.debug("Length restricted, new file name" + baseName + the_file_id);
	  }
	  fileNames.put(original_file_id,the_file_id);
  }

  public void addFile(String resourceId, Element element) {
	  // These can be inlined and processed as standard text
	  String href = element.getAttributeValue(HREF);
	  String sakaiId = baseName + href;
	  String extension = Validator.getFileExtension(sakaiId);
	  String mime = ContentTypeImageService.getContentType(extension);
      Map<String, String> itemMetadata = itemsMetaDataAdded.get(resourceId);
      boolean inline = (itemMetadata != null) && BooleanUtils.toBoolean(itemMetadata.get("inline.lessonbuilder.sakaiproject.org"));
      if ((inline || forceInline) && StringUtils.startsWith(mime, "text/")) {
          log.info("Inlining content with resource id=[{}], href=[{}]", resourceId, href);
      } else {
          addFile(href);
      }
  }



  public void addFile(String the_file_id) {

      if (filesAdded.contains(the_file_id))
	  return;
      
      // Skip files that are part of Canvas entities - they should not be imported as resources
      if (canvasEntityFiles.contains(the_file_id)) {
          log.debug("Skipping Canvas entity file from resource import: {}", the_file_id);
          return;
      }

      InputStream infile = null;
      for (int tries = 1; tries < 3; tries++) {
        try {
	  infile = utils.getFile(the_file_id);
	  String name = the_file_id;
	  int slash = the_file_id.lastIndexOf("/");
	  if (slash >=0 )
	      name = name.substring(slash+1);
	  String extension = Validator.getFileExtension(name);
	  String type = ContentTypeImageService.getContentType(extension);
	  
	  //Now the new truncated name from the map
	  if (fileNames.containsKey(the_file_id)) {
		  the_file_id = fileNames.get(the_file_id);
          log.debug("Found {} in pre-load map", the_file_id);
	  }
	  else {
          log.info("Could not find {} in preload map, File may not work.", the_file_id);
	  }

	  log.debug("Preparing to add file '{}' (length: {}) with ID '{}' (length: {})", baseName, baseName.length(), the_file_id, the_file_id.length());
	  
	  ContentResourceEdit edit = ContentHostingService.addResource(baseName + the_file_id);

	  edit.setContentType(type);
	  edit.setContent(infile);
	  edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
	  // if roles specified for this resource and student not in it, hide it
	  if (roles != null && !roles.contains("Learner"))
	      edit.setAvailability(true, null, null);
	  ContentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
	  filesAdded.add(the_file_id);

        } catch (IdUsedException e) {
	  // remove existing if we are importing whole site.
	  // otherwise this is an error (and should be impossible, as this is a new directory)
	  if (importtop && tries == 1) {
	      try {
		  ContentHostingService.removeResource(baseName + the_file_id);
		  continue;
	      } catch (Exception e1) {
	      }
	  }
	  simplePageBean.setErrKey("simplepage.create.resource.failed", e + ": " + the_file_id);
	  log.info("CC loader: unable to get file " + the_file_id + " error: " + e);
        } catch (Exception e) {
	  simplePageBean.setErrKey("simplepage.create.resource.failed", e + ": " + the_file_id);
	  log.info("CC loader: unable to get file " + the_file_id + " error: " + e);
        }
        break;  // if we get to the end, no need to retry; really a goto would be clearer
      }
  }

  public void endWebContent() {
      if (log.isDebugEnabled())
	  log.debug("ending webcontent");
  }

  public void startWebContent(String entry_point, boolean isProtected) {
      if (log.isDebugEnabled()) {
	  log.debug("start web content");
	  log.debug("protected: "+isProtected);
	  if (entry_point!=null) {
	      log.debug("entry point is: "+entry_point);
	  }
      }
  }

  public void endLearningApplicationResource() {
      if (log.isDebugEnabled())
	  log.debug("end learning application resource");
  }

  public void startLearningApplicationResource(String entry_point, boolean isProtected) {
      if (log.isDebugEnabled()) {
	  log.debug("start learning application resource");
	  log.debug("protected: "+isProtected);
	  if (entry_point!=null) {
	      log.debug("entry point is: "+entry_point);
	  }
      }
  }

  public void endAssessment() {
      if (log.isDebugEnabled())
	  log.debug("end assessment");    
  }

  public void setAssessmentXml(Element xml) {
      if (log.isDebugEnabled())
	  log.debug("assessment xml: "+xml);
  }

  public void startAssessment(String the_file_name, boolean isProtected) {
      if (log.isDebugEnabled()) {
	  log.debug("start assessment contained in: "+the_file_name);
	  log.debug("protected: "+isProtected);
      }
  }

  public void endQuestionBank() {
      if (log.isDebugEnabled())
	  log.debug("end question bank");
  }

  public void setQuestionBankXml(Element the_xml) {
      if (log.isDebugEnabled())
	  log.debug("question bank xml: "+the_xml);
  }

  public void startQuestionBank(String the_file_name, boolean isProtected) {
      if (log.isDebugEnabled()) {
	  log.debug("start question bank in: "+the_file_name);
	  log.debug("protected: "+isProtected);
      }
  }

  public void setAuthorizationServiceXml(Element the_node) {
      if (log.isDebugEnabled())
	  log.debug(the_node.toString());
  }

  public void setAuthorizationService(String cartridgeId, String webservice_url) {
      if (log.isDebugEnabled())
	  log.debug("adding auth service for "+cartridgeId+" @ "+webservice_url);
  }

  public void endAuthorization() {
      if (log.isDebugEnabled())
	  log.debug("end of authorizations");
  }

  public void startAuthorization(boolean isCartridgeScope, boolean isResourceScope, boolean isImportScope) {
      if (log.isDebugEnabled()) {
	  log.debug("start of authorizations");
	  log.debug("protect all: "+isCartridgeScope);
	  log.debug("protect resources: "+isResourceScope);
	  log.debug("protect import: "+isImportScope);
      }
  }

  public void endManifestMetadata() {
      if (log.isDebugEnabled())
	  log.debug("end of manifest metadata");
  }

  public void startManifestMetadata(String schema, String schema_version) {
      if (log.isDebugEnabled()) {
	  log.debug("start manifest metadata");
	  log.debug("schema: "+schema);
	  log.debug("schema_version: "+schema_version);
      }
  }
 
  public void setPresentationXml(Element the_xml) {
      if (log.isDebugEnabled())
	  log.debug("QTI presentation xml: "+the_xml);
  }

  public void setQTICommentXml(Element the_xml) {
      if (log.isDebugEnabled())
	  log.debug("QTI comment xml: "+the_xml);
  }

  public void setSection(String ident, String title) {
      if (log.isDebugEnabled()) {
	  log.debug("set section ident: "+ident);
	  log.debug("set section title: "+title);
      }
  }

  public void setSectionXml(Element the_xml) {
      if (log.isDebugEnabled())
	  log.debug("set Section Xml: "+the_xml);
  }

  public void endQTIMetadata() {
      if (log.isDebugEnabled())
	  log.debug("end of QTI metadata");
  }

  public void setManifestMetadataXml(Element the_md) {
      if (log.isDebugEnabled())
	  log.debug("manifest md xml: "+the_md);    
      // NOTE: need to handle languages
      if (the_md != null) {
      Element general = the_md.getChild(GENERAL, ns.getLom());
      if (general != null) {
	  Element tnode = general.getChild(TITLE, ns.getLom());
	  if (tnode != null) {
    	  title = tnode.getChildTextTrim(LANGSTRING, ns.getLom());
	      if (title == null || title.equals(""))
	    	  title = tnode.getChildTextTrim(STRING, ns.getLom());
	  }
	  Element tdescription=general.getChild(DESCRIPTION, ns.getLom());
	  if (tdescription != null) {
	      description = tdescription.getChildTextTrim(STRING, ns.getLom());
	  }

      }
      }
      if (title == null || title.equals(""))
	  title = "Cartridge";
      if ("".equals(description))
	  description = null;
      ContentCollection baseCollection = makeBaseFolder(title);
      baseName = baseCollection.getId();
      baseUrl = baseCollection.getUrl();
      // kill the hostname part. We want to use relative URLs
      int relPart = baseUrl.indexOf("/access/");
      if (relPart >= 0)
	  baseUrl = baseUrl.substring(relPart);
      //Start a folder to hold the entire sites content rather than create new top level pages
      boolean singlePage = ServerConfigurationService.getBoolean("lessonbuilder.cc.import.singlepage", false);
      if (singlePage == true)
    	  startCCFolder(null);

  }

  public void setResourceMetadataXml(Element the_md) {
      // version 1 and higher are different formats, hence a slightly weird test
      Iterator mdroles = the_md.getDescendants(new ElementFilter("intendedEndUserRole", ns.lom_ns()));
      if (mdroles != null) {
      while (mdroles.hasNext()) {
	  Element role = (Element)mdroles.next();
	      Iterator values = role.getDescendants(new ElementFilter("value", ns.lom_ns()));
	      if (values != null) {
		  while (values.hasNext()) {
	  if (roles == null)
	      roles = new HashSet<String>();
		      Element value = (Element)values.next();
		      String roleName = value.getTextTrim();
		      roles.add(roleName);
		  }
	      }
	  }
      }

      if (log.isDebugEnabled())
	  log.debug("resource md xml: "+the_md); 
  }

  public void addQTIMetadataField(String label, String entry) {
      if (log.isDebugEnabled()) {
	  log.debug("QTI md label: "+label);
	  log.debug("QTI md entry: "+entry);
      }
}

  public void setQTIComment(String the_comment) {
      if (log.isDebugEnabled())
	  log.debug("QTI comment: "+the_comment);
  }

  public void endDependency() {
      if (log.isDebugEnabled())
	  log.debug("end dependency");
  }

  public void startDependency(String source, String target) {
      if (log.isDebugEnabled())
	  log.debug("start dependency- resource : "+source+" is dependent upon: "+target);
  }

  public void startResource(String id, boolean isProtected) {

      roles = null;
      if (log.isDebugEnabled())
	  log.debug("start resource: "+id+ " protected: "+isProtected);
  }

  public void setResourceXml(Element the_xml) {
      if (log.isDebugEnabled())
	  log.debug("resource xml: "+the_xml);
  }

  public void endResource() {
      if (log.isDebugEnabled())
	  log.debug("end resource"); 
  }

  public void addAssessmentItem(QTIItem the_item) {
      if (log.isDebugEnabled())
	  log.debug("add QTI assessment item: "+the_item.toString());
    
  }

  public void addQTIMetadataXml(Element the_md) {
      if (log.isDebugEnabled())
	  log.debug("add QTI metadata xml: "+the_md);
    
  }

  public void startQTIMetadata() {
      if (log.isDebugEnabled())
	  log.debug("start QTI metadata");
  }

  public void setDiscussionXml(Element the_element) {
      if (log.isDebugEnabled())
	  log.debug("set discussion xml: "+the_element); 
  }

  public void addQuestionBankItem(QTIItem the_item) {
      if (log.isDebugEnabled())
	  log.debug("add QTI QB item: "+the_item.toString()); 
  }

  public void setQuestionBankDetails(String the_ident) {
      if (log.isDebugEnabled())
	  log.debug("set qti qb details: "+the_ident);  
  }

  // xxx/abc/../ccc
  // xxx/ccc
  // xxx/../ccc
  // ccc

  public String removeDotDot(String s) {
      while (true) {
	  int i = s.indexOf("/../");
	  if (i < 1)
            return s;
	  int j = s.lastIndexOf("/", i-1);
	  if (j < 0)
            j = 0;
	  else
            j = j + 1;
	  s = s.substring(0, j) + s.substring(i+4);
      }
  }
}
