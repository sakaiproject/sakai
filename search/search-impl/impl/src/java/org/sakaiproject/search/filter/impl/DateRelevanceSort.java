/**
 * 
 */
package org.sakaiproject.search.filter.impl;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.sakaiproject.search.api.SearchService;

/**
 * Sorts by date stamp then relevance in revese order (newst earliest)
 * @author ieb
 *
 */
public class DateRelevanceSort extends Sort
{
	public DateRelevanceSort() {
		super(new SortField[] {
				new SortField(SearchService.DATE_STAMP,true),
				SortField.FIELD_SCORE
		});
	}
}
