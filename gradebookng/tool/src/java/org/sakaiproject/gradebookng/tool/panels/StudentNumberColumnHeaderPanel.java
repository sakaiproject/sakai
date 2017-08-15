package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

/**
 *
 * @author plukasew
 */
public class StudentNumberColumnHeaderPanel extends Panel
{	
	public StudentNumberColumnHeaderPanel(final String id)
	{
		super(id);
	}
	
	@Override
	public void onInitialize()
	{
		super.onInitialize();
		
		final GradebookPage gradebookPage = (GradebookPage) getPage();
		
		// title
		final Link<String> title = new Link<String>("title")
		{
			@Override
			public void onClick()
			{
				// toggle the sort direction on each click
				final GradebookUiSettings settings = gradebookPage.getUiSettings();

				// if null, set a default sort, otherwise toggle, save, refresh.
				if (settings.getStudentNumberSortOrder() == null) {
					settings.setStudentNumberSortOrder(SortDirection.getDefault());
				} else {
					final SortDirection sortOrder = settings.getStudentNumberSortOrder();
					settings.setStudentNumberSortOrder(sortOrder.toggle());
				}

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				setResponsePage(GradebookPage.class);
			}
		};
		
		title.add(new AttributeModifier("title", new ResourceModel("column.header.studentNumber")));
		title.add(new Label("label", new ResourceModel("column.header.studentNumber")));
		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		SortDirection sort = settings.getStudentNumberSortOrder();
		if (sort != null)
		{
			title.add(new AttributeModifier("class", "gb-sort-" + sort.toString().toLowerCase()));
		}
		
		add(title);
	}
}