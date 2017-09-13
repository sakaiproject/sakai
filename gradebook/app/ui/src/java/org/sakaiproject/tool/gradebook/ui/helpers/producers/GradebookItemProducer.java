/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.ui.helpers.beans.GradebookItemBean;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradebookItemViewParams;

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
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.util.RSFUtil;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class GradebookItemProducer implements ActionResultInterceptor,
ViewComponentProducer, ViewParamsReporter, DefaultView {

    public static final String VIEW_ID = "gradebookItem";
    public String getViewID() {
        return VIEW_ID;
    }

    private String reqStar = "<span class=\"reqStar\">*</span>";

    public ViewParameters getViewParameters() {
        return new GradebookItemViewParams();
    }
    
    private EntityBeanLocator assignmentBeanLocator; 
    public void setAssignmentBeanLocator(EntityBeanLocator assignmentBeanLocator) {
        this.assignmentBeanLocator = assignmentBeanLocator;
    }
    
    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }
    
    private GradebookManager gradebookManager;
    public void setGradebookManager(GradebookManager gradebookManager) {
        this.gradebookManager = gradebookManager;
    }
    
    private GradebookService gradebookService;
    public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }
    
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
    
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
    	GradebookItemViewParams params = (GradebookItemViewParams) viewparams;
    	
    	if (params.contextId == null) {
    	    //TODO do something
    	    return;
    	}
    	
    	if (!gradebookService.currentUserHasEditPerm(params.contextId)) {
    	    UIMessage.make(tofill, "permissions_error", "gradebook.authorizationFailed.permissions_error");
            return;
    	}
    	
    	//Gradebook Info
    	Gradebook gradebook = gradebookManager.getGradebook(params.contextId);
    	Long gradebookId = gradebook.getId();
    	List<Category> categories = gradebookManager.getCategories(gradebookId);
    	
    	String newItemName = params.name;
    	String newItemDueTime = params.dueDateTime;
    	Date newItemDueDate = null;
    	if (newItemDueTime != null && !"".equals(newItemDueTime.trim())) {
    		try {
    			Long time = Long.parseLong(newItemDueTime);
    			newItemDueDate = new Date(time.longValue());
    		} catch (NumberFormatException nfe) {
    			// something funky was passed here, so we won't try to pre-set the due date
    		}
    	}
    	
    	//OTP
    	String assignmentOTP = "GradebookAssignment.";
    	String OTPKey = "";
    	if (params.assignmentId != null) {
    		OTPKey += params.assignmentId.toString();
    	} else {
    		OTPKey += EntityBeanLocator.NEW_PREFIX + "1";
    	}
    	assignmentOTP += OTPKey;
    	
    	Boolean add = (params.assignmentId == null);
    	
        //set dateEvolver
        dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);
        
        //Display None Decorator list
		Map<String, String> attrmap = new HashMap<String, String>();
		attrmap.put("style", "display:none");
		DecoratorList display_none_list =  new DecoratorList(new UIFreeAttributeDecorator(attrmap));
		
		//Setting up Dates
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DAY_OF_YEAR, 7);
    	cal.set(Calendar.HOUR_OF_DAY, 17);
    	cal.set(Calendar.MINUTE, 0);
    	Date duedate = cal.getTime();

        
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
        
        // if this is a new gradebook item, use the name parameter passed via the url
        if (add) {
            // add the name first as a UIELBinding to force it to save this value
            // if the user doesn't update the name field
            form.parameters.add( new UIELBinding(assignmentOTP + ".name", newItemName));
            UIInput.make(form, "title", assignmentOTP + ".name", newItemName);
        } else {
            UIInput.make(form, "title", assignmentOTP + ".name");
        }
        
        if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
        	UIVerbatim.make(form, "point_label", messageLocator.getMessage("gradebook.add-gradebook-item.point_label",
        			new Object[]{ reqStar }));
        } else {
        	UIVerbatim.make(form, "point_label", messageLocator.getMessage("gradebook.add-gradebook-item.percentage_label",
        			new Object[]{ reqStar }));
        }
        UIInput.make(form, "point", assignmentOTP + ".pointsPossible");
        
        
        GradebookAssignment assignment = (GradebookAssignment) assignmentBeanLocator.locateBean(OTPKey);
        
        if (add) {          
            // if a due date was passed in, set the due date
            if (newItemDueDate != null) {
            	assignment.setDueDate(newItemDueDate);
            }
        }
        
		UIBoundBoolean.make(form, "extraCredit", assignmentOTP + ".extraCredit");
		UIMessage.make(form, "extraCredit_label", "gradebook.add-gradebook-item.extra_credit");
        
        Boolean require_due_date = (assignment.getDueDate() != null);
		UIBoundBoolean.make(form, "require_due_date", "#{GradebookItemBean.requireDueDate}", require_due_date);
		UIMessage.make(form, "require_due_date_label", "gradebook.add-gradebook-item.require_due_date");
		
		UIOutput require_due_container = UIOutput.make(form, "require_due_date_container");
		UIInput dueDateField = UIInput.make(form, "due_date:", assignmentOTP + ".dueDate");
		Date initDueDate = assignment.getDueDate() != null ? assignment.getDueDate() : duedate;
		dateEvolver.evolveDateInput(dueDateField, initDueDate);
		
		// add the due date as a UIELBinding to force it to save this value
        // if the user doesn't update the due date field
        form.parameters.add( new UIELBinding(assignmentOTP + ".dueDate", initDueDate));
        form.parameters.add( new UIELBinding("#{GradebookItemBean.requireDueDate}", require_due_date));
		
		if (!require_due_date){
			require_due_container.decorators = display_none_list;
		}
        
        if (categories.size() > 0){
        	
        	UIOutput.make(form, "category_li");
        
	        String[] category_labels = new String[categories.size() + 1];
	        String[] category_values = new String[categories.size() + 1];
	        category_labels[0] = messageLocator.getMessage("gradebook.add-gradebook-item.category_unassigned");
	        category_values[0] = GradebookItemBean.CATEGORY_UNASSIGNED.toString();
	        int i=1;
	        for (Category cat : categories){
				category_labels[i] = cat.getName();
				category_values[i] = cat.getId().toString();
				i++;
	        }
	        
	        String categoryId = GradebookItemBean.CATEGORY_UNASSIGNED.toString(); // unassigned by default
	        if (assignment.getCategory() != null) {
	            categoryId = assignment.getCategory().getId().toString();
	        }
	        
	        UISelect.make(form, "category", category_values, category_labels, "#{GradebookItemBean.categoryId}", categoryId);
	        
	        if (gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
	            UIOutput.make(form, "category_instruction", messageLocator.getMessage("gradebook.add-gradebook-item.cateogry_instruction"));
	        }
        }
        
        UIBoundBoolean.make(form, "release", assignmentOTP + ".released", assignment.isReleased());
        UIBoundBoolean.make(form, "course_grade", assignmentOTP + ".counted", assignment.isCounted());
        
        form.parameters.add( new UIELBinding("#{GradebookItemBean.gradebookId}", gradebookId));
        
        //RSFUtil.addResultingViewBinding(form, "assignmentId", assignmentOTP + ".id");
        RSFUtil.addResultingViewBinding(form, "name", assignmentOTP + ".name");
        RSFUtil.addResultingViewBinding(form, "requireDueDate", "#{GradebookItemBean.requireDueDate}");
        RSFUtil.addResultingViewBinding(form, "dueDate", assignmentOTP + ".dueDate");
        
        //Action Buttons
        if (add){
        	UICommand.make(form, "add_item", UIMessage.make("gradebook.add-gradebook-item.add_item"), "#{GradebookItemBean.processActionAddItem}");
        } else {
        	UICommand.make(form, "add_item", UIMessage.make("gradebook.add-gradebook-item.edit_item"), "#{GradebookItemBean.processActionAddItem}");
        }
        UICommand.make(form, "cancel", UIMessage.make("gradebook.add-gradebook-item.cancel"), "#{GradebookItemBean.processActionCancel}");
    }

	public void interceptActionResult(ARIResult result,
			ViewParameters incoming, Object actionReturn) {
		if (incoming instanceof GradebookItemViewParams) {
			GradebookItemViewParams params = (GradebookItemViewParams) incoming;
			if (params.finishURL != null && actionReturn.equals("cancel")) {
				result.resultingView = new RawViewParameters(params.finishURL);
			}
			else if (params.finishURL != null && actionReturn.equals("submit")) {
				//tack on name and due date of newly created item
				String name = ((GradebookItemViewParams)result.resultingView).name;
				String gbItemDueTime = "";
				boolean requireDueDate = ((GradebookItemViewParams)result.resultingView).requireDueDate;
				if (requireDueDate) {
				    Date dueDate = ((GradebookItemViewParams)result.resultingView).dueDate;
				    if (dueDate != null) {
				        gbItemDueTime = dueDate.getTime() + "";
				    }
				}
				result.resultingView = new RawViewParameters(params.finishURL + "?gbItemName=" + name + "&gbItemDueTime=" + gbItemDueTime);
			}
		}
	}
}