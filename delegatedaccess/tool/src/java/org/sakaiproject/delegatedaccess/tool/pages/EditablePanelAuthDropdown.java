package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.Arrays;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SelectOption;

/**
 * This is the .auth or .anon dropdown option for shopping period admin
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class EditablePanelAuthDropdown extends Panel{

	public EditablePanelAuthDropdown(String id, IModel model, final NodeModel nodeModel, final TreeNode node, List<ListOptionSerialized> authorizationOptions) {
		super(id, model);

		SelectOption[] options = new SelectOption[authorizationOptions.size()];
		int i=0;
		for(ListOptionSerialized option : authorizationOptions){
			options[i] = new SelectOption(option.getName(), option.getId());
			i++;
		}
		
		ChoiceRenderer choiceRenderer = new ChoiceRenderer("label", "value");
		final DropDownChoice choice=new DropDownChoice("dropDownChoice", model, Arrays.asList(options), choiceRenderer){
			@Override
			public boolean isVisible() {
				return nodeModel.isDirectAccess() && nodeModel.getNodeShoppingPeriodAdmin();
			}
		};
		choice.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				nodeModel.setShoppingPeriodAuthOption((SelectOption) choice.getModelObject());

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
