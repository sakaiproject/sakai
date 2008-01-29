package org.sakaiproject.scorm.ui.reporting.util;

import java.util.Iterator;
import java.util.List;

import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class InteractionProvider extends EnhancedDataProvider {

	private static final long serialVersionUID = 1L;

	private final List<Interaction> interactions;
	
	public InteractionProvider(List<Interaction> interactions) {
		this.interactions = interactions;
	}
	
	public Iterator iterator(int first, int last) {
		return interactions.subList(first, last).iterator();
	}

	public int size() {
		return interactions.size();
	}

}
