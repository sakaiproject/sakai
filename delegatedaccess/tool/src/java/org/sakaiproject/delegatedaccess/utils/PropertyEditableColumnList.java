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
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelList;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelListInherited;

public class PropertyEditableColumnList extends PropertyRenderableColumn
{

	private int userType;
	private int fieldType;
	
	public PropertyEditableColumnList(ColumnLocation location, String header, String propertyExpression, int userType, int fieldType)
	{
		super(location, header, propertyExpression);
		this.userType = userType;
		this.fieldType = fieldType;
	}

	/**
	 * @see IColumn#newCell(MarkupContainer, String, TreeNode, int)
	 */
	public Component newCell(MarkupContainer parent, String id, TreeNode node, int level)
	{
		if(((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isDirectAccess()){
			return new EditablePanelList(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node, userType, fieldType);
		}else{
			return new EditablePanelListInherited(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node, userType, fieldType);
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