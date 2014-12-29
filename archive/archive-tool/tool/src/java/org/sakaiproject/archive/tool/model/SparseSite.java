package org.sakaiproject.archive.tool.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Sparse model for a site for performance
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class SparseSite {

	@Getter @Setter 
	private String id;
	
	@Getter @Setter
	private String title;
	
	public SparseSite(String id, String title) {
		this.id = id;
		this.title = title;
	}
	
}