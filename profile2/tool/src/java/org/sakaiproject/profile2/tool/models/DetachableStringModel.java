package org.sakaiproject.profile2.tool.models;

import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Detachable model for an instance of a String, when a List/Dataview uses simple Strings only.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class DetachableStringModel extends LoadableDetachableModel {
	
	private static final long serialVersionUID = 1L;
	private String s;
	
	public DetachableStringModel(String s) {
		if (s == null)
        {
            throw new IllegalArgumentException();
        }
        this.s = s;
	}

	@Override
	protected Object load() {
		return s;
	}


}
