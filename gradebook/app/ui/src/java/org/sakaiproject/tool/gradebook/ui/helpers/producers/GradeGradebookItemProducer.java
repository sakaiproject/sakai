package org.sakaiproject.tool.gradebook.ui.helpers.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradeGradebookItemViewParams;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.org.ponder.beanutil.entity.EntityBeanLocator;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.flow.jsfnav.DynamicNavigationCaseReporter;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class GradeGradebookItemProducer implements ActionResultInterceptor,  ViewComponentProducer, ViewParamsReporter {

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
    	List studentIds = new ArrayList();
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
    	
    	//OTP
    	String agrOTP = "AssignmentGradeRecord.";
    	String OTPKey = "";
    	if (agr != null && agr.getId() != null){
    		OTPKey += agr.getId().toString();
    	} else {
    		OTPKey += EntityBeanLocator.NEW_PREFIX + "1";
    	}
    	agrOTP += OTPKey;
    	
    	String commentOTP = "Comment.";
    	String commentOTPKey = "";
    	if (comment != null && comment.getId() != null){
    		commentOTPKey += comment.getId().toString();
    	} else {
    		commentOTPKey += EntityBeanLocator.NEW_PREFIX + "1";
    	}
    	commentOTP += commentOTPKey;
    	
        UIVerbatim.make(tofill, "instructions", messageLocator.getMessage("gradebook.grade-gradebook-item.instructions",
        		new Object[]{ reqStar }));
        
        //Start Form
        UIForm form = UIForm.make(tofill, "form");
        
        if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS){
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.points_label",
        		new Object[]{ reqStar }));
        	UIInput.make(form, "score", agrOTP + ".pointsEarned");
        	UIMessage.make(form, "points_out_of", "gradebook.grade-gradebook-item.points_out_of", new Object[]{ assignment.getPointsPossible()});
        } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE){
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.percentage_label",
            		new Object[]{ reqStar }));
        	//show percent sign
        	UIMessage.make(form, "percent_sign", "gradebook.grade-gradebook-item.percent_sign");
        	UIInput.make(form, "score", agrOTP + ".percentEarned");
        } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER){
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.letter_label",
            		new Object[]{ reqStar }));
        	UIInput.make(form, "score", agrOTP + ".letterEarned");
        }
        
        UIInput.make(form, "comments", commentOTP + ".commentText");
        
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
    
    public void interceptActionResult(ARIResult result,
			ViewParameters incoming, Object actionReturn) {
		if (incoming instanceof GradeGradebookItemViewParams) {
			GradeGradebookItemViewParams params = (GradeGradebookItemViewParams) incoming;
			if (params.finishURL != null && actionReturn.equals("cancel")) {
				result.resultingView = new RawViewParameters(params.finishURL);
			}
			else if (params.finishURL != null && actionReturn.equals("submit")) {
				result.resultingView = new RawViewParameters(params.finishURL);
			}
		}
	}

    public void setGradebookManager(GradebookManager gradebookManager) {
    	this.gradebookManager = gradebookManager;
    }
    
}