package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.pages.SettingsPage;
import org.sakaiproject.user.api.User;

public class HeaderFlagPopoverPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	public enum Flag {
		GRADE_ITEM_EXTRA_CREDIT("label.gradeitem.extracredit"),
		GRADE_ITEM_RELEASED("label.gradeitem.released"),
		GRADE_ITEM_NOT_RELEASED("label.gradeitem.released"),
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


	public HeaderFlagPopoverPanel(final String id, final Flag flag, final Long assignmentId) {
		super(id);

		this.flag = flag;
		this.assignmentId = assignmentId;
	}


	public HeaderFlagPopoverPanel(final String id, final Flag flag) {
		super(id);

		this.flag = flag;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new Label("message", getString(flag.getMessageKey())));

		GbRole role = businessService.getUserRole();
		boolean isInstructor = GbRole.INSTRUCTOR == role;

		if (isInstructor) {
			if (flag == Flag.COURSE_GRADE_NOT_RELEASED || flag == Flag.COURSE_GRADE_RELEASED) {
				Link link = new Link("link") {
					@Override
					public void onClick() {
						setResponsePage(new SettingsPage());
					}
				};
				link.add(new Label("linkText", getString("label.coursegrade.editsettings")));
				add(link);
			} else if (assignmentId != null) {
				AjaxLink link = new AjaxLink("link") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						// do nothing
					}
				};
				String javascript = String.format("sakai.gradebookng.spreadsheet.editAssignmentFromFlag('%s');",
						String.valueOf(assignmentId));
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
