/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.jsf.organizer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.TimeslotWrapper;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.util.DateFormatterUtil;


public class UserDefineTimeslotBean implements SignupBeanConstants {

	SakaiFacade sakaiFacade;

	private final int MAX_NUM_PARTICIPANTS = 1;

	private String gobackURL = "";

	private boolean validationError = false;

	//for create new meeting step 1 case
	private boolean userEverCreateCTS = false;

	private SignupMeeting signupMeeting;

	protected UIData tsTable;

	public String placeOrderBean;

	public final static String NEW_MEETING = "new_meeting";

	public final static String MODIFY_MEETING = "modify_meeting";

	public final static String COPY_MEETING = "copy_meeting";

	// discontinued time slots case
	private List<TimeslotWrapper> timeSlotWrpList;

	private List<TimeslotWrapper> destTSwrpList;
	
	private boolean someoneSignedUp;
	
	private boolean putInMultipleCalendarBlocks = true;
	
	String errorStyleValue = "background: #EEF3F6;";

	private static String HIDDEN_ISO_STARTTIME = "startTimeISO8601";
	private static String HIDDEN_ISO_ENDTIME = "endTimeISO8601";

	public void init(SignupMeeting sMeeting, String backPageURL,
			List<TimeslotWrapper> origTSwrpList, String whoPlaceOrder) {
		/* new page and initialize */
		/*
		 * this signupMeeting object has original timeslot info and later will
		 * be one by one populated with latest modified ts info
		 */
		this.signupMeeting = sMeeting;
		this.destTSwrpList = origTSwrpList; // keep old copy
		this.placeOrderBean = whoPlaceOrder;
		setGobackURL(backPageURL);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		if (origTSwrpList == null || origTSwrpList.size() == 0) {
			timeSlotWrpList = new ArrayList<TimeslotWrapper>();
			SignupTimeslot ts = new SignupTimeslot();
			ts.setStartTime(calendar.getTime());
			ts.setEndTime(calendar.getTime());
			ts.setMaxNoOfAttendees(MAX_NUM_PARTICIPANTS);
			TimeslotWrapper tsWrp = new TimeslotWrapper(ts);
			timeSlotWrpList.add(tsWrp);
		} else {
			//deep copy
			this.timeSlotWrpList = new ArrayList<TimeslotWrapper>();
			for (TimeslotWrapper origTswrp : origTSwrpList) {
				this.timeSlotWrpList.add(deepCopyTimeslotWrapper(origTswrp));
			}
		}

		//warning for user
		if(MODIFY_MEETING.equals(getPlaceOrderBean())){
			this.someoneSignedUp=true;
		}
		
		this.validationError = false;
		setPositionIndex(this.timeSlotWrpList);

	}

