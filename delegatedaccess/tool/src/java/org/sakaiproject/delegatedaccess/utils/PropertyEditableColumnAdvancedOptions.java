package org.sakaiproject.delegatedaccess.utils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.IRenderable;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyRenderableColumn;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelAdvancedOptions;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelAdvancedOptionsText;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelEmpty;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

public class PropertyEditableColumnAdvancedOptions extends PropertyRenderableColumn{


	private int userType;

	public PropertyEditableColumnAdvancedOptions(ColumnLocation location, String header, String propertyExpression, int userType) {
		super(location, header, propertyExpression);
		this.userType = userType;
	}

	/**
	 * @see IColumn#newCell(MarkupContainer, String, TreeNode, int)
	 */
	public Component newCell(MarkupContainer parent, String id, TreeNode node, int level)
	{
		if(!((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isNodeEditable()){
			return new EditablePanelEmpty(id);
		}

		if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
			if(!((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).getNodeShoppingPeriodAdmin()){
				return new EditablePanelEmpty(id);
			}
		}
		
		if(((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isDirectAccess()){
			return new EditablePanelAdvancedOptions(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node, userType);
		}else{
			return new EditablePanelAdvancedOptionsText(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node, userType);
		}
	}

	/**
	 * @see IColumn#newCell(TreeNode, int)
	 */
	public IRenderable newCell(TreeNode node, int level)
	{
		return null;
	}
}
