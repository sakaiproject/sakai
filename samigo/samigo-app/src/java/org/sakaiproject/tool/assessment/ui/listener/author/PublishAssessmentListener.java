/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding; 

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.spring.SpringBeanLocator;


/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class PublishAssessmentListener
    implements ActionListener {


  private static Log log = LogFactory.getLog(PublishAssessmentListener.class);
  //private static ContextUtil cu;
  private static final GradebookServiceHelper gbsHelper =
      IntegrationContextFactory.getInstance().getGradebookServiceHelper();
  private static final boolean integrated =
      IntegrationContextFactory.getInstance().isIntegrated();
  private static Boolean repeatedPublish = Boolean.FALSE;

  public PublishAssessmentListener() {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException {
  	synchronized(repeatedPublish)
		{
  		//FacesContext context = FacesContext.getCurrentInstance();
  		
  		UIComponent eventSource = (UIComponent) ae.getSource();
  		ValueBinding vb = eventSource.getValueBinding("value");
  		String buttonValue = (String) vb.getExpressionString(); 
  		if(buttonValue.endsWith(".button_unique_save_and_publish}"))
  		{
  			repeatedPublish = Boolean.FALSE;
  			return;
  		}
  		
  		if(!repeatedPublish.booleanValue())
  		{
  			//Map reqMap = context.getExternalContext().getRequestMap();
  			//Map requestParams = context.getExternalContext().getRequestParameterMap();
  			AuthorBean author = (AuthorBean) ContextUtil.lookupBean(
  			"author");
  			
  			AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) ContextUtil.lookupBean("assessmentSettings");
  			
  			AssessmentService assessmentService = new AssessmentService();
  			
  			AssessmentFacade assessment = assessmentService.getAssessment(
  					assessmentSettings.getAssessmentId().toString());
  			
  			// 0. sorry need double checking assesmentTitle and everything
  			boolean error = checkTitle(assessment);
  			if (error){
  				return;
  			}
  			
  			publish(assessment, assessmentSettings);
  			
  			GradingService gradingService = new GradingService();
  			PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
  			AuthorActionListener authorActionListener = new AuthorActionListener();
  			authorActionListener.prepareAssessmentsList(author, assessmentService, gradingService, publishedAssessmentService);
  			
  			
  			repeatedPublish = Boolean.TRUE;
  		}
		}
  }

  private void publish(AssessmentFacade assessment,
                       AssessmentSettingsBean assessmentSettings) {
    //String publishAssessment = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("publishAssessment");
    //log.info("***** PUBLISHING ***");
    PublishedAssessmentService publishedAssessmentService = new
        PublishedAssessmentService();
    PublishedAssessmentFacade pub = null;
    try {
       pub = publishedAssessmentService.publishAssessment(assessment);
       EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.publish", "assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + pub.getPublishedAssessmentId(), true));
    } catch (AssignmentHasIllegalPointsException gbe) {
       // Right now gradebook can only accept assessements with totalPoints > 0 
       // this  might change later
       log.warn(gbe);
        gbe.printStackTrace();
        // Add a global message (not bound to any component) to the faces context indicating the failure
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
                                                 "gradebook_exception_min_points");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(err));
        throw new AbortProcessingException(gbe);

 
    } catch (Exception e) {
        log.warn(e);
        e.printStackTrace();
        // Add a global message (not bound to any component) to the faces context indicating the failure
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages",
                                                 "gradebook_exception_error");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(err));
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

  private boolean checkTitle(AssessmentFacade assessment){
    boolean error=false;
    String assessmentName = assessment.getTitle();
    AssessmentService assessmentService = new AssessmentService();
    String assessmentId = assessment.getAssessmentBaseId().toString();

    //#a - look for error: check if core assessment title is unique
    if (assessmentName!=null &&(assessmentName.trim()).equals("")){
      String publish_error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","publish_error_message");
      FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(publish_error));
      error=true;
    }
    if (!assessmentService.assessmentTitleIsUnique(assessmentId,assessmentName,false)){
      error=true;
      String nameUnique_err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","assessmentName_error");
      FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(nameUnique_err));
    }

    //#b - check if gradebook exist, if so, if assessment title already exists in GB
    GradebookService g = null;
    if (integrated){
      g = (GradebookService) SpringBeanLocator.getInstance().
           getBean("org.sakaiproject.service.gradebook.GradebookService");
    }
    String toGradebook = assessment.getEvaluationModel().getToGradeBook();
    try{
      if (toGradebook!=null && toGradebook.equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString()) &&
          gbsHelper.isAssignmentDefined(assessmentName, g)){
        error=true;
        String gbConflict_error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","gbConflict_error");
        FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(gbConflict_error));
      }
    }
    catch(Exception e){
      log.warn("external assessment in GB has the same title:"+e.getMessage());
    }
    return error;
  }
}
