/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.jsf.JsfUtil;

public class AddSectionsBean extends CourseDependentBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(AddSectionsBean.class);
	
	private int numToAdd;
	private String category;
	private List categoryItems;
	private List sections;
	private String rowStyleClasses;
	
	private transient boolean sectionsChanged;
	
	public void init() {
		if(sections == null || sectionsChanged) {
			if(log.isDebugEnabled()) log.debug("initializing add sections bean");
			List categories = getSectionCategories();
			populateSections();
			categoryItems = new ArrayList();
			for(Iterator iter = categories.iterator(); iter.hasNext();) {
				String cat = (String)iter.next();
				categoryItems.add(new SelectItem(cat, getCategoryName(cat)));
			}
		}
	}

	public void processChangeSections(ValueChangeEvent event) {
		if(log.isDebugEnabled()) log.debug("processing a ui change in sections to add");
		sectionsChanged = true;
	}
	
	private void populateSections() {
		sections = new ArrayList();
		StringBuffer rowClasses = new StringBuffer();
		if(StringUtils.trimToNull(category) != null) {
			if(log.isDebugEnabled()) log.debug("populating sections");
			int offset = getSectionManager().getSectionsInCategory(getSiteContext(), category).size();
			for(int i=0; i<numToAdd; i++) {
				sections.add(new LocalSectionModel(getCategoryName(category) + (i+1+offset)));
				rowClasses.append("sectionPadRow");
				if(i+1<numToAdd) {
					rowClasses.append(",");
				}
			}
			rowStyleClasses = rowClasses.toString();
		}
	}
	
	private boolean isDuplicateSectionTitle(String title, Collection existingSections) {
		for(Iterator iter = existingSections.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			if(section.getTitle().equals(title)) {
				if(log.isDebugEnabled()) log.debug("Conflicting section name found: " + title);
				return true;
			}
		}
		return false;
	}
	
	public String addSections() {
		// Check for duplicate section titles
		Collection existingSections = getAllSiteSections();
		boolean nameConflict = false;
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			LocalSectionModel sectionModel = (LocalSectionModel)iter.next();
			if(isDuplicateSectionTitle(sectionModel.getTitle(), existingSections)) {
				if(log.isDebugEnabled()) log.debug("Failed to add section... duplicate title: " + sectionModel.getTitle());
				int index = sections.indexOf(sectionModel);
				// FIXME This is a terrible hack
				String componentId = "addSectionsForm:sectionTable_" + index + ":titleInput";
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
						"section_add_failure_duplicate_title", new String[] {sectionModel.getTitle()}), componentId);
				nameConflict = true;
			}
		}
		if(nameConflict) {
			setNotValidated(true);
			return "failure";
		}
		
		String courseUuid = getCourse().getUuid();
		StringBuffer titles = new StringBuffer();
		String sepChar = JsfUtil.getLocalizedMessage("section_separator");
		
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			LocalSectionModel sectionModel = (LocalSectionModel)iter.next();
			titles.append(sectionModel.getTitle());
			if(iter.hasNext()) {
				titles.append(sepChar);
				titles.append(" ");
			}
			getSectionManager().addSection(courseUuid, sectionModel.getTitle(),
					category, sectionModel.getMaxEnrollments(), sectionModel.getLocation(),
					JsfUtil.convertDateToTime(sectionModel.getStartTime(), sectionModel.isStartTimeAm()),
					JsfUtil.convertDateToTime(sectionModel.getEndTime(), sectionModel.isEndTimeAm()),
					sectionModel.isMonday(), sectionModel.isTuesday(), sectionModel.isWednesday(),
					sectionModel.isThursday(), sectionModel.isFriday(), sectionModel.isSaturday(),
					sectionModel.isSunday());
		}
		String[] params = new String[3];
		params[0] = titles.toString();
		if(sections.size() == 1) {
			params[1] = JsfUtil.getLocalizedMessage("add_section_successful_singular");
			params[2] = JsfUtil.getLocalizedMessage("section_singular");
		} else {
			params[1] = JsfUtil.getLocalizedMessage("add_section_successful_plural");
			params[2] = JsfUtil.getLocalizedMessage("section_plural");
		}
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage("add_section_successful", params));
		return "overview";
	}
	
//	private Time getTime(LocalSectionModel section, boolean useStartTime) {
//		Date startTime = section.getStartTime();
//		Date endTime = section.getEndTime();
//		boolean startTimeAm = section.isStartTimeAm();
//		boolean endTimeAm = section.isEndTimeAm();
//		
//		if(useStartTime) {
//			return JsfUtil.convertDateToTime(startTime, startTimeAm);
//		} else {
//			return JsfUtil.convertDateToTime(endTime, endTimeAm);
//		}
//	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getNumToAdd() {
		return numToAdd;
	}

	public void setNumToAdd(int numToAdd) {
		this.numToAdd = numToAdd;
	}

	public List getCategoryItems() {
		return categoryItems;
	}
	
	public class LocalSectionModel implements Serializable {
		private static final long serialVersionUID = 1L;

		public LocalSectionModel() {}
		public LocalSectionModel(String title) {this.title = title;}
		
		private String title;
	    private String location;
	    private Integer maxEnrollments;
		private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
		private Date startTime, endTime;
		private boolean startTimeAm, endTimeAm;

		public Date getEndTime() {
			return endTime;
		}
		public void setEndTime(Date endTime) {
			this.endTime = endTime;
		}
		public boolean isEndTimeAm() {
			return endTimeAm;
		}
		public void setEndTimeAm(boolean endTimeAm) {
			this.endTimeAm = endTimeAm;
		}
		public boolean isFriday() {
			return friday;
		}
		public void setFriday(boolean friday) {
			this.friday = friday;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
		public Integer getMaxEnrollments() {
			return maxEnrollments;
		}
		public void setMaxEnrollments(Integer maxEnrollments) {
			this.maxEnrollments = maxEnrollments;
		}
		public boolean isMonday() {
			return monday;
		}
		public void setMonday(boolean monday) {
			this.monday = monday;
		}
		public boolean isSaturday() {
			return saturday;
		}
		public void setSaturday(boolean saturday) {
			this.saturday = saturday;
		}
		public Date getStartTime() {
			return startTime;
		}
		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}
		public boolean isStartTimeAm() {
			return startTimeAm;
		}
		public void setStartTimeAm(boolean startTimeAm) {
			this.startTimeAm = startTimeAm;
		}
		public boolean isSunday() {
			return sunday;
		}
		public void setSunday(boolean sunday) {
			this.sunday = sunday;
		}
		public boolean isThursday() {
			return thursday;
		}
		public void setThursday(boolean thursday) {
			this.thursday = thursday;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public boolean isTuesday() {
			return tuesday;
		}
		public void setTuesday(boolean tuesday) {
			this.tuesday = tuesday;
		}
		public boolean isWednesday() {
			return wednesday;
		}
		public void setWednesday(boolean wednesday) {
			this.wednesday = wednesday;
		}
	}

	public List getSections() {
		return sections;
	}

	public String getRowStyleClasses() {
		return rowStyleClasses;
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
