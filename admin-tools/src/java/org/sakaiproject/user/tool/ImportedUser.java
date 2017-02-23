package org.sakaiproject.user.tool;

import lombok.Data;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * Model object to store a record about an imported user
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Data
public class ImportedUser {

	private String eid;
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private String type;
	private ResourceProperties properties;
	
}
