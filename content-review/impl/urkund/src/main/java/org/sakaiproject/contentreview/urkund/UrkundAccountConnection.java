/**********************************************************************************
 *
 * Copyright (c) 2017 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.contentreview.urkund;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.contentreview.urkund.util.UrkundAPIUtil;

/**
 * This class contains the properties and utility methods so it can be used to
 * make API calls and connections to a specific Urkund Account.
 *
 * Ideally you can make several of these in a single Sakai System in the event
 * that you need to use different different Urkund Accounts for different
 * tools or provisioned user spaces (such as different campuses, etc).
 *
 * A large portion of this was factored out of UrkundReviewService where it
 * originally occurred.
 *
 *
 */
@Slf4j
public class UrkundAccountConnection {
	private final static String DEFAULT_API_URL = "https://secure.urkund.com/api/";
	private final static int DEFAULT_TIMEOUT = 180000;

	private String apiURL = "";
	private String receiverAddress = "";
	private String username = null;
	private String password = null;
	private int connTimeout = 0; // Default to 0, no timeout.

	public void init() {

		log.info("init()");

		receiverAddress = serverConfigurationService.getString("urkund.address");
		username = serverConfigurationService.getString("urkund.username");
		password = serverConfigurationService.getString("urkund.password");

		apiURL = serverConfigurationService.getString("urkund.apiURL", DEFAULT_API_URL);

		// Timeout period in ms for network connections (default 180s). Set to 0 to disable timeout.
		connTimeout = serverConfigurationService.getInt("urkund.networkTimeout", DEFAULT_TIMEOUT);

	}

	/*
	 * Utility Methods below
	 */	
	//TODO : let the caller fill the UrkundSubmission to avoid extra parameters in this method
	public UrkundSubmissionData uploadFile(String submitterEmail, String externalId, String filename, byte[] filecontent, String mimeType) {
		UrkundSubmission submission = new UrkundSubmission();
		submission.setFilename(filename);
		submission.setMimeType(mimeType);
		submission.setContent(filecontent);
		submission.setSubmitterEmail(submitterEmail);
		submission.setSubject(""); // Insert site name?
		submission.setMessage(""); // Insert assignment title?
		submission.setAnon(false);
        
		String jsonResponse = UrkundAPIUtil.postDocument(apiURL, receiverAddress, externalId, submission, username, password, connTimeout);
		return getSubmissionData(externalId, jsonResponse);
	}
	public List<UrkundSubmissionData> getReports(String externalId) {
		String jsonResponse = UrkundAPIUtil.getFileInfo(apiURL, externalId, receiverAddress, username, password, connTimeout);
		return getSubmissionDataList(jsonResponse);
	}
	
	private UrkundSubmissionData getSubmissionData(String externalId, String jsonResponse){
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode root = mapper.readTree(jsonResponse);
			//this should not happen
			if(root.isArray() && root.size() > 0) {
				root = root.get(0);
			}
			UrkundSubmissionData ret = mapper.treeToValue(root, UrkundSubmissionData.class);
			if(ret.getExternalId() != null && ret.getExternalId().equals(externalId)){
				return ret;
			}
		} catch (JsonProcessingException e) {
			log.error("Error getting submission data : JsonProcessingException", e);
		} catch (IOException e) {
			log.error("Error getting submission data : IOException", e);
		} catch (Exception e) {
			log.error("Error getting submission data : Exception", e);
		}
		return null;
	}
	
	private List<UrkundSubmissionData> getSubmissionDataList(String jsonResponse){
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode root = mapper.readTree(jsonResponse);
			UrkundSubmissionData[] ret = mapper.treeToValue(root, UrkundSubmissionData[].class);
			return new ArrayList<UrkundSubmissionData>(Arrays.asList(ret));
		} catch (JsonProcessingException e) {
			log.error("Error getting submission data list : JsonProcessingException", e);
		} catch (IOException e) {
			log.error("Error getting submission data list : IOException", e);
		} catch (Exception e) {
			log.error("Error getting submission data list : Exception", e);
		}
		return null;
	}
	
	// Dependency
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService (ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

}
