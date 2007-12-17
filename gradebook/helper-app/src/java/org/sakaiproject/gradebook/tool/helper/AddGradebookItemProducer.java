package org.sakaiproject.gradebook.tool.helper;

import java.util.Date;

import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class AddGradebookItemProducer implements // DynamicNavigationCaseReporter, 
ViewComponentProducer, DefaultView {

    public static final String VIEW_ID = "assignment_add-gradebook-item";
    public String getViewID() {
        return VIEW_ID;
    }

    private String reqStar = "<span class=\"reqStar\">*</span>";

    private MessageLocator messageLocator;
    private ToolManager toolManager;
    private SessionManager sessionManager;
    
    
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

        //set dateEvolver
        dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);
        Date date = new Date();
        
    	
        UIMessage.make(tofill, "page-title", "assignment2.assignment_add-gradebook-item.title");
        UIMessage.make(tofill, "heading", "assignment2.assignment_add-gradebook-item.heading");
        UIVerbatim.make(tofill, "instructions", messageLocator.getMessage("assignment2.assignment_add-gradebook-item.instructions",
        		new Object[]{ reqStar }));
        
        //Start Form
        UIForm form = UIForm.make(tofill, "form");
        UIMessage.make(form, "gradebook_item_legend", "assignment2.assignment_add-gradebook-item.gradebook_item_legend");
        
        UIVerbatim.make(form, "title_label", messageLocator.getMessage("assignment2.assignment_add-gradebook-item.title_label",
        		new Object[]{ reqStar }));
        UIInput.make(form, "title", "Title Here");
        
        UIVerbatim.make(form, "point_label", messageLocator.getMessage("assignment2.assignment_add-gradebook-item.point_label",
        		new Object[]{ reqStar }));
        UIInput.make(form, "point", "POINTS HERE");
        
        UIVerbatim.make(form, "due_date_label", messageLocator.getMessage("assignment2.assignment_add-gradebook-item.due_date_label",
        		new Object[]{ reqStar }));
        UIInput due_date = UIInput.make(form, "due_date:", "");
        dateEvolver.evolveDateInput(due_date, date);
        
        UIMessage.make(form, "category_label", "assignment2.assignment_add-gradebook-item.category_label");
        UISelect category_select = UISelect.make(form, "category", new String[] {}, new String[] {}, "");
        UIMessage.make(form, "category_instruction", "assignment2.assignment_add-gradebook-item.category_instruction");
        
        UIMessage.make(form, "release_label", "assignment2.assignment_add-gradebook-item.release_label");
        UIBoundBoolean.make(form, "release");
        
        UIMessage.make(form, "course_grade_label", "assignment2.assignment_add-gradebook-item.course_grade_label");
        UIBoundBoolean.make(form, "course_grade");
        
        
        //Action Buttons
        //TODO i18n
        UICommand.make(form, "add_item", "Add Markbook Item", "#{GradebookItemBean.processActionAddItem}");
        //TODO i18n
        UICommand.make(form, "cancel", "Cancel", null); //"#{GradebookItemBean.processActionCancel}");
    }

    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }
    
    //public List reportNavigationCases() {
        //Tool tool = toolManager.getCurrentTool();
      //  List togo = new ArrayList();
     //   togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
     //   togo.add(new NavigationCase("done", 
      //           new RawViewParameters(SakaiURLUtil.getHelperDoneURL(tool, sessionManager))));
        

      //  return togo;
    //}


	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}


	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
    
}