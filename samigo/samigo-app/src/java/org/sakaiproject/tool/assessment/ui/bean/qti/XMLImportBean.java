/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.ui.bean.qti;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.contentpackaging.ImportService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.w3c.dom.Document;

 
/**
 * <p>Bean for QTI Import Data</p>
 */

public class XMLImportBean implements Serializable
{
	
	  /** Use serialVersionUID for interoperability. */
	  private final static long serialVersionUID = 418920360211039758L;
	  private static Log log = LogFactory.getLog(XMLImportBean.class);
	  
  private int qtiVersion;
  private String uploadFileName;
  private String importType;
  private String pathToData;
  private AuthorBean authorBean;
  private AssessmentBean assessmentBean;
  private ItemAuthorBean itemAuthorBean;
  private QuestionPoolBean questionPoolBean;
  private boolean isCP;
  
  private static final GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();
  private static final boolean integrated =
      IntegrationContextFactory.getInstance().isIntegrated();


  public XMLImportBean()
  {
    qtiVersion = QTIVersion.VERSION_1_2;//default
  }

  // put tests here...
  public static void main(String[] args)
  {
    //XMLImportBean XMLImportBean1 = new XMLImportBean();
  }

  public void importAssessment(ValueChangeEvent e)
  {
	  String sourceType = ContextUtil.lookupParam("sourceType");
	  String uploadFile = (String) e.getNewValue();
	  if ("respondus".equals(sourceType)) {
		  if(uploadFile.toLowerCase().endsWith(".zip")) {
			  isCP = true;
			  importAssessment(uploadFile, true, true);
		  }
		  else {
			  isCP = false;
			  importAssessment(uploadFile, false, true);
		  }
	  }
	  else {
		  if(uploadFile.toLowerCase().endsWith(".zip")) {
			  isCP = true;
			  importAssessment(uploadFile,true, false);
		  }
		  else {
			  isCP = false;
			  importAssessment(uploadFile, false, false);
		  }
	  }
  }
  
  /**
   * Value change on upload
   * @param e the event
   */
  
  public void importAssessment(String uploadFile, boolean isCP, boolean isRespondus)
  {
	String filename = uploadFile;
	String unzipLocation = null;
	if (isCP) {
		ImportService importService = new ImportService();
		unzipLocation = importService.unzipImportFile(uploadFile);
		filename = unzipLocation + "/" + importService.getQTIFilename();
	}
    try
    {
      processFile(filename, isRespondus);
    }
    catch (Exception ex)
    {
      ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorImportExport");
      FacesMessage message = new FacesMessage( rb.getString("import_err") + ex );
      FacesContext.getCurrentInstance().addMessage(null, message);
    }
    finally {
      // remove unsuccessful file
      log.debug("****remove unsuccessful filename="+filename);
      File f1 = new File(filename);
      f1.delete();
      if (isCP) {
    	  File f2 = new File(uploadFile);
    	  f2.delete();
    	  File f3 = new File(unzipLocation);
    	  deleteDirectory(f3);
      }
    }
  }

  /**
   *
   * @return QTI version of XML file
   */
  public int getQtiVersion()
  {
    return qtiVersion;
  }

  /**
   *
   * @param qtiVersion QTI version of XML file
   */
  public void setQtiVersion(int qtiVersion)
  {
    if (!QTIVersion.isValid(qtiVersion))
    {
      throw new IllegalArgumentException("NOT Legal Qti Version.");
    }
    this.qtiVersion = qtiVersion;
  }

  /**
   *
   * @return file name and path
   */
  public String getUploadFileName()
  {
    return uploadFileName;
  }

  /**
   *
   * @param uploadFileName file name and path
   */
  public void setUploadFileName(String uploadFileName)
  {
    this.uploadFileName = uploadFileName;
  }

  /**
   * A, S, I
   * @return type of upload
   */
  public String getImportType()
  {
    return importType;
  }

  /**
   * A, S, I
   * @param importType A, S, or I
   */
  public void setImportType(String importType)
  {
    this.importType = importType;
  }

