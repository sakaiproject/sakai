package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a group or section
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Data
@AllArgsConstructor
public class GbGroup implements Comparable<GbGroup>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String title;
	private Type type;
	
	@Override
	public int compareTo(GbGroup other) {
		return new CompareToBuilder()
			.append(this.title, other.getTitle())
			.append(this.type, other.getType())
			.toComparison();
		
	}
	
	/**
	 * Type of group
	 */
	public enum Type {
		SECTION,
		GROUP,
		ALL;
	}
	
}
