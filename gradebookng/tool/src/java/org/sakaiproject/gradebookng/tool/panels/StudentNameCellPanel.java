package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

/**
 *
 * Cell panel for the student name and eid. Link shows the student grade summary
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameCellPanel extends Panel {

	private static final long serialVersionUID = 1L;

	IModel<Map<String, Object>> model;

	public StudentNameCellPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Map<String, Object> modelData = this.model.getObject();
		final String eid = (String) modelData.get("eid");
		final String firstName = (String) modelData.get("firstName");
		final String lastName = (String) modelData.get("lastName");
		final String displayName = (String) modelData.get("displayName");
		final GbStudentNameSortOrder nameSortOrder = (GbStudentNameSortOrder) modelData.get("nameSortOrder");

		// link
		final AjaxLink<String> link = new AjaxLink<String>("link") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {

				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GbModalWindow window = gradebookPage.getStudentGradeSummaryWindow();

				final Component content = new StudentGradeSummaryPanel(window.getContentId(), StudentNameCellPanel.this.model, window);

				if (window.isShown() && window.isVisible()) {
					window.replace(content);
					content.setVisible(true);
					target.add(content);
				} else {
					window.setContent(content);
					window.setComponentToReturnFocusTo(this);
					window.show(target);
				}

				content.setOutputMarkupId(true);
				String modalTitle = (new StringResourceModel("heading.studentsummary",
						null, new Object[]{displayName, eid})).getString();
				target.appendJavaScript(String.format(
						"new GradebookGradeSummary($(\"#%s\"), false, \"%s\");",
						content.getMarkupId(), modalTitle));
			}
		};
		link.setOutputMarkupId(true);

		// name label
		link.add(new Label("name", getFormattedStudentName(firstName, lastName, nameSortOrder)));

		// eid label, configurable
		link.add(new Label("eid", eid) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return true; // TODO use config, will need to be passed in the model map
			}

		});

		add(link);
	}

	/**
	 * Helper to format a student name based on the sort type.
	 *
	 * Sorted by Last Name = Smith, John (jsmith26) Sorted by First Name = John Smith (jsmith26)
	 *
	 * @param firstName
	 * @param lastName
	 * @param sortOrder
	 * @return
	 */
	private String getFormattedStudentName(final String firstName, final String lastName, final GbStudentNameSortOrder sortOrder) {

		final String msg = "formatter.studentname." + sortOrder.name();
		if (GbStudentNameSortOrder.LAST_NAME == sortOrder) {
			return String.format(getString(msg), lastName, firstName);
		}
		if (GbStudentNameSortOrder.FIRST_NAME == sortOrder) {
			return String.format(getString(msg), firstName, lastName);
		}
		return firstName;
	}

}
