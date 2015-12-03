package org.sakaiproject.gradebookng.tool.model;

import org.apache.wicket.ajax.AjaxRequestTarget;
import lombok.Getter;

public class ScoreChangedEvent {
	@Getter
	private AjaxRequestTarget target;
	@Getter
	private Long categoryId;
	@Getter
	private String studentUuid;

	public ScoreChangedEvent(String studentUuid, Long categoryId, AjaxRequestTarget target) {
		this.studentUuid = studentUuid;
		this.categoryId = categoryId;
		this.target = target;
	}
}