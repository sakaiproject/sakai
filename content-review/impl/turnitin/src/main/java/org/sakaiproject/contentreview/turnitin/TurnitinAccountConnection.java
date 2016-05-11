/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.contentreview.turnitin;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.turnitin.util.TurnitinAPIUtil;
import org.w3c.dom.Document;

/**
 * This class contains the properties and utility methods so it can be used to
 * make API calls and connections to a specific Turnitin Account.
 *
 * Ideally you can make several of these in a single Sakai System in the event
 * that you need to use different different Turnitin Accounts for different
 * tools or provisioned user spaces (such as different campuses, etc).
 *
 * A large portion of this was factored out of TurnitinReviewService where it
 * originally occurred.
 *
 * @author sgithens
 *
 */
@Slf4j
public class TurnitinAccountConnection {

	private String aid = null;
	private String said = null;
	private String secretKey = null;
	private String apiURL = "https://api.turnitin.com/api.asp?";
	private String proxyHost = null;
	private String proxyPort = null;
	private String defaultInstructorEmail = null;
	private String defaultInstructorFName = null;
	private String defaultInstructorLName = null;
	private String defaultInstructorPassword = null;
	private boolean useSourceParameter = false;
	private int turnitinConnTimeout = 0; // Default to 0, no timeout.
	private boolean studentAccountNotified = true;
	private boolean instructorAccountNotified = true;
	private int sendSubmissionNotification = 0;
	private Long maxRetry = null;
	private boolean useGrademark = false;
	private boolean migrate = false;

	// Proxy if set
	private Proxy proxy = null;

	//note that the assignment id actually has to be unique globally so use this as a prefix
	// eg. assignid = defaultAssignId + siteId
	private String defaultAssignId = null;

	private String defaultClassPassword = null;

	private String defaultInstructorId = null;

	public void init() {

		log.info("init()");

		proxyHost = serverConfigurationService.getString("turnitin.proxyHost");

		proxyPort = serverConfigurationService.getString("turnitin.proxyPort");

		if (!"".equals(proxyHost) && !"".equals(proxyPort)) {
			try {
				SocketAddress addr = new InetSocketAddress(proxyHost, new Integer(proxyPort).intValue());
				proxy = new Proxy(Proxy.Type.HTTP, addr);
				log.debug("Using proxy: " + proxyHost + " " + proxyPort);
			} catch (NumberFormatException e) {
				log.debug("Invalid proxy port specified: " + proxyPort);
			}
		} else if (System.getProperty("http.proxyHost") != null && !System.getProperty("http.proxyHost").equals("")) {
			try {
				SocketAddress addr = new InetSocketAddress(System.getProperty("http.proxyHost"), new Integer(System.getProperty("http.proxyPort")).intValue());
				proxy = new Proxy(Proxy.Type.HTTP, addr);
				log.debug("Using proxy: " + System.getProperty("http.proxyHost") + " " + System.getProperty("http.proxyPort"));
			} catch (NumberFormatException e) {
				log.debug("Invalid proxy port specified: " + System.getProperty("http.proxyPort"));
			}
		}

		aid = serverConfigurationService.getString("turnitin.aid");

		said = serverConfigurationService.getString("turnitin.said");

		secretKey = serverConfigurationService.getString("turnitin.secretKey");

		apiURL = serverConfigurationService.getString("turnitin.apiURL","https://api.turnitin.com/api.asp?");

		defaultInstructorEmail = serverConfigurationService.getString("turnitin.defaultInstructorEmail");

		defaultInstructorFName = serverConfigurationService.getString("turnitin.defaultInstructorFName");

		defaultInstructorLName = serverConfigurationService.getString("turnitin.defaultInstructorLName");

		defaultInstructorPassword = serverConfigurationService.getString("turnitin.defaultInstructorPassword");

		useSourceParameter = serverConfigurationService.getBoolean("turnitin.useSourceParameter", false);

		migrate = serverConfigurationService.getBoolean("turnitin.migrate", false);

		useGrademark = serverConfigurationService.getBoolean("turnitin.useGrademark", true);

		// Account notification options (default value is false)
		boolean sendAccountNotifications = (serverConfigurationService.getBoolean("turnitin.sendnotifications", false) ||
		                                   serverConfigurationService.getBoolean("turnitin.sendAccountNotifications", false));

		instructorAccountNotified = serverConfigurationService.getBoolean("turnitin.sendAccountNotifications.instructors", sendAccountNotifications);
		studentAccountNotified = serverConfigurationService.getBoolean("turnitin.sendAccountNotifications.student", sendAccountNotifications);

		// Submission notification options (default value is false)
		sendSubmissionNotification = serverConfigurationService.getBoolean("turnitin.sendSubmissionNotifications", false) ? 1 : 0;

		log.debug("Notification options:" + 
		         " instructorAccountNotified=" + instructorAccountNotified +
		         " studentAccountNotified=" + studentAccountNotified + 
			 " sendSubmissionNotification=" + sendSubmissionNotification);

		//note that the assignment id actually has to be unique globally so use this as a prefix
		// assignid = defaultAssignId + siteId
		defaultAssignId = serverConfigurationService.getString("turnitin.defaultAssignId");;

		defaultClassPassword = serverConfigurationService.getString("turnitin.defaultClassPassword","changeit");;

		defaultInstructorId = serverConfigurationService.getString("turnitin.defaultInstructorId","admin");

		maxRetry = Long.valueOf(serverConfigurationService.getInt("turnitin.maxRetry",100));

		// Timeout period in ms for network connections (default 180s). Set to 0 to disable timeout.
		turnitinConnTimeout = serverConfigurationService.getInt("turnitin.networkTimeout", 180000);

	}

