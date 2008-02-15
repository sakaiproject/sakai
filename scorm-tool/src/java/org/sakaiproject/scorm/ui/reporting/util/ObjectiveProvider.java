package org.sakaiproject.scorm.ui.reporting.util;

import java.util.Iterator;
import java.util.List;

import org.sakaiproject.scorm.model.api.Objective;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class ObjectiveProvider extends EnhancedDataProvider {

	private static final long serialVersionUID = 1L;

	private final List<Objective> objectives;
	
	public ObjectiveProvider(List<Objective> objectives) {
		this.objectives = objectives;
	}
	
	public Iterator iterator(int first, int last) {
		return objectives.subList(first, last).iterator();
	}

	public int size() {
		return objectives.size();
	}
}
