package org.sakaiproject.profile2.tool.models;

import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.profile2.model.Person;

/**
 * Detachable model for an instance of a Person
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class DetachablePersonModel extends LoadableDetachableModel {
	
	private static final long serialVersionUID = 1L;
	private Person p;
	
	public DetachablePersonModel(Person p) {
		if (p == null)
        {
            throw new IllegalArgumentException();
        }
        this.p = p;
	}

	@Override
	protected Object load() {
		return p;
	}


}
