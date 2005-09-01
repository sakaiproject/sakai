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
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.tree2.UITreeData;
import org.sakaiproject.tool.section.jsf.JsfUtil;

public class AddSectionsBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String COL_ID = "singleColumn";
	
	// TODO Figure out how to get the client id of components
	private static final String UNREACHABLE_CLIENT_ID = "unknown";
	
	private static final Log log = LogFactory.getLog(AddSectionsBean.class);
	
	private int numToAdd;
	private String category;
	private List categoryItems;
	private List sections;
	
	public void init() {
		if(log.isDebugEnabled()) log.debug("initializing add sections bean");
		List categories = getSectionCategories();
		populateSections();
		categoryItems = new ArrayList();
		for(Iterator iter = categories.iterator(); iter.hasNext();) {
			String cat = (String)iter.next();
			categoryItems.add(new SelectItem(cat, getCategoryName(cat)));
		}
	}

	public void processChangeSections(ValueChangeEvent event) {
		if(log.isDebugEnabled()) log.debug("processing a ui change in sections to add");
		populateSections();
	}
	
	private void populateSections() {
		sections = new ArrayList();
		if(StringUtils.trimToNull(category) != null) {
			if(log.isDebugEnabled()) log.debug("populating sections");
			int offset = getSectionManager().getSectionsInCategory(getSiteContext(), category).size();
			for(int i=0; i<numToAdd; i++) {
				sections.add(new LocalSectionModel(getCategoryName(category) + (i+1+offset)));
			}
		}
	}
	
	public String addSections() {
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
					category, sectionModel.getMaxEnrollments(), sectionModel.getLocation(), sectionModel.getStartTime(),
					sectionModel.isStartTimeAm(), sectionModel.getEndTime(), sectionModel.isEndTimeAm(),
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
		JsfUtil.addRedirectSafeMessage(JsfUtil.getLocalizedMessage("add_section_successful", params));
		return "overview";
	}
	
	public void setSectionTable(HtmlDataTable sectionTable) {
		if(log.isDebugEnabled()) log.debug("setting section table");

		if (sectionTable.findComponent(COL_ID) == null) {
			Application app = FacesContext.getCurrentInstance().getApplication();

			UIColumn col = new UIColumn();
			col.setId(COL_ID);

			// Create the panel grid
			HtmlPanelGrid detailTable = new HtmlPanelGrid();
			detailTable.setColumns(2);

			// Add the title label
			HtmlOutputText outputTitle = new HtmlOutputText();
			outputTitle.setValue(JsfUtil.getLocalizedMessage("section_title"));
			detailTable.getChildren().add(outputTitle);
			
			// Add the title input
			HtmlInputText inputTitle = new HtmlInputText();
			inputTitle.setValueBinding("value", app.createValueBinding("#{section.title}"));
			detailTable.getChildren().add(inputTitle);

			// Add the days label
			HtmlOutputText outputDays = new HtmlOutputText();
			outputDays.setValue(JsfUtil.getLocalizedMessage("section_days"));
			detailTable.getChildren().add(outputDays);
			
			// Add the days checkboxes
			HtmlPanelGroup daysGroup = new HtmlPanelGroup();
			
			HtmlSelectBooleanCheckbox monday = new HtmlSelectBooleanCheckbox();
			monday.setValueBinding("value", app.createValueBinding("#{section.monday}"));
			daysGroup.getChildren().add(monday);
			HtmlOutputLabel mondayLabel = new HtmlOutputLabel();
			mondayLabel.setFor(UNREACHABLE_CLIENT_ID);
			mondayLabel.setValueBinding("value", app.createValueBinding("#{msgs.day_of_week_monday}"));
			daysGroup.getChildren().add(mondayLabel);

			HtmlSelectBooleanCheckbox tuesday = new HtmlSelectBooleanCheckbox();
			tuesday.setValueBinding("value", app.createValueBinding("#{section.tuesday}"));
			daysGroup.getChildren().add(tuesday);
			HtmlOutputLabel tuesdayLabel = new HtmlOutputLabel();
			tuesdayLabel.setFor(UNREACHABLE_CLIENT_ID);
			tuesdayLabel.setValueBinding("value", app.createValueBinding("#{msgs.day_of_week_tuesday}"));
			daysGroup.getChildren().add(tuesdayLabel);
			
			HtmlSelectBooleanCheckbox wednesday = new HtmlSelectBooleanCheckbox();
			wednesday.setValueBinding("value", app.createValueBinding("#{section.wednesday}"));
			daysGroup.getChildren().add(wednesday);
			HtmlOutputLabel wednesdayLabel = new HtmlOutputLabel();
			wednesdayLabel.setFor(UNREACHABLE_CLIENT_ID);
			wednesdayLabel.setValueBinding("value", app.createValueBinding("#{msgs.day_of_week_wednesday}"));
			daysGroup.getChildren().add(wednesdayLabel);
			
			HtmlSelectBooleanCheckbox thursday = new HtmlSelectBooleanCheckbox();
			thursday.setValueBinding("value", app.createValueBinding("#{section.thursday}"));
			daysGroup.getChildren().add(thursday);
			HtmlOutputLabel thursdayLabel = new HtmlOutputLabel();
			thursdayLabel.setFor(UNREACHABLE_CLIENT_ID);
			thursdayLabel.setValueBinding("value", app.createValueBinding("#{msgs.day_of_week_thursday}"));
			daysGroup.getChildren().add(thursdayLabel);
			
			HtmlSelectBooleanCheckbox friday = new HtmlSelectBooleanCheckbox();
			friday.setValueBinding("value", app.createValueBinding("#{section.friday}"));
			daysGroup.getChildren().add(friday);
			HtmlOutputLabel fridayLabel = new HtmlOutputLabel();
			fridayLabel.setFor(UNREACHABLE_CLIENT_ID);
			fridayLabel.setValueBinding("value", app.createValueBinding("#{msgs.day_of_week_friday}"));
			daysGroup.getChildren().add(fridayLabel);
			
			HtmlSelectBooleanCheckbox saturday = new HtmlSelectBooleanCheckbox();
			saturday.setValueBinding("value", app.createValueBinding("#{section.saturday}"));
			daysGroup.getChildren().add(saturday);
			HtmlOutputLabel saturdayLabel = new HtmlOutputLabel();
			saturdayLabel.setFor(UNREACHABLE_CLIENT_ID);
			saturdayLabel.setValueBinding("value", app.createValueBinding("#{msgs.day_of_week_saturday}"));
			daysGroup.getChildren().add(saturdayLabel);
			
			HtmlSelectBooleanCheckbox sunday = new HtmlSelectBooleanCheckbox();
			sunday.setValueBinding("value", app.createValueBinding("#{section.sunday}"));
			daysGroup.getChildren().add(sunday);
			HtmlOutputLabel sundayLabel = new HtmlOutputLabel();
			sundayLabel.setFor(UNREACHABLE_CLIENT_ID);
			sundayLabel.setValueBinding("value", app.createValueBinding("#{msgs.day_of_week_sunday}"));
			daysGroup.getChildren().add(sundayLabel);
			
			detailTable.getChildren().add(daysGroup);
			
			// Add the start time label
			HtmlOutputText outputStartTime = new HtmlOutputText();
			outputStartTime.setValue(JsfUtil.getLocalizedMessage("section_start_time"));
			detailTable.getChildren().add(outputStartTime);

			// Add the start time input group
			HtmlPanelGroup startTimeGroup = new HtmlPanelGroup();

			HtmlInputText startTimeInput = new HtmlInputText();
			startTimeInput.setValueBinding("value", app.createValueBinding("#{section.startTime}"));
			startTimeGroup.getChildren().add(startTimeInput);
			
			HtmlSelectOneRadio startTimeRadio = new HtmlSelectOneRadio();
			startTimeRadio.setValueBinding("value", app.createValueBinding("#{section.startTimeAm}"));

			List startTimeList = new ArrayList();
			SelectItem startAm = new SelectItem();
			startAm.setValue("true");
			startAm.setLabel(JsfUtil.getLocalizedMessage("time_of_day_am_cap"));
			startTimeList.add(startAm);

			SelectItem startPm = new SelectItem();
			startPm.setValue("false");
			startPm.setLabel(JsfUtil.getLocalizedMessage("time_of_day_pm_cap"));
			startTimeList.add(startPm);

			UISelectItems startTimeItems = new UISelectItems();
			startTimeItems.setValue(startTimeList);
			startTimeRadio.getChildren().add(startTimeItems);
			
			startTimeGroup.getChildren().add(startTimeRadio);
			detailTable.getChildren().add(startTimeGroup);
			
			// Add the end time label
			HtmlOutputText outputEndTime = new HtmlOutputText();
			outputEndTime.setValue(JsfUtil.getLocalizedMessage("section_end_time"));
			detailTable.getChildren().add(outputEndTime);

			// Add the end time input group
			HtmlPanelGroup endTimeGroup = new HtmlPanelGroup();

			HtmlInputText endTimeInput = new HtmlInputText();
			endTimeInput.setValueBinding("value", app.createValueBinding("#{section.endTime}"));
			endTimeGroup.getChildren().add(endTimeInput);
			
			HtmlSelectOneRadio endTimeRadio = new HtmlSelectOneRadio();
			endTimeRadio.setValueBinding("value", app.createValueBinding("#{section.endTimeAm}"));

			List endTimeList = new ArrayList();
			SelectItem endAm = new SelectItem();
			endAm.setValue("true");
			endAm.setLabel(JsfUtil.getLocalizedMessage("time_of_day_am_cap"));
			endTimeList.add(endAm);

			SelectItem endPm = new SelectItem();
			endPm.setValue("false");
			endPm.setLabel(JsfUtil.getLocalizedMessage("time_of_day_pm_cap"));
			endTimeList.add(endPm);

			UISelectItems endTimeItems = new UISelectItems();
			endTimeItems.setValue(endTimeList);
			endTimeRadio.getChildren().add(endTimeItems);
			
			endTimeGroup.getChildren().add(endTimeRadio);
			detailTable.getChildren().add(endTimeGroup);

			// Add the max size label
			HtmlOutputText outputMax = new HtmlOutputText();
			outputMax.setValue(JsfUtil.getLocalizedMessage("section_max_size"));
			detailTable.getChildren().add(outputMax);
			
			// Add the max size input
			HtmlInputText inputMax = new HtmlInputText();
			inputMax.setValueBinding("value", app.createValueBinding("#{section.maxEnrollments}"));
			detailTable.getChildren().add(inputMax);

			// Add the location label
			HtmlOutputText outputLocation = new HtmlOutputText();
			outputLocation.setValue(JsfUtil.getLocalizedMessage("section_location"));
			detailTable.getChildren().add(outputLocation);
			
			// Add the location input
			HtmlInputText inputLocation = new HtmlInputText();
			inputLocation.setValueBinding("value", app.createValueBinding("#{section.location}"));
			detailTable.getChildren().add(inputLocation);

			// Add the grid to the table cell
			col.getChildren().add(detailTable);
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
