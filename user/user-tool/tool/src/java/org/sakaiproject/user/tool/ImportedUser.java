package org.sakaiproject.user.tool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;

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
	
	/**
	 * Used as a helper to get the properties in from a String
	 */
	private String rawProps;
	
	/**
	 * Holds the actual properties for this User
	 */
	private ResourceProperties properties;

	/**
	 * Override so that we can do a bit of extra parsing
	 * @param s	The raw props that are to be converted.
	 * Should be of the form a=1;b=2,c=3.
	 */
	public void setRawProps(String s) {
		
		rawProps = s;
		
		//split to list of pairs, each pair separated by a ;
		String[] array = StringUtils.split(s, ';');
		List<String> list = Arrays.asList(array);
		
		//split each pair into the props, each one separated by =
		properties = new BaseResourcePropertiesEdit();
		for(String pair: list) {
			String[] kv = StringUtils.split(pair, '=');
			properties.addProperty(kv[0], kv[1]);
		}
		
	}
	
}
