package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;

import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.coursemanagement.api.Section;

/**
 * 
 * Header panel for the section column
 * Will only be called if there are sections to render otherwise the entire column is hidden
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class SectionColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public SectionColumnHeaderPanel(String id, final List<Section> sections) {
		super(id);
		
		//title
		add(new Label("title", new ResourceModel("column.header.section")));
		
		//dropdown
		DropDownChoice<Section> sectionList = new DropDownChoice<Section>("sectionList", sections, new ChoiceRenderer<Section>() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(Section s) {
				return s.getTitle();
			}
			
			@Override
			public String getIdValue(Section s, int index) {
				return s.getEid();
			}
			
		});
		add(sectionList);
		
	}
	
	
	
}
