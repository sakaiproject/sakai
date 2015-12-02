package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

/**
 * 
 * Header panel for each category column in the UI
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class CategoryColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private IModel<CategoryDefinition> modelData;
	
	public CategoryColumnHeaderPanel(String id, IModel<CategoryDefinition> modelData) {
		super(id);
		this.modelData = modelData;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		final CategoryDefinition category = this.modelData.getObject();
		
		//title
		add(new Label("title", Model.of(category.getName())));
		
	}
	
}
