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
*	  http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.*;

import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.facade.Role;

import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * Helper class to build student section filter menus.
 */
public class SectionSelector implements Serializable {
	private static final Log log = LogFactory.getLog(SectionSelector.class);

	private Integer selectedSectionFilterValue = new Integer(0);
	private List sectionFilterList;
	private List sectionFilterSelectItems;

	public boolean isAllSelected() {
		return (selectedSectionFilterValue.intValue() == 0);
	}

	public boolean isUnassignedSelected() {
		int filterValue = selectedSectionFilterValue.intValue();
		return ((filterValue != 0) && (sectionFilterList.get(filterValue) == null));
	}

	public String getSelectedSectionUid() {
		int filterValue = selectedSectionFilterValue.intValue();
		if (filterValue == 0) {
			return null;
		} else {
			return (String)sectionFilterList.get(filterValue);
		}
	}

	/**
	 * Initialize the menus based on the current state of section
	 * definitions. This does not, however, change the currently
	 * selected section (unless it's no longer valid). The current
	 * selection is remembered between requests.
	 */
	public void init(SectionAwareness sectionAwareness, String siteContext) {
		sectionFilterList = new ArrayList();
		sectionFilterSelectItems = new ArrayList();

		int pos = 0;

		sectionFilterList.add(null);	// Just a placeholder
		sectionFilterSelectItems.add(new SelectItem(new Integer(pos++), FacesUtil.getLocalizedString("search_sections_all")));

		// If there are unassigned students, add them next.

		// Get the list of sections. For now, just use whatever default
		// sorting we get from the Section Awareness component.
		List sectionCategories = sectionAwareness.getSectionCategories();
		for (Iterator catIter = sectionCategories.iterator(); catIter.hasNext(); ) {
			String category = (String)catIter.next();
			List sections = sectionAwareness.getSectionsInCategory(siteContext, category);
			for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
				CourseSection section = (CourseSection)iter.next();
				sectionFilterList.add(section.getUuid());
				sectionFilterSelectItems.add(new SelectItem(new Integer(pos++), section.getTitle()));
			}
		}

		// If the selected value now falls out of legal range due to sections
		// being deleted, throw it back to the default value of 0 (meaning everyone).
		if (selectedSectionFilterValue.intValue() >= sectionFilterSelectItems.size()) {
			if (log.isInfoEnabled()) log.info("selectedSectionFilterValue=" + selectedSectionFilterValue.intValue() + " but menu size=" + sectionFilterSelectItems.size());
			selectedSectionFilterValue = new Integer(0);
		}
	}

	public Integer getSelectedSectionFilterValue() {
		return selectedSectionFilterValue;
	}
	public void setSelectedSectionFilterValue(Integer selectedSectionFilterValue) {
		if (log.isDebugEnabled()) log.debug("setSelectedSectionFilterValue " + selectedSectionFilterValue);
		this.selectedSectionFilterValue = selectedSectionFilterValue;
	}

	public List getSectionFilterSelectItems() {
		return sectionFilterSelectItems;
	}

}
