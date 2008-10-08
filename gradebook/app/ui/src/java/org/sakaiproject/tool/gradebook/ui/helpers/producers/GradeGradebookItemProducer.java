package org.sakaiproject.tool.gradebook.ui.helpers.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
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
    private GradebookManager gradebookManager;
    
    private UserDirectoryService userDirectoryService;
    public void setUserDirectoryService(UserDirectoryService userDirectoryService){
    	this.userDirectoryService = userDirectoryService;
    }
    
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
    	GradeGradebookItemViewParams params = (GradeGradebookItemViewParams) viewparams;

    	if (params.contextId == null || params.assignmentId == null ||  params.userId == null){
    		//DO something
    		return;
    	}
    	
    	//Gradebook Info
    	Gradebook gradebook  = gradebookManager.getGradebook(params.contextId);
    	Long gradebookId = gradebook.getId();
    	String userId = params.userId;
    	List<String> studentIds = new ArrayList<String>();
    	studentIds.add(userId);
    	
    	//get Options
    	Assignment assignment = gradebookManager.getAssignment(params.assignmentId);
    	AssignmentGradeRecord agr = gradebookManager.getAssignmentGradeRecordForAssignmentForStudent(assignment, userId);
    	List<Comment> comments = gradebookManager.getComments(assignment, studentIds);
    	Comment comment = new Comment();
    	if (comments != null && comments.size() > 0){
    		comment = (Comment)comments.get(0);
    	}
        String student_name = "";
        try{
            User user = userDirectoryService.getUser(userId);
            student_name = user.getDisplayName();
        } catch(UserNotDefinedException ex) {
            	return;
        }
            
    	UIMessage.make(tofill, "heading", "gradebook.grade-gradebook-item.heading", new Object[]{ assignment.getName(), student_name } );
        
        //Start Form
        UIForm form = UIForm.make(tofill, "form");
        
        String gradeToDisplay = null;
        if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS){
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.points_label",
        		new Object[]{ reqStar }));
        	gradeToDisplay = agr.getPointsEarned() != null ? agr.getPointsEarned().toString() : null;
        	UIMessage.make(form, "points_out_of", "gradebook.grade-gradebook-item.points_out_of", new Object[]{ assignment.getPointsPossible()});
        } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE){
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.percentage_label",
            		new Object[]{ reqStar }));
        	//show percent sign
        	UIMessage.make(form, "percent_sign", "gradebook.grade-gradebook-item.percent_sign");
        	gradeToDisplay = agr.getPercentEarned() != null ? agr.getPercentEarned().toString() : null;
        } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER){
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.letter_label",
            		new Object[]{ reqStar }));
        	gradeToDisplay = agr.getLetterEarned();
        }
        
        // add the grade info now. the UIInputs only pass along the parameter info
        // if there is a change to the value. this gets the old values in there
        // and then they will be overwritten if there is an input value set
        form.parameters.add( new UIELBinding("#{AssignmentGradeRecordBean.enteredGrade}", gradeToDisplay));
        form.parameters.add( new UIELBinding("#{AssignmentGradeRecordBean.commentText}", comment.getCommentText()));
        
        UIInput.make(form, "score", "#{AssignmentGradeRecordBean.enteredGrade}", gradeToDisplay);
        UIInput.make(form, "commentText", "#{AssignmentGradeRecordBean.commentText}", comment.getCommentText());
        
        form.parameters.add( new UIELBinding("#{AssignmentGradeRecordBean.gradebookId}", gradebookManager.getGradebook(params.contextId).getId()));
        form.parameters.add( new UIELBinding("#{AssignmentGradeRecordBean.studentId}", params.userId));
        form.parameters.add( new UIELBinding("#{AssignmentGradeRecordBean.assignmentId}", params.assignmentId));
        
        //Action Buttons
        UICommand.make(form, "submit", UIMessage.make("gradebook.grade-gradebook-item.submit"), "#{AssignmentGradeRecordBean.processActionSubmitGrade}");
        UICommand.make(form, "cancel", UIMessage.make("gradebook.grade-gradebook-item.cancel"), "#{AssignmentGradeRecordBean.processActionCancel}");
    }

    public ViewParameters getViewParameters() {
        return new GradeGradebookItemViewParams();
    }
    
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }

    public void setGradebookManager(GradebookManager gradebookManager) {
    	this.gradebookManager = gradebookManager;
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