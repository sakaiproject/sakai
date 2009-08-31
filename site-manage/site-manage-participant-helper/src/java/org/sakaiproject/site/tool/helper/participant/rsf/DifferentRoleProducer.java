package org.sakaiproject.site.tool.helper.participant.rsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.participant.impl.SiteAddParticipantHandler;
import org.sakaiproject.site.tool.helper.participant.impl.UserRoleEntry;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.ac.cam.caret.sakai.rsf.util.SakaiURLUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Assign different roles while adding participant
 * @author
 *
 */
public class DifferentRoleProducer implements ViewComponentProducer, NavigationCaseReporter, ActionResultInterceptor{

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(DifferentRoleProducer.class);
	
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
	public void setUserDiretoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}
	  
    public void fillComponents(UIContainer tofill, ViewParameters arg1, ComponentChecker arg2) {
		List<Role> roles = handler.getRoles();
	    roleIds = new String[roles.size()];
	    int i = 0;
	    for (Role role: roles) {
	      roleIds[i++] = role.getId();
	    }
	    
    	UIBranchContainer content = UIBranchContainer.make(tofill, "content:");
        
    	UIForm differentRoleForm = UIForm.make(content, "differentRole-form");
    	
        // list of users
        String curItemNum = null;
        i = 0;
        for (Iterator<UserRoleEntry> it=handler.userRoleEntries.iterator(); it.hasNext(); i++) {
        	curItemNum = Integer.toString(i);
        	UserRoleEntry userRoleEntry = it.next();
        	String userEId = userRoleEntry.userEId;
        	// default to userEid
        	String userName = userEId;
        	// if there is last name or first name specified, use it
        	if (userRoleEntry.lastName != null && userRoleEntry.lastName.length() > 0 
        			|| userRoleEntry.firstName != null && userRoleEntry.firstName.length() > 0)
        		userName = userRoleEntry.lastName + "," + userRoleEntry.firstName;
        	// get user from directory
        	try
        	{
        		User u = userDirectoryService.getUserByEid(userEId);
        		userName = u.getSortName();
        	}
        	catch (Exception e)
        	{
        		M_log.info(this + ":fillComponents: cannot find user with eid=" + userEId);
        	}
            // SECOND LINE
            UIBranchContainer userRow = UIBranchContainer.make(differentRoleForm, "user-row:", curItemNum);
            UIOutput.make(userRow, "user-name", userEId + "(" + userName + ")");
            UISelect.make(userRow, "role-select", roleIds, "siteAddParticipantHandler.userRoleEntries." + i + ".role", handler.getUserRole(userEId));

  		}
        
    	UICommand.make(differentRoleForm, "continue", messageLocator.getMessage("gen.continue"), "#{siteAddParticipantHandler.processDifferentRoleContinue}");
    	UICommand.make(differentRoleForm, "back", messageLocator.getMessage("gen.back"), "#{siteAddParticipantHandler.processDifferentRoleBack}");
    	UICommand.make(differentRoleForm, "cancel", messageLocator.getMessage("gen.cancel"), "#{siteAddParticipantHandler.processCancel}");
   
    	//process any messages
        targettedMessageList = handler.targettedMessageList;
        if (targettedMessageList != null && targettedMessageList.size() > 0) {
			for (int k = 0; k < targettedMessageList.size(); k++ ) {
				UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", Integer.valueOf(k).toString());
				TargettedMessage msg = targettedMessageList.messageAt(k);
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
         
    }
    
    public ViewParameters getViewParameters() {
    	AddViewParameters params = new AddViewParameters();

        params.id = null;
        return params;
    }
    
    public List<NavigationCase> reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase("continue", new SimpleViewParameters(EmailNotiProducer.VIEW_ID)));
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
