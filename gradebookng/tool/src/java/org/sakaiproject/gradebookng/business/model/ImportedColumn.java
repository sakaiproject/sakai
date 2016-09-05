package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Describes the type of column imported
 */
@NoArgsConstructor
@AllArgsConstructor
public class ImportedColumn implements Serializable {

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

	/**
	 * Column titles are the only thing we care about for comparisons so that we can filter out duplicates.
	 * Must also match type and exclude IGNORE
	 */
	@Override
	public boolean equals(final Object o) {
		final ImportedColumn other = (ImportedColumn) o;
		if(StringUtils.isBlank(this.columnTitle) || StringUtils.isBlank(other.columnTitle)){
			return false;
		}
		if(this.type == Type.IGNORE || other.type == Type.IGNORE){
			return false;
		}
		if(StringUtils.equalsIgnoreCase(this.columnTitle, other.getColumnTitle()) && this.type == other.getType()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(this.columnTitle)
				.append(this.type)
				.toHashCode();
	}

}
