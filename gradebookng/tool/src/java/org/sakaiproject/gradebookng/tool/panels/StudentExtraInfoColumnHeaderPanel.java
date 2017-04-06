package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

/**
 *
 * Header panel for an optional student extra value field
 *
 */
public class StudentExtraInfoColumnHeaderPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final IModel<SortDirection> model;
	private final String headerTitle;

	public StudentExtraInfoColumnHeaderPanel(final String id, final IModel<SortDirection> model, final String headerTitle) {
		super(id, model);
		this.model = model;
		this.headerTitle = headerTitle;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final GradebookPage gradebookPage = (GradebookPage) getPage();

		// title
		final Link<String> title = new Link<String>("title") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {

				// toggle the sort direction on each click
				final GradebookUiSettings settings = gradebookPage.getUiSettings();

				// if null, set a default sort, otherwise toggle, save, refresh.
				if (settings.getStudentExtraInfoSortOrder() == null) {
					settings.setStudentExtraInfoSortOrder(SortDirection.getDefault());
				} else {
					final SortDirection sortOrder = settings.getStudentExtraInfoSortOrder();
					settings.setStudentExtraInfoSortOrder(sortOrder.toggle());
				}

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				setResponsePage(GradebookPage.class);
			}
		};

		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		title.add(new AttributeModifier("title", this.headerTitle));
		title.add(new Label("label", this.headerTitle.toUpperCase()));
		if (settings != null && settings.getStudentExtraInfoSortOrder() != null) {
			title.add(
				new AttributeModifier("class", "gb-sort-" + settings.getStudentExtraInfoSortOrder().toString().toLowerCase()));
		}
		add(title);
	}
}
