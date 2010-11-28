package org.sakaiproject.profile2.model;

import java.io.Serializable;

import lombok.Data;

/**
 * This is the base model for a Person, containing a limited set of fields. It is extended by Person.
 * 
 * <p>Note about serialisation. The User object is not serialisable and does not contain a no-arg constructor
 * so cannot be manually serialised via the serializable methods (readObject, writeObject).</p> 
 * 
 * <p>Hence why it is not used instead. So the most useful values it provides are extracted and set into this object.</p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Data
public class BasicPerson implements Serializable, Comparable<Object> {

	private static final long serialVersionUID = 1L;
	
	private String uuid;
	private String displayName;
	private String type;
	
	//default sort
	public int compareTo(Object o) {
		String field = ((BasicPerson)o).getDisplayName();
        int lastCmp = displayName.compareTo(field);
        return (lastCmp != 0 ? lastCmp : displayName.compareTo(field));
	}
}
