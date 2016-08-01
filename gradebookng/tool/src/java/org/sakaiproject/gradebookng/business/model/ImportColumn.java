package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Describes the type of column imported
 */
@NoArgsConstructor
@AllArgsConstructor
public class ImportColumn implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String columnTitle;

	@Getter
	@Setter
	private String points;

	@Getter
	@Setter
	private Type type = Type.REGULAR;

	public enum Type {
		GBITEM_WITH_POINTS,
		GBITEM_WITH_COMMENTS,
		REGULAR;
	}

}