	/**
	 * This will update the time-slots with latest changes by user during
	 * modifying the events.
	 * 
	 * @param userModifiedTSwrapperList -
	 *            a list of TimeslotWrapper objects
	 * @param needUpdateTimeslots -
	 *            a list of SignupTimeslot objects
	 * @param showAttendeeName -
	 *            a boolean value
	 * @param deletedTSList -
	 *            contain a list of SignupTimslot objects, which will be removed from original list.
	 * @throws Exception
	 */
	public void modifyTimesSlotsWithChanges(List<TimeslotWrapper> userModifiedTSwrapperList,
			List<SignupTimeslot> needUpdateTimeslots, Calendar newEventStartTime,
			boolean showAttendeeName, List<SignupTimeslot> deletedTSList) throws Exception {
		/*
		 * The key here is that there will be no time-slot removed in old events
		 * only the new one can be added in. You can only cancel time slot.
		 */
		//long eventNewStartTime = newEventStartTime.getTime().getTime();
		if (userModifiedTSwrapperList != null && !userModifiedTSwrapperList.isEmpty()) {
			
			/*First: update the modified info into TS*/
			Date userMdfEventStartDate = getStartTime(userModifiedTSwrapperList);
			for (TimeslotWrapper tsWrp : userModifiedTSwrapperList) {
				if(tsWrp.getDeleted())//no need to be updated
					continue;
				
				SignupTimeslot userModifiedOne = tsWrp.getTimeSlot();
				int markerPos = tsWrp.getTsMarker();
				/*update the original existed timeslots*/
				if (markerPos < needUpdateTimeslots.size()) {
					/* get the corresponding ts */
					SignupTimeslot needUPdateOne = needUpdateTimeslots.get(markerPos);
					
					/* get start time - consider recurring events case */
					needUPdateOne.setStartTime(getUpdatedTime(newEventStartTime,
							userMdfEventStartDate, userModifiedOne.getStartTime()));
					needUPdateOne.setEndTime(getUpdatedTime(newEventStartTime,
							userMdfEventStartDate, userModifiedOne.getEndTime()));
					needUPdateOne.setMaxNoOfAttendees(userModifiedOne.getMaxNoOfAttendees());
					needUPdateOne.setDisplayAttendees(showAttendeeName);
				} 
			}
			
			/*second: remove the delete timeslot*/
			int orignalTS_size = needUpdateTimeslots.size();
			List<Integer> removedList = getUserDeletedTSItems(userModifiedTSwrapperList);
			for (int i = removedList.size()-1; i >=0; i--) {
				int markerPos = (Integer)removedList.get(i).intValue();
				if (markerPos < orignalTS_size) {
					/*for further removal of attendees preparation*/
					deletedTSList.add(needUpdateTimeslots.get(markerPos));
					
					/*remove from original list*/
					needUpdateTimeslots.remove(markerPos);
				}				
			}
			
			
			/*third: This one has to come in third -important!!!! 
			 * Add newly added TS to the list*/
			for (TimeslotWrapper tsWrp : userModifiedTSwrapperList) {
				SignupTimeslot userModifiedOne = tsWrp.getTimeSlot();
				int markerPos = tsWrp.getTsMarker();
				if (markerPos == Integer.MAX_VALUE) {
					/* newly added timeslots */
					SignupTimeslot newTs = new SignupTimeslot();
					newTs.setStartTime(getUpdatedTime(newEventStartTime, userMdfEventStartDate,
							userModifiedOne.getStartTime()));
					newTs.setEndTime(getUpdatedTime(newEventStartTime, userMdfEventStartDate,
							userModifiedOne.getEndTime()));
					newTs.setMaxNoOfAttendees(userModifiedOne.getMaxNoOfAttendees());
					newTs.setDisplayAttendees(showAttendeeName);

					needUpdateTimeslots.add(newTs);
				}
			}

			/* Make sure they are in right order */
			doSort(needUpdateTimeslots);
		}

	}
	
	private TimeslotWrapper deepCopyTimeslotWrapper(TimeslotWrapper copyOne) {
		SignupTimeslot old = copyOne.getTimeSlot();
		
		SignupTimeslot newTs = new SignupTimeslot();
		newTs.setId(old.getId());
		newTs.setStartTime(old.getStartTime());
		newTs.setEndTime(old.getEndTime());
		newTs.setStartTimeString(old.getStartTimeString());
		newTs.setEndTimeString(old.getEndTimeString());
		newTs.setCanceled(old.isCanceled());
		newTs.setLocked(old.isLocked());
		newTs.setDisplayAttendees(old.isDisplayAttendees());
		newTs.setMaxNoOfAttendees(old.getMaxNoOfAttendees());
		newTs.setAttendees(null);
		newTs.setWaitingList(null);
	
		if (old.getAttendees() != null) {
			List<SignupAttendee> atts = old.getAttendees();
			List<SignupAttendee> newOnes = new ArrayList<SignupAttendee>();
			for (SignupAttendee s : atts) {
				SignupAttendee one = new SignupAttendee();
				one.setAttendeeUserId(s.getAttendeeUserId());
				one.setDisplayName(s.getDisplayName());
				one.setSignupSiteId(s.getSignupSiteId());
				one.setComments(s.getComments());
				one.setCalendarId(s.getCalendarId());
				one.setCalendarEventId(s.getCalendarEventId());
	
				newOnes.add(one);
			}
	
			newTs.setAttendees(newOnes);
		}
	
		if (old.getWaitingList() != null) {
			List<SignupAttendee> atts = old.getWaitingList();
			List<SignupAttendee> waitList = new ArrayList<SignupAttendee>();
			for (SignupAttendee s : atts) {
				SignupAttendee one = new SignupAttendee();
				one.setAttendeeUserId(s.getAttendeeUserId());
				one.setDisplayName(s.getDisplayName());
				one.setSignupSiteId(s.getSignupSiteId());
				one.setComments(s.getComments());
	
				waitList.add(one);
			}
		}
		
		TimeslotWrapper newTsWrp = new TimeslotWrapper(newTs);
		newTsWrp.setPositionInTSlist(copyOne.getPositionInTSlist());
		newTsWrp.setTsMarker(copyOne.getTsMarker());
		newTsWrp.setDeleted(copyOne.getDeleted());
		newTsWrp.setCurrentUserId(copyOne.getCurrentUserId());
		
		return newTsWrp;
	}
	
