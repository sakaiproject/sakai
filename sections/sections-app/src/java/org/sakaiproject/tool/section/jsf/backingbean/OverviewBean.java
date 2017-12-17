/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.section.api.SectionManager.ExternalIntegrationConfig;
import org.sakaiproject.tool.section.decorator.SectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the overview page.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class OverviewBean extends FilteredSectionListingBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String instructions;
	private boolean externallyManaged;

	private List<SectionDecorator> sectionsToDelete;

	public void init() {
		super.init();
		// Determine whether this course is externally managed
		externallyManaged = getSectionManager().isExternallyManaged(getCourse().getUuid());

		// Generate the instructions for this user for the app in its current state
		if(externallyManaged) {
			if(isSectionAssignable()) {
				instructions = JsfUtil.getLocalizedMessage("overview_instructions_auto_ta");
			} else {
				if(getApplicationConfiguration() == ExternalIntegrationConfig.AUTOMATIC_MANDATORY) {
					instructions = JsfUtil.getLocalizedMessage("overview_instructions_mandatory_auto_instructor");
				} else {
					instructions = JsfUtil.getLocalizedMessage("overview_instructions_auto_instructor");
				}
			}
		} else {
			instructions = "";
		}
	}

	protected Comparator<SectionDecorator> getComparator() {
		String sortColumn = getPrefs().getOverviewSortColumn();
		boolean sortAscending = getPrefs().isOverviewSortAscending();

		if("title".equals(sortColumn)) {
			return SectionDecorator.getTitleComparator(sortAscending);
		} else if("managers".equals(sortColumn)) {
			return SectionDecorator.getManagersComparator(sortAscending);
		} else if("totalEnrollments".equals(sortColumn)) {
			return SectionDecorator.getEnrollmentsComparator(sortAscending, false);
		} else if("available".equals(sortColumn)) {
			return SectionDecorator.getEnrollmentsComparator(sortAscending, true);
		} else if("meetingDays".equals(sortColumn)) {
			return SectionDecorator.getDayComparator(sortAscending);
		} else if("meetingTimes".equals(sortColumn)) {
			return SectionDecorator.getTimeComparator(sortAscending);
		} else if("location".equals(sortColumn)) {
			return SectionDecorator.getLocationComparator(sortAscending);
		}
		log.error("Invalid sort specified.");
		return null;
	}

	public String confirmDelete() {
		sectionsToDelete = new ArrayList<SectionDecorator>();
		for(Iterator<SectionDecorator> iter = sections.iterator(); iter.hasNext();) {
			SectionDecorator decoratedSection = iter.next();
			if(decoratedSection.isFlaggedForRemoval()) {
				sectionsToDelete.add(decoratedSection);
			}
		}
		if(sectionsToDelete.isEmpty()) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("overview_delete_section_choose"));
			return null; // Don't go anywhere
		} else {
			return "deleteSections";
		}
	}
	
	public String deleteSections() {
		Set<String> set = new HashSet<String>();
		for(Iterator<SectionDecorator> iter = sectionsToDelete.iterator(); iter.hasNext();) {
			set.add(iter.next().getUuid());
		}
		getSectionManager().disbandSections(set);
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage("overview_delete_section_success"));
		return "overview";
	}
	public boolean isDeleteRendered() {
		return (!externallyManaged) && sections.size() > 0 && isSectionManagementEnabled();
	}

	public boolean isExternallyManaged() {
		return externallyManaged;
	}

	public List getSectionsToDelete() {
		return sectionsToDelete;
	}

	@Override
	public String getSortColumn() {
		return getPrefs().getOverviewSortColumn();
	}

	@Override
	public boolean isSortAscending() {
		return getPrefs().isOverviewSortAscending();
	}

	@Override
	public void setSortAscending(boolean sortAscending) {
		getPrefs().setOverviewSortAscending(sortAscending);
	}

	@Override
	public void setSortColumn(String sortColumn) {
		getPrefs().setOverviewSortColumn(sortColumn);
	}

	@Override
	public String getCategoryFilter() {
		return getPrefs().getOverviewCategoryFilter();
	}

	@Override
	public String getMyFilter() {
		return getPrefs().getOverviewMyFilter();
	}

	@Override
	public void setCategoryFilter(String categoryFilter) {
		getPrefs().setOverviewCategoryFilter(categoryFilter);
	}

	@Override
	public void setMyFilter(String myFilter) {
		getPrefs().setOverviewMyFilter(myFilter);
	}

	public String getInstructions() {
		return instructions;
	}
}
