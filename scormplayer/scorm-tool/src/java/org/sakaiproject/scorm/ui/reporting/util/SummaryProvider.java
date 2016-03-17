package org.sakaiproject.scorm.ui.reporting.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;

import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.comparator.ActivitySummaryComparator;
import org.sakaiproject.scorm.model.api.comparator.ActivitySummaryComparator.CompType;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class SummaryProvider extends EnhancedDataProvider {

	private static final long serialVersionUID = 1L;
	private final List<ActivitySummary> summaries;
	private final ActivitySummaryComparator comp = new ActivitySummaryComparator();

	public SummaryProvider(List<ActivitySummary> summaries) {
		this.summaries = summaries;
		setSort( "title", true );
	}
	
	public Iterator<ActivitySummary> iterator(int first, int count) {

		// Get the sort type
		SortParam sort = getSort();
		String sortProp = sort.getProperty();
		boolean sortAsc = sort.isAscending();

		// Set the sort type in the comparator
		if( StringUtils.equals( sortProp, "scaled" ) )
		{
			comp.setCompType( CompType.Score );
		}
		else if( StringUtils.equals( sortProp, "completionStatus" ) )
		{
			comp.setCompType( CompType.CompletionStatus );
		}
		else if( StringUtils.equals( sortProp, "successStatus" ) )
		{
			comp.setCompType( CompType.SuccessStatus );
		}
		else
		{
			comp.setCompType( CompType.Title );
		}

		// Sort using the comparator in the direction requested
		if( sortAsc )
		{
			Collections.sort( summaries, comp );
		}
		else
		{
			Collections.sort( summaries, Collections.reverseOrder( comp ) );
		}

		// Return sub list of sorted collection
		return summaries.subList(first, first + count).iterator();
	}

	public int size() {
		return summaries.size();
	}
}
