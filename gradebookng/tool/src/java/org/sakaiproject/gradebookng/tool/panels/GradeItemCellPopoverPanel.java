package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

public class GradeItemCellPopoverPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public GradeItemCellPopoverPanel(String id, IModel<Map<String,Object>> model, List<GradeItemCellPanel.GradeCellNotification> notifications) {
		super(id, model);

		final Map<String, Object> modelData = model.getObject();

		WebMarkupContainer closePopoverLink = new WebMarkupContainer("closePopoverLink");
		closePopoverLink.add(new AttributeModifier("data-assignmentid", (Long) modelData.get("assignmentId")));
		closePopoverLink.add(new AttributeModifier("data-studentUuid", (String) modelData.get("studentUuid")));
		add(closePopoverLink);

		WebMarkupContainer saveErrorNotification = new WebMarkupContainer("saveErrorNotification");
		saveErrorNotification.setVisible(notifications.contains(GradeItemCellPanel.GradeCellNotification.ERROR));
		saveErrorNotification.add(new Label("message", new ResourceModel(GradeItemCellPanel.GradeCellNotification.ERROR.getMessage())));
		add(saveErrorNotification);

		String comment = (String) modelData.get("comment");
		WebMarkupContainer hasCommentNotification = new WebMarkupContainer("hasCommentNotification");
		hasCommentNotification.setVisible(notifications.contains(GradeItemCellPanel.GradeCellNotification.HAS_COMMENT) && StringUtils.isNotBlank(comment));
		hasCommentNotification.add(new Label("message", new ResourceModel(GradeItemCellPanel.GradeCellNotification.HAS_COMMENT.getMessage())));
		add(hasCommentNotification);

		if (hasCommentNotification.isVisible()) {
			hasCommentNotification.add(new Label("snippet", makeSnippet(comment, 100)));

			hasCommentNotification.add(new AttributeModifier("data-assignmentid", (Long) modelData.get("assignmentId")));
			hasCommentNotification.add(new AttributeModifier("data-studentUuid", (String) modelData.get("studentUuid")));

			WebMarkupContainer editCommentContainer = new WebMarkupContainer("editCommentContainer") {
				@Override
				public boolean isVisible() {
					return (boolean) modelData.get("gradeable");
				}
			};
			hasCommentNotification.add(editCommentContainer);
		}

		WebMarkupContainer isOverLimitNotification = new WebMarkupContainer("isOverLimitNotification");
		isOverLimitNotification.setVisible(notifications.contains(GradeItemCellPanel.GradeCellNotification.OVER_LIMIT));
		isOverLimitNotification.add(new Label("message", new ResourceModel(GradeItemCellPanel.GradeCellNotification.OVER_LIMIT.getMessage())));
		add(isOverLimitNotification);

		WebMarkupContainer concurrentEditNotification = new WebMarkupContainer("concurrentEditNotification");
		concurrentEditNotification.setVisible(notifications.contains(GradeItemCellPanel.GradeCellNotification.CONCURRENT_EDIT));
		concurrentEditNotification.add(new Label("message", new ResourceModel(GradeItemCellPanel.GradeCellNotification.CONCURRENT_EDIT.getMessage())));
		add(concurrentEditNotification);

		WebMarkupContainer isExternalNotification = new WebMarkupContainer("isExternalNotification");
		isExternalNotification.setVisible(notifications.contains(GradeItemCellPanel.GradeCellNotification.IS_EXTERNAL));
		isExternalNotification.add(new Label("message", new ResourceModel(GradeItemCellPanel.GradeCellNotification.IS_EXTERNAL.getMessage())));
		add(isExternalNotification);

		WebMarkupContainer isInvalidNotification = new WebMarkupContainer("isInvalidNotification");
		isInvalidNotification.setVisible(notifications.contains(GradeItemCellPanel.GradeCellNotification.INVALID));
		isInvalidNotification.add(new Label("message", new ResourceModel(GradeItemCellPanel.GradeCellNotification.INVALID.getMessage())));
		add(isInvalidNotification);
	}


	private String makeSnippet(String text, int max) {
		if (text.length() <= max) {
			return text;
		}

		int end = text.lastIndexOf(' ', max - 3);

		if (end == -1) {
			return text.substring(0, max - 3) + "...";
		}

		int newEnd = end;
		do {
			end = newEnd;
			newEnd = text.indexOf(' ', end + 1);
			if (newEnd == -1) {
				newEnd = text.length();
			}
		} while ((text.substring(0, newEnd) + "...").length() < max);

		return text.substring(0, end) + "...";
	}
}
