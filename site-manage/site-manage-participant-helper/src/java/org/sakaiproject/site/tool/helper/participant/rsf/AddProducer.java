package org.sakaiproject.site.tool.helper.participant.rsf;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.tool.helper.participant.impl.SiteAddParticipantHandler;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.UserDirectoryService;

import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import org.sakaiproject.rsf.util.SakaiURLUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.stringutil.StringList;
/**
 * 
 * @author
 *
 */
public class AddProducer implements ViewComponentProducer, NavigationCaseReporter, DefaultView, ViewParamsReporter, ActionResultInterceptor {

	/** Our log (commons). */
	private static final Logger M_log = LoggerFactory.getLogger(AddProducer.class);

    public SiteAddParticipantHandler handler;
    public static final String VIEW_ID = "Add";
    public MessageLocator messageLocator;
    public FrameAdjustingProducer frameAdjustingProducer;
    public SessionManager sessionManager;

    public String getViewID() {
        return VIEW_ID;
    }
    
    private TargettedMessageList targettedMessageList;
	public void setTargettedMessageList(TargettedMessageList targettedMessageList) {
		this.targettedMessageList = targettedMessageList;
	}
	
	public UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

    public void fillComponents(UIContainer tofill, ViewParameters arg1, ComponentChecker arg2) {
    	
    	boolean isCourseSite = handler.isCourseSite();
    	
    	UIBranchContainer content = UIBranchContainer.make(tofill, "content:");
    	
    	org.sakaiproject.coursemanagement.api.CourseManagementService cms = (org.sakaiproject.coursemanagement.api.CourseManagementService) ComponentManager.get(org.sakaiproject.coursemanagement.api.CourseManagementService.class);
    	
    	if (isCourseSite && cms != null)
    	{
    		// show specific instructions for adding participant into course site
    		UIMessage.make(content, "add.official", "add.official");
    		UIMessage.make(content, "add.official1", "add.official1");
    		UIMessage.make(content, "add.official.instruction", "add.official.instruction");
	    }
        
    	UIForm participantForm = UIForm.make(content, "participant-form");
    	// csrf token
    	UIInput.make(participantForm, "csrfToken", "#{siteAddParticipantHandler.csrfToken}", handler.csrfToken);

    	String pickerAction = handler.getServerConfigurationString("officialAccountPickerAction");
		if (pickerAction != null && !"".equals(pickerAction))
		{
			UIOutput.make(participantForm, "officialAccountPickerLabel", handler.getServerConfigurationString("officialAccountPickerLabel"));
			UIOutput.make(participantForm, "officialAccountPickerAction", pickerAction);
		}

		String allowAddNonOfficialParticipant = handler.getAllowNonOfficialAccount();
    	if (allowAddNonOfficialParticipant.equalsIgnoreCase("true"))
    	{
    		UIMessage.make(participantForm, "nonOfficialAddMultiple", "add.multiple.nonofficial");
    	}

		// all participant
		UIInput.make(participantForm, "accountParticipant", "#{siteAddParticipantHandler.accountParticipant}", handler.accountParticipant);
		UIOutput.make(participantForm, "accountSectionTitle", messageLocator.getMessage("accountSectionTitle"));
		UIOutput.make(participantForm, "accountName", messageLocator.getMessage("accountName"));
		UIOutput.make(participantForm, "accountLabel", messageLocator.getMessage("accountLabel"));

		UICommand.make(participantForm, "continue", messageLocator.getMessage("gen.continue"), "#{siteAddParticipantHandler.processGetParticipant}");
        UICommand.make(participantForm, "cancel", messageLocator.getMessage("gen.cancel"), "#{siteAddParticipantHandler.processCancel}");
        
        //process any messages
        targettedMessageList = handler.targettedMessageList;
        if (targettedMessageList != null && targettedMessageList.size() > 0) {
			for (int i = 0; i < targettedMessageList.size(); i++ ) {
				TargettedMessage msg = targettedMessageList.messageAt(i);
				if (msg.severity == TargettedMessage.SEVERITY_ERROR)
				{
					UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", Integer.toString(i));
					
			    	if (msg.args != null ) 
			    	{
			    		UIMessage.make(errorRow,"error", msg.acquireMessageCode(), (Object[]) msg.args);
			    	} 
			    	else 
			    	{
			    		UIMessage.make(errorRow,"error", msg.acquireMessageCode());
			    	}
				}
				else if (msg.severity == TargettedMessage.SEVERITY_INFO)
				{
					UIBranchContainer errorRow = UIBranchContainer.make(tofill,"info-row:", Integer.toString(i));
						
			    	if (msg.args != null ) 
			    	{
			    		UIMessage.make(errorRow,"info", msg.acquireMessageCode(), (Object[]) msg.args);
			    	} 
			    	else 
			    	{
			    		UIMessage.make(errorRow,"info", msg.acquireMessageCode());
			    	}
				}
		    	
			}
        }
        //frameAdjustingProducer.fillComponents(tofill, "resize", "resetFrame");
         
    }
    
    public ViewParameters getViewParameters() {
    	AddViewParameters params = new AddViewParameters();

        params.id = null;
        return params;
    }
    
    public List<NavigationCase> reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<>();
        togo.add(new NavigationCase("differentRole", new SimpleViewParameters(DifferentRoleProducer.VIEW_ID)));
        return togo;
    }
    
    public void interceptActionResult(ARIResult result, ViewParameters incoming,
            Object actionReturn) 
    {
        if ("done".equals(actionReturn)) {
          Tool tool = handler.getCurrentTool();
           result.resultingView = new RawViewParameters(SakaiURLUtil.getHelperDoneURL(tool, sessionManager));
        }
    }

}
