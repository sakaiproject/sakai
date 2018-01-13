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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
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
import org.sakaiproject.tool.assessment.ui.listener.util.TimeUtil;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.Document;

/**
 * <p>Bean for QTI Import Data</p>
 */
@Slf4j
public class XMLImportBean implements Serializable
{
	
	  /** Use serialVersionUID for interoperability. */
	  private final static long serialVersionUID = 418920360211039758L;
	  
  private int qtiVersion;
  private String uploadFileName;
  private String importType;
  private String pathToData;
  private AuthorBean authorBean;
  private AssessmentBean assessmentBean;
  private ItemAuthorBean itemAuthorBean;
  private QuestionPoolBean questionPoolBean;
  private boolean isCP;
  private String importType2;
  
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

	  if (uploadFile!= null && uploadFile.startsWith("SizeTooBig:")) {
		  FacesContext context = FacesContext.getCurrentInstance();
		  ExternalContext external = context.getExternalContext();
		  String paramValue = ((Long)((ServletContext)external.getContext()).getAttribute("FILEUPLOAD_SIZE_MAX")).toString();
		  Long sizeMax = null;
		  float sizeMax_float = 0f;
		  if (paramValue != null) {
			  sizeMax = Long.parseLong(paramValue);
			  sizeMax_float = sizeMax.floatValue()/1024;
		  } 
		  int sizeMax_int = Math.round(sizeMax_float);
		  ResourceLoader rb =new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorImportExport");
		  String sizeTooBigMessage = MessageFormat.format(rb.getString("import_size_too_big"), uploadFile.substring(11), sizeMax_int);
	      FacesMessage message = new FacesMessage(sizeTooBigMessage);
	      FacesContext.getCurrentInstance().addMessage(null, message);
	      // remove unsuccessful file
	      log.debug("****Clean up file:"+uploadFile);
	      File upload = new File(uploadFile);
	      upload.delete();
	      authorBean.setImportOutcome("importAssessment");
	      return;
	  }
	  authorBean.setImportOutcome("author");
  
