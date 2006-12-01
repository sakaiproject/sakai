/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.tool.section.decorator.SectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

public abstract class FilteredSectionListingBean extends CourseDependentBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(FilteredSectionListingBean.class);
	
	protected List<SectionDecorator> sections;
	protected List<SelectItem> categorySelectItems;

	public void init() {
		setDefaultPrefs();
		// Get the filter settings
		String categoryFilter = getCategoryFilter();
		String myFilter = getMyFilter();
		
		// Get all sections in the site
		List sectionSet = getAllSiteSections();
		sections = new ArrayList<SectionDecorator>();

		for(Iterator sectionIter = sectionSet.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection)sectionIter.next();
			String catName = getCategoryName(section.getCategory());

			// If we are filtering by categories, and the section is not in this category, skip it
			if(StringUtils.trimToNull(categoryFilter) != null && ! categoryFilter.equals(section.getCategory())) {
				if(log.isDebugEnabled()) log.debug("Filtering out " + section.getTitle() + ", since it is not in category " + categoryFilter);
				continue;
			}

			// Generate the string showing the TAs
			List<ParticipationRecord> tas = getSectionManager().getSectionTeachingAssistants(section.getUuid());
			List<String> taNames = generateTaNames(tas);
			List<String> taUids = generateTaUids(tas);

			// If we're filtering by my sections, and the TAs in the section don't include me, skip this section
			if("MY".equals(myFilter)) {
				String userUid = getUserUid();
				if( ! taUids.contains(userUid)) {
					if(log.isDebugEnabled()) log.debug("Filtering out " + section.getTitle() + ", since user " + userUid + " is not a TA");
					continue;
				}
			}

			int totalEnrollments = getSectionManager().getTotalEnrollments(section.getUuid());

			SectionDecorator decoratedSection = new SectionDecorator(
					section, catName, taNames, totalEnrollments);
			sections.add(decoratedSection);
		}

		// Populate the category names and select items, ordered just like the category ids
		categorySelectItems = generateCategorySelectItems();

		// Sort the collection set
		Collections.sort(sections, getComparator());
	}

	protected void setDefaultPrefs() {
		if(getSortColumn() == null) {
			setSortColumn("title");
			setSortAscending(true);
		}
	}
	
	protected List<String> generateTaNames(List<ParticipationRecord> tas) {
		// Generate the string showing the TAs
		List<String> taNames = new ArrayList<String>();
		for(Iterator taIter = tas.iterator(); taIter.hasNext();) {
			ParticipationRecord ta = (ParticipationRecord)taIter.next();
			taNames.add(StringUtils.abbreviate(ta.getUser().getSortName(), getPrefs().getMaxNameLength()));
		}

		Collections.sort(taNames);
		return taNames;
	}

	protected List<String> generateTaUids(List<ParticipationRecord> tas) {
		List<String> taUids = new ArrayList<String>();
		for(Iterator<ParticipationRecord> iter = tas.iterator(); iter.hasNext();) {
			taUids.add(iter.next().getUser().getUserUid());
		}
		return taUids;
	}
	
	protected List<SelectItem> generateCategorySelectItems() {
		 List<SelectItem> list = new ArrayList<SelectItem>();
		for(Iterator<String> iter =  getUsedCategories().iterator(); iter.hasNext();) {
			String catId = iter.next();
			String catName = getCategoryName(catId);
			list.add(new SelectItem(catId,
					JsfUtil.getLocalizedMessage("student_view_category_sections", new String[] {catName})));
		}
		return list;
	}
	
	protected abstract Comparator<SectionDecorator> getComparator();

	public abstract String getCategoryFilter();

	public abstract void setCategoryFilter(String categoryFilter);

	public abstract String getMyFilter();

	public abstract void setMyFilter(String myFilter);
	
	public List<SectionDecorator> getSections() {
		return sections;
	}

	public void setSections(List<SectionDecorator> sections) {
		this.sections = sections;
	}

	public abstract boolean isSortAscending();
	public abstract void setSortAscending(boolean sortAscending);

	public abstract String getSortColumn();
	public abstract void setSortColumn(String sortColumn);

	public List<SelectItem> getCategorySelectItems() {
		return categorySelectItems;
	}

	public void setCategorySelectItems(List<SelectItem> categorySelectItems) {
		this.categorySelectItems = categorySelectItems;
	}
}
