/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.rsf.producers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.rsf.params.BeginAssessmentViewParameters;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Producer to show basic Assessment info and allow the user (student) to begin the assessment if
 * they want. Also ensures that the user is logged in and has the proper authorization as well as
 * that the assessment is currently available.
 * 
 * This code contains some parts I'd rather not have to do in order to pass control off from RSF to
 * JSF... please don't judge, but feel free to propose better solutions or rewrite delivery in RSF
 * completely ;-)
 * 
 * @author Joshua Ryan  josh@asu.edu   alt^I
 * 
 */
@Slf4j
 public class BeginAssessmentProducer implements ViewComponentProducer, DefaultView  {

  public HttpServletRequest httpServletRequest;
  public HttpServletResponse httpServletResponse;
  
  public static final String VIEW_ID = "BeginTakingAssessment";

  public String getViewID() {
    return VIEW_ID;
  }

  public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
    BeginAssessmentViewParameters params = null;
    String alias = "";

    if (viewparams != null){
      params = (BeginAssessmentViewParameters) viewparams;
      alias = params.pubReference;
    }
    else{
      log.warn("Something bad... we have no viewparams");
      return;
    }

    PublishedAssessmentService service = new PublishedAssessmentService();
    PublishedAssessmentFacade pub = null;

    try {
	pub = service.getPublishedAssessment(alias);
    } catch (Exception e) {
	log.warn("Something bad... can't find publisehd assessment " + e);
	return;
    }

    String path = "/samigo-app/servlet/Login?id=" + pub.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS);
    String url = httpServletRequest.getRequestURL().toString();
    String context = httpServletRequest.getContextPath();
    String finalUrl = url.substring(0,url.lastIndexOf(context))+path;
    try {
      httpServletResponse.sendRedirect(finalUrl);
    }
    catch (IOException e) {
      log.error(e.getMessage(), e);
    }

  }

  public ViewParameters getViewParameters() {
    return new BeginAssessmentViewParameters();
  }

}

