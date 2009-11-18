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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.sakaiproject.signup.tool.jsf.SignupMeetingWrapper;
import org.sakaiproject.signup.tool.jsf.SignupMeetingsBean;
import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <p>
 * This class will provides Download-Excel functionality.
 * </P>
 * 
 * @author Peter Liu
 */
public class DownloadEventBean extends SignupMeetingsBean {

	private static Log log = LogFactory.getLog(DownloadEventBean.class);

	private static final String DOWNLOAD_ACT_URL = "downloadEvents";

	private static final String FROM_ATTENDEE_EVENT_PAGE = "signupMeeting";

	private static final String FROM_ORGANIZER_EVENT_PAGE = "orgSignupMeeting";

	private static final String DOWNLOAD_EVENT_DETAILS_FILE_NAME = "EventsWorksheet.xls";

	private String downloadVersion = FULL_VERSION;

	/**
	 * Constructor
	 */
	public DownloadEventBean() {
		setViewDateRang(null);// overwrite default
	}

	/**
	 * Call by JSP page
	 * @return String - download page URL
	 */
	public String startDownload() {
		if (hasSelectedOnes()) {
			String fileName = DOWNLOAD_EVENT_DETAILS_FILE_NAME;

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
	 * Called by organizer's or participant's meeting JSP page 
	 * @return  URL string for download jsp page
	 */
	public String downloadOneEvent() {
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

			downloadExcelSpreadsheet(wrappers, downloadVersion, DOWNLOAD_EVENT_DETAILS_FILE_NAME);
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
			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext()
					.getResponse();
			responseSettings(fileName, response);
			out = response.getOutputStream();

			excelSpreadsheet(out, smWrappers, downloadType);

			out.flush();
		} catch (IOException ex) {
			log.warn("Error generating spreadsheet for download event:" + ex.getMessage());
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

	private boolean isCurrentUserAllowedUpdateSite() {
		String currentUserId = sakaiFacade.getCurrentUserId();
		String currentSiteId = sakaiFacade.getCurrentLocationId();
		boolean isAllowedUpdateSite = (sakaiFacade.isAllowedSite(currentUserId,
				sakaiFacade.SIGNUP_UPDATE_SITE, currentSiteId) || sakaiFacade.isAllowedSite(
				currentUserId, sakaiFacade.SIGNUP_CREATE_SITE, currentSiteId));

		return isAllowedUpdateSite;
	}

	private void excelSpreadsheet(OutputStream os, List<SignupMeetingWrapper> meetingWrappers,
			String downloadType) throws IOException {
		EventWorksheet worksheet = new EventWorksheet(getSakaiFacade());

		Workbook wb = worksheet.getEventWorkbook(meetingWrappers, downloadType);
		wb.write(os);
	}

	private void responseSettings(String filename, HttpServletResponse response) {
		// Stop IE from misbehaving
		response.reset(); // Eliminate the added-on stuff
		response.setHeader("Pragma", "public"); // Override old-style cache
		// control
		response.setHeader("Cache-Control",
				"public, must-revalidate, post-check=0, pre-check=0, max-age=0"); // New-style
		// Standard headers
		response.setContentType("application/vnd.ms-excel ");
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
