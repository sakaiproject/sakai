/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.Enrollment;

/**
 * A ListFilter describes how a collection of items should be filtered for
 * display in the UI.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class ListFilter {
    private static final Log log = LogFactory.getLog(ListFilter.class);

    private String searchFilter;
    private Object sortObject;
    private boolean descending;
    private int offset;
    private int limit;

	/**
     * Creates a new ListFilter
     *
	 * @param searchFilter
	 * @param sortObject
	 * @param descending
	 * @param offset
	 * @param limit
	 */
	public ListFilter(String searchFilter, Object sortObject,
			boolean descending, int offset, int limit) {
		super();
		this.searchFilter = searchFilter;
		this.sortObject = sortObject;
		this.descending = descending;
		this.offset = offset;
		this.limit = limit;
	}

    /**
     * Create an unsorted, unpaged filter
     *
     * @param searchFilter
     */
    public ListFilter(String searchFilter) {
        super();
        this.searchFilter = searchFilter;
    }

    /**
	 * @return Returns the descending.
	 */
	public boolean isDescending() {
		return descending;
	}
	/**
	 * @param descending The descending to set.
	 */
	public void setDescending(boolean descending) {
		this.descending = descending;
	}
	/**
	 * @return Returns the limit.
	 */
	public int getLimit() {
		return limit;
	}
	/**
	 * @param limit The limit to set.
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	/**
	 * @return Returns the offset.
	 */
	public int getOffset() {
		return offset;
	}
	/**
	 * @param offset The offset to set.
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	/**
	 * @return Returns the searchFilter.
	 */
	public String getSearchFilter() {
		return searchFilter;
	}
	/**
	 * @param searchFilter The searchFilter to set.
	 */
	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
	}
	/**
	 * @return Returns the sortObject.
	 */
	public Object getSortObject() {
		return sortObject;
	}
	/**
	 * @param sortObject The sortObject to set.
	 */
	public void setSortObject(Object sortObject) {
		this.sortObject = sortObject;
	}
	/**
     * Filter a list of enrollments by the criteria specified in this filter
     *
	 * @param enrollments A List of enrollments
	 * @return A list of enrollments that match this filter
	 */
	public List filterEnrollments(List enrollments) {
        List matches = new ArrayList();
        for(Iterator enrollmentIter = enrollments.iterator(); enrollmentIter.hasNext();) {
            Enrollment enrollment = (Enrollment)enrollmentIter.next();
            if(match(enrollment.getUser().getSortName()) ||
            		match(enrollment.getUser().getDisplayName()) ||
					match(enrollment.getUser().getDisplayUid())) {
                matches.add(enrollment);
            }
        }
        return matches;
	}

    /**
     * Check to see if the searchFilter matches a string.  A string matches if
     * it case insensitively starts with the searchFilter.
     *
     * @param str The String to be checked for matching
     *
     * @return Whether a string matches this ListFilter's searchString
     */
    private boolean match(String str) {
        return str.toLowerCase().startsWith(searchFilter.toLowerCase());
    }
}



