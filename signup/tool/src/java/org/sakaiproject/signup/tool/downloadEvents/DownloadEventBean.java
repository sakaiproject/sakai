/**
 * Copyright (c) 2007-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

package org.sakaiproject.signup.tool.downloadEvents;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupMeetingsBean;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This class provides Download to Excel and CSV functionality.
 * </P>
 * 
 * @author Peter Liu
 * @author Steve Swinsburg (CSV export)
 */
@Slf4j
public class DownloadEventBean extends SignupMeetingsBean {

	private static final String DOWNLOAD_ACT_URL = "downloadEvents";

	private static final String FROM_ATTENDEE_EVENT_PAGE = "signupMeeting";

	private static final String FROM_ORGANIZER_EVENT_PAGE = "orgSignupMeeting";

	private static final String DOWNLOAD_EVENT_DETAILS_XLS_FILE_NAME = "EventsWorksheet.xlsx";
	
	private static final String DOWNLOAD_EVENT_DETAILS_CSV_FILE_NAME = "EventsWorksheet.csv";
	
	private static final String XLS_MIME_TYPE="application/vnd.ms-excel";
	
	private static final String CSV_MIME_TYPE="text/csv";
	
	private static final int XLS = 1;
	
	private static final int CSV = 2;

	private String downloadVersion = FULL_VERSION;

	/**
	 * Constructor
	 */
	public DownloadEventBean() {
		setViewDateRang(null);// overwrite default
	}

	/**
	 * Call by JSP page to start downloading the XLS spreadsheet
	 * @return String - download page URL
	 */
	public String startXlsDownload() {
		if (hasSelectedOnes()) {
			String fileName = DOWNLOAD_EVENT_DETAILS_XLS_FILE_NAME;

			String downloadVersionType = FULL_VERSION;
			if (isCurrentUserAllowedUpdateSite()) {
				/* get both print and data versions */
				downloadVersionType = FULL_DATA_BOTH_VERSION;
			}

			downloadExcelSpreadsheet(getSignupMeetings(), downloadVersionType, fileName);
		} else {
			Utilities.addErrorMessage(Utilities.rb.getString("you.need.select.one.toDownload"));
		}

		// clearSelectedOnes();
		return DOWNLOAD_ACT_URL;
	}
	
	/**
	 * Call by JSP page to start dpwnloading the CSV spreadsheet
	 * @return String - download page URL
	 */
	public String startCsvDownload() {
		if (hasSelectedOnes()) {
			downloadCsvSpreadsheet(getSignupMeetings(), DOWNLOAD_EVENT_DETAILS_CSV_FILE_NAME);
		} else {
			Utilities.addErrorMessage(Utilities.rb.getString("you.need.select.one.toDownload"));
		}

		// clearSelectedOnes();
		return DOWNLOAD_ACT_URL;
	}

	/**
	 * Called by organizer's or participant's meeting JSP page 
	 * @return  URL string for download jsp page
	 */
	public String downloadOneEventAsExcel() {
		SignupMeetingWrapper wrapper = null;
		String path = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestServletPath();

		if (path != null && path.indexOf(FROM_ORGANIZER_EVENT_PAGE) > 0) {
			wrapper = getOrganizerSignupMBean().getMeetingWrapper();
		} else if (path != null && path.indexOf(FROM_ATTENDEE_EVENT_PAGE) > 0)
			wrapper = getAttendeeSignupMBean().getMeetingWrapper();

		if (wrapper != null) {
			List<SignupMeetingWrapper> wrappers = new ArrayList<SignupMeetingWrapper>();
			wrapper.setToDownload(true);// activation for download
			wrappers.add(wrapper);
			String downloadVersion = FULL_VERSION;
			if (wrapper.getMeeting().getPermission().isUpdate()) {
				downloadVersion = FULL_DATA_BOTH_VERSION;
			}

			downloadExcelSpreadsheet(wrappers, downloadVersion, DOWNLOAD_EVENT_DETAILS_XLS_FILE_NAME);
			// clear up
			wrapper.setToDownload(false);
		}

		return "";
	}
	
	/**
	 * Called by organizer's or participant's meeting JSP page 
	 * @return  URL string for download jsp page
	 */
	public String downloadOneEventAsCsv() {
		return downloadOneEvent(CSV);
	}
	
	private String downloadOneEvent(int type) {
		SignupMeetingWrapper wrapper = null;
		String path = FacesContext.getCurrentInstance().getExternalContext().getRequestServletPath();

		if (path != null && path.indexOf(FROM_ORGANIZER_EVENT_PAGE) > 0) {
			wrapper = getOrganizerSignupMBean().getMeetingWrapper();
		} else if (path != null && path.indexOf(FROM_ATTENDEE_EVENT_PAGE) > 0)
			wrapper = getAttendeeSignupMBean().getMeetingWrapper();

		if (wrapper != null) {
			List<SignupMeetingWrapper> wrappers = new ArrayList<SignupMeetingWrapper>();
			wrapper.setToDownload(true);// activation for download
			wrappers.add(wrapper);
			String downloadVersion = FULL_VERSION;
			if (wrapper.getMeeting().getPermission().isUpdate()) {
				downloadVersion = FULL_DATA_BOTH_VERSION;
			}
			
			switch(type) {
			case 1: downloadExcelSpreadsheet(wrappers, downloadVersion, DOWNLOAD_EVENT_DETAILS_XLS_FILE_NAME); break;
			case 2: downloadCsvSpreadsheet(wrappers, DOWNLOAD_EVENT_DETAILS_CSV_FILE_NAME); break;
			default: log.error("Invalid download type ("+type+"). Download aborted."); break;

			}
			// clear up
			wrapper.setToDownload(false);
		}

		return "";
	}
	
	

