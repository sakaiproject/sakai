package org.sakaiproject.delegatedaccess.tool.pages;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

public class EditablePanelDate  extends Panel{

	public EditablePanelDate(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final boolean startDate)
	{
		super(id);
		final DateTextField date = new DateTextField("dateTextField", inputModel, "dd MMMM yyyy"){
			@Override
			public boolean isVisible() {
				return nodeModel.isDirectAccess();
			}
		};
		date.add(new AjaxFormComponentUpdatingBehavior("onblur")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
            	if(startDate){
            		nodeModel.setShoppingPeriodStartDate(date.getModelObject());
            	}else{
            		nodeModel.setShoppingPeriodEndDate(date.getModelObject());
            	}
            }
            
        });
		add(date);
	}

}
