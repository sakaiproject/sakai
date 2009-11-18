/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/

package org.sakaiproject.signup.tool.downloadEvents;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttachment;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupGroup;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.util.SignupBeanConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.time.api.Time;

import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * <p> This class will provides formatting data to Excel style functionality.
 * </P>
 * 
 * @author Peter Liu
 */
public class EventWorksheet implements MeetingTypes, SignupBeanConstants {

	private ResourceLoader rb = new ResourceLoader("messages");

	private String[] tabTitles_Organizor;

	private String[] tabTitles_Participant;

	private String[] tabTitles_shortVersion;

	private Workbook wb = null;

	private String currentTabTitles[];

	private static int rowHigh = 15;

	private SimpleDateFormat dateFormat;

	Map<String, CellStyle> styles = null;

	CreationHelper createHelper = null;

	private SakaiFacade sakaiFacade;

	/**
	 * Constructor
	 * 
	 * @param sakaiFacade -
	 *            a SakaiFacade Object
	 */
	public EventWorksheet(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
		wb = new HSSFWorkbook();
		/* could also define wb = new XSSFWorkbook(); */

		styles = WorksheetStyleClass.createStyles(wb);

		dateFormat = new SimpleDateFormat("", rb.getLocale());
		dateFormat.setTimeZone(sakaiFacade.getTimeService().getLocalTimeZone());

		initTableThRow();

		createHelper = wb.getCreationHelper();
	}

	/**
	 * Obtain the created excel workbook with various worksheets according to
	 * the downlaod type
	 * 
	 * @param meetingWrappers -
	 *            a list of SignupMeetingWrapper objects
	 * @param downloadType -
	 *            a download type string
	 * @return a Workbook object
	 */
	public Workbook getEventWorkbook(List<SignupMeetingWrapper> meetingWrappers, String downloadType) {

		if (meetingWrappers != null) {
			/* case 1: participant's download without data sheet */
			if (FULL_VERSION.equals(downloadType)) {
				/* event overview version */
				if(meetingWrappers.size() > 1){
					createShortVersonWorksheet(meetingWrappers);
				}

				int serialNum = 1;
				boolean hasSerialNum = meetingWrappers.size() > 1;
				for (SignupMeetingWrapper smWrapper : meetingWrappers) {
					if (smWrapper.isToDownload()) {
						createWorksheet(smWrapper, serialNum, hasSerialNum);
						serialNum++;
					}
				}
				/* case 2: organizer's version with data sheet */
			} else if (FULL_DATA_BOTH_VERSION.equals(downloadType)) {
				List<SignupMeetingWrapper> dataWrappers = new ArrayList<SignupMeetingWrapper>();
				for (SignupMeetingWrapper smWrapper : meetingWrappers) {
					// filter out unselected ones
					if (smWrapper.isToDownload()) {
						dataWrappers.add(smWrapper);
					}
				}

				int serialNum = 1;
				boolean hasSerialNum = meetingWrappers.size() > 1;
				if (dataWrappers.size() > 1) {
					// data version comes first since only one sheet
					createAttendeeDataWorksheet(dataWrappers);

					/* print version */
					/* event overview version */
					createShortVersonWorksheet(meetingWrappers);

					for (SignupMeetingWrapper smWrapper : dataWrappers) {
						createWorksheet(smWrapper, serialNum, hasSerialNum);
						serialNum++;
					}

				} else {/* one record only */
					for (SignupMeetingWrapper smWrapper : dataWrappers) {
						createWorksheet(smWrapper, serialNum, false);
					}

					createAttendeeDataWorksheet(dataWrappers);
				}
			}
		}

		// CreateCellReference();
		return wb;
	}

	/**
	 * Create a data excel worksheet for attendee's informaiton
	 */
	private Workbook createAttendeeDataWorksheet(List<SignupMeetingWrapper> wrappers) {
		String eventTitle = rb.getString("sheet_name_Attendee_schedules", "Attendees' Schedules");
		Sheet sheet = wb.createSheet(eventTitle);
		PrintSetup printSetup = sheet.getPrintSetup();
		printSetup.setLandscape(true);
		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);