	  if ("2".equals(sourceType)) {
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
	boolean fileNotFound = false;
	if (isCP) {
		ImportService importService = new ImportService();
		unzipLocation = importService.unzipImportFile(uploadFile);
		filename = unzipLocation + "/" + importService.getQTIFilename();
	}
    try
    {
      processFile(filename, uploadFile, isRespondus);
    }
    catch (FileNotFoundException fnfex)
    {
      fileNotFound = true;
      ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorImportExport");
      FacesMessage message = new FacesMessage( rb.getString("import_qti_not_found") );
      FacesContext.getCurrentInstance().addMessage(null, message);
    }
    catch (Exception ex)
    {
      ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorImportExport");
      FacesMessage message = new FacesMessage( rb.getString("import_err") );
      FacesContext.getCurrentInstance().addMessage(null, message);
    }
    finally {
      boolean success = false;    	
      // remove unsuccessful file
      if (!fileNotFound) {
        log.debug("****Clean up file: "+filename);
        File f1 = new File(filename);
        success = f1.delete();
        if (!success) {
    	  log.error ("Failed to delete file " + filename);
        }
      }
      if (isCP) {
    	  File f2 = new File(uploadFile);
    	  success = f2.delete();
          if (!success) {
        	  log.error ("Failed to delete file " + uploadFile);
          }
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
  
  public String getImportType2()
  {
    return importType2;
  }

  /**
   * A, S, I
   * @param importType A, S, or I
   */
  public void setImportType2(String importType2)
  {
    this.importType2 = importType2;
  }

  private void processFile(String fileName, String uploadFile, boolean isRespondus) throws Exception
  {
    itemAuthorBean.setTarget(ItemAuthorBean.FROM_ASSESSMENT); // save to assessment

    AssessmentService assessmentService = new AssessmentService();
    // Create an assessment based on the uploaded file
    List failedMatchingQuestions = new ArrayList();
    AssessmentFacade assessment = createImportedAssessment(fileName, qtiVersion, isRespondus, failedMatchingQuestions);
    if (failedMatchingQuestions.size() > 0)
    {
      String importedFilename = getImportedFilename(uploadFile);	
      ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorImportExport");
      StringBuffer sb = new StringBuffer("\"");
      sb.append(importedFilename);
      sb.append("\" ");
      sb.append(rb.getString("respondus_matching_err_1"));
      sb.append(" ");
      for(int i = 0; i < failedMatchingQuestions.size() - 1; i++) {
    	  sb.append(" ");
    	  sb.append(failedMatchingQuestions.get(i));
    	  sb.append(", ");
      }
      sb.append(failedMatchingQuestions.get(failedMatchingQuestions.size() - 1));
      sb.append(". ");
      sb.append(rb.getString("respondus_matching_err_2"));
      FacesMessage message = new FacesMessage(sb.toString());
      FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    // change grading book settings if there is no gradebook in the site
    boolean hasGradebook = false;
    GradebookExternalAssessmentService g = null;
   if (integrated){
     g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().
          getBean("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
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
    
    List list = assessmentService.getBasicInfoOfAllActiveAssessments(
                     AssessmentFacadeQueries.TITLE,true);
	TimeUtil tu = new TimeUtil();
	String display_dateFormat= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","output_data_picker_w_sec");
	SimpleDateFormat displayFormat = new SimpleDateFormat(display_dateFormat, new ResourceLoader().getLocale());
    Iterator iter = list.iterator();
	while (iter.hasNext()) {
		AssessmentFacade assessmentFacade= (AssessmentFacade) iter.next();
		assessmentFacade.setTitle(FormattedText.convertFormattedTextToPlaintext(assessmentFacade.getTitle()));
		try {
			String lastModifiedDateDisplay = tu.getDisplayDateTime(displayFormat, assessmentFacade.getLastModifiedDate(), false);
			assessmentFacade.setLastModifiedDateForDisplay(lastModifiedDateDisplay);  
		} catch (Exception ex) {
			log.warn("Unable to format date: " + ex.getMessage());
		}
	}
    authorBean.setAssessments(list);
  }
  
  private String getImportedFilename(String filename) {
	  String temp_filename_1 = filename.substring(filename.lastIndexOf("/") + 1);
	  String temp_filename_2 = temp_filename_1.substring(temp_filename_1.indexOf("."));
	  String temp_filename_3 = temp_filename_1.substring(0, temp_filename_1.substring(0, temp_filename_1.indexOf(".")).lastIndexOf("_"));
	  String final_filename = temp_filename_3 + temp_filename_2;
	  return final_filename;
  }
  private void deleteDirectory(File directory) {
	  if(directory.exists()) {
		  File[] files = directory.listFiles();
		  for(int i=0; i < files.length; i++) {
			  if(files[i].isDirectory()) {
				  deleteDirectory(files[i]);
			  }
			  else {
				boolean success = files[i].delete();
    				if (!success)
      					log.error("Delete Failed.");
			  }
		  }
	  }
		boolean success = directory.delete();
    		if (!success)
  			log.error("Delete Failed.");
  }

  /**
   * Create assessment from uploaded QTI XML
   * @param fullFileName file name and path
   * @param qti QTI version
   * @param isRespondus true/false
   * @return
   */
  
  private AssessmentFacade createImportedAssessment(String fullFileName, int qti, boolean isRespondus, List failedMatchingQuestions) throws Exception
  {
    //trim = true so that xml processing instruction at top line, even if not.
    Document document = null;
	try {
		document = XmlUtil.readDocument(fullFileName, true);
	} catch (Exception e) {
		throw(e);
	}
    QTIService qtiService = new QTIService();
    if (isCP) {
    	return qtiService.createImportedAssessment(document, qti, fullFileName.substring(0, fullFileName.lastIndexOf("/")), isRespondus, failedMatchingQuestions);
    }
    else {
    	return qtiService.createImportedAssessment(document, qti, null, isRespondus, failedMatchingQuestions);
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
      FacesMessage message = new FacesMessage( rb.getString("import_err_pool") );
      FacesContext.getCurrentInstance().addMessage(null, message);
    }
  }
   
  
  /**
   * Process uploaded QTI XML 
   * assessment as question pool
 * @throws Exception 
   */
  private void processAsPoolFile(String uploadFile) throws Exception
  {
    itemAuthorBean.setTarget(ItemAuthorBean.FROM_QUESTIONPOOL); // save to questionpool

    // Get the file name
    String fileName = uploadFile;

    // Create a questionpool based on the uploaded assessment file
    QuestionPoolFacade questionPool = createImportedQuestionPool(fileName, qtiVersion);

    // remove uploaded file
    try{
      log.debug("****filename="+fileName);
      File upload = new File(fileName);
      boolean success = upload.delete();
      if (!success)
	log.error ("Failed to delete file " + fileName);
    }
    catch(Exception e){
	log.error(e.getMessage(), e);
    }
  }
  
  /**
   * Create questionpool from uploaded QTI assessment XML
   * @param fullFileName file name and path
   * @param qti QTI version
   * @return
 * @throws Exception 
   */
  private QuestionPoolFacade createImportedQuestionPool(String fullFileName, int qti) throws Exception
  {
    //trim = true so that xml processing instruction at top line, even if not.
    Document document;
	try {
		document = XmlUtil.readDocument(fullFileName, true);
	} catch (Exception e) {
		throw(e);
	}
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
