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
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelDropdown;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelDropdownText;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelEmpty;

/**
 * Column renderer for the dropdown column (role)
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class PropertyEditableColumnDropdown extends PropertyRenderableColumn
{

	private Map<String, String> roleMap;
	private int type;
	private String[] subAdminRoles;
	
	public PropertyEditableColumnDropdown(ColumnLocation location, String header, String propertyExpression, Map<String, String> roleMap, int type, String[] subAdminRoles)
	{
		super(location, header, propertyExpression);
		this.roleMap = roleMap;
		this.type = type;
		this.subAdminRoles = subAdminRoles;
	}

	/**
	 * @see IColumn#newCell(MarkupContainer, String, TreeNode, int)
	 */
	public Component newCell(MarkupContainer parent, String id, TreeNode node, int level)
	{
		if(!((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isNodeEditable()){
			return new EditablePanelEmpty(id);
		}
		
		if(((NodeModel) ((DefaultMutableTreeNode) node).getUserObject()).isDirectAccess()){
			return new EditablePanelDropdown(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node, roleMap, type, subAdminRoles);
		}else{
			return new EditablePanelDropdownText(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node, roleMap, type);
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
