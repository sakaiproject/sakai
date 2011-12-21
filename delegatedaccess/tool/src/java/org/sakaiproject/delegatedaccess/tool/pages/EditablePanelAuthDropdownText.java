package org.sakaiproject.delegatedaccess.tool.pages;

import javax.swing.tree.TreeNode;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

public class EditablePanelAuthDropdownText extends Panel{
	public EditablePanelAuthDropdownText(String id, IModel model, final NodeModel nodeModel, final TreeNode node) {
		super(id);
		IModel<String> labelModel = new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				String auth = null;
				if(nodeModel.isDirectAccess()){
					auth = nodeModel.getNodeShoppingPeriodAuth();
				}else{
					auth = nodeModel.getInheritedShoppingPeriodAuth();
				}
				if(auth != null && !"".equals(auth)){
					return new StringResourceModel(auth, null).getString();
				}else{
					return "";
				}
			}
		};
		add(new Label("inherited", labelModel){
			public boolean isVisible() {
				return !nodeModel.isDirectAccess() || !nodeModel.getNodeShoppingPeriodAdmin();
			};
		});
	}
}
