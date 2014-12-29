/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.citation.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.sakaiproject.citation.util.api.SearchException;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;


/**
 * 
 *
 */
public interface CitationCollection extends Entity
{
	public final static String SORT_BY_AUTHOR = "author";
	public final static String SORT_BY_YEAR   = "year";
	public final static String SORT_BY_TITLE  = "title";
	public final static String SORT_BY_POSITION  = "position";
	public final static String SORT_BY_UUID   = "uuid";
	public final static String SORT_BY_DEFAULT_ORDER = "default";
	
	public String getSaveUrl();

	/**
	 * Appends the specified Citation at the end of this list.
	 * @param element
	 * @return true if the list changed as a result of this operation.
	 */
	public void add(Citation element);
	
	/**
	 * Appends all of the Citations in the specified CitationCollection to the end of this CitationCollection, in the order that they are 
	 * returned by the specified CitationCollection's iterator. This operation fails if this list and the other list are the same
	 * Object.
	 * @param other The list containing the Citations to be appended to this list.
	 * @return true if the list changed as a result of this operation.
	 */
	public void addAll(CitationCollection other);
	
	/**
	 * Access a sorted list of all Citations belonging to the CitationCollection, where the sort order is
	 * determined by the Comparator (@see http://java.sun.com/j2se/1.4.2/docs/api/java/util/Comparator.html).  
	 * @param c The comparator that determines the relative ordering of any two Citations. 
	 * @return The sorted list of Citations.  May be empty but will not be null.
	 */
//	public CitationCollection getCitations(Comparator c);
	
	/**
	 * Access an ordered list of a subset of the Citations belonging to the CitationCollection, where membership in the subset is
	 * determined by the filter (@see Filter).  
	 * @param f The filter that determines membership in the subset.
	 * @return The filtered list of Citations.  May be empty but will not be null.
	 */
//	public CitationCollection getCitations(Filter f);
	
	/**
	 * Access a sorted list of a subset of the Citations belonging to the CitationCollection, where the sort order is
	 * determined by the Comparator (@see http://java.sun.com/j2se/1.4.2/docs/api/java/util/Comparator.html) and 
	 * membership in the subset is determined by the filter (@see Filter). 
	 * @param c The comparator that determines the relative ordering of any two Citations.
	 * @param f The filter that determines membership in the subset.
	 * @return The sorted, filtered list of Citations.  May be empty but will not be null. 
	 */
//	public CitationCollection getCitations(Comparator c, Filter f);
	
	/**
	 * Access all Citations in the list with name-value pairs in their properties matching the name-value pairs in 
	 * the properties parameter. Returns a list of Citations satisfying the criteria, which may be empty if no Citations
	 * in the list satisfy the criteria.  
	 * @param properties A mapping of name-value pairs indicating names of properties as Strings and values of properties as Strings.
	 * @return the list of elements that match, which may be empty but not null.
	 */
//	public CitationCollection getCitations(Map properties);
		
	/**
     * 
     */
    public void clear();
	
	/**
	 * Access a particular Citation from the list.
	 * @param index The index the the Citation in the unsorted, unfiltered list. 
	 * @return The Citation.
	 */
//	public Citation getCitation(int index);
	
	/**
	 * Access the first occurrence in this list of a Citation with name-value pairs in its properties matching the name-value pairs in 
	 * the properties parameter. Returns the element, or null if no element in the list matched the properties.
	 * @param properties A mapping of name-value pairs indicating names of properties as Strings and values of properties as Strings.
	 * @return the first element that matches, or null if no element in the list matches the properties.
	 */
//	public Citation getCitation(Map properties);
		
	/**
	 * @param item
	 * @return
	 */
	public boolean contains(Citation item);
	
	/**
	 * Returns an iterator over the Citation objects in this CitationCollection.
	 * @return an Iterator over the Citation objects in this CitationCollection
	 */
//	public Iterator iterator();
	
	/**
	 * Access a particular Citation from the list.
	 * @param id
	 * @return The Citation.
	 */
	public Citation getCitation(String id) throws IdUnusedException;
	
	/**
	 * Access an ordered list of all Citations belonging to the CitationCollection.
	 * @return The ordered list of Citation objects. May be empty but will not be null.
	 */
	public List getCitations();
	
//	public boolean containsAll(CitationCollection list);
	