	/*
	 * Utility Methods below
	 */

	/**
	 * Get's a Map of TII options that are the same for every one of these
	 * calls. Things like encrpyt and diagnostic.
	 *
	 * This can be used as well for changing things dynamically and testing.
	 *
	 * @return
	 */
	public Map getBaseTIIOptions() {
		String diagnostic = "0"; //0 = off; 1 = on
		String encrypt = "0"; //encryption flag

		Map togo = TurnitinAPIUtil.packMap(null,
				"diagnostic", diagnostic,
				"encrypt", encrypt,
				"said", said,
				"aid", aid
		);

		if (useSourceParameter || migrate) {
			togo.put("src", "9");
		}

		return togo;
	}

	public Map callTurnitinReturnMap(Map params) throws TransientSubmissionException, SubmissionException {
		return TurnitinAPIUtil.callTurnitinReturnMap(apiURL, params, secretKey, turnitinConnTimeout, proxy);
	}

	public Document callTurnitinReturnDocument(Map params) throws TransientSubmissionException, SubmissionException {
		return TurnitinAPIUtil.callTurnitinReturnDocument(apiURL, params, secretKey, turnitinConnTimeout, proxy, false);
	}

	public Document callTurnitinReturnDocument(Map params, boolean multiPart) throws TransientSubmissionException, SubmissionException {
		return TurnitinAPIUtil.callTurnitinReturnDocument(apiURL, params, secretKey, turnitinConnTimeout, proxy, multiPart);
	}

	public Map callTurnitinWDefaultsReturnMap(Map params) throws SubmissionException, TransientSubmissionException {
		params.putAll(getBaseTIIOptions());
		return TurnitinAPIUtil.callTurnitinReturnMap(apiURL, params, secretKey, turnitinConnTimeout, proxy);
	}

	public InputStream callTurnitinWDefaultsReturnInputStream(Map params) throws SubmissionException, TransientSubmissionException {
		params.putAll(getBaseTIIOptions());
		return TurnitinAPIUtil.callTurnitinReturnInputStream(apiURL, params, secretKey, turnitinConnTimeout, proxy, false);
	}

	public Document callTurnitinWDefaultsReturnDocument(Map params) throws SubmissionException, TransientSubmissionException {
		params.putAll(getBaseTIIOptions());
		return TurnitinAPIUtil.callTurnitinReturnDocument(apiURL, params, secretKey, turnitinConnTimeout, proxy, false);
	}

	public String buildTurnitinURL(Map params) {
		return TurnitinAPIUtil.buildTurnitinURL(apiURL, params, secretKey);
	}


	/*
	 * Dependency Getters/Setters Below
	 */
	public boolean isUseSourceParameter() {
		return useSourceParameter;
	}

	public void setUseSourceParameter(boolean useSourceParameter) {
		this.useSourceParameter = useSourceParameter;
	}

	public boolean getUseGradeMark() {
		return useGrademark;
	}

	public void setUseGradeMark(boolean useGrademark) {
		this.useGrademark = useGrademark;
	}

	public boolean getMigrateSRC() {
		return migrate;
	}

	public void setMigrateSRC(boolean migrate) {
		this.migrate = migrate;
	}
        
	// Dependency
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService (ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public boolean isStudentAccountNotified() {
		return studentAccountNotified;
	}

	public void setStudentAccountNotified(boolean studentAccountNotified) {
		this.studentAccountNotified = studentAccountNotified;
	}

	public int getSendSubmissionNotification() {
		return sendSubmissionNotification;
	}

	public void setSendSubmissionNotification(int sendSubmissionNotification) {
		this.sendSubmissionNotification = sendSubmissionNotification;
	}

	public Long getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(Long maxRetry) {
		this.maxRetry = maxRetry;
	}

	public String getDefaultAssignId() {
		return defaultAssignId;
	}

	public void setDefaultAssignId(String defaultAssignId) {
		this.defaultAssignId = defaultAssignId;
	}

	public String getDefaultClassPassword() {
		return defaultClassPassword;
	}

	public void setDefaultClassPassword(String defaultClassPassword) {
		this.defaultClassPassword = defaultClassPassword;
	}

	public boolean isInstructorAccountNotified() {
		return instructorAccountNotified;
	}

	public void setInstructorAccountNotified(boolean instructorAccountNotified) {
		this.instructorAccountNotified = instructorAccountNotified;
	}

  	public String getDefaultInstructorEmail() {
		return defaultInstructorEmail;
	}

  	public String getDefaultInstructorFName() {
		return defaultInstructorFName;
	}

  	public String getDefaultInstructorLName() {
		return defaultInstructorLName;
	}

  	public String getDefaultInstructorId() {
		return defaultInstructorId;
	}
}
