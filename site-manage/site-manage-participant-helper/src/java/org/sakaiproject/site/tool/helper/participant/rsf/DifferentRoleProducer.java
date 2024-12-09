/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.site.tool.helper.participant.rsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.rsf.util.SakaiURLUtil;
import org.sakaiproject.site.tool.helper.participant.impl.SiteAddParticipantHandler;
import org.sakaiproject.site.tool.helper.participant.impl.UserRoleEntry;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import lombok.extern.slf4j.Slf4j;
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
import uk.org.ponder.stringutil.StringList;

/**
 * Assign different roles while adding participant
 */
@Slf4j
public class DifferentRoleProducer implements ViewComponentProducer, NavigationCaseReporter, ActionResultInterceptor{

	public static final String VIEW_ID = "DifferentRole";

	@Setter private SiteAddParticipantHandler handler;
	@Setter private MessageLocator messageLocator;
	@Setter private SessionManager sessionManager;
    @Setter private TargettedMessageList targettedMessageList;
	@Setter private UserDirectoryService userDirectoryService;

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters arg1, ComponentChecker arg2) {
    	UIBranchContainer content = UIBranchContainer.make(tofill, "content:");
    	
		List<Role> roles = handler.getRoles();
	    StringList roleIds = new StringList();
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
    	// csrf token
    	UIInput.make(differentRoleForm, "csrfToken", "#{siteAddParticipantHandler.csrfToken}", handler.csrfToken);
    	
        // list of users
        int i = 0;
		for (Iterator<UserRoleEntry> it = handler.userRoleEntries.iterator(); it.hasNext(); i++) {
        	UserRoleEntry userRoleEntry = it.next();
        	String userEId = userRoleEntry.getEid();
        	// default to userEid
        	String userName = userEId;
        	String displayId = userEId;
        	// if there is last name or first name specified, use it
        	if (userRoleEntry.getLastName() != null && !userRoleEntry.getLastName().isEmpty()
        			|| userRoleEntry.getFirstName() != null && !userRoleEntry.getFirstName().isEmpty())
        		userName = userRoleEntry.getLastName() + "," + userRoleEntry.getFirstName();

        	try {
        		// get user from directory
        		User u = userDirectoryService.getUserByEid(userEId);
        		userName = u.getSortName();
        		displayId = u.getDisplayId();
        	} catch (Exception e) {
        		log.debug("cannot find user with eid={}", userEId);
        	}
            // SECOND LINE
            UIBranchContainer userRow = UIBranchContainer.make(differentRoleForm, "user-row:", Integer.toString(i));
            UIOutput.make(userRow, "user-name", displayId + " ( " + userName + " )");
            UISelect.make(userRow, "role-select", roleIds.toStringArray(), "siteAddParticipantHandler.userRoleEntries." + i + ".role", handler.getUserRole(userEId));
  		}
        
    	UICommand.make(differentRoleForm, "continue", messageLocator.getMessage("gen.continue"), "#{siteAddParticipantHandler.processDifferentRoleContinue}");
    	UICommand.make(differentRoleForm, "back", messageLocator.getMessage("gen.back"), "#{siteAddParticipantHandler.processDifferentRoleBack}");
    	UICommand.make(differentRoleForm, "cancel", messageLocator.getMessage("gen.cancel"), "#{siteAddParticipantHandler.processCancel}");
   
    	//process any messages
    	targettedMessageList = handler.getTargettedMessageList();
        if (targettedMessageList != null && targettedMessageList.size() > 0) {
			for (int j = 0; j < targettedMessageList.size(); j++ ) {
				TargettedMessage msg = targettedMessageList.messageAt(i);
				if (msg.severity == TargettedMessage.SEVERITY_ERROR) {
					UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", Integer.toString(j));
					
			    	if (msg.args != null ) {
			    		UIMessage.make(errorRow,"error", msg.acquireMessageCode(), msg.args);
			    	} else {
			    		UIMessage.make(errorRow,"error", msg.acquireMessageCode());
			    	}
				} else if (msg.severity == TargettedMessage.SEVERITY_INFO) {
					UIBranchContainer errorRow = UIBranchContainer.make(tofill,"info-row:", Integer.toString(j));
						
			    	if (msg.args != null ) {
			    		UIMessage.make(errorRow,"info", msg.acquireMessageCode(), msg.args);
			    	} else {
			    		UIMessage.make(errorRow,"info", msg.acquireMessageCode());
			    	}
				}
			}
        }
    }
    
    public ViewParameters getViewParameters() {
    	AddViewParameters params = new AddViewParameters();
        params.setId(null);
        return params;
    }
    
    public List<NavigationCase> reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<>();
        togo.add(new NavigationCase("continue", new SimpleViewParameters(EmailNotiProducer.VIEW_ID)));
        togo.add(new NavigationCase("back", new SimpleViewParameters(AddProducer.VIEW_ID)));
        return togo;
    }

	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
		if ("done".equals(actionReturn)) {
			Tool tool = handler.getCurrentTool();
			result.resultingView = new RawViewParameters(SakaiURLUtil.getHelperDoneURL(tool, sessionManager));
		}
	}
}
