package org.sakaiproject.delegatedaccess.utils;

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
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelAdvancedUserOptions;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelAdvancedUserOptionsText;
import org.sakaiproject.delegatedaccess.tool.pages.EditablePanelEmpty;

public class PropertyEditableColumnAdvancedUserOptions extends PropertyRenderableColumn{

	public static final String SETTINGS_ALLOW_SET_BECOME_USER = "allowBecomeUser";
	private Map<String, Object> settings;
	
	public PropertyEditableColumnAdvancedUserOptions(ColumnLocation location, String header, String propertyExpression, Map<String, Object> settings) {
		super(location, header, propertyExpression);
		this.settings = settings;
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
			return new EditablePanelAdvancedUserOptions(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node, settings);
		}else{
			return new EditablePanelAdvancedUserOptionsText(id, new PropertyModel(node, getPropertyExpression()), (NodeModel) ((DefaultMutableTreeNode) node).getUserObject(), node, settings);
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
