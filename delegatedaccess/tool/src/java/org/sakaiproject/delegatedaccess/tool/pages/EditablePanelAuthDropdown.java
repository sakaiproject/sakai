package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.Arrays;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

public class EditablePanelAuthDropdown extends Panel{

	public EditablePanelAuthDropdown(String id, IModel model, final NodeModel nodeModel, final TreeNode node) {
		super(id, model);
		
		final DropDownChoice choice=new DropDownChoice("dropDownChoice", model, Arrays.asList(".auth", ".anon")){
			@Override
			public boolean isVisible() {
				return nodeModel.isDirectAccess();
			}
		};
		choice.add(new AjaxFormComponentUpdatingBehavior("onblur")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
            	nodeModel.setShoppingPeriodAuth(choice.getModelObject().toString());
            }
            
        });
		add(choice);
	}

}
