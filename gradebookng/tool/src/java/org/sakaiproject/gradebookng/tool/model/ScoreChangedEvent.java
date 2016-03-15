package org.sakaiproject.gradebookng.tool.model;

import org.apache.wicket.ajax.AjaxRequestTarget;

import lombok.Getter;

public class ScoreChangedEvent {

	@Getter
	private final AjaxRequestTarget target;

	@Getter
	private final Long categoryId;

	@Getter
	private final String studentUuid;

	public ScoreChangedEvent(final String studentUuid, final Long categoryId, final AjaxRequestTarget target) {
		this.studentUuid = studentUuid;
		this.categoryId = categoryId;
		this.target = target;
	}
}