		/* Define column numbers and width here */
		int numberOfColumn = 12;
		sheet.setColumnWidth(0, 25 * 256);// event title
		sheet.setColumnWidth(1, 20 * 256);// attendee display name
		sheet.setColumnWidth(2, 20 * 256);// attendee user id
		sheet.setColumnWidth(3, 25 * 256);// attendee user email
		sheet.setColumnWidth(4, 25 * 256);// site name
		sheet.setColumnWidth(5, 20 * 256);// appointment start time
		sheet.setColumnWidth(6, 16 * 256);// duration
		sheet.setColumnWidth(7, 22 * 256);// #num of attendees
		sheet.setColumnWidth(8, 25 * 256);// #user comment
		sheet.setColumnWidth(9, 20 * 256);// event owner
		sheet.setColumnWidth(10, 20 * 256);// event location
		sheet.setColumnWidth(11, 20 * 256);// event start time
		sheet.setColumnWidth(12, 20 * 256);// duration

		if (wrappers == null)
			return wb;

		int rowNum = 0;
		Cell cell = null;
		Row titleRow = sheet.createRow(rowNum++);
		titleRow.setHeightInPoints(rowHigh);
		for (int i = 0; i <= numberOfColumn; i++) {
			titleRow.createCell(i).setCellStyle(styles.get("item_leftBold"));
		}

		int cellNum = 0;
		titleRow.getCell(cellNum++)
				.setCellValue(rb.getString("wksheet_meeting_name", "Event Name"));
		titleRow.getCell(cellNum++)
				.setCellValue(rb.getString("wksheet_user_name", "Attendee Name"));
		titleRow.getCell(cellNum++).setCellValue(
				rb.getString("wksheet_user_id", "Attendee User Id"));
		titleRow.getCell(cellNum++).setCellValue(rb.getString("wksheet_user_email", "Email"));
		titleRow.getCell(cellNum++).setCellValue(rb.getString("wksheet_site_name", "Site Title"));
		titleRow.getCell(cellNum++).setCellValue(
				rb.getString("wksheet_appointment_start_time", "Appointment Time"));
		titleRow.getCell(cellNum++).setCellValue(
				rb.getString("wksheet_appointment_duration", "Duration (min)"));
		titleRow.getCell(cellNum++).setCellValue(
				rb.getString("wksheet_num_of_attendees", "#Num Attendees in Slot"));
		titleRow.getCell(cellNum++).setCellValue(
				rb.getString("wksheet_user_comment", "User Comment"));
		titleRow.getCell(cellNum++).setCellValue(rb.getString("wksheet_organizer", "Organizer"));
		titleRow.getCell(cellNum++).setCellValue(rb.getString("wksheet_location", "Location"));
		titleRow.getCell(cellNum++).setCellValue(
				rb.getString("wksheet_meeting_start_time", "Event Start Time"));
		titleRow.getCell(cellNum++).setCellValue(
				rb.getString("wksheet_meeting_duration", "Event Duration (min)"));

		for (SignupMeetingWrapper wrp : wrappers) {
			List<SignupTimeslot> tsItems = wrp.getMeeting().getSignupTimeSlots();
			if (tsItems != null) {
				for (SignupTimeslot tsItem : tsItems) {
					List<SignupAttendee> attendees = tsItem.getAttendees();
					if (attendees != null) {
						for (SignupAttendee att : attendees) {
							Row row = sheet.createRow(rowNum++);
							for (int i = 0; i <= numberOfColumn; i++) {
								row.createCell(i).setCellStyle(styles.get("item_left"));
							}
							User attendee = sakaiFacade.getUser(att.getAttendeeUserId());
							/* reset */
							cellNum = 0;

							/* meeting title */
							cell = row.getCell(cellNum++);
							cell.setCellValue(wrp.getMeeting().getTitle());

							/* attendee name */
							cell = row.getCell(cellNum++);
							cell.setCellValue(attendee.getDisplayName());

							cell = row.getCell(cellNum++);
							cell.setCellValue(attendee.getEid());

							cell = row.getCell(cellNum++);
							cell.setCellValue(attendee.getEmail());

							cell = row.getCell(cellNum++);
							cell.setCellValue(getSiteTitle(att.getSignupSiteId()));

							cell = row.getCell(cellNum++);
							cell.setCellValue(sakaiFacade.getTimeService().newTime(
									tsItem.getStartTime().getTime()).toStringLocalFull());

							cell = row.getCell(cellNum++);
							cell.setCellValue(getDurationLength(tsItem.getEndTime(), tsItem
									.getStartTime()));// minutes

							cell = row.getCell(cellNum++);
							cell.setCellValue(tsItem.getAttendees().size());

							cell = row.getCell(cellNum++);
							cell.setCellValue(att.getComments());

							cell = row.getCell(cellNum++);
							cell.setCellValue(sakaiFacade.getUserDisplayName(wrp.getMeeting()
									.getCreatorUserId()));

							cell = row.getCell(cellNum++);
							cell.setCellValue(wrp.getMeeting().getLocation());

							cell = row.getCell(cellNum++);
							cell.setCellValue(sakaiFacade.getTimeService().newTime(
									wrp.getMeeting().getStartTime().getTime()).toStringLocalFull());

							cell = row.getCell(cellNum++);
							cell.setCellValue(getDurationLength(wrp.getMeeting().getEndTime(), wrp
									.getMeeting().getStartTime()));
						}
					}
				}

			}
		}

