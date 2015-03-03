package org.sakaiproject.gradebookng.business.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Model for retrieving config info about the gradebook
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradebookConfiguration {

	@Getter
	@Setter
	private String siteId;
	
	@Getter
	@Setter
	private String toolPlacementId;
	
}
