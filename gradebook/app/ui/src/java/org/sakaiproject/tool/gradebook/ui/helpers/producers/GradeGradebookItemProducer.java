package org.sakaiproject.tool.gradebook.ui.helpers.producers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.ui.helpers.params.FinishedHelperViewParams;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradeGradebookItemViewParams;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import uk.org.ponder.beanutil.entity.EntityBeanLocator;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.flow.jsfnav.DynamicNavigationCaseReporter;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class GradeGradebookItemProducer implements DynamicNavigationCaseReporter, 
ViewComponentProducer, ViewParamsReporter, DefaultView {

    public static final String VIEW_ID = "grade-gradebook-item";
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
    	
    	UIMessage.make(tofill, "heading", "gradebook.grade-gradebook-item.heading", new Object[]{ assignment.getName()} );
    	
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
        String student_name = "";
        
        try{
        User user = userDirectoryService.getUser(userId);
        student_name = user.getDisplayName();
        } catch(UserNotDefinedException ex) {
        	return;
        }
        
        UIOutput.make(form, "student_name", student_name);
        UIOutput.make(form, "name", assignment.getName());
        UIOutput.make(form, "point_value", (assignment.getPointsPossible() != null ? assignment.getPointsPossible().toString() : ""));
        
        if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS){
        	UIMessage.make(form, "entry_type", "gradebook.grade-gradebook-item.entry_type_points");
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.points_label",
        		new Object[]{ reqStar }));
        	UIInput.make(form, "score", agrOTP + ".pointsEarned");
        } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE){
        	UIMessage.make(form, "entry_type", "gradebook.grade-gradebook-item.entry_type_percentage");
        	UIVerbatim.make(form, "points_label", messageLocator.getMessage("gradebook.grade-gradebook-item.percentage_label",
            		new Object[]{ reqStar }));
        	//show percent sign
        	UIMessage.make(form, "percent_sign", "gradebook.grade-gradebook-item.percent_sign");
        	UIInput.make(form, "score", agrOTP + ".percentEarned");
        } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER){
        	UIMessage.make(form, "entry_type", "gradebook.grade-gradebook-item.entry_type_letter");
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
    
    public List reportNavigationCases() {
        List togo = new ArrayList();
        togo.add(new NavigationCase("cancel", new SimpleViewParameters(FinishedHelperProducer.VIEWID)));
        togo.add(new NavigationCase("submit", 
                new FinishedHelperViewParams(FinishedHelperProducer.VIEWID, null, null)));
        

        return togo;
    }

    public void setGradebookManager(GradebookManager gradebookManager) {
    	this.gradebookManager = gradebookManager;
    }
    
}