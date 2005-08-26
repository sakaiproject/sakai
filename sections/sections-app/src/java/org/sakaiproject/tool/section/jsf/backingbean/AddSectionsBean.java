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
import java.util.Iterator;
import java.util.List;

import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddSectionsBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String COL_ID = "singleColumn";
	private static final String SECTION_CELL_ID = "singleCell";

	
	private static final Log log = LogFactory.getLog(AddSectionsBean.class);
	
	private Integer numToAdd;
	private String category;
	private List categoryItems;
	private List sections;
	
	public void init() {
		if(log.isDebugEnabled()) log.debug("initializing add sections bean");
		
		List categories = getSectionCategories();
		if(category == null) {
			category = (String)categories.get(0);
		}
		
		if(numToAdd == null) {
			numToAdd = new Integer(1);
		}
		if(sections == null) {
			sections = new ArrayList();
			populateSections();
		}
		if(categoryItems == null) {
			categoryItems = new ArrayList();
			for(Iterator iter = categories.iterator(); iter.hasNext();) {
				String cat = (String)iter.next();
				categoryItems.add(new SelectItem(cat, getCategoryName(cat)));
			}
		}
	}

	public void processChangeSections(ValueChangeEvent event) {
		// Get the new number of sections
		if(log.isDebugEnabled()) log.debug("processing a ui change in sections to add");

		// This event fires before the update_model_values jsf phase, so we need
		// to update the model manually in this phase
		// TODO Is there a better way to do this?
		String newNum = (String)FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap().get("addSectionsForm:numToAdd");
		numToAdd = new Integer(Integer.parseInt(newNum));
		if(log.isDebugEnabled()) log.debug("setting number of sections to add to " + numToAdd);
		
		String newCat = (String)FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap().get("addSectionsForm:category");
		category = newCat;
		if(log.isDebugEnabled()) log.debug("setting category to " + category);

		populateSections();
	}
	
	private void populateSections() {
		if(log.isDebugEnabled()) log.debug("populating sections");
		sections.clear();
		int offset = getSectionManager().getSectionsInCategory(getSiteContext(), category).size();
		for(int i=0; i<numToAdd.intValue(); i++) {
			sections.add(new LocalSectionModel(getCategoryName(category) + (i+1+offset)));
		}
	}

	public String addSections() {
		String courseUuid = getCourse().getUuid();
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			LocalSectionModel sectionModel = (LocalSectionModel)iter.next();
			getSectionManager().addSection(courseUuid, sectionModel.getTitle(), category, 10, null, null, false, null, false, false, false, false, false, false, false, false);
		}
		return "overview";
	}
	
	public void setSectionTable(HtmlDataTable sectionTable) {
		if (sectionTable.findComponent(COL_ID) == null) {
			Application app = FacesContext.getCurrentInstance().getApplication();

			UIColumn col = new UIColumn();
			col.setId(COL_ID);

			HtmlInputText contents = new HtmlInputText();
			contents.setId(SECTION_CELL_ID);
			contents.setValueBinding("value",
				app.createValueBinding("#{section.title}"));
			col.getChildren().add(contents);
			sectionTable.getChildren().add(col);
		}
	}
	
	public HtmlDataTable getSectionTable() {
		return null;
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getNumToAdd() {
		return numToAdd;
	}

	public void setNumToAdd(Integer numToAdd) {
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
	    private int maxEnrollments;
		private boolean monday, tuesday, wednesday, thursday, friday, saturday, sunday;
		private String startTime, endTime;
		private boolean startTimeAm, endTimeAm;

		public String getEndTime() {
			return endTime;
		}
		public void setEndTime(String endTime) {
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
		public int getMaxEnrollments() {
			return maxEnrollments;
		}
		public void setMaxEnrollments(int maxEnrollments) {
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
		public String getStartTime() {
			return startTime;
		}
		public void setStartTime(String startTime) {
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
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
