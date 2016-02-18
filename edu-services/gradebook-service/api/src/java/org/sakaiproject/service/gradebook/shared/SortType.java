package org.sakaiproject.service.gradebook.shared;

/**
 * Represents the different ways an (internal) Assignment can be sorted
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum SortType {

	SORT_BY_NONE, //no explicit sorting
	SORT_BY_DATE,
	SORT_BY_NAME,
	SORT_BY_MEAN,
	SORT_BY_POINTS,
	SORT_BY_RELEASED,
	SORT_BY_COUNTED,
	SORT_BY_EDITOR,
	SORT_BY_CATEGORY,
	SORT_BY_SORTING; //default
}
