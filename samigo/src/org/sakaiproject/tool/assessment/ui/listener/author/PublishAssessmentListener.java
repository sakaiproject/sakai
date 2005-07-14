/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class PublishAssessmentListener
    implements ActionListener {


  private static Log log = LogFactory.getLog(PublishAssessmentListener.class);
  private static ContextUtil cu;

  public PublishAssessmentListener() {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    //System.out.println("debugging ActionEvent: " + ae);
    //System.out.println("debug requestParams: " + requestParams);
    //System.out.println("debug reqMap: " + reqMap);

    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) cu.
        lookupBean(
        "assessmentSettings");
    /**
         String assessmentId = (String) FacesContext.getCurrentInstance().
             getExternalContext().getRequestParameterMap().get("assessmentId");
     */
    AssessmentService assessmentService = new AssessmentService();
    PublishedAssessmentService publishedAssessmentService = new
        PublishedAssessmentService();
    AssessmentFacade assessment = assessmentService.getAssessment(
        assessmentSettings.getAssessmentId().toString());
    publish(assessment, assessmentSettings);

    // get the managed bean, author and set all the list
    GradingService gradingService = new GradingService();
    HashMap map = gradingService.getSubmissionSizeOfAllPublishedAssessments();
    AuthorBean author = (AuthorBean) cu.lookupBean(
        "author");

    // 1. need to update active published list in author bean
    ArrayList activePublishedList = publishedAssessmentService.
        getBasicInfoOfAllActivePublishedAssessments(
        author.getPublishedAssessmentOrderBy(),author.isPublishedAscending());
    author.setPublishedAssessments(activePublishedList);
    setSubmissionSize(activePublishedList, map);

    // 2. need to update active published list in author bean
    ArrayList inactivePublishedList = publishedAssessmentService.
        getBasicInfoOfAllInActivePublishedAssessments(
        author.getInactivePublishedAssessmentOrderBy(),author.isInactivePublishedAscending());
    author.setInactivePublishedAssessments(inactivePublishedList);
    setSubmissionSize(inactivePublishedList, map);

    // 3. reset the core listing
    // 'cos user may change core assessment title and publish - sigh
    ArrayList assessmentList = assessmentService.
        getBasicInfoOfAllActiveAssessments(
        author.getCoreAssessmentOrderBy(),author.isCoreAscending());
    // get the managed bean, author and set the list
    author.setAssessments(assessmentList);
  }

  private void publish(AssessmentFacade assessment,
                       AssessmentSettingsBean assessmentSettings) {
    String publishAssessment = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("publishAssessment");
    //System.out.println("***** PUBLISHING ***");
    PublishedAssessmentService publishedAssessmentService = new
        PublishedAssessmentService();
    PublishedAssessmentFacade pub = null;
    try {
       pub = publishedAssessmentService.publishAssessment(assessment);
    } catch (Exception e) {
        log.warn(e);
        e.printStackTrace();
        // Add a global message (not bound to any component) to the faces context indicating the failure
        FacesMessage message = new FacesMessage("There was an error publishing this assessment.  Ensure that the name is unique.");
        FacesContext.getCurrentInstance().addMessage(null, message);
        throw new AbortProcessingException(e);
    }

    // let's check if we need a publishedUrl
    String releaseTo = pub.getAssessmentAccessControl().getReleaseTo();
    if (releaseTo != null) {
      // generate an alias to the pub assessment
      String alias = assessmentSettings.getAlias();
      PublishedMetaData meta = new PublishedMetaData(pub.getData(),
          AssessmentMetaDataIfc.ALIAS, alias);
      publishedAssessmentService.saveOrUpdateMetaData(meta);
    }
  }

  private void setSubmissionSize(ArrayList list, HashMap map) {
    for (int i = 0; i < list.size(); i++) {
      PublishedAssessmentFacade p = (PublishedAssessmentFacade) list.get(i);
      Integer size = (Integer) map.get(p.getPublishedAssessmentId());
      if (size != null) {
        p.setSubmissionSize(size.intValue());
        //System.out.println("*** submission size" + size.intValue());
      }
    }
  }

}
