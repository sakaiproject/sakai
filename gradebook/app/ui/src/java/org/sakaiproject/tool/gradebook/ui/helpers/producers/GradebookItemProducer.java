package org.sakaiproject.tool.gradebook.ui.helpers.producers;

import java.util.Iterator;
import java.util.List;

import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradebookItemViewParams;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

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
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.util.RSFUtil;

public class GradebookItemProducer implements ActionResultInterceptor,
ViewComponentProducer, ViewParamsReporter, DefaultView {

    public static final String VIEW_ID = "gradebookItem";
    public String getViewID() {
        return VIEW_ID;
    }

    private String reqStar = "<span class=\"reqStar\">*</span>";

    private MessageLocator messageLocator;
    private ToolManager toolManager;
    private SessionManager sessionManager;
    private GradebookManager gradebookManager;
    
	/*
	 * You can change the date input to accept time as well by uncommenting the lines like this:
	 * dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
	 * and commenting out lines like this:
	 * dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);
	 * -AZ
	 * And vice versa - RWE
	 */
	private FormatAwareDateInputEvolver dateEvolver;
	public void setDateEvolver(FormatAwareDateInputEvolver dateEvolver) {
		this.dateEvolver = dateEvolver;
	}
	
    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
    	GradebookItemViewParams params = (GradebookItemViewParams) viewparams;
    	//Gradebook Info
    	Long gradebookId = gradebookManager.getGradebook(params.contextId).getId();
    	List categories = gradebookManager.getCategories(gradebookId);
    	
    	//OTP
    	String assignmentOTP = "Assignment.";
    	if (params.assignmentId != null) {
    		assignmentOTP += params.assignmentId.toString();
    	} else {
    		assignmentOTP += EntityBeanLocator.NEW_PREFIX + "1";
    	}
    	
    	Boolean add = (params.assignmentId == null);
    	
        //set dateEvolver
        dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);
        
        if (add){
        	UIMessage.make(tofill, "heading", "gradebook.add-gradebook-item.heading_add");
        } else {
        	UIMessage.make(tofill, "heading", "gradebook.add-gradebook-item.heading_edit");
        }
        
        UIVerbatim.make(tofill, "instructions", messageLocator.getMessage("gradebook.add-gradebook-item.instructions",
        		new Object[]{ reqStar }));
        
        //Start Form
        UIForm form = UIForm.make(tofill, "form");
        
        UIVerbatim.make(form, "title_label", messageLocator.getMessage("gradebook.add-gradebook-item.title_label",
        		new Object[]{ reqStar }));
        UIInput.make(form, "title", assignmentOTP + ".name");
        
        UIVerbatim.make(form, "point_label", messageLocator.getMessage("gradebook.add-gradebook-item.point_label",
        		new Object[]{ reqStar }));
        UIInput.make(form, "point", assignmentOTP + ".pointsPossible");
        
        UIInput due_date = UIInput.make(form, "due_date:", assignmentOTP + ".dueDate");
        dateEvolver.evolveDateInput(due_date);
        
        if (categories.size() > 0){
        	
        	UIOutput.make(form, "category_li");
        
	        String[] category_labels = new String[categories.size() + 1];
	        String[] category_values = new String[categories.size() + 1];
	        category_labels[0] = messageLocator.getMessage("gradebook.add-gradebook-item.category_unassigned");
	        category_values[0] = ""; 
	        int i =1;
	        for (Iterator catIter = categories.iterator(); catIter.hasNext();){
	        	Category cat = (Category) catIter.next();
				category_labels[i] = cat.getName();
				category_values[i] = cat.getId().toString();
				i++;
	        }
	        
	        UISelect.make(form, "category", category_values, category_labels, "#{GradebookItemBean.categoryId}");
        }
        
        UIBoundBoolean.make(form, "release", assignmentOTP + ".released");
        UIBoundBoolean.make(form, "course_grade", assignmentOTP + ".counted");
        
        form.parameters.add( new UIELBinding("#{GradebookItemBean.gradebookId}", gradebookManager.getGradebook(params.contextId).getId()));
        
        //RSFUtil.addResultingViewBinding(form, "assignmentId", assignmentOTP + ".id");
        RSFUtil.addResultingViewBinding(form, "name", assignmentOTP + ".name");
        
        //Action Buttons
        if (add){
        	UICommand.make(form, "add_item", UIMessage.make("gradebook.add-gradebook-item.add_item"), "#{GradebookItemBean.processActionAddItem}");
        } else {
        	UICommand.make(form, "add_item", UIMessage.make("gradebook.add-gradebook-item.edit_item"), "#{GradebookItemBean.processActionAddItem}");
        }
        UICommand.make(form, "cancel", UIMessage.make("gradebook.add-gradebook-item.cancel"), "#{GradebookItemBean.processActionCancel}");
    }

    public ViewParameters getViewParameters() {
        return new GradebookItemViewParams();
    }
    
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}


	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
    public void setGradebookManager(GradebookManager gradebookManager) {
    	this.gradebookManager = gradebookManager;
    }

	public void interceptActionResult(ARIResult result,
			ViewParameters incoming, Object actionReturn) {
		if (incoming instanceof GradebookItemViewParams) {
			GradebookItemViewParams params = (GradebookItemViewParams) incoming;
			if (params.finishURL != null && actionReturn.equals("cancel")) {
				result.resultingView = new RawViewParameters(params.finishURL);
			}
			else if (params.finishURL != null && actionReturn.equals("submit")) {
				//tack on name of newly created item
				String name = ((GradebookItemViewParams)result.resultingView).name;
				result.resultingView = new RawViewParameters(params.finishURL + "?value=" + name);
			}
		}
	}
}