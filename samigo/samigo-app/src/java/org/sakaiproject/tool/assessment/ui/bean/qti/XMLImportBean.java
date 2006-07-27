/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.ui.bean.qti;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.io.File;

import org.w3c.dom.Document;

import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;

/**
 * <p>Bean for QTI Import Data</p>
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class XMLImportBean implements Serializable
{
	
	  /** Use serialVersionUID for interoperability. */
	  private final static long serialVersionUID = 418920360211039758L;

  private int qtiVersion;
  private String uploadFileName;
  private String importType;
  private AuthorBean authorBean;
  private AssessmentBean assessmentBean;
  private ItemAuthorBean itemAuthorBean;
  private ResourceBundle rb = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.AuthorImportExport");

  public XMLImportBean()
  {
    qtiVersion = QTIVersion.VERSION_1_2;//default
  }

  // put tests here...
  public static void main(String[] args)
  {
    //XMLImportBean XMLImportBean1 = new XMLImportBean();
  }

  /**
   * Value change on upload
   * @param e the event
   */
  public void importFromQti(ValueChangeEvent e)
  {
    String uploadFile = (String) e.getNewValue();

    try
    {
      processFile(uploadFile);
    }
    catch (Exception ex)
    {
      FacesMessage message = new FacesMessage( rb.getString("import_err") + ex );
      FacesContext.getCurrentInstance().addMessage(null, message);
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

  private void processFile(String uploadFile)
  {
    itemAuthorBean.setTarget(ItemAuthorBean.FROM_ASSESSMENT); // save to assessment

    // Get the file name
    String fileName = uploadFile;

    // Create an assessment based on the uploaded file
    AssessmentFacade assessment = createImportedAssessment(fileName, qtiVersion);

    // Go to editAssessment.jsp, so prepare assessmentBean
    assessmentBean.setAssessment(assessment);
    // reset in case anything hanging around
    authorBean.setAssessTitle("");
    authorBean.setAssessmentDescription("");
    authorBean.setAssessmentTypeId("");
    authorBean.setAssessmentTemplateId(AssessmentTemplateFacade.DEFAULTTEMPLATE.toString());

    // update core AssessmentList: get the managed bean, author and set the list
    AssessmentService assessmentService = new AssessmentService();
    ArrayList list = assessmentService.getBasicInfoOfAllActiveAssessments(
                     AssessmentFacadeQueries.TITLE,true);
    //
    authorBean.setAssessments(list);

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
   * Create assessment from uploaded QTI XML
   * @param fullFileName file name and path
   * @param qti QTI version
   * @return
   */
  private AssessmentFacade createImportedAssessment(String fullFileName, int qti)
  {
    //trim = true so that xml processing instruction at top line, even if not.
    Document document = XmlUtil.readDocument(fullFileName, true);
    QTIService qtiService = new QTIService();
    return qtiService.createImportedAssessment(document, qti);
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

}
