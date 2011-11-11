package org.sakaiproject.delegatedaccess.tool.pages;

/**
 * BaseTreePage is a base page for all pages that want to use AbstractTree.  It extends BasePage as well.
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.delegatedaccess.model.HierarchyNodeSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;


public abstract class BaseTreePage extends BasePage
{

	private List<String> accessNodes = new ArrayList<String>();

	/**
	 * Returns the tree on this pages. This is used to collapse, expand, ect
	 * 
	 * @return Tree instance on this page
	 */
	protected abstract AbstractTree getTree();

	//NodeCache stores HierarchyNodeSerialed nodes for faster lookups
	private Map<String,HierarchyNodeSerialized> nodeCache = new HashMap<String, HierarchyNodeSerialized>();

	/**
	 * Creates the model that feeds the tree.
	 * 
	 * @return New instance of tree model.
	 */
	protected TreeModel createTreeModelForUser(String userId, boolean addDirectChildren, boolean cascade)
	{
		//Returns a List that represents the tree/node architecture:
		//  List{ List{node, List<children>}, List{node, List<children>}, ...}.
		List<List> l1 = projectLogic.getTreeListForUser(userId, addDirectChildren, cascade, accessNodes);
		//order tree model:
		orderTreeModel(l1);

		return convertToTreeModel(l1, userId);
	}

	/**
	 * takes a list representation of the tree and orders it Alphabetically
	 * @param hierarchy
	 */
	private void orderTreeModel(List<List> hierarchy){
		if(hierarchy != null){
			for(List nodeList : hierarchy){
				orderTreeModel((List)nodeList.get(1));
			}
			Collections.sort(hierarchy, new NodeListComparator());
		}
	}

	/**
	 * returns a list of direct children for the parent node.  The children will 
	 * have empty lists for their children
	 * 
	 * @param parent
	 * @return
	 */
	private List<List> getDirectChildren(HierarchyNodeSerialized parent){
		List<List>returnList = new ArrayList<List>();

		if(parent != null){
			Set<String> parentChildren = parent.directChildNodeIds;
			for(String childId : parentChildren){
				List child = new ArrayList();
				child.add(getNode(childId));
				child.add(new ArrayList());
				returnList.add(child);
			}
		}
		return returnList;
	}

	/**
	 * Checks nodeCache for node with given id.  If not found,
	 * looks up the node in the db and saves it in the cache
	 * 
	 * @param id
	 * @return
	 */
	private HierarchyNodeSerialized getNode(String id){
		HierarchyNodeSerialized node = nodeCache.get(id);
		if(node == null){
			node = projectLogic.getNode(id);
			nodeCache.put(id, node);
		}
		return node;
	}

	/**
	 * Takes a list representation of a tree and creates the TreeModel
	 * 
	 * @param map
	 * @param userId
	 * @return
	 */
	private TreeModel convertToTreeModel(List<List> map, String userId)
	{
		TreeModel model = null;
		if(!map.isEmpty() && map.size() == 1){

			DefaultMutableTreeNode rootNode = add(null, map, getRealmMap(), userId);
			model = new DefaultTreeModel(rootNode);
		}
		return model;
	}

	/**
	 * returns a map of all realms and their roles from sakaiProxy.getSiteTemplates()
	 * 
	 * @return
	 */
	private Map<String, List<String>> getRealmMap(){
		List<AuthzGroup> siteTemplates = sakaiProxy.getSiteTemplates();
		final Map<String, List<String>> realmMap = new HashMap<String, List<String>>();
		for(AuthzGroup group : siteTemplates){
			List<String> roles = new ArrayList<String>();
			for(Role role : group.getRoles()){
				roles.add(role.getId());
			}
			realmMap.put(group.getId(), roles);
		}
		return realmMap;
	}

	/**
	 * Adds node to parent and creates the NodeModel to store in the tree
	 * @param parent
	 * @param sub
	 * @param realmMap
	 * @param userId
	 * @return
	 */
	private DefaultMutableTreeNode add(DefaultMutableTreeNode parent, List<List> sub, Map<String, List<String>> realmMap, String userId)
	{
		DefaultMutableTreeNode root = null;
		for (List nodeList : sub)
		{
			HierarchyNodeSerialized node = (HierarchyNodeSerialized) nodeList.get(0);
			List children = (List) nodeList.get(1);
			String realm = "";
			String role = "";
			if(accessNodes.contains(node.id)){
				String[] realmRole = projectLogic.getAccessRealmRole(userId, node.id);
				realm = realmRole[0];
				role = realmRole[1];
			}
			NodeModel parentNodeModel = null;
			if(parent != null){
				parentNodeModel = ((NodeModel) parent.getUserObject());
			}
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(new NodeModel(node.id, node, accessNodes.contains(node.id), realmMap, realm, role, parentNodeModel, projectLogic.getRestrictedToolSerializedList(userId, node.id))){
				@Override
				public boolean isLeaf() {
					return ((NodeModel) this.getUserObject()).getNode().childNodeIds.isEmpty();
				}
			};
			if(parent == null){
				//we have the root, set it
				root = child;
			}else{
				parent.add(child);
			}
			if(!children.isEmpty()){
				add(child, children, realmMap, userId);
			}
		}
		return root;
	}

	/**
	 * Adds children node to a node that hasn't had it's children populated.  This is used to increase the efficiency
	 * of the tree so you can create the structure on the fly with ajax
	 * 
	 * @param node
	 * @param tree
	 * @param target
	 * @param userId
	 * @return
	 */
	protected boolean addChildrenNodes(Object node, AbstractTree tree, AjaxRequestTarget target, String userId){
		boolean anyAdded = false;
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node;
		NodeModel nodeModel = (NodeModel) ((DefaultMutableTreeNode) node).getUserObject();
		if(nodeModel.getNode() != null){
			Map<String, List<String>> realmMap = getRealmMap();
			List<List> childrenNodes = getDirectChildren(nodeModel.getNode());
			Collections.sort(childrenNodes, new NodeListComparator());
			for(List childList : childrenNodes){
				boolean newlyAdded = addChildNodeToTree((HierarchyNodeSerialized) childList.get(0), parentNode, realmMap, userId);
				anyAdded = anyAdded || newlyAdded;
			}
		}
		if(anyAdded){
			//don't let the children be expanded too
			collapseEmptyFoldersHelper(parentNode);
		}
		return anyAdded;
	}

	/**
	 * This is a helper function for addChildrenNodes.  It will add the child nodes to the parent node and create the NodeModel.
	 * 
	 * @param childNode
	 * @param parentNode
	 * @param realmMap
	 * @param userId
	 * @return
	 */
	private boolean addChildNodeToTree(HierarchyNodeSerialized childNode, DefaultMutableTreeNode parentNode, Map<String, List<String>> realmMap, String userId){
		boolean added = false;
		if(!doesChildExist(childNode.id, parentNode)){
			String realm = "";
			String role = "";
			if(accessNodes.contains(childNode.id)){
				String[] realmRole = projectLogic.getAccessRealmRole(userId, childNode.id);
				realm = realmRole[0];
				role = realmRole[1];
			}
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(new NodeModel(childNode.id, childNode, accessNodes.contains(childNode.id), realmMap, realm, role, ((NodeModel) parentNode.getUserObject()), projectLogic.getRestrictedToolSerializedList(userId, childNode.id))){
				@Override
				public boolean isLeaf() {
					return ((NodeModel) this.getUserObject()).getNode().childNodeIds.isEmpty();
				}
			};
			parentNode.add(child);
			added = true;
		}
		return added;
	}

	/**
	 * Determines if the child exists in the tree structure.  This is a helper function for addChildNodeToTree to ensure 
	 * the duplicate child nodes aren't added
	 * 
	 * @param childNodeId
	 * @param parentNode
	 * @return
	 */
	private boolean doesChildExist(String childNodeId, DefaultMutableTreeNode parentNode){
		boolean exists = false;

		for(int i = 0; i < parentNode.getChildCount(); i++){
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parentNode.getChildAt(i);
			if(childNodeId.equals(((NodeModel) child.getUserObject()).getNodeId())){
				exists = true;
				break;
			}
		}

		return exists;
	}

	/**
	 * This saves the state of the tree.  It goes through the entire structure and saves the access and role information
	 * for each node.  It will remove and add all information.
	 * @param userId
	 */
	protected void updateNodeAccess(String userId){
		if(getTree() != null){
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getTree().getModelObject().getRoot();
			if(rootNode != null){
				updateNodeAccessHelper(rootNode, userId);
			}
		}
	}
	/**
	 * This is a helper function for updateNodeAccess.
	 * @param node
	 * @param userId
	 */
	private void updateNodeAccessHelper(DefaultMutableTreeNode node, String userId){
		if(node.getUserObject() != null){
			NodeModel nodeModel = (NodeModel) node.getUserObject();
			if(nodeModel.isModified()){
				projectLogic.updateNodePermissionsForUser(nodeModel, userId);
			}

			//check the rest of the children:
			for(int i = 0; i < node.getChildCount(); i++){
				updateNodeAccessHelper((DefaultMutableTreeNode)node.getChildAt(i), userId);
			}
		}
	}

	/**
	 * This collapses all empty folders in the tree.  This helps the user know that they need to click on the folder
	 * in order to populate the children nodes.  (used when the structure is being populated on the fly w/ajax and the
	 * folders haven't been populated yet)
	 */
	protected void collapseEmptyFolders(){
		collapseEmptyFoldersHelper((DefaultMutableTreeNode) getTree().getModelObject().getRoot());
	}

	/**
	 * Helper function for collapseEmptyFoldersHelper
	 * @param node
	 */
	private void collapseEmptyFoldersHelper(DefaultMutableTreeNode node){
		if(node != null){
			if(!node.isLeaf() && node.getChildCount() == 0){
				//this is a node that isn't a leaf but hasn't had the children updated, make it collapse
				getTree().getTreeState().collapseNode(node);
			}
			for(int i = 0; i < node.getChildCount(); i++){
				collapseEmptyFoldersHelper((DefaultMutableTreeNode)node.getChildAt(i));
			}
		}
	}

	/**
	 * This is a simple comparator to order the tree nodes alphabetically
	 *
	 */
	private class NodeListComparator implements Comparator<List>{
		public int compare(List o1, List o2) {
			return ((HierarchyNodeSerialized) o1.get(0)).title.compareToIgnoreCase(((HierarchyNodeSerialized) o2.get(0)).title);
		}
	}

	protected AjaxLink getExpandCollapseLink(){
		//Expand Collapse Link:
		final Label expandCollapse = new Label("expandCollapse", new StringResourceModel("exapndNodes", null));
		expandCollapse.setOutputMarkupId(true);
		AjaxLink expandLink  = new AjaxLink("expandAll")
		{
			boolean expand = true;
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				if(expand){
					getTree().getTreeState().expandAll();
					expandCollapse.setDefaultModel(new StringResourceModel("collapseNodes", null));
					collapseEmptyFolders();
				}else{
					getTree().getTreeState().collapseAll();
					expandCollapse.setDefaultModel(new StringResourceModel("exapndNodes", null));
				}
				target.addComponent(expandCollapse);
				getTree().updateTree(target);
				expand = !expand;

			}
			@Override
			public boolean isVisible() {
				return getTree().getDefaultModelObject() != null;
			}
		};
		expandLink.add(expandCollapse);
		return expandLink;
	}

}