package org.sakaiproject.profile2.model;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Extension of UserProfile to provide some additional fields we need during import
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

public class ImportableUserProfile extends UserProfile {

	private static final long serialVersionUID = 1L;
	
	@Getter @Setter
	private String eid;
	
	@Getter @Setter
	private String officialImageUrl;
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