		return wb;
	}

	/**
	 * Create a short version excel worksheet
	 */
	private Workbook createShortVersonWorksheet(List<SignupMeetingWrapper> wrappers) {

		String eventTitle = rb.getString("event_overview", "Events Overview");
		Sheet sheet = wb.createSheet(eventTitle);
		PrintSetup printSetup = sheet.getPrintSetup();
		printSetup.setLandscape(true);
		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);

		sheet.setColumnWidth(0, 20 * 256);
		sheet.setColumnWidth(1, 15 * 256);
		sheet.setColumnWidth(2, 16 * 256);
		sheet.setColumnWidth(3, 15 * 256);
		sheet.setColumnWidth(4, 25 * 256);
		sheet.setColumnWidth(5, 19 * 256);

		// title row
		Row titleRow = sheet.createRow(0);
		titleRow.setHeightInPoints(35);
		for (int i = 0; i <= 5; i++) {
			titleRow.createCell(i).setCellStyle(styles.get("title"));
		}
		Cell titleCell = titleRow.getCell(0);
		titleCell.setCellValue(eventTitle);
		sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$F$1"));

		// Cureent viewer row
		Row row = sheet.createRow(2);
		row.setHeightInPoints(rowHigh);
		Cell cell = row.createCell(0);
		cell.setCellValue(rb.getString("event_viewer", "Viewer:"));
		cell.setCellStyle(styles.get("item_leftBold"));
		cell = row.createCell(1);
		cell.setCellStyle(styles.get("item_left"));
		cell.setCellValue(getCurrentUserName());

		// site title row
		row = sheet.createRow(3);
		row.setHeightInPoints(rowHigh);
		cell = row.createCell(0);
		cell.setCellValue(rb.getString("event_site_title", "Site Title:"));
		cell.setCellStyle(styles.get("item_leftBold"));
		cell = row.createCell(1);
		cell.setCellStyle(styles.get("item_left"));
		cell.setCellValue(getCurrentSiteTitle());

		// Table titles th row
		row = sheet.createRow(5);
		row.setHeightInPoints(rowHigh);
		for (int i = 0; i <= 5; i++) {
			row.createCell(i).setCellStyle(styles.get("tabColNames"));
		}
		cell = row.getCell(0);
		cell.setCellValue(tabTitles_shortVersion[0]);
		cell = row.getCell(1);
		cell.setCellValue(tabTitles_shortVersion[1]);
		cell = row.getCell(2);
		cell.setCellValue(tabTitles_shortVersion[2]);
		cell = row.getCell(3);
		cell.setCellValue(tabTitles_shortVersion[3]);
		cell = row.getCell(4);
		cell.setCellValue(tabTitles_shortVersion[4]);
		cell = row.getCell(5);
		cell.setCellValue(tabTitles_shortVersion[5]);

		/* table row data */
		int rowNum = 6;
		int seqNum = 1;
		for (SignupMeetingWrapper wrp : wrappers) {
			if (wrp.isToDownload()) {
				row = sheet.createRow(rowNum);
				int rowHighNum = 1;
				rowNum++;
				for (int i = 0; i <= 5; i++) {
					row.createCell(i).setCellStyle(styles.get("tabItem_fields"));
				}
				// event ttile
				cell = row.getCell(0);
				cell.setCellStyle(styles.get("item_left_wrap"));
				cell.setCellValue(wrp.getMeeting().getTitle());
				Hyperlink sheetLink = createHelper.createHyperlink(Hyperlink.LINK_DOCUMENT);
				String hlinkAddr = "'" + wrp.getMeeting().getTitle() + " (" + seqNum + ")'" + "!A1";
				sheetLink.setAddress(hlinkAddr);
				cell.setHyperlink(sheetLink);
				cell.setCellStyle(styles.get("hyperLink"));
				seqNum++;

				// event owner
				cell = row.getCell(1);
				cell.setCellValue(wrp.getCreator());

				// event location
				cell = row.getCell(2);
				cell.setCellValue(wrp.getMeeting().getLocation());

				// event Date
				cell = row.getCell(3);
				cell.setCellValue(getShortWeekDayName(wrp.getStartTime()) + ", "
						+ getTime(wrp.getStartTime()).toStringLocalShortDate());

				// event time period
				cell = row.getCell(4);
				cell.setCellValue(getMeetingPeriodShortVersion(wrp));

				// event status
				cell = row.getCell(5);
				cell.setCellValue(ExcelPlainTextFormat.convertFormattedHtmlTextToExcelPlaintext(wrp
						.getAvailableStatus()));
			}
		}

		// end of table line
		row = sheet.createRow(rowNum);
		for (int i = 0; i <= 5; i++) {
			row.createCell(i).setCellStyle(styles.get("tab_endline"));
		}

		return wb;
	}

	/**
	 * Create a full version excel worksheet
	 */
	private void createWorksheet(SignupMeetingWrapper wrapper, int serialNum, boolean hasSerialNum) {
		String eventTitle = wrapper.getMeeting().getTitle();
		if (hasSerialNum) {
			eventTitle = eventTitle + " (" + serialNum + ")";
		}

		Sheet sheet = wb.createSheet(eventTitle);
		PrintSetup printSetup = sheet.getPrintSetup();
		printSetup.setLandscape(true);
		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);

		sheet.setColumnWidth(0, 3 * 256);
		sheet.setColumnWidth(1, 3 * 256);
		sheet.setColumnWidth(2, 17 * 256);
		sheet.setColumnWidth(3, 15 * 256);
		sheet.setColumnWidth(4, 22 * 256);
		sheet.setColumnWidth(5, 22 * 256);
		sheet.setColumnWidth(6, 22 * 256);

		// title row
		Row titleRow = sheet.createRow(0);
		titleRow.setHeightInPoints(35);
		for (int i = 1; i <= 7; i++) {
			titleRow.createCell(i).setCellStyle(styles.get("title"));
		}
		Cell titleCell = titleRow.getCell(2);
		titleCell.setCellValue(eventTitle);
		sheet.addMergedRegion(CellRangeAddress.valueOf("$C$1:$H$1"));

		// owner row
		Row row = sheet.createRow(2);
		row.setHeightInPoints(rowHigh);
		Cell cell = row.createCell(2);
		cell.setCellValue(rb.getString("event_owner"));
		cell.setCellStyle(styles.get("item_leftBold"));
		cell = row.createCell(3);
		cell.setCellStyle(styles.get("item_left"));
		cell.setCellValue(wrapper.getCreator());

		// meeting Date row
		row = sheet.createRow(3);
		row.setHeightInPoints(rowHigh);
		cell = row.createCell(2);
		cell.setCellValue(rb.getString("event_date"));
		cell.setCellStyle(styles.get("item_leftBold"));
		cell = row.createCell(3);
		cell.setCellStyle(styles.get("item_left"));
		cell.setCellValue(getTime(wrapper.getStartTime()).toStringLocalDate());

		// Time Period row
		row = sheet.createRow(4);
		row.setHeightInPoints(rowHigh);
		cell = row.createCell(2);
		cell.setCellValue(rb.getString("event_time_period"));
		cell.setCellStyle(styles.get("item_leftBold"));
		cell = row.createCell(3);
		cell.setCellStyle(styles.get("item_left"));
		cell.setCellValue(getMeetingPeriod(wrapper.getMeeting()));

		// Sign-up Begins row
		row = sheet.createRow(5);
		row.setHeightInPoints(rowHigh);
		cell = row.createCell(2);
		cell.setCellValue(rb.getString("event_signup_start"));
		cell.setCellStyle(styles.get("item_leftBold"));
		cell = row.createCell(3);
		cell.setCellStyle(styles.get("item_left"));
		cell.setCellValue(getTime(wrapper.getMeeting().getSignupBegins()).toStringLocalDate()
				+ ", " + getTime(wrapper.getMeeting().getSignupBegins()).toStringLocalTime());

		// Sign-up Ends row
		row = sheet.createRow(6);
		row.setHeightInPoints(rowHigh);
		cell = row.createCell(2);
		cell.setCellValue(rb.getString("event_signup_deadline"));
		cell.setCellStyle(styles.get("item_leftBold"));
		cell = row.createCell(3);
		cell.setCellStyle(styles.get("item_left"));
		cell.setCellValue(getTime(wrapper.getMeeting().getSignupDeadline()).toStringLocalDate()
				+ ", " + getTime(wrapper.getMeeting().getSignupDeadline()).toStringLocalTime());

		// Available To row
		row = sheet.createRow(7);
		for (int i = 1; i <= 5; i++) {
			row.createCell(i);
		}
		cell = row.getCell(2);
		cell.setCellValue(rb.getString("event_publish_to"));
		cell.setCellStyle(styles.get("item_leftBold"));
		cell = row.getCell(3);
		cell.setCellStyle(styles.get("item_left_wrap"));
		String availSitesGroups = getAvailableSitesGroups(wrapper.getMeeting());
		cell.setCellValue(availSitesGroups);
		int rownum = getNumRows(availSitesGroups);
		row.setHeightInPoints(rowHigh * rownum);
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$8:$F$8"));

		// Description row
		row = sheet.createRow(8);
		for (int i = 1; i <= 7; i++) {
			row.createCell(i);// setCellStyle(styles.get("description"));
		}
		// cell = row.createCell(2);
		cell = row.getCell(2);
		cell.setCellValue(rb.getString("event_description"));
		cell.setCellStyle(styles.get("item_leftBold"));
		cell = row.getCell(3);
		cell.setCellStyle(styles.get("item_left_wrap_top"));
		String description = wrapper.getMeeting().getDescription();
		if (description != null && description.length() > 0) {
			description = ExcelPlainTextFormat
					.convertFormattedHtmlTextToExcelPlaintext(description);
			row.setHeightInPoints(rowHigh * getDescRowNum(description));
		}
		cell.setCellValue(description);
		sheet.addMergedRegion(CellRangeAddress.valueOf("$D$9:$H$9"));

		/* add attachment links */
		int cur_rowNum = 9;
		row = sheet.createRow(cur_rowNum);
		for (int i = 1; i <= 5; i++) {
			row.createCell(i);
		}
		row.setHeightInPoints(rowHigh);
		cell = row.getCell(2);
		cell.setCellValue(rb.getString("attachments"));
		cell.setCellStyle(styles.get("item_leftBold"));
		List<SignupAttachment> attachs = wrapper.getEventMainAttachments();
		if (attachs != null && !attachs.isEmpty()) {
			for (int i = 0; i < attachs.size(); i++) {
				SignupAttachment attach = attachs.get(i);
				if (i > 0) {// start with second attachment
					cur_rowNum++;
					row = sheet.createRow(cur_rowNum);// create next
					// attachment row
					row.setHeightInPoints(rowHigh);
					for (int j = 1; j <= 5; j++) {
						row.createCell(j);
					}
				}

				cell = row.getCell(3);
				cell.setCellStyle(styles.get("hyperLink"));
				cell.setCellValue(attach.getFilename());
				cell.setHyperlink(setAttachmentURLLinks(attach));
			}
		} else {
			cell = row.getCell(3);
			cell.setCellStyle(styles.get("item_left_wrap"));
			cell.setCellValue(rb.getString("event_no_attachment"));
		}

		/* Case: for announcement event */
		if (ANNOUNCEMENT.equals(wrapper.getMeeting().getMeetingType())) {
			row = sheet.createRow(cur_rowNum + 3);
			row.setHeightInPoints(rowHigh);
			cell = row.createCell(3);
			cell.setCellValue(rb.getString("event_is_open_session",
					"This is an open session meeting. No sign-up is necessary."));
			cell.setCellStyle(styles.get("item_leftBold"));

			return;
		}

		/* Case: for group and individual events */
		// Table titles row
		cur_rowNum = cur_rowNum + 2;
		row = sheet.createRow(cur_rowNum);
		row.setHeightInPoints(rowHigh);
		for (int i = 2; i <= 7; i++) {
			row.createCell(i).setCellStyle(styles.get("tabColNames"));
		}
		cell = row.getCell(2);
		currentTabTitles = isOrganizer(wrapper.getMeeting()) ? tabTitles_Organizor
				: tabTitles_Participant;
		cell.setCellValue(currentTabTitles[0]);
		sheet.addMergedRegion(CellRangeAddress.valueOf("$C$" + (cur_rowNum + 1) + ":$D$"
				+ (cur_rowNum + 1)));
		cell = row.getCell(4);
		cell.setCellValue(currentTabTitles[1]);
		cell = row.getCell(5);
		cell.setCellValue(currentTabTitles[2]);
		cell = row.getCell(6);
		cell.setCellValue(currentTabTitles[3]);

		// Table schedule Info
		int rowNum = cur_rowNum + 1;
		List<SignupTimeslot> tsItems = wrapper.getMeeting().getSignupTimeSlots();
		if (tsItems != null) {
			for (SignupTimeslot tsItem : tsItems) {
				row = sheet.createRow(rowNum);
				int rowHighNum = 1;
				rowNum++;
				for (int i = 1; i <= 6; i++) {
					row.createCell(i).setCellStyle(styles.get("tabItem_fields"));
				}
				// timeslot period
				cell = row.getCell(2);
				cell.setCellValue(getTimeSlotPeriod(tsItem, wrapper.getMeeting()
						.isMeetingCrossDays()));
				sheet.addMergedRegion(CellRangeAddress.valueOf("$C$" + rowNum + ":$D$" + rowNum));// "$C$11:$D$11"

				// Max # of participants
				cell = row.getCell(4);
				if (tsItem.isUnlimitedAttendee())
					cell.setCellValue(rb.getString("event_unlimited"));
				else if (isOrganizer(wrapper.getMeeting())) {
					cell.setCellValue(tsItem.getMaxNoOfAttendees());
				} else {
					int availableSpots = tsItem.getAttendees() != null ? tsItem
							.getMaxNoOfAttendees()
							- tsItem.getAttendees().size() : tsItem.getMaxNoOfAttendees();
					availableSpots = availableSpots < 1 ? 0 : availableSpots;
					String value = String.valueOf(availableSpots);
					if (tsItem.isLocked())
						value = rb.getString("event_is_locked");
					else if (tsItem.isCanceled())
						value = rb.getString("event_is_canceled");

					cell.setCellValue(value);
				}

				// attendee
				cell = row.getCell(5);
				List<SignupAttendee> attendees = tsItem.getAttendees();
				String aNames = rb.getString("event_show_no_attendee_info");
				if (isDisplayNames(wrapper.getMeeting())) {
					if (attendees != null && attendees.size() > rowHighNum) {
						rowHighNum = attendees.size();
					}
					aNames = getNames(attendees);
				}
				if (tsItem.isCanceled() && isOrganizer(wrapper.getMeeting())) {
					aNames = rb.getString("event_is_canceled");
				}
				cell.setCellValue(aNames);
				cell.setCellStyle(styles.get("attendee_layout"));

				// waiters
				cell = row.getCell(6);
				String fieldValue = "";
				if (isOrganizer(wrapper.getMeeting())) {
					List<SignupAttendee> waiters = tsItem.getWaitingList();
					if (waiters != null && waiters.size() > rowHighNum) {
						rowHighNum = waiters.size();
					}
					fieldValue = getNames(waiters);
				} else {
					fieldValue = getYourStatus(tsItem);
				}
				cell.setCellValue(fieldValue);
				cell.setCellStyle(styles.get("attendee_layout"));

				// set row high
				row.setHeightInPoints(rowHigh * rowHighNum);
			}
		}

		// end of table line
		row = sheet.createRow(rowNum);
		for (int i = 2; i <= 7; i++) {
			row.createCell(i).setCellStyle(styles.get("tab_endline"));
		}

		/* process attendee's comments */
		rowNum = rowNum + 2;
		// Comment Title row
		Row commentsRow = sheet.createRow(rowNum);
		commentsRow.setHeightInPoints(25);
		for (int i = 1; i <= 7; i++) {
			commentsRow.createCell(i).setCellStyle(styles.get("commentTitle"));
		}
		Cell commentsCell = commentsRow.getCell(2);
		commentsCell.setCellValue(rb.getString("event_comments_title", "Participant's Comments"));
		sheet.addMergedRegion(CellRangeAddress
				.valueOf("$C$" + (rowNum + 1) + ":$H$" + (rowNum + 1)));
		// separate line
		rowNum++;
		row = sheet.createRow(rowNum);
		for (int i = 2; i <= 4; i++) {
			row.createCell(i).setCellStyle(styles.get("tab_endline"));
		}

		rowNum++;
		;
		boolean hasComment = false;
		if (tsItems != null) {
			for (SignupTimeslot ts : tsItems) {
				List<SignupAttendee> attendees = ts.getAttendees();
				if (attendees != null) {
					for (SignupAttendee att : attendees) {
						if (isOrganizer(wrapper.getMeeting()) || isViewerSelf(att)) {
							String comment = att.getComments();
							if (comment != null && comment.trim().length() > 0) {
								row = sheet.createRow(rowNum++);
								for (int i = 1; i <= 7; i++) {
									row.createCell(i);
								}
								cell = row.getCell(2);
								cell.setCellValue(sakaiFacade.getUserDisplayName(att
										.getAttendeeUserId())
										+ ":");
								cell.setCellStyle(styles.get("item_leftBold"));
								cell = row.getCell(3);
								cell.setCellStyle(styles.get("item_left_wrap_top"));
								comment = ExcelPlainTextFormat
										.convertFormattedHtmlTextToExcelPlaintext(comment);
								row.setHeightInPoints(rowHigh * getDescRowNum(comment));

								cell.setCellValue(comment);
								sheet.addMergedRegion(CellRangeAddress.valueOf("$D$" + rowNum
										+ ":$H$" + rowNum));
								rowNum++;// one row space between comment
								hasComment = true;
							}
						}
					}
				}
			}

		}

		if (!hasComment) {
			row = sheet.createRow(rowNum);
			row.createCell(2);
			cell = row.getCell(2);
			cell.setCellValue(rb.getString("event_no_comments",
					"There is no comments written by participants."));
			cell.setCellStyle(styles.get("item_leftBold"));
		}

	}

	/**
	 * This will convert the Java date object to a Sakai's Time object, which
	 * provides all the useful methods for output.
	 * 
	 * @param date
	 *            a Java Date object.
	 * @return a Sakai's Time object.
	 */
	private Time getTime(Date date) {
		Time time = sakaiFacade.getTimeService().newTime(date.getTime());
		return time;
	}

	private String getTimeSlotPeriod(SignupTimeslot ts, boolean isCrossDay) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTime(ts.getStartTime()).toStringLocalTime());
		if (isCrossDay)
			sb.append(", " + getShortWeekDayName(ts.getStartTime()));

		sb.append("  -  ");
		sb.append(getTime(ts.getEndTime()).toStringLocalTime());
		if (isCrossDay)
			sb.append(", " + getShortWeekDayName(ts.getEndTime()));

		return sb.toString();
	}

	private String getMeetingPeriod(SignupMeeting sm) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTime(sm.getStartTime()).toStringLocalTime());
		if (sm.isMeetingCrossDays())
			sb.append(", " + getShortWeekDayName(sm.getStartTime()));

		sb.append("  -  ");
		sb.append(getTime(sm.getEndTime()).toStringLocalTime());
		if (sm.isMeetingCrossDays()) {
			sb.append(", " + getShortWeekDayName(sm.getEndTime()) + ", ");
			sb.append(getTime(sm.getEndTime()).toStringLocalDate());
		}
		return sb.toString();
	}

	private String getMeetingPeriodShortVersion(SignupMeetingWrapper wrp) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTime(wrp.getStartTime()).toStringLocalTime());
		if (wrp.getMeeting().isMeetingCrossDays())
			sb.append(", " + getShortWeekDayName(wrp.getStartTime()));

		sb.append("  -  ");
		sb.append(getTime(wrp.getEndTime()).toStringLocalTime());
		if (wrp.getMeeting().isMeetingCrossDays()) {
			sb.append(", " + getShortWeekDayName(wrp.getEndTime()) + ", ");
			sb.append(getTime(wrp.getEndTime()).toStringLocalDate());
		}
		return sb.toString();
	}

	private String getNames(List<SignupAttendee> attendees) {
		if (attendees == null)
			return "";

		StringBuffer sb = new StringBuffer();
		for (SignupAttendee att : attendees) {
			sb.append(sakaiFacade.getUserDisplayName(att.getAttendeeUserId()));
			sb.append("\n");
		}
		/* remove the last'\n' one */
		return sb.length() > 1 ? sb.substring(0, sb.length() - 1) : "";
	}

	private String getAvailableSitesGroups(SignupMeeting sm) {
		StringBuffer sb = new StringBuffer();
		List<SignupSite> sites = sm.getSignupSites();
		for (SignupSite site : sites) {
			sb.append(site.getTitle());
			if (site.isSiteScope()) {
				sb.append(" " + rb.getString("event_site_level"));
				sb.append("\n");
				continue;
			}

			sb.append(" " + rb.getString("event_group_level"));
			sb.append("\n");
			List<SignupGroup> groups = site.getSignupGroups();
			if (groups != null) {
				for (SignupGroup grp : groups) {
					sb.append("   - " + grp.getTitle());
					sb.append("\n");
				}
			}

		}
		/* remove the last'\n' one */
		return sb.length() > 1 ? sb.substring(0, sb.length() - 1) : "";
	}

	private int getNumRows(String s) {
		int num = 1;
		int pos = -1;
		if (s != null) {
			while (pos < s.length()) {
				pos = s.indexOf("\n", pos + 1);
				if (pos > 0)
					num++;
				else
					break;
			}
		}

		return num;
	}

	private int getDescRowNum(String description) {
		if (description == null)
			return 1;

		int size = description.length();
		int descRows = size / 100;
		if (descRows < 1)
			descRows = 1;
		int newLineNum = getNumRows(description);
		if (newLineNum > descRows)
			descRows = newLineNum;

		return descRows;
	}

	private boolean isOrganizer(SignupMeeting sm) {
		return sm.getPermission().isUpdate() || sm.getPermission().isDelete();
	}

	private boolean isViewerSelf(SignupAttendee att) {
		if (this.sakaiFacade.getCurrentUserId().equals(att.getAttendeeUserId()))
			return true;

		return false;
	}

	private boolean isDisplayNames(SignupMeeting sm) {
		boolean display = false;
		if (sm.getPermission().isUpdate() || sm.getPermission().isDelete())
			display = true;
		else {
			if (sm.getSignupTimeSlots() != null
					&& sm.getSignupTimeSlots().get(0).isDisplayAttendees())
				display = true;
		}

		return display;
	}

	private double getDurationLength(Date endTime, Date startTime) {
		double duration = 0;
		duration = endTime.getTime() - startTime.getTime();
		duration = duration / (1000 * 60);// minutes
		return duration;
	}

	private String getYourStatus(SignupTimeslot ts) {
		List<SignupAttendee> atts = ts.getAttendees();
		if (atts != null) {
			for (SignupAttendee a : atts) {
				if (a.getAttendeeUserId().equals(sakaiFacade.getCurrentUserId()))
					return rb.getString("event_you_signed_up");
			}
		}

		List<SignupAttendee> waiters = ts.getWaitingList();
		if (waiters != null) {
			for (SignupAttendee w : waiters) {
				if (w.getAttendeeUserId().equals(sakaiFacade.getCurrentUserId()))
					return rb.getString("event_you_on_wait_list", "on waiting list");
			}
		}
		return "";
	}

	private String getShortWeekDayName(Date date) {
		dateFormat.applyLocalizedPattern("EEE");
		return dateFormat.format(date);
	}

	private String getCurrentUserName() {
		return sakaiFacade.getUserDisplayName(sakaiFacade.getCurrentUserId());
	}

	private String getCurrentSiteTitle() {
		String siteId = sakaiFacade.getCurrentLocationId();
		return getSiteTitle(siteId);
	}

	private String getSiteTitle(String siteId) {
		Site site;
		try {
			site = sakaiFacade.getSiteService().getSite(siteId);
		} catch (IdUnusedException e) {
			return "";
		}
		String title = site.getTitle();// + ": " + site.getShortDescription();
		return title;
	}

	private HSSFHyperlink setAttachmentURLLinks(SignupAttachment attach) {
		HSSFHyperlink hsHyperlink = new HSSFHyperlink(HSSFHyperlink.LINK_URL);
		String link = this.sakaiFacade.getServerConfigurationService().getServerUrl()
				+ attach.getLocation();
		hsHyperlink.setAddress(link);
		hsHyperlink.setLabel(attach.getFilename());
		return hsHyperlink;
	}

	private void initTableThRow() {
		tabTitles_Organizor = new String[4];
		tabTitles_Organizor[0] = rb.getString("tab_time_slot", "Time Slot");
		tabTitles_Organizor[1] = rb.getString("tab_max_attendee", "Max # of Participants");
		tabTitles_Organizor[2] = rb.getString("tab_attendees", "Participants");
		tabTitles_Organizor[3] = rb.getString("tab_waiting_list", "Wait List");

		tabTitles_Participant = new String[4];
		tabTitles_Participant[0] = rb.getString("tab_time_slot", "Time Slot");
		tabTitles_Participant[1] = rb.getString("tab_event_available_slots", "Available Slots");
		tabTitles_Participant[2] = rb.getString("tab_attendees", "Participants");
		tabTitles_Participant[3] = rb.getString("tab_event_your_status", "Your Status");

		tabTitles_shortVersion = new String[6];
		tabTitles_shortVersion[0] = rb.getString("tab_event_name", "Meeting Title");
		tabTitles_shortVersion[1] = rb.getString("tab_event_owner", "Organizer");
		tabTitles_shortVersion[2] = rb.getString("tab_event_location", "Location");
		tabTitles_shortVersion[3] = rb.getString("tab_event_date", "Date");
		tabTitles_shortVersion[4] = rb.getString("tab_event_time", "Time");
		tabTitles_shortVersion[5] = rb.getString("tab_event_availability", "Status");
	}

}
