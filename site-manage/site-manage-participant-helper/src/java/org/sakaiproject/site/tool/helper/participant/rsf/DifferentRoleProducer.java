package org.sakaiproject.site.tool.helper.participant.rsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.participant.impl.SiteAddParticipantHandler;
import org.sakaiproject.site.tool.helper.participant.impl.UserRoleEntry;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import org.sakaiproject.rsf.util.SakaiURLUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.stringutil.StringList;

/**
 * Assign different roles while adding participant
 * @author
 *
 */
public class DifferentRoleProducer implements ViewComponentProducer, NavigationCaseReporter, ActionResultInterceptor{

	/** Our log (commons). */
	private static Logger M_log = LoggerFactory.getLogger(DifferentRoleProducer.class);
	
    public SiteAddParticipantHandler handler;
    public static final String VIEW_ID = "DifferentRole";
    public MessageLocator messageLocator;
    public FrameAdjustingProducer frameAdjustingProducer;
    public SessionManager sessionManager;

    private String[] roleIds;
    
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
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	  
    public void fillComponents(UIContainer tofill, ViewParameters arg1, ComponentChecker arg2) {

		String locationId = developerHelperService.getCurrentLocationId();
		String siteTitle = "";
		try {
			Site s = siteService.getSite(locationId);
			siteTitle = s.getTitle();
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		UIMessage.make(tofill, "addconf.confirming", "addconf.confirming", new Object[]{siteTitle});

		UIBranchContainer content = UIBranchContainer.make(tofill, "content:");

		List<Role> roles = handler.getRoles();
	    StringList roleIds = new StringList();
	    int i = 0;
	    for (Role role: roles) {
	    	if (!role.isProviderOnly())
	    	{
	    		roleIds.add(role.getId());
	    		UIBranchContainer roleRow = UIBranchContainer.make(content, "role-row:", role.getId());
	            UIOutput.make(roleRow, "role-id", role.getId());
	            UIOutput.make(roleRow, "role-description", StringUtils.trimToEmpty(role.getDescription()));
	    	}
	    }
        
    	UIForm differentRoleForm = UIForm.make(content, "differentRole-form");

		String[] values = new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString()};
		String[] labels = new String[] {
				messageLocator.getMessage("addnoti.sendnow"),
				messageLocator.getMessage("addnoti.dontsend")
		};
		StringList notiItems = new StringList();
		UISelect notiSelect = UISelect.make(differentRoleForm, "select-noti", null,
				"#{siteAddParticipantHandler.emailNotiChoice}", handler.emailNotiChoice);
		String selectID = notiSelect.getFullID();
		notiSelect.optionnames = UIOutputMany.make(labels);
		for (int j = 0; j < values.length; j++) {

			UIBranchContainer notiRow = UIBranchContainer.make(differentRoleForm, "noti-row:", Integer.toString(j));

			UISelectLabel lb = UISelectLabel.make(notiRow, "noti-label", selectID, j);
			UISelectChoice choice = UISelectChoice.make(notiRow, "noti-select", selectID, j);
			UILabelTargetDecorator.targetLabel(lb, choice);

			notiItems.add(values[j]);
		}
		notiSelect.optionlist.setValue(notiItems.toStringArray());

    	// csrf token
    	UIInput.make(differentRoleForm, "csrfToken", "#{siteAddParticipantHandler.csrfToken}", handler.csrfToken);
    	
        // list of users
        String curItemNum = null;
        i = 0;
        for (Iterator<UserRoleEntry> it=handler.userRoleEntries.iterator(); it.hasNext(); i++) {
        	curItemNum = Integer.toString(i);
        	UserRoleEntry userRoleEntry = it.next();
        	String userEId = userRoleEntry.userEId;
        	// default to userEid
        	String lastName = "";
        	String firstName = "";
        	String displayId = userEId;
        	// if there is last name or first name specified, use it
        	if (userRoleEntry.lastName != null && userRoleEntry.lastName.length() > 0
        			|| userRoleEntry.firstName != null && userRoleEntry.firstName.length() > 0) {
        		lastName = userRoleEntry.lastName;
        		firstName = userRoleEntry.firstName;
        	}
        	// get user from directory
        	try
        	{
        		User u = userDirectoryService.getUserByEid(userEId);
        		lastName = u.getLastName();
        		firstName = u.getFirstName();
        		displayId = u.getDisplayId();
        	}
        	catch (Exception e)
        	{
        		M_log.debug(this + ":fillComponents: cannot find user with eid=" + userEId);
        	}
            // SECOND LINE
            UIBranchContainer userRow = UIBranchContainer.make(differentRoleForm, "user-row:", curItemNum);
            UIOutput.make(userRow, "last-name", lastName);
            UIOutput.make(userRow, "first-name", firstName);
            UIOutput.make(userRow, "user-name", displayId);
            UISelect.make(userRow, "role-select", roleIds.toStringArray(), "siteAddParticipantHandler.userRoleEntries." + i + ".role", handler.getUserRole(userEId));

  		}
		
		UISelect.make(differentRoleForm, "role-select-all", roleIds.toStringArray(), handler.sameRoleChoice, String.valueOf(handler.getRoles().get(0)));

		// SAK-26101 only show the status choice when "activeInactiveUser" is set to be true
		String activeInactiveUser = handler.getServerConfigurationString("activeInactiveUser", Boolean.FALSE.toString());
		if (activeInactiveUser.equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			UIOutput.make(differentRoleForm, "status-message", messageLocator.getMessage("participant.status"));
			// status choice
			String[] statusValues = new String[] { "active", "inactive"};
			String[] statusLabels = new String[] {
					messageLocator.getMessage("sitegen.siteinfolist.active"),
					messageLocator.getMessage("sitegen.siteinfolist.inactive")
			};
			StringList statusItems = new StringList();
			UISelect statusSelect = UISelect.make(differentRoleForm, "select-status", null, "#{siteAddParticipantHandler.statusChoice}", handler.statusChoice);
			statusSelect.optionnames = UIOutputMany.make(statusLabels);
			String statusSelectID = statusSelect.getFullID();
			for (int k = 0; k < statusValues.length; ++k) {
				UIBranchContainer statusRow = UIBranchContainer.make(differentRoleForm,"status-row:", Integer.toString(k));
				UISelectLabel lb = UISelectLabel.make(statusRow, "status-label", statusSelectID, k);
				UISelectChoice choice =UISelectChoice.make(statusRow, "status-select", statusSelectID, k);
				UILabelTargetDecorator.targetLabel(lb, choice);
				statusItems.add(statusValues[k]);
			}
			statusSelect.optionlist.setValue(statusItems.toStringArray());
		}

		UIMessage.make(content, "addconf.finish", "addconf.finish");

    	UICommand.make(differentRoleForm, "continue", messageLocator.getMessage("gen.finish"), "#{siteAddParticipantHandler.processConfirmContinue}");
    	UICommand.make(differentRoleForm, "back", messageLocator.getMessage("gen.back"), "#{siteAddParticipantHandler.processDifferentRoleBack}");
    	UICommand.make(differentRoleForm, "cancel", messageLocator.getMessage("gen.cancel"), "#{siteAddParticipantHandler.processCancel}");
   
    	//process any messages
    	targettedMessageList = handler.targettedMessageList;
        if (targettedMessageList != null && targettedMessageList.size() > 0) {
			for (int ii = 0; ii < targettedMessageList.size(); ii++ ) {
				TargettedMessage msg = targettedMessageList.messageAt(i);
				if (msg.severity == TargettedMessage.SEVERITY_ERROR)
				{
					UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", Integer.valueOf(ii).toString());
					
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
					UIBranchContainer errorRow = UIBranchContainer.make(tofill,"info-row:", Integer.valueOf(ii).toString());
						
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
         
    }
    
    public ViewParameters getViewParameters() {
    	AddViewParameters params = new AddViewParameters();

        params.id = null;
        return params;
    }
    
    public List<NavigationCase> reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase("back", new SimpleViewParameters(AddProducer.VIEW_ID)));
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
