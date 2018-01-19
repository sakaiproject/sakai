/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.calendar.tool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.cheftool.ControllerState;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.CalendarUtil;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.util.FormattedText;

/**
 * Maintains user interface state for the MyCalendar action class.
 */
@Slf4j
public class CalendarActionState
	extends ControllerState
	implements SessionBindingListener
{
	private List wizardImportedEvents;

	private String importWizardType;

	private String importWizardState;

	private CalendarFilter calendarFilter = new CalendarFilter();
	
	private int m_currentday;
	private String eventId = "";
	private String selectedCalendarReference = "";
	private int m_currentyear;
	private int m_currentmonth;
	
	private boolean m_isNewCal = true;

	// this attachment list is never set to null!
	private List m_attachments = EntityManager.newReferenceList();
    private int m_newday;
    
	private String m_nextDate = "";
	private String m_prevDate = "";
	private CalendarUtil m_scalObj;
	private String m_primaryCalendarReference = null;
	private String prevState = "";

	private String m_AttachmentFlag = "false";
	private LocalEvent savedData = new LocalEvent();
	private boolean m_IsPastAlertOff = true;

	private boolean m_DelfieldAlertOff = true;

	private String m_state = "";
	private String currentpage = "second";

	private String m_returnState;
	private CalendarEventEdit m_primaryCalendarEdit;

	private String m_addfields = "";

	public String getAddfields()
	{
		return m_addfields;
	}


	
	public String getState()
	{
		return m_state;
	}
	
	public void setState(String state)
	{
		m_state = state;
	}

	
	public String getReturnState()
	{
		return m_returnState;
	}
	
	public void setReturnState(String returnState)
	{
		m_returnState = returnState;
	}


	/**
	 * Get edit The edit object
	 */
	public void setPrimaryCalendarEdit(CalendarEventEdit edit)
	{
		m_primaryCalendarEdit = edit;
	}

	/**
	 * Get edit object
	 * @return m_edit The edit object
	 */
	public CalendarEventEdit getPrimaryCalendarEdit()
	{
		return m_primaryCalendarEdit;
	}

	
	public void setCurrentPage(String page)
	{
		currentpage = page;
	}


	
	public String getCurrentPage()
	{
		return currentpage;
	}


	
	public String getfromAttachmentFlag()
	{
		return m_AttachmentFlag;
	}


	
	public void setfromAttachmentFlag(String flag)
	{
		m_AttachmentFlag = flag;
	}


	/**
	 * Get
	 */
	public List getAttachments()
	{

		return m_attachments;

	}	//	getAttachment

	/**
	* Set
	*/
	public void setAttachments(List attachments)
	{
		if (attachments != null)
		{
			m_attachments = EntityManager.newReferenceList(attachments);
		}
		else
		{
			m_attachments.clear();
		}
	}	//	setAttachments


	/**
	 * Get the status of preview: true - view new created; false - view revised existed
	 * @return The current status
	 */
	public boolean getIsNewCalendar()
	{
		return m_isNewCal;

	}	//	gsetIsCalendar


	/**
	* Set the status of preview: true - view new created; false - view revised existed
	* @param preview_status The status of preview: true - view new created; false - view revised existed
	*/
	public void setIsNewCalendar(boolean isNewcal)
	{
		// if there's a change
		if (isNewcal != m_isNewCal)
		{
			// remember the new
			m_isNewCal = isNewcal;
		}

	}	// setIsNewCalendar

	/**
	 * Get the status of past alert off: true - no alert shown; false - alert shown
	 * @return IsPastAlertOff
	 */
	public boolean getIsPastAlertOff()
	{
		return m_IsPastAlertOff;

	}	//	getIsPastAlertOff

	/**
	 * Get the status of delfield alert off: true - no alert shown; false - alert shown
	 * @return DelfieldAlertOff
	 */
	public boolean getDelfieldAlertOff()
	{
		return m_DelfieldAlertOff;

	}	//	getDelfieldAlertOff

	/**
	 * Gets the main calendar ID associated with the event list.  Many calendars may be merged into this list, but there is only one one calendar that is used for adding/modifying events.
	 */
	public String getPrimaryCalendarReference()
	{
		return m_primaryCalendarReference;
	}

	/**
	 * Set the status of past alert off: true - no alert shown; false - alert shown
	 * @param IsPastAlertOff The status of past alert off: true - no alert shown; false - alert shown
	 */
	public void setIsPastAlertOff(boolean IsPastAlertOff)
	{
		m_IsPastAlertOff = IsPastAlertOff;

	}	// setIsPastAlertOff

	/**
	 * Set the status of delfield alert off: true - no alert shown; false - alert shown
	 * @param DelfieldAlertOff The status of delfield alert off: true - no alert shown; false - alert shown
	 */
	public void setDelfieldAlertOff(boolean DelfieldAlertOff)
	{
		m_DelfieldAlertOff = DelfieldAlertOff;

	}	// setDelfieldAlertOff

	/**
	 * Sets the main calendar ID associated with the event list.  Many calendars may be merged into this
	 * list, but there is only one one calendar that is used for adding/modifying events/
	 */
	public void setPrimaryCalendarReference(String reference)
	{
		m_primaryCalendarReference = reference;
	}

	
	public void setCalendarEventId(String calendarReference, String eventId)
	{
		this.eventId = eventId;
		setSelectedCalendarReference(calendarReference);
	}

	
	public String getCalendarEventId()
	{
		return eventId;
	}

	
	public void setNewData(String calendarReference, String title, String description, int month, int day, String year, int hour, int minute, int dhour, int dminute, String type, String Am, String location, Map addfieldsMap, String intentionStr)
	{
		savedData = new LocalEvent();
		savedData.setData(calendarReference, title,description,month,day,year,hour,minute,dhour,dminute,type,Am,location,addfieldsMap, intentionStr);
	}

	
	public void clearData()
	{
		savedData = new LocalEvent();
	}

	
	public LocalEvent getNewData()
	{
		return savedData;
	}

	
	public void setcurrentDate(int currentday){ m_currentday = currentday; }
	
	public int getcurrentDate() { return m_currentday;}

	
	public void setnewDate(int newday) { m_newday = newday; }
	
	public int getnewDate() { return m_newday;}


	
	public void setnextDate(String nextDate){ m_nextDate = nextDate;}
	
	public String getnextDate(){return m_nextDate;}

	
	public void setprevDate(String prevDate){ m_prevDate = prevDate;}
	
	public String getprevDate(){return m_prevDate;}


	
	public CalendarUtil getCalObj()
	{
		return m_scalObj;
	}

	
	public void setCalObj(CalendarUtil calObj)
	{
		m_scalObj = calObj;
	}

	
	public void setPrevState(String state){ prevState = state; }

	
	public String getPrevState(){ return prevState; }


	
	public void setcurrentDay(int currentday){ m_currentday = currentday; }

	
	public void setcurrentMonth(int currentmonth){ m_currentmonth = currentmonth; }

	
	public void setcurrentYear(int currentyear){ m_currentyear = currentyear; }


	
	public int getcurrentDay(){ return m_currentday;}

	
	public int getcurrentMonth(){ return m_currentmonth;}

	
	public int getcurrentYear(){ return m_currentyear; }


	
	public CalendarActionState()
	{
		init();
	}

	/* (non-Javadoc)
	 * @see org.chefproject.core.ControllerState#recycle()
	 */
	public void recycle()
	{
		super.recycle();
		init();

	}	// recycle

	/* (non-Javadoc)
	 * @see org.chefproject.core.ControllerState#init()
	 */
	protected void init()
	{
		m_state = null;
		m_scalObj = new CalendarUtil();
		m_currentday = m_scalObj.getDayOfMonth();
		m_currentyear = m_scalObj.getYear();
		m_currentmonth = m_scalObj.getMonthInteger();
		
		calendarFilter.setListViewDateRangeToDefault();
	}




	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(java.util.Observable observable, java.lang.Object obj) {
	}

	/**
	 * Used in the MyCalendar action and associated Velocity templates to store
	 * a single calendar event in an arbitrary calendar, not necessary the current
	 * primary calendar.
	 */
	public class LocalEvent
	{
		private String title;
		private String description;
		private int month;
		private String year;
		private int day;
		private int hour;
		private int minute;
		private int durationHour;
		private int durationMinute;
		private String type;
		private String am;
		private String location;
		private String calendarReference;
		private String intentionStr;

		// This is a map of additional properties.  These are dynamic and not like title, location, etc.
		private Map addFieldsMap;

		/**
		 * Default constructor
		 */
		public LocalEvent()
		{
		   title = null;
		   description = null;
		   month = 0;
		   day = 0;
		   year = "0";
		   hour = 0;
		   minute = -1;
		   durationHour = -1;
		   durationMinute = -1;
		   type = null;
		   am = null;
		   location="";
		   calendarReference = null;
		   addFieldsMap = new HashMap();
		   intentionStr = "";
		}

		/**
		 * @param fieldsMap Map of additional properties
		 */
		public void setData(String calendarReference, String title, String description, int month, int day, String year, int hour, int minute, int dhour, int dminute, String type, String am, String location, Map fieldsMap, String intentionStr)
		{
		   this.title = title;
		   this.description = description;
		   this.month = month;
		   this.day = day;
		   this.year = year;
		   this.hour = hour;
		   this.minute = minute;
		   this.durationHour = dhour;
		   this.durationMinute = dminute;
		   this.type = type;
		   this.am = am;
		   this.location = location;
		   this.calendarReference = calendarReference;
		   this.addFieldsMap = fieldsMap;
		   this.intentionStr = intentionStr;
		}

		
		public String getTitle()
		{
			return title;
		}

		
		public String getDescription()
		{
			return description;
		}

		
		public int getMonth()
		{
			return month;
		}

		
		public int getDay()
		{
			return day;
		}

		
		public String getYear()
		{
			return year;
		}

		
		public int getYearInt()
		{
			return new Integer(year).intValue ();
		}

		
		public int getHour()
		{
			return hour;
		}

		
		public int getMinute()
		{
			return minute;
		}

		
		public int getDurationHour()
		{
			return durationHour;
		}

		
		public int getDurationMinute()
		{
			return durationMinute;
		}

		
		public String getType()
		{
			return type;
		}

		
		public String getAm()
		{
			return am;
		}

		
		public String getLocation()
		{
			return location;
		}

		/**
		 * Gets the value for one of the additional attribute fields.
		 */
		public String getAddfieldValue(String fieldname)
		{
			fieldname = FormattedText.unEscapeHtml(fieldname);
			Set addfieldsKey = addFieldsMap.keySet();

			Iterator it = addfieldsKey.iterator();
			String prop_name = "";
			String prop_value = "";

			while (it.hasNext())
			{
				prop_name = (String) it.next();
				if (prop_name.equals(fieldname))
				{
					prop_value = (String) addFieldsMap.get(prop_name);
					return prop_value;
				}
			}
			return prop_value;
		}
		
		
		public String getIntentionStr()
		{
			return intentionStr;
		} // getIntentionStr

	}	// localEvent

	/**
	 * Gets the currently selected calendar.
	 */
	public String getSelectedCalendarReference()
	{
		return selectedCalendarReference;
	}

	/**
	 * Sets the currently selected calendar.
	 */
	public void setSelectedCalendarReference(String string)
	{
		selectedCalendarReference = string;
	}

		
	/**
	 * Returns the calendar filter that is currently being used for the list
	 * view and printing.
	 */
	public CalendarFilter getCalendarFilter()
	{
		return calendarFilter;
	}


	
	public void setImportWizardState(String importWizardState)
	{
		this.importWizardState = importWizardState;		
	}

	
	public String getImportWizardState()
	{
		return importWizardState;
	}


	
	public void setImportWizardType(String importWizardType)
	{
		this.importWizardType = importWizardType;
	}

	
	public String getImportWizardType()
	{
		return importWizardType;
	}


	
	public void setWizardImportedEvents(List wizardImportedEvents)
	{
		this.wizardImportedEvents = wizardImportedEvents;		
	}

	
	public List getWizardImportedEvents()
	{
		return wizardImportedEvents;
	}
	
	
	
	private Site m_editSite;
	private CalendarEventEdit m_edit;
	
	/**
	 * Set edit The canlender event edit object
	 */
	public void setEdit(CalendarEventEdit edit)
	{
		m_edit = edit;
	}

	
	/*******************************************************************************
	* SessionBindingListener implementation
	*******************************************************************************/

	public void valueBound(SessionBindingEvent event)
	{
	}

	public void valueUnbound(SessionBindingEvent event)
	{
		if (log.isDebugEnabled())
			log.debug("valueUnbound()");

		// pass it on to my edits
		if ((m_editSite != null) && (m_editSite instanceof SessionBindingListener))
		{
			((SessionBindingListener) m_editSite).valueUnbound(event);
		}

		if ((m_edit != null) && (m_edit instanceof SessionBindingListener))
		{
			((SessionBindingListener) m_edit).valueUnbound(event);
		}

	} // valueUnbound	


}	// class CalendarActionState



