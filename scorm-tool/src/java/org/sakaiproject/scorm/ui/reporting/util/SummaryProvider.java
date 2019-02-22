/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.reporting.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;

import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.comparator.ActivitySummaryComparator;
import org.sakaiproject.scorm.model.api.comparator.ActivitySummaryComparator.CompType;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class SummaryProvider extends EnhancedDataProvider
{
	private static final long serialVersionUID = 1L;

	private final List<ActivitySummary> summaries;
	private final ActivitySummaryComparator comp = new ActivitySummaryComparator();

	public SummaryProvider(List<ActivitySummary> summaries)
	{
		this.summaries = summaries;
		setSort( "title", SortOrder.ASCENDING );
	}

	@Override
	public Iterator<ActivitySummary> iterator(long first, long count)
	{
		// Get the sort type
		SortParam sort = getSort();
		String sortProp = (String) sort.getProperty();
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
		return summaries.subList((int) first, (int) first + (int) count).iterator();
	}

	@Override
	public long size()
	{
		return summaries.size();
	}
}
