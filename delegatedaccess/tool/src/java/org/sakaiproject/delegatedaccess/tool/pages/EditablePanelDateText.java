package org.sakaiproject.delegatedaccess.tool.pages;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.tree.TreeNode;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

public class EditablePanelDateText extends Panel{
	private SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
	public EditablePanelDateText(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final boolean startDate) {
		super(id);

		IModel<String> labelModel = new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				Date date = null;
				if(startDate)
					if(nodeModel.isDirectAccess())
						date = nodeModel.getNodeShoppingPeriodStartDate();
					else
						date = nodeModel.getInheritedShoppingPeriodStartDate();
				else
					if(nodeModel.isDirectAccess())
						date = nodeModel.getNodeShoppingPeriodEndDate();
					else
						date = nodeModel.getInheritedShoppingPeriodEndDate();
				if(date == null){
					return "";
				}else{
					return format.format(date);
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
