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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.tool.section.decorator.SectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

@Slf4j
public abstract class FilteredSectionListingBean extends CourseDependentBean implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum FilterState {NONE, COMPACT, SPLIT, SINGLE};
	
	protected FilterState currentFilterState;
	protected List<SectionDecorator> sections;
	protected List<SelectItem> categorySelectItems;
	
	protected boolean siteWithoutSections;

	public void init() {
		
		if (log.isDebugEnabled()) log.debug("FilteredSectionListingBean init()");
		
		setDefaultPrefs();
		// Get the filter settings
		String categoryFilter = getCategoryFilter();
		String myFilter = getMyFilter();
		
		// Get all sections in the site
		List sectionSet = getAllSiteSections();

		// Keep track of whether there are no sections in this site
		siteWithoutSections = sectionSet.isEmpty();

		sections = new ArrayList<SectionDecorator>();

		// Get the total enrollments for all groups
		Map sectionSize = getSectionManager().getEnrollmentCount(sectionSet);

		// Get the TAs for all groups
		Map<String,List<ParticipationRecord>> sectionTAs = getSectionManager().getSectionTeachingAssistantsMap(sectionSet);
		
		for(Iterator sectionIter = sectionSet.iterator(); sectionIter.hasNext();) {
			CourseSection section = (CourseSection)sectionIter.next();
			String catName = getCategoryName(section.getCategory());

			// If we are filtering by categories, and the section is not in this category, skip it
			if(StringUtils.trimToNull(categoryFilter) != null && ! categoryFilter.equals(section.getCategory())) {
				if(log.isDebugEnabled()) log.debug("Filtering out " + section.getTitle() + ", since it is not in category " + categoryFilter);
				continue;
			}

			// Generate the string showing the TAs
			List<ParticipationRecord> tas = (List<ParticipationRecord>) sectionTAs.get(section.getUuid());
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

			int totalEnrollments = sectionSize.containsKey(section.getUuid()) ? 
					(Integer) sectionSize.get(section.getUuid()) : 0;

			SectionDecorator decoratedSection = new SectionDecorator(
					section, catName, taNames, totalEnrollments, true);
			sections.add(decoratedSection);
		}

		// Populate the category names and select items, ordered just like the category ids
		categorySelectItems = generateCategorySelectItems();
		computeFilterState(sectionSet);

		// Sort the collection set
		Collections.sort(sections, getComparator());
		
	}

	protected void computeFilterState(List sectionSet) {
		if(sectionSet.size() <= 1) {
			// Don't display a filter if there's zero or one section in the site
			currentFilterState = FilterState.NONE;
		} else if(isSectionAssignable() && categorySelectItems.size() == 1) {
			// Display the compact filter for TAs  if there's only one category, but more than one section
			currentFilterState = FilterState.COMPACT;
		} else if(isSectionAssignable()) {
			// Show the split filter for TAs if there are multiple sections and categories
			currentFilterState = FilterState.SPLIT;
		} else if(categorySelectItems.size() > 1){
			// Instructors get the single filter if there are multiple categories
			currentFilterState = FilterState.SINGLE;
		} else {
			// Instructors get no filter when there is only one categories
			currentFilterState = FilterState.NONE;
		}
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


	public boolean isDisplayCompactFilter() {
		return currentFilterState == FilterState.COMPACT;
	}

	public boolean isDisplaySplitFilter() {
		return currentFilterState == FilterState.SPLIT;
	}

	public boolean isDisplaySingleFilter() {
		return currentFilterState == FilterState.SINGLE;
	}

	public boolean isSiteWithoutSections() {
		return siteWithoutSections;
	}
}
