package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.pages.SettingsPage;

public class HeaderFlagPopoverPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	public enum Flag {
		GRADE_ITEM_EXTRA_CREDIT("label.gradeitem.extracredit"),
		GRADE_ITEM_RELEASED("label.gradeitem.released"),
		GRADE_ITEM_NOT_RELEASED("label.gradeitem.notreleased"),
		GRADE_ITEM_COUNTED("label.gradeitem.counted"),
		GRADE_ITEM_NOT_COUNTED("label.gradeitem.notcounted"),
		COURSE_GRADE_RELEASED("label.coursegrade.released"),
		COURSE_GRADE_NOT_RELEASED("label.coursegrade.notreleased");

		private String messageKey;

		Flag(final String messageKey) {
			this.messageKey = messageKey;
		}

		public String getMessageKey() {
			return this.messageKey;
		}
	}

	Flag flag;
	Long assignmentId;
	GbRole role;

	public HeaderFlagPopoverPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);

		final Map<String, Object> modelData = model.getObject();
		this.flag = (Flag) modelData.get("flag");
		this.assignmentId = (Long) modelData.get("assignmentId");
		this.role = (GbRole) modelData.get("role");
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new Label("message", getString(this.flag.getMessageKey())));

		final boolean isInstructor = GbRole.INSTRUCTOR == this.role;

		if (isInstructor) {
			if (this.flag == Flag.COURSE_GRADE_NOT_RELEASED || this.flag == Flag.COURSE_GRADE_RELEASED) {
				final Link link = new Link("link") {
					@Override
					public void onClick() {
						setResponsePage(SettingsPage.class);
					}
				};
				link.add(new Label("linkText", getString("label.coursegrade.editsettings")));
				add(link);
			} else if (this.assignmentId != null) {
				final GbAjaxLink link = new GbAjaxLink("link") {
					@Override
					public void onClick(final AjaxRequestTarget target) {
						// do nothing
					}
				};
				final String javascript = String.format("sakai.gradebookng.spreadsheet.editAssignmentFromFlag('%s');",
						String.valueOf(this.assignmentId));
				link.add(new AttributeAppender("onclick", javascript));
				link.add(new Label("linkText", getString("assignment.option.edit")));
				add(link);
			} else {
				add(new WebMarkupContainer("link").setVisible(false));
			}
		} else {
			add(new WebMarkupContainer("link").setVisible(false));
		}
	}

	public String toPopoverString() {
		return ComponentRenderer.renderComponent(this).toString();
	}
}
