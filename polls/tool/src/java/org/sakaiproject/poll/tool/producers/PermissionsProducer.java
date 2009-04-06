/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.tool.producers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;

import uk.org.ponder.beanutil.PathUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class PermissionsProducer implements ViewComponentProducer,NavigationCaseReporter {

	public static final String VIEW_ID = "votePermissions";



	
	private MessageLocator messageLocator;


	private static final String PERMISSION_PREFIX ="poll";
	private static Log m_log = LogFactory.getLog(PermissionsProducer.class);

	public String getViewID() {
		// TODO Auto-generated method stub
		return VIEW_ID;
	}

	public void setMessageLocator(MessageLocator messageLocator) {

		this.messageLocator = messageLocator;
	}



	

	Map<String, String> perms = null;

	public void setRoleperms(Map<String, String> perms)
	{
		this.perms = perms;
	}

	private ExternalLogic externalLogic;    
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}



	public void fillComponents(UIContainer tofill, ViewParameters arg1,
			ComponentChecker arg2) {




		//populate the site name ect
		UIOutput.make(tofill,"permissions-title",messageLocator.getMessage("permissions_title"));
		UIOutput.make(tofill,"permissions-instruction",messageLocator.getMessage("permissions_instruction"));

		UIOutput.make(tofill,"site-name",externalLogic.getSiteTile(externalLogic.getCurrentLocationReference()));


		//we need a list of permissions	

		String[] perms = new String[]{
				PollListManager.PERMISSION_VOTE,
				PollListManager.PERMISSION_ADD,
				PollListManager.PERMISSION_DELETE_OWN,
				PollListManager.PERMISSION_DELETE_ANY,
				PollListManager.PERMISSION_EDIT_OWN,
				PollListManager.PERMISSION_EDIT_ANY
		};
		for (int i =0; i < perms.length;i++){
			String thisPerm = (String)perms[i];
			thisPerm = thisPerm.substring(thisPerm.indexOf('.') + 1);
			UIBranchContainer b = UIBranchContainer.make(tofill,"head-row:", Integer.valueOf(i).toString());
			UIOutput.make(b,"perm-name",thisPerm);
		}

		List<String> roleIds = externalLogic.getRoleIdsInRealm(externalLogic.getCurrentLocationReference());
		UIForm form = UIForm.make(tofill,"perm-form");
		UIOutput.make(form,"permissions-role",messageLocator.getMessage("permissions_role"));
		for (int i =0; i < roleIds.size(); i++){
			String roleId = roleIds.get(i);
			m_log.debug("got role " + roleId);
			UIBranchContainer row = UIBranchContainer.make(form,"permission-row:",roleId);
			UIOutput.make(row,"role",roleId);
			//now iterate through the permissions

			String prefix = PathUtil.composePath("roleperms",roleId);
			for (int ip =0; ip < perms.length;ip++){
				String thisPerm = (String)perms[ip];
				thisPerm = thisPerm.substring(thisPerm.indexOf('.') + 1);
				UIBranchContainer col = UIBranchContainer.make(row,"box-row:", thisPerm);
				m_log.debug("drawing box for "+ thisPerm + " for role " + roleId);
				//Boolean.valueOf(role.isAllowed((String)perms[ip]))

				UIBoundBoolean.make(col, "perm-box","#{" + prefix +"."+ thisPerm + "}",
						Boolean.valueOf(externalLogic.isRoleAllowedInRealm(roleId, externalLogic.getCurrentLocationReference(), (String)perms[ip])));

			}
		}
		UICommand sub = UICommand.make(form, "submit",messageLocator.getMessage("new_poll_submit"), "#{permissionAction.setPermissions}");
		sub.parameters.add(new UIELBinding("#{permissionAction.submissionStatus}", "submit"));
		UICommand cancel = UICommand.make(form, "cancel",messageLocator.getMessage("vote_cancel"),"#{permissionAction.cancel}");
		cancel.parameters.add(new UIELBinding("#{permissionAction.submissionStatus}", "cancel"));



	}

	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>(); // Always navigate back to this view.
		togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		togo.add(new NavigationCase("Success", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		return togo;
	}	

}
