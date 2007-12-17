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

    public static final String VIEW_ID = "add-gradebook-item";
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
        
        UIVerbatim.make(tofill, "instructions", messageLocator.getMessage("gradebook.add-gradebook-item.instructions",
        		new Object[]{ reqStar }));
        
        //Start Form
        UIForm form = UIForm.make(tofill, "form");
        
        UIVerbatim.make(form, "title_label", messageLocator.getMessage("gradebook.add-gradebook-item.title_label",
        		new Object[]{ reqStar }));
        UIInput.make(form, "title", "Title Here");
        
        UIVerbatim.make(form, "point_label", messageLocator.getMessage("gradebook.add-gradebook-item.point_label",
        		new Object[]{ reqStar }));
        UIInput.make(form, "point", "POINTS HERE");
        
        UIVerbatim.make(form, "due_date_label", messageLocator.getMessage("gradebook.add-gradebook-item.due_date_label",
        		new Object[]{ reqStar }));
        UIInput due_date = UIInput.make(form, "due_date:", "");
        dateEvolver.evolveDateInput(due_date, date);
        
        UISelect category_select = UISelect.make(form, "category", new String[] {}, new String[] {}, "");
                
        UIBoundBoolean.make(form, "release");
        UIBoundBoolean.make(form, "course_grade");
        
        
        //Action Buttons
        UICommand.make(form, "add_item", UIMessage.make("gradebook.add-gradebook-item.add_item"), "#{GradebookItemBean.processActionAddItem}");
        UICommand.make(form, "cancel", UIMessage.make("gradebook.add-gradebook-item.cancel"), null); //"#{GradebookItemBean.processActionCancel}");
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