package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SelectOption;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

/**
 * Creates the dropdown panel for the "Role" column in TreeTable
 *  
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class EditablePanelDropdown extends Panel
{

	private NodeModel nodeModel;
	private TreeNode node;

	public EditablePanelDropdown(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final  Map<String, String> roleMap, final int type)
	{
		super(id);

		this.nodeModel = nodeModel;
		this.node = node;

		SelectOption[] options = new SelectOption[roleMap.size()];
		int i = 0;
		//now sort the map
		List<String> sortList = new ArrayList<String>(roleMap.values());
		Collections.sort(sortList, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});
		Map<String, String> sortedReturnMap = new HashMap<String, String>();
		for(String value : sortList){
			for(Entry<String, String> entry : roleMap.entrySet()){
				if(value.equals(entry.getValue())){
					options[i] = new SelectOption(entry.getValue(), entry.getKey());
					if(entry.getKey().equals(nodeModel.getRealm() + ":" + nodeModel.getRole())){
						nodeModel.setRoleOption(options[i]);
					}
					i++;
					break;
				}
			}
		}
		ChoiceRenderer choiceRenderer = new ChoiceRenderer("label", "value");
		final DropDownChoice choice=new DropDownChoice("roleChoices", inputModel, Arrays.asList(options), choiceRenderer){
			@Override
			public boolean isVisible() {
				if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == type){
					return nodeModel.isDirectAccess() && nodeModel.getNodeShoppingPeriodAdmin();
				}else{
					return nodeModel.isDirectAccess();
				}
			}
		};
		choice.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				String value = ((SelectOption) choice.getModelObject()).getValue();
				String[] realmRoleSplit = value.split(":");
				if(realmRoleSplit.length == 2){
					nodeModel.setRealm(realmRoleSplit[0]);
					nodeModel.setRole(realmRoleSplit[1]);
				}

				//In order for the models to refresh, you have to call "expand" or "collapse" then "updateTree",
				//since I don't want to expand or collapse, I just call whichever one the node is already
				//Refreshing the tree will update all the models and information (like role) will be generated onClick
				if(((BaseTreePage)target.getPage()).getTree().getTreeState().isNodeExpanded(node)){
					((BaseTreePage)target.getPage()).getTree().getTreeState().expandNode(node);
				}else{
					((BaseTreePage)target.getPage()).getTree().getTreeState().collapseNode(node);
				}
				((BaseTreePage)target.getPage()).getTree().updateTree(target);
			}

		});
		add(choice);
	}




}
