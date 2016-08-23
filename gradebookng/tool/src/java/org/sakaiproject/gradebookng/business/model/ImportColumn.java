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
	private Type type = Type.GB_ITEM_WITHOUT_POINTS;

	public enum Type {
		GB_ITEM_WITH_POINTS,
		GB_ITEM_WITHOUT_POINTS,
		COMMENTS,
		USER_ID,
		USER_NAME,
		IGNORE;
	}

	/**
	 * Helper to determine if the type of column can be ignored
	 * @return
	 */
	public boolean isIgnorable() {
		if(this.type == Type.USER_ID || this.type == Type.USER_NAME || this.type == Type.IGNORE) {
			return true;
		}
		return false;
	}

}
