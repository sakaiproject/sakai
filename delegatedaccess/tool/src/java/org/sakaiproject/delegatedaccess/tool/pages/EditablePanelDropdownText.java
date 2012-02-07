package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

public class EditablePanelDropdownText extends Panel{
	
	public EditablePanelDropdownText(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final Map<String, String> realmMap, final int type)
	{
		super(id);
		
		//show the inherited role if the user hasn't selected this node
		IModel<String> labelModel = new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				String[] inheritedAccess;
				if(nodeModel.isDirectAccess()){
					inheritedAccess = nodeModel.getNodeAccessRealmRole();
				}else{
					inheritedAccess = nodeModel.getInheritedAccessRealmRole();
				}
				if("".equals(inheritedAccess[0])){
					return "";
				}else{
					String realmRole = inheritedAccess[0] + ":" + inheritedAccess[1];
					if(realmMap.containsKey(realmRole)){
						return realmMap.get(realmRole);
					}else{
						return realmRole;
					}
				}
			}
		};
		Label label = new Label("realmRole", labelModel){
			public boolean isVisible() {
				if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == type){
					return !nodeModel.isDirectAccess() || !nodeModel.getNodeShoppingPeriodAdmin();
				}else{
					return !nodeModel.isDirectAccess();
				}
			};
		};
		add(label);
	}

}
