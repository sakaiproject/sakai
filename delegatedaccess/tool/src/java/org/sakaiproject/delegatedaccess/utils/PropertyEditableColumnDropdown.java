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

/**
 * Column renderer for the dropdown column (role)
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class PropertyEditableColumnDropdown extends PropertyRenderableColumn
{

	private Map<String, List<String>> realmMap;
	private int type;
	
	public PropertyEditableColumnDropdown(ColumnLocation location, String header, String propertyExpression, Map<String, List<String>> realmMap, int type)
	{
		super(location, header, propertyExpression);
		this.realmMap = realmMap;
		this.type = type;
	}

	/**
	 * @see IColumn#newCell(MarkupContainer, String, TreeNode, int)
	 */
	public Component newCell(MarkupContainer parent, String id, TreeNode node, int level)
	{
		return new EditablePanelDropdown(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node, realmMap, type);
	}

	/**
	 * @see IColumn#newCell(TreeNode, int)
	 */
	public IRenderable newCell(TreeNode node, int level)
	{
		return null;
	}


}
