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



package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringHelper;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.qti.XMLImportBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ImportAssessmentListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ImportAssessmentListener.class);
  private static final String f = "sample12Assessment.xml";
//  private static final String f = "sample12Assessment2.xml";
//  private static final String f = "respondus_IMS_QTI_sample12Assessment.xml";

  private static final String testFileName =
    "/tmp/jsf/upload_tmp/testImport/" + f;
  private static ContextUtil cu;

  public ImportAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    //Get the beans
    AuthorBean author = (AuthorBean) cu.lookupBean("author");
    AssessmentBean assessmentBean =
      (AssessmentBean) cu.lookupBean("assessmentBean");
    ItemAuthorBean itemauthorBean = (ItemAuthorBean) cu.lookupBean(
      "itemauthor");
    itemauthorBean.setTarget(itemauthorBean.FROM_ASSESSMENT); // save to assessment
    XMLImportBean xmlImport = (XMLImportBean) cu.lookupBean("xmlImport");

    // Get the file name
//    String fileName = xmlImport.getUploadFileName();
    String fileName = testFileName;

    // Create an assessment based on the uploaded file
    AssessmentFacade assessment = createImportedAssessment(fileName);

    // Go to editAssessment.jsp, so prepare assessmentBean
    assessmentBean.setAssessment(assessment);
    // reset in case anything hanging around
    author.setAssessTitle("");
    author.setAssessmentDescription("");
    author.setAssessmentTypeId("");
    author.setAssessmentTemplateId(AssessmentTemplateFacade.
      DEFAULTTEMPLATE.toString());

    // update core AssessmentList: get the managed bean, author and set the list
    AssessmentService assessmentService = new AssessmentService();
    ArrayList list = assessmentService.getBasicInfoOfAllActiveAssessments(
                     AssessmentFacadeQueries.TITLE,true);
    //
    author.setAssessments(list);
  }

  private AssessmentFacade createImportedAssessment(String fullFileName)
  {
    AuthoringHelper ah = new AuthoringHelper(QTIVersion.VERSION_1_2);
    Document document = XmlUtil.readDocument(fullFileName);
    log.debug("Created doc.");
    return ah.createImportedAssessment(document);
  }

}