	private void downloadExcelSpreadsheet(List<SignupMeetingWrapper> smWrappers,
			String downloadType, String fileName) {

		FacesContext fc = FacesContext.getCurrentInstance();
		ServletOutputStream out = null;
		try {
			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
			responseSettings(fileName, XLS_MIME_TYPE, response);
			out = response.getOutputStream();

			excelSpreadsheet(out, smWrappers, downloadType);

			out.flush();
		} catch (IOException ex) {
			log.warn("Error generating XLS spreadsheet for download event:" + ex.getMessage());
		} finally {
			if (out != null)
				closeStream(out);
		}
		fc.responseComplete();
	}
	
	private void downloadCsvSpreadsheet(List<SignupMeetingWrapper> smWrappers, String fileName) {

		FacesContext fc = FacesContext.getCurrentInstance();
		ServletOutputStream out = null;
		try {
			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
			responseSettings(fileName, CSV_MIME_TYPE, response);
			out = response.getOutputStream();

			csvSpreadsheet(out, smWrappers);

			out.flush();
		} catch (IOException ex) {
			log.warn("Error generating CSV spreadsheet for download event:" + ex.getMessage());
		} finally {
			if (out != null)
				closeStream(out);
		}
		fc.responseComplete();
	}

	private void clearSelectedOnes() {
		List<SignupMeetingWrapper> sMeetings = this.signupMeetings;
		if (sMeetings == null)
			return;

		for (SignupMeetingWrapper wrapper : sMeetings) {
			wrapper.setToDownload(false);
		}

	}

	private boolean hasSelectedOnes() {
		List<SignupMeetingWrapper> sMeetings = getSignupMeetings();
		if (sMeetings == null)
			return false;

		for (SignupMeetingWrapper wrapper : sMeetings) {
			if (wrapper.isToDownload())
				return true;
		}

		return false;
	}

	/**
	 * Jump in method from jsp page
	 * 
	 * @return download Jsp page string
	 */
	public String downloadSelections() {
		if (getViewDateRang() == null) {
			if (isCurrentUserAllowedUpdateSite()) {
				setViewDateRang(ALL_FUTURE);
			} else
				setViewDateRang(VIEW_MY_SIGNED_UP);
		}

		return DOWNLOAD_ACT_URL;
	}


	private void excelSpreadsheet(OutputStream os, List<SignupMeetingWrapper> meetingWrappers,
			String downloadType) throws IOException {
		EventWorksheet worksheet = new EventWorksheet(getSakaiFacade());

		Workbook wb = worksheet.getEventWorkbook(meetingWrappers, downloadType);
		wb.write(os);
	}
	
	private void csvSpreadsheet(OutputStream os, List<SignupMeetingWrapper> meetingWrappers) throws IOException {
		
		CSVExport export = new CSVExport(meetingWrappers, getSakaiFacade());
		
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(os), ',');
	    
		//header
		List<String> header = export.getHeaderRow();
		
		int cols = header.size(); //total number of columns is based on header row
		
		String[] headerRow = new String[cols];
		headerRow = header.toArray(headerRow);
		writer.writeNext(headerRow);
		
		//data rows
		List<List<String>> data = export.getDataRows();
		Iterator<List<String>> iter = data.iterator();
		while(iter.hasNext()) {
			List<String> row = iter.next();
			String[] dataRow = new String[cols];
			dataRow = row.toArray(dataRow);
			writer.writeNext(dataRow);
		}
		
		writer.close();
		
	}

	private void responseSettings(String filename, String mimetype, HttpServletResponse response) {
		// Stop IE from misbehaving
		response.reset(); // Eliminate the added-on stuff
		response.setHeader("Pragma", "public"); // Override old-style cache
		// control
		response.setHeader("Cache-Control",
				"public, must-revalidate, post-check=0, pre-check=0, max-age=0"); // New-style
		// Standard headers
		response.setContentType(mimetype);
		response.setHeader("Content-disposition", "attachment; filename=" + filename);
	}

	private void closeStream(ServletOutputStream out) {
		try {
			out.close();
		} catch (IOException e) {
			log.warn("Error closing the output stream: " +e.getMessage());
		}
	}

	public String getDownloadVersion() {
		return downloadVersion;
	}

	public void setDownloadVersion(String downloadVersion) {
		this.downloadVersion = downloadVersion;
	}

	/**
	 * It's a getter method for UI.
	 * 
	 * @return a String message.
	 */
	@Override
	public String getMeetingUnavailableMessages() {
		String message = super.getMeetingUnavailableMessages();
		/* no meeting available cases: */
		if (isAllowedToCreate() && (isSelectedViewFutureMeetings() || isSelectedViewAllMeetings()))
			message = Utilities.rb.getString("no_events_in_this_period_attendee_orgnizer");

		return message;
	}
}
