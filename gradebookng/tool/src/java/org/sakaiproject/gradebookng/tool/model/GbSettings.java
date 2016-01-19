package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import java.util.List;

import org.sakaiproject.service.gradebook.shared.GradebookInformation;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for the info we need for the settings.
 */
public class GbSettings implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private GradebookInformation gradebookInformation;

	@Getter
	@Setter
	private List<GbGradingSchemaEntry> gradingSchemaEntries;

	public GbSettings(final GradebookInformation gradebookInformation) {
		this.gradebookInformation = gradebookInformation;
	}

}