	private List<Integer> getUserDeletedTSItems(List<TimeslotWrapper> userModifiedTSwrapperList){
		List<Integer> deletedTSlist= new ArrayList<Integer>();
		for (TimeslotWrapper tsWrp : userModifiedTSwrapperList) {
			if(tsWrp.getDeleted()){
				int markerPos = tsWrp.getTsMarker();
				deletedTSlist.add(new Integer(markerPos));
			}
		}
		
		doSort(deletedTSlist);
		
		return deletedTSlist;
	}

	public String doSave() {
		if (validationError) {
			validationError = false;
			return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
		}
		// init("");
		preProcess();
		setUserEverCreateCTS(true);

		/* pass it to destination list */
		destTSwrpList = getTimeSlotWrpList();
		/*
		 * this start/end times info via signupMeeting object pointer back to
		 * parent page
		 */
		this.signupMeeting.setStartTime(getEventStartTime());
		this.signupMeeting.setEndTime(getEventEndTime());
		return getGobackURL();
	}

	public String doCancel() {
		/*
		 * this start/end times info via signupMeeting object pointer back to
		 * parent page JSF will populate null value for start/end time due to
		 * disabled fields
		 */
		this.signupMeeting.setStartTime(getEventStartTime());
		this.signupMeeting.setEndTime(getEventEndTime());
		String goBackUrl = getGobackURL();
		clear();
		return goBackUrl;
	}

