/**
 * Copyright (c) 2003-2008 The Apereo Foundation
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
package org.sakaiproject.tool.gradebook.ui.helpers.producers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradeGradebookItemViewParams;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class GradeGradebookItemProducer extends HelperAwareProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

    public static final String VIEW_ID = "gradeGradebookItem";
    public String getViewID() {
        return VIEW_ID;
    }

    private String reqStar = "<span class=\"reqStar\">*</span>";

    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }
    
    private UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService){
    	this.userDirectoryService = userDirectoryService;
    }
    
    private GradebookService gradebookService;
    public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }
    
    private HttpServletResponse httpServletResponse;
    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }
    
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
    	GradeGradebookItemViewParams params = (GradeGradebookItemViewParams) viewparams;

    	if (params.contextId == null || params.assignmentId == null ||  params.userId == null){
    		//DO something
    		return;
    	}
    	
    	// Firefox absolutely completely refuses to not cache this page for some reason.
    	// Even with the meta's in the HTML.  -SWG  ASNN-293
    	httpServletResponse.setHeader("Pragma", "no-cache");
    	httpServletResponse.setHeader("Cache-Control", "no-cache");
    	httpServletResponse.setDateHeader("Expires", 0 );
    	
    	if (!gradebookService.isUserAbleToGradeItemForStudent(params.contextId, params.assignmentId, params.userId)) {
    	    UIMessage.make(tofill, "permissions_error", "gradebook.authorizationFailed.permissions_error");
    	    return;
    	}
    	
    	//get Options
    	GradeDefinition gradeDef = gradebookService.getGradeDefinitionForStudentForItem(params.contextId, params.assignmentId, params.userId);
    	Assignment assignment = gradebookService.getAssignment(params.contextId, params.assignmentId);
    	Long gradebookId = ((Gradebook) gradebookService.getGradebook(params.contextId)).getId();
    	
    	String grade = "";
    	String comment = "";
    	int gradeEntryType;
    	if (gradeDef != null) {
    	    grade = gradeDef.getGrade();
    	    comment = gradeDef.getGradeComment();
    	    gradeEntryType = gradeDef.getGradeEntryType();
    	} else {
    	    gradeEntryType = gradebookService.getGradeEntryType(params.contextId);
    	}
    	
        String student_name = "";
        try{
            User user = userDirectoryService.getUser(params.userId);
            student_name = user.getDisplayName();
        } catch(UserNotDefinedException ex) {
            	return;
        }
            
    	UIMessage.make(tofill, "heading", "gradebook.grade-gradebook-item.heading", new Object[]{ assignment.getName(), student_name } );
        
        //Start Form
        UIForm form = UIForm.make(tofill, "form");
        
        if (gradeEntryType == GradebookService.GRADE_TYPE_POINTS){
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.points_label",
        		new Object[]{ reqStar }));
        	UIMessage.make(form, "points_out_of", "gradebook.grade-gradebook-item.points_out_of", new Object[]{ assignment.getPoints()});
        } else if (gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE){
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.percentage_label",
            		new Object[]{ reqStar }));
        	//show percent sign
        	UIMessage.make(form, "percent_sign", "gradebook.grade-gradebook-item.percent_sign");
        } else if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER){
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.letter_label",
            		new Object[]{ reqStar }));
        }
        
        // add the grade info now. the UIInputs only pass along the parameter info
        // if there is a change to the value. this gets the old values in there
        // and then they will be overwritten if there is an input value set
        form.parameters.add( new UIELBinding("#{AssignmentGradeRecordBean.enteredGrade}", grade));
        form.parameters.add( new UIELBinding("#{AssignmentGradeRecordBean.commentText}", comment));

        UIInput.make(form, "score", "#{AssignmentGradeRecordBean.enteredGrade}", grade);
        UIInput.make(form, "commentText", "#{AssignmentGradeRecordBean.commentText}", comment);

        form.parameters.add( new UIELBinding("#{AssignmentGradeRecordBean.gradebookId}", gradebookId));
        form.parameters.add( new UIELBinding("#{AssignmentGradeRecordBean.studentId}", params.userId));
        form.parameters.add( new UIELBinding("#{AssignmentGradeRecordBean.assignmentId}", params.assignmentId));
        
        //Action Buttons
        UICommand.make(form, "submit", UIMessage.make("gradebook.grade-gradebook-item.submit"), "#{AssignmentGradeRecordBean.processActionSubmitGrade}");
        UICommand.make(form, "cancel", UIMessage.make("gradebook.grade-gradebook-item.cancel"), "#{AssignmentGradeRecordBean.processActionCancel}");
    }

    public ViewParameters getViewParameters() {
        return new GradeGradebookItemViewParams();
    }

	public List<NavigationCase> reportNavigationCases()
	{
		List<NavigationCase> nav= new ArrayList<NavigationCase>();
		nav.add(new NavigationCase("submit", new SimpleViewParameters(
	            FinishedHelperProducer.VIEW_ID)));
		nav.add(new NavigationCase("cancel", new SimpleViewParameters(
	            FinishedHelperProducer.VIEW_ID)));
		return nav;
	}
    
}