	/**
	 * Returns the index in this list of the first occurrence of the specified Citation, or -1 if this list does not contain this Citation.
	 * @param item The element to search for.
	 * @return the index in this list of the first occurrence of the specified Citation, or -1 if this list does not contain this Citation.
	 */
//	public int indexOf(Citation item);
	
	/**
	 * Returns the index in this list of the last occurrence of the specified Citation, or -1 if this list does not contain this Citation.
	 * @param item The element to search for.
	 * @return the index in this list of the last occurrence of the specified Citation, or -1 if this list does not contain this Citation.
	 */
//	public int lastIndexOf(Citation item);
	
	/**
	 * @param c
	 */
//	public void sort(Comparator c);
	
	/**
	 * Move an item from one place in the list to another.
	 * @param from The index of the element to be moved.
	 * @param to The index the element should have after the move.
	 * @return true if the move succeeded.
	 */
//	public boolean move(int from, int to);
	
	/**
	 * Move an item from a specified index in the list to the beginning of the list (index of 0).
	 * @param index
	 * @return true if the move succeeded.
	 */
//	public boolean moveToFront(int index);
	
	/**
	 * Move an item from a specified index in the list to the end of the list (index of list.size() - 1).
	 * @param index
	 * @return true if the move succeeded.
	 */
//	public boolean moveToBack(int index);
	
	/**
	 * Inserts the specified Citation at the specified position in this list.
	 * @param index
	 * @param element
	 * @return true if the list changed as a result of this operation.
	 */
//	public boolean add(int index, Citation element);
	
	/**
     * @return
     */
    public String getDescription();
	
	/**
	 * Access the unique identifier for this CitationCollection.
	 */
	public String getId(); 
	
	/**
	 * Inserts all of the Citations in the specified CitationCollection into this CitationCollection, in the order that they are returned by 
	 * the specified CitationCollection's iterator, beginning at the location indicated by the index parameter. Shifts any subsequent 
	 * elements to the right (increases their indices by the number indicating the size of the other list). This operation fails 
	 * if this list and the other list are the same Object.
	 * @param index The offset from the beginning of this list at which the first Citation from the other list should be inserted. 
	 * @param other The list containing the Citations to be inserted into this list.
	 * @return true if the list changed as a result of this operation.
	 */
//	public boolean addAll(int index, CitationCollection other); 
	
	/**
	 * Removes all of the elements from this list.
	 */
//	public void clear();
	
	/**
	 * Removes the element at the specified position in this list. Shifts any subsequent elements to the left (subtracts one from their 
	 * indices). Returns the element that was removed from the list (null if index out of bounds).
	 * @param index
	 * @return the element that was removed from the list (null if index out of bounds)
	 */
//	public Citation remove(int index);
	
	/**
	 * Removes the first occurrence in this list of a Citation with name-value pairs in its properties matching the name-value pairs in 
	 * the properties parameter. Shifts any subsequent elements to the left (subtracts one from their indices). Returns the element that 
	 * was removed from the list (null if no element in the list matched the properties).
	 * @param item The element to be removed from the list, if present.
	 * @return the element that was removed from the list (null if no element in the list matched the properties).
	 */
//	public Citation remove(Map properties);

	/**
	 * Access the timestamp of the most recent saved revision to this citation collection.
	 */
	public Date getLastModifiedDate();
    
	/**
     * @return
     */
    public String getSort();

    /**
     * @return
     */
    public String getTitle();

	/**
	 * Returns true if this collection contains no elements.
	 * @return true if this collection contains no elements.
	 */
	public boolean isEmpty();
    
    /**
	 * @return
	 */
	public CitationIterator iterator();
    
    /**
	 * Removes the first occurrence in this list of the specified Citation. Shifts any subsequent elements to the left (subtracts one from 
	 * their indices). Returns true if the list contained the specified citation.
	 * @param item The element to be removed from the list, if present.
	 * @return true if the list changed as a result of this call.
	 */
	public boolean remove(Citation item);
    
    /**
     * @param citation
     */
    public void saveCitation(Citation citation);
    
 	/**
     * 
     * @param comparator
     */
    public void setSort(Comparator comparator);
    
    /**
     * 
     * @param sortBy
     * @param ascending
     */
    public void setSort(String sortBy, boolean ascending);

	/**
	 * Access the number of citations in this collection.
	 * @return
	 */
	public int size();

	/**
	 * 
	 * @param buffer
	 * @param citationIds
	 * @throws IOException 
	 */
	public void exportRis(StringBuilder buffer, List<String> citationIds) throws IOException;

}	// interface Citation