  private void processFile(String fileName, boolean isRespondus)
  {
    itemAuthorBean.setTarget(ItemAuthorBean.FROM_ASSESSMENT); // save to assessment

    AssessmentService assessmentService = new AssessmentService();
    // Create an assessment based on the uploaded file
    AssessmentFacade assessment = createImportedAssessment(fileName, qtiVersion, isRespondus);

    
    // change grading book settings if there is no gradebook in the site
    boolean hasGradebook = false;
   GradebookService g = null;
   if (integrated){
     g = (GradebookService) SpringBeanLocator.getInstance().
          getBean("org.sakaiproject.service.gradebook.GradebookService");
   }
   try{
     if (gbsHelper.isAssignmentDefined(assessment.getTitle(), g)){
   	  hasGradebook= true;
      }
   }
   catch(Exception e){
     log.debug("Error calling gradebookHelper");
   }
   

   // gradebook options, don't know how this is supposed to work, leave alone for now
   if (!hasGradebook && assessment!=null){
   	assessment.getEvaluationModel().setToGradeBook(EvaluationModelIfc.NOT_TO_GRADEBOOK.toString());
   }
   assessmentService.saveAssessment(assessment);
   

    // Go to editAssessment.jsp, so prepare assessmentBean
    assessmentBean.setAssessment(assessment);
    // reset in case anything hanging around
    authorBean.setAssessTitle("");
    authorBean.setAssessmentDescription("");
    authorBean.setAssessmentTypeId("");
    authorBean.setAssessmentTemplateId(AssessmentTemplateFacade.DEFAULTTEMPLATE.toString());

    // update core AssessmentList: get the managed bean, author and set the list
    
    ArrayList list = assessmentService.getBasicInfoOfAllActiveAssessments(
                     AssessmentFacadeQueries.TITLE,true);
    Iterator iter = list.iterator();
	while (iter.hasNext()) {
		AssessmentFacade assessmentFacade= (AssessmentFacade) iter.next();
		assessmentFacade.setTitle(FormattedText.convertFormattedTextToPlaintext(assessmentFacade.getTitle()));
	}
    authorBean.setAssessments(list);
  }
  
  private void deleteDirectory(File directory) {
	  if(directory.exists()) {
		  File[] files = directory.listFiles();
		  for(int i=0; i < files.length; i++) {
			  if(files[i].isDirectory()) {
				  deleteDirectory(files[i]);
			  }
			  else {
				  files[i].delete();
			  }
		  }
	  }
	  directory.delete();
  }

  /**
   * Create assessment from uploaded QTI XML
   * @param fullFileName file name and path
   * @param qti QTI version
   * @return
   */
  private AssessmentFacade createImportedAssessment(String fullFileName, int qti)
  {
	  return createImportedAssessment(fullFileName, qti, false);
  }
  
  private AssessmentFacade createImportedAssessment(String fullFileName, int qti, boolean isRespondus)
  {
    //trim = true so that xml processing instruction at top line, even if not.
    Document document = XmlUtil.readDocument(fullFileName, true);
    QTIService qtiService = new QTIService();
    if (isCP) {
    	return qtiService.createImportedAssessment(document, qti, fullFileName.substring(0, fullFileName.lastIndexOf("/")), isRespondus);
    }
    else {
    	return qtiService.createImportedAssessment(document, qti, null, isRespondus);
    }
  }

  public AuthorBean getAuthorBean()
  {
    return authorBean;
  }

  public void setAuthorBean(AuthorBean authorBean)
  {
    this.authorBean = authorBean;
  }

  public AssessmentBean getAssessmentBean()
  {
    return assessmentBean;
  }

  public void setAssessmentBean(AssessmentBean assessmentBean)
  {
    this.assessmentBean = assessmentBean;
  }

  public ItemAuthorBean getItemAuthorBean()
  {
    return itemAuthorBean;
  }

  public void setItemAuthorBean(ItemAuthorBean itemAuthorBean)
  {
    this.itemAuthorBean = itemAuthorBean;
  }

  /**
   * Value change on upload
   * @param e the event
   */
  public void importPoolFromQti(ValueChangeEvent e)
  {
    String uploadFile = (String) e.getNewValue();

    try
    {
    	processAsPoolFile(uploadFile);
    }
    catch (Exception ex)
    {
      ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorImportExport");
      FacesMessage message = new FacesMessage( rb.getString("import_err") + ex );
      FacesContext.getCurrentInstance().addMessage(null, message);
    }
  }
   
  
  /**
   * Process uploaded QTI XML 
   * assessment as question pool
   */
  private void processAsPoolFile(String uploadFile)
  {
    itemAuthorBean.setTarget(ItemAuthorBean.FROM_QUESTIONPOOL); // save to questionpool

    // Get the file name
    String fileName = uploadFile;

    // Create a questionpool based on the uploaded assessment file
    QuestionPoolFacade questionPool = createImportedQuestionPool(fileName, qtiVersion);

    // remove uploaded file
    try{
      //System.out.println("****filename="+fileName);
      File upload = new File(fileName);
      upload.delete();
    }
    catch(Exception e){
      System.out.println(e.getMessage());
    }
  }
  
  /**
   * Create questionpool from uploaded QTI assessment XML
   * @param fullFileName file name and path
   * @param qti QTI version
   * @return
   */
  private QuestionPoolFacade createImportedQuestionPool(String fullFileName, int qti)
  {
    //trim = true so that xml processing instruction at top line, even if not.
    Document document = XmlUtil.readDocument(fullFileName, true);
    QTIService qtiService = new QTIService();
    return qtiService.createImportedQuestionPool(document, qti);
  }  
  
  public QuestionPoolBean getQuestionPoolBean()
  {
    return questionPoolBean;
  }

  public void setQuestionPoolBean(QuestionPoolBean questionPoolBean)
  {
    this.questionPoolBean = questionPoolBean;
  }  
 
  public String getPathToData()
  {
    return pathToData;
  }

  public void setPathToData(String pathToData)
  {
    this.pathToData = pathToData;
  }  
}