	public String addOneTSBlock() {
		this.someoneSignedUp=false;
		
		if (this.validationError) {
			this.validationError = false;
			return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		SignupTimeslot ts = new SignupTimeslot();
		ts.setStartTime(calendar.getTime());
		
		calendar.add(Calendar.MINUTE, 15);
		ts.setEndTime(calendar.getTime());
		
		ts.setMaxNoOfAttendees(MAX_NUM_PARTICIPANTS);
		TimeslotWrapper tsWrp = new TimeslotWrapper(ts);

		getTimeSlotWrpList().add(tsWrp);
		setPositionIndex(this.timeSlotWrpList);

		return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
	}

	public String deleteTSblock() {
		this.someoneSignedUp=false;
		
		TimeslotWrapper tsWrapper = (TimeslotWrapper) this.tsTable.getRowData();
		if (this.timeSlotWrpList != null) {
			for (TimeslotWrapper tmp : this.timeSlotWrpList) {
				if(tmp.getDeleted())
					continue;
				
				if (tmp.getPositionInTSlist() == tsWrapper.getPositionInTSlist()) {
					if (tmp.getTsMarker() == Integer.MAX_VALUE){
						/*remove newly added TS by user*/
						timeSlotWrpList.remove(tmp);
					}
					else{
						/*Mark the deleted original TS for further removal.
						 * This will involve to remove attendees and notification process in later stage*/
						tmp.setDeleted(true);
					}
					break;
				}

			}
			if (isEmptyTimeslotWrpList())
				addOneTSBlock();//always a timeslot there
				
		}

		return CUSTOM_DEFINED_TIMESLOT_PAGE_URL;
	}
	
	private boolean isEmptyTimeslotWrpList(){
		if (this.timeSlotWrpList.size() == 0)
			return true;
		
		for (TimeslotWrapper wrp : this.timeSlotWrpList) {
			if(!wrp.getDeleted())
				return false;
		}
		
		return true;
	}

	public void validateTimeslots(ActionEvent e) {
		if (this.timeSlotWrpList == null)
			return;
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int position = 1;
		int i = 1;
		for (TimeslotWrapper tsWrp : this.timeSlotWrpList) {
			if(tsWrp.getDeleted())
				continue;//skip
			
			String isoStartTime = params.get((position - 1) + HIDDEN_ISO_STARTTIME);
			String isoEndTime = params.get((position - 1) + HIDDEN_ISO_ENDTIME);
			
			if (isoStartTime == null && isoEndTime == null) {
				while (isoStartTime == null) {
					position++;
					isoStartTime = params.get((position - 1) + HIDDEN_ISO_STARTTIME);
					isoEndTime = params.get((position - 1) + HIDDEN_ISO_ENDTIME);
				}
			}

			if(DateFormatterUtil.isValidISODate(isoStartTime)){
				tsWrp.getTimeSlot().setStartTime(DateFormatterUtil.parseISODate(isoStartTime));
			}
			
			if(DateFormatterUtil.isValidISODate(isoEndTime)){
				tsWrp.getTimeSlot().setEndTime(DateFormatterUtil.parseISODate(isoEndTime));
			}
			Date endTime = tsWrp.getTimeSlot().getEndTime();
			Date startTime = tsWrp.getTimeSlot().getStartTime();
			if (endTime.before(startTime) || endTime.equals(startTime)) {
				this.validationError = true;
				tsWrp.setErrorStyle(this.errorStyleValue);
				Utilities.addErrorMessage(MessageFormat.format(Utilities.rb
						.getString("event.endTimeslot_should_after_startTimeslot"), i));
				return;
			}
			position++;
			i++;
		}

	}

	private void preProcess() {
		
		/*set any date for deleted TS just for sorting purpose to avoid crash
		 * The JSF make such data value to null*/
		for (TimeslotWrapper wrp : timeSlotWrpList) {
			if(wrp.getDeleted()){
				wrp.getTimeSlot().setStartTime(new Date());
				wrp.getTimeSlot().setEndTime(new Date(new Date().getTime() + 100));//make a different time only
			}
		}
		
		doSort(getTimeSlotWrpList());

		setPositionIndex(this.timeSlotWrpList);

	}

	/**
	 * only clean up the change stuffs.
	 */
	private void clear() {
		this.someoneSignedUp=false;
		this.timeSlotWrpList = null;
		this.gobackURL = "";
		this.putInMultipleCalendarBlocks = true;
	}

	/**
	 * Reset everything for this bean object called by other beans
	 */
	public void reset(String whoCalled) {
		/* only the same bean can reset to clean up his own data */
		if (whoCalled.equals(this.placeOrderBean)) {
			this.timeSlotWrpList = null;
			this.destTSwrpList = null;
			//this.dataTsUpdated = false;
			this.validationError = false;
			this.placeOrderBean = "";
			this.gobackURL = "";
			this.someoneSignedUp = false;
			this.userEverCreateCTS = false;
			this.putInMultipleCalendarBlocks = true;
		}
	}

	public boolean getTruncatedAttendees() {		
		if (this.destTSwrpList != null && this.signupMeeting.getSignupTimeSlots() != null){
			List<SignupTimeslot> orgTsList = this.signupMeeting.getSignupTimeSlots();
			int count=0;
			for (TimeslotWrapper tsWrp : destTSwrpList) {
				SignupTimeslot userModifiedOne = tsWrp.getTimeSlot();
				int markerPos = tsWrp.getTsMarker();

				if (markerPos < orgTsList.size()) {
					List<SignupAttendee> atts = orgTsList.get(markerPos).getAttendees();
					if(atts !=null && (tsWrp.getTimeSlot().getMaxNoOfAttendees() < atts.size())){
						return true;
					}
					count++;					
				}
			}
			if(count < orgTsList.size()){
				/*deleted some original rows*/
				return true;
			}
		}
		
		return false;
	}

	private void doSort(List ls) {
		Collections.sort(ls);
	}

	/*this index is for javaScript purpose on the UI*/
	private void setPositionIndex(List<TimeslotWrapper> ls) {
		if (ls != null && ls.size() > 0) {
			int i = 0;
			for (TimeslotWrapper tsWrp : ls) {
				if(tsWrp.getDeleted())
					continue;
				
				tsWrp.setPositionInTSlist(i);
				i++;
			}
		}

	}

	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	public List<TimeslotWrapper> getTimeSlotWrpList() {
		return timeSlotWrpList;
	}

	public void setTimeSlotWrpList(List<TimeslotWrapper> timeSlotWrpList) {
		this.timeSlotWrpList = timeSlotWrpList;
	}

	public String getGobackURL() {
		return gobackURL;
	}

	public void setGobackURL(String gobackURL) {
		this.gobackURL = gobackURL;
	}


	/**
	 * This is a getter method which provide current Iframe id for refresh
	 * IFrame purpose.
	 * 
	 * @return a String
	 */
	public String getIframeId() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
				.getExternalContext().getRequest();
		String iFrameId = (String) request.getAttribute("sakai.tool.placement.id");
		return iFrameId;
	}

