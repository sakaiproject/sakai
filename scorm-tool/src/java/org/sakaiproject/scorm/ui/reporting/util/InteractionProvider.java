package org.sakaiproject.scorm.ui.reporting.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;

import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.model.api.comparator.InteractionComparator;
import org.sakaiproject.scorm.model.api.comparator.InteractionComparator.CompType;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class InteractionProvider extends EnhancedDataProvider {

	private static final long serialVersionUID = 1L;
	private final List<Interaction> interactions;
	private final InteractionComparator comp = new InteractionComparator();

	public InteractionProvider(List<Interaction> interactions) {
		this.interactions = interactions;
		setSort( "interactionId", true );
	}

	public Iterator<Interaction> iterator(int first, int count) {

		// Get the sort type
		SortParam sort = getSort();
		String sortProp = sort.getProperty();
		boolean sortAsc = sort.isAscending();

		// Set the sort type in the comparator
		if( StringUtils.equals( sortProp, "description" ) )
		{
			comp.setCompType( CompType.Description );
		}
		else if( StringUtils.equals( sortProp, "type" ) )
		{
			comp.setCompType( CompType.Type );
		}
		else if( StringUtils.equals( sortProp, "result" ) )
		{
			comp.setCompType( CompType.Result );
		}
		else
		{
			comp.setCompType( CompType.InteractionID );
		}

		// Sort using the comparator in the direction requested
		if( sortAsc )
		{
			Collections.sort( interactions, comp );
		}
		else
		{
			Collections.sort( interactions, Collections.reverseOrder( comp ) );
		}

		// Return sub list of sorted collection
		return interactions.subList(first, first + count).iterator();
	}

	public int size() {
		return interactions.size();
	}
}
