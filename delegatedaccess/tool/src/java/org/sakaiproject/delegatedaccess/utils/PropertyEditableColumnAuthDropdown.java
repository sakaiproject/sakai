package org.sakaiproject.delegatedaccess.utils;

import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.IRenderable;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyRenderableColumn;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelAuthDropdown;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelAuthDropdownText;

public class PropertyEditableColumnAuthDropdown extends PropertyRenderableColumn
{

	private List<ListOptionSerialized> authorizationOptions;
	public PropertyEditableColumnAuthDropdown(ColumnLocation location, String header, String propertyExpression, List<ListOptionSerialized> authorizationOptions)
	{
		super(location, header, propertyExpression);
		this.authorizationOptions = authorizationOptions;
	}

	/**
	 * @see IColumn#newCell(MarkupContainer, String, TreeNode, int)
	 */
	public Component newCell(MarkupContainer parent, String id, TreeNode node, int level)
	{
		if(((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isDirectAccess()){
			return new EditablePanelAuthDropdown(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node, authorizationOptions);
		}else{
			return new EditablePanelAuthDropdownText(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node);
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
