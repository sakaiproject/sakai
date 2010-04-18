package org.sakaiproject.profile2.model;

import java.io.Serializable;

/**
 * This is the base model for a Person, containing a limited set of fields. It is extended by Person.
 * 
 * <p>Note about serialisation. The User object is not serialisable and does not contain a no-arg constructor so cannot be manually serialised via
 * the serializable methods (readObject, writeObject). Hence why it is not used instead.
 * So the most useful values it provides are extracted and set into this object.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class BasicPerson implements Serializable, Comparable<Object> {

	
	private static final long serialVersionUID = 1L;
	private String uuid;
	private String displayName;
	private String type;
	private String email;
	
	/**
	 * No arg constructor
	 */
	public BasicPerson() {}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	//default sort
	public int compareTo(Object o) {
		String field = ((BasicPerson)o).getDisplayName();
        int lastCmp = displayName.compareTo(field);
        return (lastCmp != 0 ? lastCmp : displayName.compareTo(field));
	}
}
