package org.sakaiproject.site.tool.helper.participant.rsf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.site.tool.helper.participant.rsf.AddViewParameters;
import org.sakaiproject.site.tool.helper.participant.impl.SiteAddParticipantHandler;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.ac.cam.caret.sakai.rsf.util.SakaiURLUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
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
	private static Log M_log = LogFactory.getLog(AddProducer.class);
	
    public SiteAddParticipantHandler handler;
    public static final String VIEW_ID = "Add";
    public MessageLocator messageLocator;
    public FrameAdjustingProducer frameAdjustingProducer;
    public SessionManager sessionManager;
    public SiteService siteService = null;
    public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

    public String getViewID() {
        return VIEW_ID;
    }
    
    private TargettedMessageList targettedMessageList;
	public void setTargettedMessageList(TargettedMessageList targettedMessageList) {
		this.targettedMessageList = targettedMessageList;
	}
	
	public UserDirectoryService userDirectoryService;
	public void setUserDiretoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

    public void fillComponents(UIContainer tofill, ViewParameters arg1, ComponentChecker arg2) {
    	
    	boolean isCourseSite = handler.isCourseSite();
    	
    	UIBranchContainer content = UIBranchContainer.make(tofill, "content:");
    	
    	if (isCourseSite)
    	{
    		// show specific instructions for adding participant into course site
    		UIMessage.make(content, "add.official", "add.official");
    		UIMessage.make(content, "add.official1", "add.official1");
    		UIMessage.make(content, "add.official.instruction", "add.official.instruction");
	    }
        
    	UIForm participantForm = UIForm.make(content, "participant-form");
    	
    	// official participant
    	UIInput.make(participantForm, "officialAccountParticipant", "#{siteAddParticipantHandler.officialAccountParticipant}", handler.officialAccountParticipant);
    	UIOutput.make(participantForm, "officialAccountSectionTitle", handler.getServerConfigurationString("officialAccountSectionTitle"));
    	UIOutput.make(participantForm, "officialAccountName", handler.getServerConfigurationString("officialAccountName"));
    	UIOutput.make(participantForm, "officialAccountLabel", handler.getServerConfigurationString("officialAccountLabel"));
    	
    	String pickerAction = handler.getServerConfigurationString("officialAccountPickerAction");
		if (pickerAction != null && !"".equals(pickerAction))
		{
			UIOutput.make(participantForm, "officialAccountPickerLabel", handler.getServerConfigurationString("officialAccountPickerLabel"));
			UIOutput.make(participantForm, "officialAccountPickerAction", pickerAction);
		}
    	
		// non official participant
    	String allowAddNonOfficialParticipant = handler.getServerConfigurationString("nonOfficialAccount", "true");
    	if (allowAddNonOfficialParticipant.equalsIgnoreCase("true"))
    	{
    		UIInput.make(participantForm, "nonOfficialAccountParticipant", "#{siteAddParticipantHandler.nonOfficialAccountParticipant}", handler.nonOfficialAccountParticipant);
	    	UIOutput.make(participantForm, "nonOfficialAccountSectionTitle", handler.getServerConfigurationString("nonOfficialAccountSectionTitle"));
	    	UIOutput.make(participantForm, "nonOfficialAccountName", handler.getServerConfigurationString("nonOfficialAccountName"));
	    	UIOutput.make(participantForm, "nonOfficialAccountLabel", handler.getServerConfigurationString("nonOfficialAccountLabel"));
	    	UIMessage.make(participantForm, "nonOfficialAddMultiple", "add.multiple");
    	}
    	
    	// role choice
    	String[] values = new String[] { "sameRole", "differentRole"};
	    String[] labels = new String[] {
	    		messageLocator.getMessage("add.assign"), 
	    		messageLocator.getMessage("add.assign2")
	    		};
	    
	    StringList roleItems = new StringList();
	    
	    UISelect roleSelect = UISelect.make(participantForm, "select-roles", null, "#{siteAddParticipantHandler.roleChoice}", handler.roleChoice);

	    roleSelect.optionnames = UIOutputMany.make(labels);
	    String selectID = roleSelect.getFullID();
	    for (int i = 0; i < values.length; ++i) {
		    UIBranchContainer roleRow = UIBranchContainer.make(participantForm,"role-row:", Integer.toString(i));
            UISelectLabel lb = UISelectLabel.make(roleRow, "role-label", selectID, i);
            UISelectChoice choice =UISelectChoice.make(roleRow, "role-select", selectID, i);
            UILabelTargetDecorator.targetLabel(lb, choice);
            
            roleItems.add(values[i]);
        }
        roleSelect.optionlist.setValue(roleItems.toStringArray());        
		
    	UICommand.make(participantForm, "continue", messageLocator.getMessage("gen.continue"), "#{siteAddParticipantHandler.processGetParticipant}");
        UICommand.make(participantForm, "cancel", messageLocator.getMessage("gen.cancel"), "#{siteAddParticipantHandler.processCancel}");
        
        //process any messages
        targettedMessageList = handler.targettedMessageList;
        if (targettedMessageList != null && targettedMessageList.size() > 0) {
			for (int i = 0; i < targettedMessageList.size(); i++ ) {
				UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", new Integer(i).toString());
				TargettedMessage msg = targettedMessageList.messageAt(i);
		    	if (msg.args != null ) 
		    	{
		    		UIMessage.make(errorRow,"error", msg.acquireMessageCode(), (Object[]) msg.args);
		    	} 
		    	else 
		    	{
		    		UIMessage.make(errorRow,"error", msg.acquireMessageCode());
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
        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase("sameRole", new SimpleViewParameters(SameRoleProducer.VIEW_ID)));
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