	public UIData getTsTable() {
		return tsTable;
	}

	public void setTsTable(UIData tsTable) {
		this.tsTable = tsTable;
	}

	public Date getEventStartTime() {

		return getStartTime(this.destTSwrpList);
	}

	public Date getEventEndTime() {

		if (this.destTSwrpList == null || this.destTSwrpList.isEmpty())
			return new Date();

		doSort(this.destTSwrpList);
		Date endTime = null;
		for (int i = 0; i < this.destTSwrpList.size(); i++) {
			TimeslotWrapper ts = (TimeslotWrapper) destTSwrpList.get(i);
			if(!ts.getDeleted()){
				endTime = ts.getTimeSlot().getEndTime();
				break;
			}
		}
		
		for (TimeslotWrapper tsWrp : this.destTSwrpList) {
			if(tsWrp.getDeleted())
				continue;
			
			Date tmpDate = tsWrp.getTimeSlot().getEndTime();
			if (endTime.before(tmpDate))
				endTime = tmpDate;
		}

		return endTime;
	}

	private Date getStartTime(List<TimeslotWrapper> tsList) {
		if (tsList == null || tsList.isEmpty())
			return new Date();

		doSort(tsList);
		
		for (int i = 0; i < tsList.size(); i++) {
			TimeslotWrapper ts = (TimeslotWrapper) tsList.get(i);
			if(!ts.getDeleted())
				return ts.getTimeSlot().getStartTime();
		}

		return new Date();
	}

	/**
	 * This provides the event total duration time in minutes.
	 * 
	 * @return int value for duration in minutes
	 */
	public int getEventDuration() {
		long duration =  (getEventEndTime().getTime() - getEventStartTime().getTime())
				/ MINUTE_IN_MILLISEC;
		return (int) duration;
	}

	private Date getUpdatedTime(Calendar newEventStartDate, Date origEventDate, Date d) {

		long diffs = newEventStartDate.getTime().getTime() - origEventDate.getTime();
		Date date = new Date(diffs + d.getTime());

		return date;
	}

	public List<TimeslotWrapper> getDestTSwrpList() {
		return destTSwrpList;
	}

	public void setDestTSwrpList(List<TimeslotWrapper> destTSwrpList) {
		this.destTSwrpList = destTSwrpList;
	}

	public String getPlaceOrderBean() {
		return placeOrderBean;
	}

	public void setPlaceOrderBean(String placeOrderBean) {
		this.placeOrderBean = placeOrderBean;
	}

	public String getCopyBeanOrderName() {
		return COPY_MEETING;
	}
	
	public String getNewMeetingBeanOrderName() {
		return NEW_MEETING;
	}
	
	public boolean getWarnUserModify(){
		if(MODIFY_MEETING.equals(getPlaceOrderBean())){
			return true;
		}
		
		return false;
	}

	public boolean getSomeoneSignedUp() {			
			return this.someoneSignedUp;
	}

	public void setSomeoneSignedUp(boolean someoneSignedUp) {
		this.someoneSignedUp = someoneSignedUp;
	}

	public boolean getPutInMultipleCalendarBlocks() {
		return putInMultipleCalendarBlocks;
	}

	public void setPutInMultipleCalendarBlocks(boolean putInMultipleCalendarBlocks) {
		this.putInMultipleCalendarBlocks = putInMultipleCalendarBlocks;
	}

	public boolean isUserEverCreateCTS() {
		return userEverCreateCTS;
	}

	public void setUserEverCreateCTS(boolean userEverCreateCTS) {
		this.userEverCreateCTS = userEverCreateCTS;
	}
	

}
