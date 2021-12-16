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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader;
import org.sakaiproject.jsf2.spreadsheet.SpreadsheetDataFileWriterXlsx;
import org.sakaiproject.jsf2.spreadsheet.SpreadsheetUtil;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.coursemanagement.SectionEnrollments;
import org.sakaiproject.tool.section.decorator.EnrollmentDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Controls the roster page.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
@Slf4j
public class RosterBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String CAT_COLUMN_PREFIX = "cat";
	private static final String NO_SECTIONS_FILTER_KEY = "NoSections";

	private String searchText;
	private int firstRow;
	private int enrollmentsSize;
	private boolean externallyManaged;
	private List<SelectItem> filterItems;
	private List<EnrollmentDecorator> enrollments;
	private List<String> categories;
	private List<EnrollmentRecord> siteStudents;
	private List<EnrollmentDecorator> unpagedEnrollments;
	@Getter private List<SelectItem> sectionFilterSelectItems;

    public void init() {
		// Determine whether this course is externally managed
		externallyManaged = getSectionManager().isExternallyManaged(getCourse().getUuid());

		// Get the section categories
		categories = getSectionManager().getSectionCategories(getSiteContext());

		// Get the default search text
		if(StringUtils.trimToNull(searchText) == null) {
			searchText = JsfUtil.getLocalizedMessage("roster_search_text");
		}

		// Get the site enrollments
		//List<EnrollmentRecord> siteStudents;
		if(searchText.equals(JsfUtil.getLocalizedMessage("roster_search_text"))) {
			siteStudents = getSectionManager().getSiteEnrollments(getSiteContext());
		} else {
			siteStudents = getSectionManager().findSiteEnrollments(getSiteContext(), searchText);
		}

		// Get the section enrollments
		Set<String> studentUids = new HashSet<String>();
		for(Iterator iter = siteStudents.iterator(); iter.hasNext();) {
			ParticipationRecord record = (ParticipationRecord)iter.next();
			studentUids.add(record.getUser().getUserUid());
		}
		SectionEnrollments sectionEnrollments = getSectionManager().getSectionEnrollmentsForStudents(getSiteContext(), studentUids);

		// Construct the list of filter items
		filterItems = new ArrayList<SelectItem>();
		filterItems.add(new SelectItem("", JsfUtil.getLocalizedMessage("filter_all_sections")));
		filterItems.add(new SelectItem("MY", JsfUtil.getLocalizedMessage("filter_my_category_sections", new String[] {""})));
		for(Iterator<String> iter = categories.iterator(); iter.hasNext();) {
			String cat = iter.next();
			filterItems.add(new SelectItem(cat, JsfUtil.getLocalizedMessage("filter_my_category_sections",
					new String[] {getCategoryName(cat)})));
		}

		// If this is a TA, and we're filtering, get the TA's participation records
		List<CourseSection> assignedSections = null;
		if(StringUtils.trimToNull(getFilter()) != null) {
			assignedSections = findAssignedSections();
		}

		//Adding Section filtering selections for instructors
		sectionFilterSelectItems = new ArrayList<>();

		// The first choice is always "All available sections"
		sectionFilterSelectItems.add(new SelectItem("", JsfUtil.getLocalizedMessage("filter_all_sections")));

		// Add the available sections
		getSectionManager().getSections(getSiteContext()).forEach(section -> {
			if (isExternallyManaged() && StringUtils.isNotBlank(section.getEid())) {
				sectionFilterSelectItems.add(new SelectItem(section.getEid(), section.getTitle()));
			} else {
				sectionFilterSelectItems.add(new SelectItem(section.getTitle(), section.getTitle()));
			}
		});
		sectionFilterSelectItems.add(new SelectItem(NO_SECTIONS_FILTER_KEY, JsfUtil.getLocalizedMessage("filter_no_sections")));

		// Construct the decorated enrollments for the UI
		decorateEnrollments(siteStudents, sectionEnrollments, assignedSections);
	}

	private void decorateEnrollments(List<EnrollmentRecord> siteStudents, SectionEnrollments sectionEnrollments, List<CourseSection> assignedSections) {
		unpagedEnrollments = new ArrayList<>();
		siteStudents.forEach(enrollment -> {
			// Build a map of categories to sections in which the student is enrolled
			Map<String, CourseSection> studentSectionsMap = new HashMap<>();

			// Iterate site sections to see if the student is a member of and the filter matches the selected section
			boolean includeStudent = categories.stream().filter(category -> {
				CourseSection section = sectionEnrollments.getSection(enrollment.getUser().getUserUid(), category);
				boolean isValidSection = false;

				if (section != null) {
					if(("MY".equals(getFilter()) || section.getCategory().equals(getFilter()))
							&& assignedSections.contains(section)) {
						isValidSection = true;

					} else if (StringUtils.isNotBlank(section.getEid()) && section.getEid().equals(getFilter())) {
						isValidSection = true;

					} else if (StringUtils.isNotBlank(section.getTitle()) && section.getTitle().equals(getFilter())) {
						isValidSection = true;
					}

					studentSectionsMap.put(category, section);
				}

				return isValidSection;
			}).count() > 0; // If there are valid sections for the current filter, include the student in the view

			// If there is no filter, the section is valid and the student shouldn't be excluded
			if (StringUtils.isBlank(getFilter())) {
				includeStudent = true;
			}

			// If the filter is set to "no sections" filter and the student sections map is empty, student should be included
			if (NO_SECTIONS_FILTER_KEY.equals(getFilter()) && studentSectionsMap.isEmpty()) {
				includeStudent = true;
			}

			if(includeStudent) {
				EnrollmentDecorator decorator = new EnrollmentDecorator(enrollment, studentSectionsMap);
				unpagedEnrollments.add(decorator);
			}
		});

		// Sort the list
		Collections.sort(unpagedEnrollments, getComparator());

		// Filter the list of enrollments
		enrollments = new ArrayList<EnrollmentDecorator>();
		int lastRow;
		int maxDisplayedRows = getPrefs().getRosterMaxDisplayedRows();
		if(maxDisplayedRows < 1 || firstRow + maxDisplayedRows > unpagedEnrollments.size()) {
			lastRow = unpagedEnrollments.size();
		} else {
			lastRow = firstRow + maxDisplayedRows;
		}
		enrollments.addAll(unpagedEnrollments.subList(firstRow, lastRow));
		enrollmentsSize = unpagedEnrollments.size();
	}

	private List<CourseSection> findAssignedSections() {
		List<CourseSection> assignedSections = new ArrayList<CourseSection>();
		for(Iterator<CourseSection> secIter = getSectionManager().getSections(getSiteContext()).iterator(); secIter.hasNext();) {
			CourseSection section = secIter.next();
			List<ParticipationRecord> tas = getSectionManager().getSectionTeachingAssistants(section.getUuid());
			for(Iterator<ParticipationRecord> taIter = tas.iterator(); taIter.hasNext();) {
				ParticipationRecord ta = taIter.next();
				if(ta.getUser().getUserUid().equals(getUserUid())) {
					assignedSections.add(section);
					break;
				}
			}
		}
		return assignedSections;
	}

	private Comparator<EnrollmentDecorator> getComparator() {
		String sortColumn = getPrefs().getRosterSortColumn();
		boolean sortAscending = getPrefs().isRosterSortAscending();

		if("studentName".equals(sortColumn)) {
			return EnrollmentDecorator.getNameComparator(sortAscending);
		} else if("displayId".equals(sortColumn)) {
			return EnrollmentDecorator.getDisplayIdComparator(sortAscending);
		} else {
			return EnrollmentDecorator.getCategoryComparator(sortColumn, sortAscending);
		}
	}

	public HtmlDataTable getRosterDataTable() {
		return null;
	}

	public void setRosterDataTable(HtmlDataTable rosterDataTable) {
		Set usedCategories = getUsedCategories();

		if (rosterDataTable.findComponent(CAT_COLUMN_PREFIX + "0") == null) {
			Application app = FacesContext.getCurrentInstance().getApplication();

			// Add columns for each category. Be sure to create unique IDs
			// for all child components.
			int colpos = 0;
			for (Iterator iter = usedCategories.iterator(); iter.hasNext(); colpos++) {
				String category = (String)iter.next();
				String categoryName = getCategoryName(category);

				UIColumn col = new UIColumn();
				col.setId(CAT_COLUMN_PREFIX + colpos);

                HtmlCommandSortHeader sortHeader = new HtmlCommandSortHeader();
                sortHeader.setId(CAT_COLUMN_PREFIX + "sorthdr_" + colpos);
                sortHeader.setArrow(true);
                sortHeader.setColumnName(category);
                //sortHeader.setActionListener(app.createMethodBinding("#{rosterBean.sort}", new Class[] {ActionEvent.class}));

				HtmlOutputText headerText = new HtmlOutputText();
				headerText.setId(CAT_COLUMN_PREFIX + "hdr_" + colpos);
				headerText.setValue(categoryName);

                sortHeader.getChildren().add(headerText);
                col.setHeader(sortHeader);

				HtmlOutputText contents = new HtmlOutputText();
				contents.setId(CAT_COLUMN_PREFIX + "cell_" + colpos);
				contents.setValueBinding("value",
					app.createValueBinding("#{enrollment.categoryToSectionMap['" + category + "'].title}"));
				col.getChildren().add(contents);
				rosterDataTable.getChildren().add(col);
			}
		}
	}

	public void search(ActionEvent event) {
//		firstRow = 0;
	}

	public void clearSearch(ActionEvent event) {
		firstRow = 0;
		searchText = null;
	}

	public List getEnrollments() {
		return enrollments;
	}
	public int getEnrollmentsSize() {
		return enrollmentsSize;
	}
	public boolean isExternallyManaged() {
		return externallyManaged;
	}
	public String getSearchText() {
		return searchText;
	}
	public void setSearchText(String searchText) {
        if (StringUtils.trimToNull(searchText) == null) {
        	searchText = JsfUtil.getLocalizedMessage("roster_search_text");
        }
    	if (!StringUtils.equals(searchText, this.searchText)) {
	    	if (log.isDebugEnabled()) log.debug("setSearchString " + searchText);
	        this.searchText = searchText;
	        setFirstRow(0); // clear the paging when we update the search
	    }
	}
	public int getFirstRow() {
		return firstRow;
	}
	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public String getFilter() {
		return getPrefs().getRosterFilter();
	}

	public void setFilter(String filter) {
		getPrefs().setRosterFilter(filter);
	}

    public List getFilterItems() {
        return filterItems;
    }

    public void export(ActionEvent event){
    	log.debug("export(");
        List<List<Object>> spreadsheetData = new ArrayList<List<Object>>();
        
        Map<String, String> sectionTutors = new HashMap<String, String>();
        // Get the section enrollments
        Set<String> studentUids = new HashSet<String>();
        for(Iterator iter = siteStudents.iterator(); iter.hasNext();) {
            ParticipationRecord record = (ParticipationRecord)iter.next();
            studentUids.add(record.getUser().getUserUid());
        }
        SectionEnrollments sectionEnrollments = getSectionManager().getSectionEnrollmentsForStudents(getSiteContext(), studentUids);
        // Add the header row
        List<Object> header = new ArrayList<Object>();
        header.add(JsfUtil.getLocalizedMessage("roster_table_header_name"));
        header.add(JsfUtil.getLocalizedMessage("roster_table_header_id"));
        
        int categories = 0;
        for (Iterator<String> iter = getUsedCategories().iterator(); iter.hasNext();){
            String category = (String)iter.next();          
            String categoryName = getCategoryName(category);
            header.add(categoryName);
            categories++;
        }
        
        //SAK-20962 Show TA doesn't work with multiple section types
        boolean showTAs = false;
        if (categories == 1) {
        	header.add(JsfUtil.getLocalizedMessage("roster_table_header_ta"));
        	showTAs = true;
        	
        } 
        spreadsheetData.add(header);
        for (EnrollmentDecorator enrollment : unpagedEnrollments ) {
            List<Object> row = new ArrayList<Object>();
            
            row.add(enrollment.getUser().getSortName());
            row.add(enrollment.getUser().getDisplayId());

            Set<String> usedCategories = getUsedCategories();
            for (String category : usedCategories) {
                CourseSection section = sectionEnrollments.getSection(enrollment.getUser().getUserUid(), category);

                if(section!=null){
                	row.add(section.getTitle());
                	//SAK-20092 add the TA's
                	if (showTAs) {
                		if (sectionTutors.get(section.getUuid()) != null) {
                			row.add(sectionTutors.get(section.getUuid()));
                		} else {
                			String ta = getSectionTutorsAsString(section.getUuid());
                			row.add(ta);
                			sectionTutors.put(section.getUuid(), ta);
                		}
                	}
                }else{
                    row.add("");
                    if (showTAs) {
                    	row.add("");
                    }
                }
            }
            spreadsheetData.add(row);
        }
        String spreadsheetName = getDownloadFileName(getCourse().getTitle());
        SpreadsheetUtil.downloadSpreadsheetData(spreadsheetData, spreadsheetName, new SpreadsheetDataFileWriterXlsx());

    }

    private String getSectionTutorsAsString(String section) {
    	List<ParticipationRecord> tas = getSectionManager().getSectionTeachingAssistants(section);
    	StringBuilder sb = new StringBuilder();
    	for (int i =0; i < tas.size(); i++) {
    		ParticipationRecord participant = tas.get(i);
    		if (i > 0) {
    			sb.append(", ");
    		}
    		sb.append(participant.getUser().getDisplayName());
    	}

    	return sb.toString();
    }

    protected String getDownloadFileName(String rawString) {
        String dateString = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date());
        return (rawString + "_" + dateString).replaceAll("[\\W&&[^\\u0080-\\uffff]]", "_");
    }
}
