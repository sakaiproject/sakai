package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeLog;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.util.FormatHelper;

/**
 * Panel for the grade log window
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class GradeLogPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public GradeLogPanel(final String id, final IModel<Map<String, Object>> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Map<String, Object> modelData = (Map<String, Object>) getDefaultModelObject();
		final Long assignmentId = (Long) modelData.get("assignmentId");
		final String studentUuid = (String) modelData.get("studentUuid");

		// get the data
		final List<GbGradeLog> gradeLog = this.businessService.getGradeLog(studentUuid, assignmentId);

		// render list
		final ListView<GbGradeLog> listView = new ListView<GbGradeLog>("log", gradeLog) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<GbGradeLog> item) {

				final GbGradeLog gradeLog = item.getModelObject();

				final String logDate = FormatHelper.formatDateTime(gradeLog.getDateGraded());
				final String grade = FormatHelper.formatGrade(gradeLog.getGrade());

				final GbUser grader = GradeLogPanel.this.businessService.getUser(gradeLog.getGraderUuid());
				final String graderDisplayId = (grader != null) ? grader.getDisplayId() : getString("unknown.user.id");

				// add the entry
				item.add(new Label("entry",
						new StringResourceModel("grade.log.entry", null, new Object[] { logDate, grade, graderDisplayId }))
								.setEscapeModelStrings(false));

			}
		};
		add(listView);

		// no entries
		final Label emptyLabel = new Label("empty", new ResourceModel("grade.log.none"));
		emptyLabel.setVisible(gradeLog.isEmpty());
		add(emptyLabel);

		// done button
		add(new AjaxLink<Void>("done") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				GradeLogPanel.this.window.close(target);
			}
		});

		// heading
		// TODO if user has been deleted since rendering the GradebookPage, handle a null here gracefully
		final GbUser user = this.businessService.getUser(studentUuid);
		GradeLogPanel.this.window.setTitle(
				(new StringResourceModel("heading.gradelog", null,
						new Object[] { user.getDisplayName(), user.getDisplayId() })).getString());

	}

}
