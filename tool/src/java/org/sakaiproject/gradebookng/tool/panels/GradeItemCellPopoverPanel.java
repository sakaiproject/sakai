package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

public class GradeItemCellPopoverPanel extends Panel {

	public GradeItemCellPopoverPanel(String id, IModel<Map<String,Object>> model, List<GradeItemCellPanel.GradeCellNotification> notifications) {
		super(id, model);

		WebMarkupContainer saveErrorNotification = new WebMarkupContainer("saveErrorNotification");
		saveErrorNotification.setVisible(notifications.contains(GradeItemCellPanel.GradeCellNotification.ERROR));
		saveErrorNotification.add(new Label("message", new ResourceModel(GradeItemCellPanel.GradeCellNotification.ERROR.getMessage())));
		add(saveErrorNotification);

		WebMarkupContainer hasCommentNotification = new WebMarkupContainer("hasCommentNotification");
		hasCommentNotification.setVisible(notifications.contains(GradeItemCellPanel.GradeCellNotification.HAS_COMMENT));
		hasCommentNotification.add(new Label("message", new ResourceModel(GradeItemCellPanel.GradeCellNotification.HAS_COMMENT.getMessage())));
		add(hasCommentNotification);

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
	}

}
