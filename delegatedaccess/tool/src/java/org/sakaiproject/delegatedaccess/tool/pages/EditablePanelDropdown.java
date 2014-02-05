/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SelectOption;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

/**
 * Creates the dropdown panel for the "Role" column in TreeTable
 *  
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class EditablePanelDropdown extends Panel
{

	private NodeModel nodeModel;
	private TreeNode node;

	public EditablePanelDropdown(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final  Map<String, String> roleMap, final int type, String[] subAdminRoles)
	{
		super(id);

		this.nodeModel = nodeModel;
		this.node = node;

		SelectOption[] options = new SelectOption[roleMap.size()];
		int i = 0;
		//now sort the map
		List<String> sortList = new ArrayList<String>(roleMap.values());
		Collections.sort(sortList, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		Map<String, String> sortedReturnMap = new HashMap<String, String>();
		for(String value : sortList){
			for(Entry<String, String> entry : roleMap.entrySet()){
				if(value.equals(entry.getValue())){
					options[i] = new SelectOption(entry.getValue(), entry.getKey());
					if(entry.getKey().equals(nodeModel.getRealm() + ":" + nodeModel.getRole())){
						nodeModel.setRoleOption(options[i]);
					}
					i++;
					break;
				}
			}
		}
		//sub admins should only be able to update up to their level granted to them
		List<String> restrictedRoles = new ArrayList<String>();
		if(subAdminRoles != null){
			//this is a node where we need to restrict based on the sub admin's role
			String[] realmRole = null;
			if(nodeModel.getNodeSubAdminSiteAccess() != null){
				realmRole = nodeModel.getNodeSubAdminSiteAccess();
			}
			String subAdminRole = "";
			if(realmRole != null && realmRole.length == 2 && !"".equals(realmRole[0]) && !"".equals(realmRole[1])){
				subAdminRole = realmRole[0] + ":" + realmRole[1];
			}
			for(String level : subAdminRoles){
				//each level consists of "realm:role;realm:role;..."
				String[] splitRoles = level.split(";");
				boolean foundRole = false;
				for(String levelRole : splitRoles){
					if(!"".equals(subAdminRole) && subAdminRole.equals(levelRole)){
						foundRole = true;
						break;
					}
				}
				if(foundRole){
					break;
				}else{
					restrictedRoles.addAll(Arrays.asList(splitRoles));
				}
			}
		}
		final List<String> restrictedRolesFinal = restrictedRoles;
		
		ChoiceRenderer choiceRenderer = new ChoiceRenderer("label", "value");
		final DropDownChoice choice=new DropDownChoice("roleChoices", inputModel, Arrays.asList(options), choiceRenderer){
			@Override
			public boolean isVisible() {
				if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == type){
					return nodeModel.isDirectAccess() && nodeModel.getNodeShoppingPeriodAdmin();
				}else{
					return nodeModel.isDirectAccess();
				}
			}
			@Override
			protected boolean isDisabled(Object object, int index,
					String selected) {
				for(String role : restrictedRolesFinal){
					if(role.equals(((SelectOption) object).getValue())){
						return true;
					}
				}
				return super.isDisabled(object, index, selected);
			}
		};
		choice.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				String value = ((SelectOption) choice.getModelObject()).getValue();
				String[] realmRoleSplit = value.split(":");
				if(realmRoleSplit.length == 2){
					nodeModel.setRealm(realmRoleSplit[0]);
					nodeModel.setRole(realmRoleSplit[1]);
				}

				//In order for the models to refresh, you have to call "expand" or "collapse" then "updateTree",
				//since I don't want to expand or collapse, I just call whichever one the node is already
				//Refreshing the tree will update all the models and information (like role) will be generated onClick
				if(((BaseTreePage)target.getPage()).getTree().getTreeState().isNodeExpanded(node)){
					((BaseTreePage)target.getPage()).getTree().getTreeState().expandNode(node);
				}else{
					((BaseTreePage)target.getPage()).getTree().getTreeState().collapseNode(node);
				}
				((BaseTreePage)target.getPage()).getTree().updateTree(target);
			}

		});
		add(choice);
	}




}
