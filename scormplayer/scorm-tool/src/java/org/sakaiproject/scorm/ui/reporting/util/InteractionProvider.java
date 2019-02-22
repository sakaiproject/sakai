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

import org.sakaiproject.scorm.model.api.Interaction;
import org.sakaiproject.scorm.model.api.comparator.InteractionComparator;
import org.sakaiproject.scorm.model.api.comparator.InteractionComparator.CompType;
import org.sakaiproject.wicket.markup.html.repeater.util.EnhancedDataProvider;

public class InteractionProvider extends EnhancedDataProvider
{
	private static final long serialVersionUID = 1L;

	private final List<Interaction> interactions;
	private final InteractionComparator comp = new InteractionComparator();

	public InteractionProvider(List<Interaction> interactions)
	{
		this.interactions = interactions;
		setSort( "interactionId", SortOrder.ASCENDING );
	}

	@Override
	public Iterator<Interaction> iterator(long first, long count)
	{
		// Get the sort type
		SortParam sort = getSort();
		String sortProp = (String) sort.getProperty();
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
		return interactions.subList((int) first, (int) first + (int) count).iterator();
	}

	@Override
	public long size()
	{
		return interactions.size();
	}
}
