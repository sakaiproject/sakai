package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

/**
 *
 * Header panel for the student name/eid
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameColumnHeaderPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	IModel<GbStudentNameSortOrder> model;

	public StudentNameColumnHeaderPanel(final String id, final IModel<GbStudentNameSortOrder> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final GradebookPage gradebookPage = (GradebookPage) getPage();

		// setup model
		final GbStudentNameSortOrder sortType = this.model.getObject();

		// title
		final Link<String> title = new Link<String>("title") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {

				// toggle the sort direction on each click
				final GradebookUiSettings settings = gradebookPage.getUiSettings();

				// if null, set a default sort, otherwise toggle, save, refresh.
				if (settings.getStudentSortOrder() == null) {
					settings.setStudentSortOrder(SortDirection.getDefault());
				} else {
					final SortDirection sortOrder = settings.getStudentSortOrder();
					settings.setStudentSortOrder(sortOrder.toggle());
				}

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				setResponsePage(GradebookPage.class);
			}
		};

		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		title.add(new AttributeModifier("title", new ResourceModel("column.header.students")));
		title.add(new Label("label", new ResourceModel("column.header.students")));
		if (settings != null && settings.getStudentSortOrder() != null) {
			title.add(
				new AttributeModifier("class", "gb-sort-" + settings.getStudentSortOrder().toString().toLowerCase()));
		}
		add(title);

		// sort by first/last name link
		final GbAjaxLink<GbStudentNameSortOrder> sortByName = new GbAjaxLink<GbStudentNameSortOrder>("sortByName", this.model) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {

				// get current sort
				final GbStudentNameSortOrder currentSort = StudentNameColumnHeaderPanel.this.model.getObject();

				// get next
				final GbStudentNameSortOrder newSort = currentSort.toggle();

				// set the sort
				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GradebookUiSettings settings = gradebookPage.getUiSettings();
				settings.setNameSortOrder(newSort);

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				setResponsePage(GradebookPage.class);

			}
		};

		// the label changes depending on the state so we wrap it in a model
		final IModel<String> sortByNameModel = new Model<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {

				// shows the label opposite to the current sort type
				if (sortType == GbStudentNameSortOrder.FIRST_NAME) {
					return getString("sortbyname.option.last");
				} else {
					return getString("sortbyname.option.first");
				}
			}
		};

		sortByName.add(new Label("sortByNameLabel", sortByNameModel));
		add(sortByName);
	}
}
