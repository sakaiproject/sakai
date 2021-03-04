/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.meetings.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.meetings.api.BBBEndpoint;
import org.sakaiproject.meetings.api.MeetingsException;
import org.sakaiproject.meetings.api.persistence.Meeting;
import org.sakaiproject.meetings.api.MeetingsService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for interacting with any BigBlueButton API version.
 *
 * @author Nuno Fernandes
 */
@Slf4j
@Getter
public class BBBEndpointImpl implements BBBEndpoint {

    // API Server Path
    private final static String API_SERVERPATH = "/api/";

    // API Calls
    private final static String APICALL_CREATE = "create";
    private final static String APICALL_ISMEETINGRUNNING = "isMeetingRunning";
    private final static String APICALL_GETMEETINGINFO = "getMeetingInfo";
    private final static String APICALL_JOIN = "join";
    private final static String APICALL_END = "end";
    private final static String APICALL_VERSION = "";
    private final static String APICALL_GETRECORDINGS = "getRecordings";
    private final static String APICALL_PUBLISHRECORDINGS = "publishRecordings";
    private final static String APICALL_PROTECTRECORDINGS = "updateRecordings";
    private final static String APICALL_DELETERECORDINGS = "deleteRecordings";
    private final static String DEFAULT_BBB_URL = "http://test-install.blindsidenetworks.com/bigbluebutton";
    private final static String DEFAULT_BBB_SALT = "8cd8ef52e8e101574e400365b55e11a6";

    // API Response Codes
    private final static String APIRESPONSE_SUCCESS = "SUCCESS";
    private final static String APIRESPONSE_FAILED = "FAILED";

    // API Versions
    public final static String APIVERSION_063 = "0.63";
    public final static String APIVERSION_064 = "0.64";
    public final static String APIVERSION_070 = "0.70";
    public final static String APIVERSION_080 = "0.80";
    public final static String APIVERSION_081 = "0.81";
    public final static String APIVERSION_MINIMUM = APIVERSION_063;
    public final static String APIVERSION_LATEST = APIVERSION_081;

    private String baseUrl = "http://127.0.0.1/bigbluebutton";
    private String salt = null;
    private Random randomGenerator = new Random(System.currentTimeMillis());

    @Resource private ServerConfigurationService config;
    @Resource private ContentHostingService contentHostingService;
    @Resource private SecurityService securityService;

    public void init() {

        String baseUrlString = config.getString(MeetingsService.CFG_URL, DEFAULT_BBB_URL);
        if (StringUtils.isBlank(baseUrlString)) {
            log.warn("No BigBlueButton server specified. The bbb.url property in sakai.properties must be set to a single url. There should be a corresponding shared secret value in the bbb.salt property.");
            return;
        }

        String saltString = config.getString(MeetingsService.CFG_SALT, DEFAULT_BBB_SALT);
        if (StringUtils.isBlank(saltString)) {
            log.warn("BigBlueButton shared secret was not specified! Use 'bbb.salt = your_bbb_shared_secret' in sakai.properties.");
            return;
        }

        // Clean Url.
        baseUrl = baseUrlString.substring(baseUrlString.length() - 1, baseUrlString.length()).equals("/") ? baseUrlString : baseUrlString + "/";
        salt = saltString;
    }

    // -----------------------------------------------------------------------
    // --- BBB API implementation methods ------------------------------------
    // -----------------------------------------------------------------------
    /** Create a meeting on BBB server */
    public Meeting createMeeting(final Meeting meeting, boolean autoclose, boolean recordingenabled,
            boolean recordingreadynotification, boolean preuploadpresentation) throws MeetingsException {

        try {
            // build query
            StringBuilder query = new StringBuilder();
            query.append("meetingID=");
            query.append(URLEncoder.encode(meeting.getId(), getParametersEncoding()));
            query.append("&name=");
            query.append(URLEncoder.encode(meeting.getName(), getParametersEncoding()));
            query.append("&voiceBridge=");
            query.append(meeting.getVoiceBridge());
            query.append("&attendeePW=");
            String attendeePW = meeting.getAttendeePassword();
            query.append(attendeePW);
            query.append("&moderatorPW=");
            String moderatorPW = meeting.getModeratorPassword();
            query.append(moderatorPW);
            if (autoclose) {
                query.append("&logoutURL=");
                StringBuilder logoutUrl = new StringBuilder(config.getServerUrl());
                logoutUrl.append(MeetingsService.TOOL_WEBAPP);
                logoutUrl.append("/bbb-autoclose.html");
                query.append(URLEncoder.encode(logoutUrl.toString(), getParametersEncoding()));
            }

            Map<String, String> properties = meeting.getProperties();

            String disablePublicChat = properties.getOrDefault("disablePublicChat", Boolean.FALSE.toString());
            query.append("&lockSettingsDisablePublicChat=" + disablePublicChat);

            String disablePrivateChat = properties.getOrDefault("disablePrivateChat", Boolean.FALSE.toString());
            query.append("&lockSettingsDisablePrivateChat=" + disablePrivateChat);

            // BSN: Parameters required for playback recording
            boolean recording = ( recordingenabled && meeting.getRecording() != null && meeting.getRecording().booleanValue() );
            query.append("&record=" + Boolean.toString(recording));
            query.append("&duration=");
            String duration = meeting.getRecordingDuration() != null? meeting.getRecordingDuration().toString(): "0";
            query.append(duration);

            // BSN: Parameters added for monitoring and recording search
            for(Entry<String, String> entry : meeting.getMeta().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                query.append("&meta_" + key + "=");
                query.append(URLEncoder.encode(value, getParametersEncoding()));
            }
            // BSN: Ends

            // Composed Welcome message
            ResourceLoader toolMessages = new ResourceLoader("meetings");
            String welcomeMessage = toolMessages.getFormattedMessage("meetings_welcome_message_opening", new Object[] { "<b>%%CONFNAME%%</b>" } );
            String welcomeDescription = meeting.getProperties().get("welcomeMessage");
            if (!"<br />".equals(welcomeDescription)) {
                welcomeMessage += "<br><br>" + welcomeDescription;
            }

            welcomeMessage += "<br><br>" + toolMessages.getFormattedMessage("meetings_welcome_message_general_info", new Object[] {toolMessages.getString("meetings_welcome_message_external_link"), "%%DIALNUM%%", "%%CONFNUM%%"} );

            if (recording) {
                welcomeMessage += "<br><br><b>" + toolMessages.getFormattedMessage("meetings_welcome_message_recording_warning", new Object[] {} ) + "</b>";
            }
            if (duration.compareTo("0") > 0) {
                welcomeMessage += "<br><br><b>" + toolMessages.getFormattedMessage("meetings_welcome_message_duration_warning", new Object[] { duration });
            }

            if (recording && recordingreadynotification) {
                query.append("&meta_bn-recording-ready-url=");
                StringBuilder recordingReadyUrl = new StringBuilder(config.getServerUrl());
                recordingReadyUrl.append("/direct");
                recordingReadyUrl.append(MeetingsService.TOOL_WEBAPP);
                recordingReadyUrl.append("/recordingReady");
                query.append(URLEncoder.encode(recordingReadyUrl.toString(), getParametersEncoding()));
            }

            query.append("&welcome=");
            query.append(URLEncoder.encode(welcomeMessage, getParametersEncoding()));
            query.append(getCheckSumParameterForQuery(APICALL_CREATE, query.toString()));

            SecurityAdvisor sa = editResourceSecurityAdvisor();
            // Preupload presentation.
            String presentationXML = "";
            String presentation = "";
            String presentationName = meeting.getPresentation();
            if (preuploadpresentation && presentationName != null && !presentationName.isEmpty()) {
                presentation = URLDecoder.decode(presentationName.substring(presentationName.indexOf("/attachment")), "UTF-8");
                securityService.pushAdvisor(sa);
                // Open access to resource used as preuploaded presentation.
                contentHostingService.setPubView(presentation, true);
                // Set XML body.
                StringBuilder presentationUrl = new StringBuilder(config.getServerUrl());
                presentationUrl.append(presentationName);
                presentationXML = "<?xml version='1.0' encoding='UTF-8'?><modules><module name=\"presentation\"><document url=\"" + presentationUrl + "\" /></module></modules>";
            }
            // Do API call.
            Map<String, Object> response = doAPICall(APICALL_CREATE, query.toString(), presentationXML);
            // Close access to resource used as preuploaded presentation.
            if (presentation != "") {
                contentHostingService.setPubView(presentation, false);
            }
        } catch (MeetingsException e) {
            throw e;
        } catch (UnsupportedEncodingException e) {
            throw new MeetingsException(MeetingsException.MESSAGEKEY_INTERNALERROR, e.getMessage(), e);
        }
        return meeting;
    }

    private SecurityAdvisor editResourceSecurityAdvisor() {

        return (userId, function, reference) -> {
            return SecurityAdvisor.SecurityAdvice.ALLOWED;
        };
    }

    /** Check if meeting is running on BBB server. */
    public boolean isMeetingRunning(String meetingID) throws MeetingsException {

        try {
            StringBuilder query = new StringBuilder();
            query.append("meetingID=");
            query.append(URLEncoder.encode(meetingID, getParametersEncoding()));
            query.append(getCheckSumParameterForQuery(APICALL_ISMEETINGRUNNING, query.toString()));

            Map<String, Object> response = doAPICall(APICALL_ISMEETINGRUNNING, query.toString());
            return Boolean.parseBoolean((String) response.get("running"));
        } catch (Exception e) {
            throw new MeetingsException(MeetingsException.MESSAGEKEY_INTERNALERROR, e.getMessage(), e);
        }
    }

    public Map<String, Object> getMeetingInfo(String meetingID, String password) throws MeetingsException {

        log.debug("getMeetingInfo({}, ***)", meetingID);

        try {
            StringBuilder query = new StringBuilder();
            query.append("meetingID=");
            query.append(URLEncoder.encode(meetingID, getParametersEncoding()));
            query.append("&password=");
            query.append(password);
            query.append(getCheckSumParameterForQuery(APICALL_GETMEETINGINFO, query.toString()));
            Map<String, Object> response = doAPICall(APICALL_GETMEETINGINFO, query.toString());
            // nullify password fields
            for (String key : response.keySet()) {
                if ("attendeePW".equals(key) || "moderatorPW".equals(key)) {
                    response.put(key, null);
                }
            }
            return response;
        } catch (MeetingsException e) {
            log.debug("getMeetingInfo.Exception: MessageKey=" + e.getMessageKey() + ", Message=" + e.getMessage() );
            throw new MeetingsException(e.getMessageKey(), e.getMessage(), e);
        } catch (Exception e) {
            log.debug("Exception: Message=" + e.getMessage() );
            throw new MeetingsException(MeetingsException.MESSAGEKEY_INTERNALERROR, e.getMessage(), e);
        }
    }

    /** Get recordings from BBB server */
    public Map<String, Object> getRecordings(String meetingID) throws MeetingsException {

        log.debug("getRecordings({})", meetingID);

        try {
            // Paginate queries for fetching recordings.
            List<String> meetingIDs = Arrays.asList(meetingID.split("\\s*,\\s*"));
            int pages = meetingIDs.size() / 25 + 1;
            // Fetch recordings in pages.
            List<Object> recordings = new ArrayList<>();
            int fromIndex, toIndex;
            for (int page = 1; page <= pages; ++page) {
                fromIndex = (page - 1) * 25;
                toIndex = page * 25;
                if (toIndex > meetingIDs.size()) {
                    toIndex = meetingIDs.size();
                }
                List<String> subMeetingIDs = meetingIDs.subList(fromIndex, toIndex);
                recordings.addAll(getRecordings(subMeetingIDs));
            }
            // Prepare and return response with recordings.
            Map<String, Object> response = new HashMap<>();
            response.put("returncode", "SUCCESS");
            response.put("recordings", recordings);
            return response;
        } catch (MeetingsException e) {
            log.error("getRecordings.Exception: MessageKey={}, Message={}", e.getMessageKey(), e.getMessage());
            throw new MeetingsException(e.getMessageKey(), e.getMessage(), e);
        }
    }

    /** Get recordings from BBB server */
    protected List<Object> getRecordings(List<String> meetingIds) throws MeetingsException {

        if (log.isDebugEnabled()) meetingIds.forEach(log::debug);

        try {
            if (!meetingIds.isEmpty()) {
                String meetingID = String.join(",", meetingIds);
                StringBuilder query = new StringBuilder();
                query.append("meetingID=");
                query.append(URLEncoder.encode(meetingID, getParametersEncoding()));
                query.append(getCheckSumParameterForQuery(APICALL_GETRECORDINGS, query.toString()));
                Map<String, Object> response = doAPICall(APICALL_GETRECORDINGS, query.toString());
                // Make sure that the date retrived is a unix timestamp.
                if (response.get("returncode").equals("SUCCESS") && response.get("messageKey") == null) {
                    for (Map<String, String> items : (List<Map<String, String>>) response.get("recordings")) {
                        items.put("startTime", items.get("startTime"));
                        items.put("endTime", items.get("endTime"));
                    }
                    return (List<Object>) response.get("recordings");
                }
            }
        } catch (MeetingsException e) {
            log.debug("getRecordings.Exception: MessageKey=" + e.getMessageKey() + ", Message=" + e.getMessage() );
            throw new MeetingsException(e.getMessageKey(), e.getMessage(), e);
        } catch (Exception e) {
            log.debug("Exception: Message=" + e.getMessage() );
            throw new MeetingsException(MeetingsException.MESSAGEKEY_INTERNALERROR, e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    /** End/delete a meeting on BBB server */
    public boolean endMeeting(String meetingID, String password) throws MeetingsException {

        log.debug("endMeeting({}, ****)", meetingID);

        StringBuilder query = new StringBuilder();
        try {
            query.append("meetingID=");
            query.append(URLEncoder.encode(meetingID, getParametersEncoding()));
            query.append("&password=");
            query.append(password);
            query.append(getCheckSumParameterForQuery(APICALL_END, query.toString()));
            doAPICall(APICALL_END, query.toString());
        } catch (MeetingsException e) {
            if (MeetingsException.MESSAGEKEY_NOTFOUND.equals(e.getMessageKey())) {
                // we can safely ignore this one: the meeting is not running
                return true;
            }
            throw e;
        } catch (Exception e) {
            log.debug("Exception: Message=" + e.getMessage() );
            throw new MeetingsException(MeetingsException.MESSAGEKEY_INTERNALERROR, e.getMessage(), e);
        }
        return true;
    }

    /** Delete a recording on BBB server */
    public boolean deleteRecordings(String recordID) throws MeetingsException {

        log.debug("deletedRecordings({})", recordID);

        StringBuilder query = new StringBuilder();
        query.append("recordID=");
        query.append(recordID);
        query.append(getCheckSumParameterForQuery(APICALL_DELETERECORDINGS, query.toString()));

        try {
            doAPICall(APICALL_DELETERECORDINGS, query.toString());
        } catch (MeetingsException e) {
            throw e;
        }

        return true;
    }

    /** Publish/Unpublish a recording on BBB server */
    public boolean publishRecordings(String recordID, String publish) throws MeetingsException {

        log.debug("publishRecordings({}, {})", recordID, publish);

        StringBuilder query = new StringBuilder();
        query.append("recordID=");
        query.append(recordID);
        query.append("&publish=");
        query.append(publish);
        query.append(getCheckSumParameterForQuery(APICALL_PUBLISHRECORDINGS, query.toString()));

        try {
            doAPICall(APICALL_PUBLISHRECORDINGS, query.toString());
        } catch (MeetingsException e) {
            throw e;
        }

        return true;
    }

    /** Protect/Unprotect a recording on BBB server */
    public boolean protectRecordings(String recordID, String protect) throws MeetingsException {

        log.debug("protectRecordings({}, {})", recordID, protect);

        StringBuilder query = new StringBuilder();
        query.append("recordID=");
        query.append(recordID);
        query.append("&protect=");
        query.append(protect);
        query.append(getCheckSumParameterForQuery(APICALL_PROTECTRECORDINGS, query.toString()));

        try {
            doAPICall(APICALL_PROTECTRECORDINGS, query.toString());
        } catch (MeetingsException e) {
            throw e;
        }

        return true;
    }

    /** Build the join meeting url based on user role */
    public String getJoinMeetingURL(String meetingID, String userId, String userDisplayName, String password) {

        log.debug("getJoinMeetingURL({}, {}, {}, ****)", meetingID, userId, userDisplayName);

        StringBuilder query = new StringBuilder();
        try {
            query.append("meetingID=");
            query.append(URLEncoder.encode(meetingID, getParametersEncoding()));
            if (userId != null) {
                query.append("&userID=");
                query.append(URLEncoder.encode(userId, getParametersEncoding()));
            }
        } catch (UnsupportedEncodingException e) {
        }
        query.append("&fullName=");
        if (userDisplayName == null) {
            userDisplayName = "user";
        }
        try {
            query.append(URLEncoder.encode(userDisplayName, getParametersEncoding()));
        } catch (UnsupportedEncodingException e) {
            query.append(userDisplayName);
        }
        query.append("&password=");
        query.append(password);
        query.append(getCheckSumParameterForQuery(APICALL_JOIN, query.toString()));

        StringBuilder url = new StringBuilder(baseUrl);
        if (url.toString().endsWith("/api")) {
            url.append("/");
        } else {
            url.append(API_SERVERPATH);
        }
        url.append(APICALL_JOIN);
        url.append("?");
        url.append(query);

        return url.toString();
    }

    /** Make sure the meeting (still) exists on BBB server */
    public void makeSureMeetingExists(Meeting meeting, boolean autoclose, boolean recordingenabled, boolean recordingreadynotification, boolean preuploadpresentation)
            throws MeetingsException {
        createMeeting(meeting, autoclose, recordingenabled, recordingreadynotification, preuploadpresentation);
    }

    /** Get the BBB API version running on BBB server */
    public final String getAPIVersion() {

        log.debug("getAPIVersion()");

        String version = null;
        try {
            Map<String, Object> response = doAPICall(APICALL_VERSION, null);
            version = (String) response.get("version");
            version = version != null ? version.trim() : null;
            if (version == null || Float.valueOf(version.substring(0, 3)) < 0.0) {
                log.warn("Invalid BigBlueButton version ({})", version);
                version = null;
            }
            version = version.trim();
        } catch (MeetingsException e) {
            if (MeetingsException.MESSAGEKEY_NOACTION.equals(e.getMessageKey())) {
                // we are clearly connecting to BBB < 0.70 => assuming minimum
                // version (0.63)
                version = APIVERSION_MINIMUM;
            } else {
                // something went wrong => warn user
                log.warn("Unable to check BigBlueButton version: " + e.getMessage());
                version = null;
            }
        } catch (Exception e) {
            // something went wrong => warn user
            log.warn("Unable to check BigBlueButton version: " + e.getMessage());
            version = null;
        }

        return version;
    }

    // -----------------------------------------------------------------------
    // --- BBB API utility methods -------------------------------------------
    // -----------------------------------------------------------------------
    /** Compute the query string checksum based on the security salt */
    protected String getCheckSumParameterForQuery(String apiCall, String queryString) {

        if (salt != null) {
            return "&checksum=" + DigestUtils.shaHex(apiCall + queryString + salt);
        } else {
            return "";
        }
    }

    /** Encoding used when encoding url parameters */
    protected String getParametersEncoding() {
        return "UTF-8";
    }

    protected Map<String, Object> doAPICall(String apiCall, String query) throws MeetingsException {
        return doAPICall(apiCall, query, "");
    }

    /** Make an API call */
    protected Map<String, Object> doAPICall(String apiCall, String query, String presentation)
            throws MeetingsException {

        StringBuilder urlStr = new StringBuilder(baseUrl);
        if (urlStr.toString().endsWith("/api")){
            urlStr.append("/");
        } else {
            urlStr.append(API_SERVERPATH);
        }
        urlStr.append(apiCall);
        if (query != null) {
            urlStr.append("?");
            urlStr.append(query);
        }

        try {
            // open connection
            log.debug("doAPICall.call: " + apiCall + "?" + (query != null ? query : ""));

            URL url = new URL(urlStr.toString());
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setUseCaches(false);
            httpConnection.setDoOutput(true);
            if(presentation != ""){
                httpConnection.setRequestMethod("POST");
                httpConnection.setRequestProperty("Content-Type", "text/xml");
                httpConnection.setRequestProperty("Content-Length", "" + Integer.toString(presentation.getBytes().length));
                httpConnection.setRequestProperty("Content-Language", "en-US");
                httpConnection.setDoInput(true);

                DataOutputStream wr = new DataOutputStream( httpConnection.getOutputStream() );
                wr.writeBytes (presentation);
                wr.flush();
                wr.close();
            } else {
                httpConnection.setRequestMethod("GET");
            }
            httpConnection.connect();

            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // read response
                String stringXml = "";
                try (
                    InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                ) {
                    stringXml = reader.lines().filter(l -> !l.startsWith("<?xml version=\"1.0\"?>"))
                        .map(String::trim).collect(Collectors.joining());
                }
                httpConnection.disconnect();

                // parse response
                log.debug("doAPICall.response: {}",  stringXml);
                //Patch to fix the NaN error
                stringXml = stringXml.replaceAll(">.\\s+?<", "><");

                Document dom = null;

                // Initialize XML libraries
                DocumentBuilderFactory docBuilderFactory;
                DocumentBuilder docBuilder;
                docBuilderFactory = DocumentBuilderFactory.newInstance();
                try {
                    docBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    docBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

                    docBuilder = docBuilderFactory.newDocumentBuilder();

                    dom = docBuilder.parse(new InputSource( new StringReader(stringXml)));
                } catch (ParserConfigurationException e) {
                    log.error("Failed to initialise XML Parser", e);
                }

                Map<String, Object> response = getNodesAsMap(dom, "response");

                String returnCode = (String) response.get("returncode");
                if (APIRESPONSE_FAILED.equals(returnCode)) {
                    throw new MeetingsException((String) response.get("messageKey"), (String) response.get("message"));
                }

                return response;

            } else {
                throw new MeetingsException(MeetingsException.MESSAGEKEY_HTTPERROR, "BBB server responded with HTTP status code " + responseCode);
            }

        } catch (MeetingsException e) {
            if (!e.getMessageKey().equals("notFound")) {
                log.debug("doAPICall.MeetingsException: MessageKey=" + e.getMessageKey() + ", Message=" + e.getMessage());
            }
            throw new MeetingsException( e.getMessageKey(), e.getMessage(), e);
        } catch (IOException e) {
            log.debug("doAPICall.IOException: Message=" + e.getMessage());
            throw new MeetingsException(MeetingsException.MESSAGEKEY_UNREACHABLE, e.getMessage(), e);

        } catch (SAXException e) {
            log.debug("doAPICall.SAXException: Message=" + e.getMessage());
            throw new MeetingsException(MeetingsException.MESSAGEKEY_INVALIDRESPONSE, e.getMessage(), e);

        } catch (IllegalArgumentException e) {
            log.debug("doAPICall.IllegalArgumentException: Message=" + e.getMessage());
            throw new MeetingsException(MeetingsException.MESSAGEKEY_INVALIDRESPONSE, e.getMessage(), e);

        } catch (Exception e) {
            log.debug("doAPICall.Exception: Message=" + e.getMessage());
            throw new MeetingsException(MeetingsException.MESSAGEKEY_UNREACHABLE, e.getMessage(), e);
        }
    }

    protected Map<String, Object> getNodesAsMap(Document dom, String elementTagName) {
        return processNode(dom.getElementsByTagName(elementTagName).item(0));
    }

    protected Map<String, Object> processNode(Node _node) {

        Map<String, Object> map = new LinkedHashMap<>();
        NodeList responseNodes = _node.getChildNodes();
        int images = 1; //counter for images (i.e image1, image2, image3)
        for (int i = 0; i < responseNodes.getLength(); i++) {
            Node node = responseNodes.item(i);
            String nodeName = node.getNodeName().trim();
            if (node.getChildNodes().getLength() == 1
                    && ( node.getChildNodes().item(0).getNodeType() == org.w3c.dom.Node.TEXT_NODE || node.getChildNodes().item(0).getNodeType() == org.w3c.dom.Node.CDATA_SECTION_NODE) ) {
                String nodeValue = node.getTextContent();
                if (nodeName == "image" && node.getAttributes() != null){
                    Map<String, String> imageMap = new LinkedHashMap<String, String>();
                    Node heightAttr = node.getAttributes().getNamedItem("height");
                    Node widthAttr = node.getAttributes().getNamedItem("width");
                    Node altAttr = node.getAttributes().getNamedItem("alt");

                    imageMap.put("height", heightAttr.getNodeValue());
                    imageMap.put("width", widthAttr.getNodeValue());
                    imageMap.put("title", altAttr.getNodeValue());
                    imageMap.put("url", nodeValue);
                    map.put(nodeName + images, imageMap);
                    images++;
                } else {
                    map.put(nodeName, nodeValue != null ? nodeValue.trim() : null);
                }
            } else if (node.getChildNodes().getLength() == 0
                    && node.getNodeType() != org.w3c.dom.Node.TEXT_NODE
                    && node.getNodeType() != org.w3c.dom.Node.CDATA_SECTION_NODE) {
                map.put(nodeName, "");
            } else if ( node.getChildNodes().getLength() >= 1
                    && node.getChildNodes().item(0).getChildNodes().item(0).getNodeType() != org.w3c.dom.Node.TEXT_NODE
                    && node.getChildNodes().item(0).getChildNodes().item(0).getNodeType() != org.w3c.dom.Node.CDATA_SECTION_NODE ) {

                List<Object> list = new ArrayList<>();
                for (int c = 0; c < node.getChildNodes().getLength(); c++) {
                    Node n = node.getChildNodes().item(c);
                    list.add(processNode(n));
                }
                if (nodeName == "preview"){
                    Node n = node.getChildNodes().item(0);
                    map.put(nodeName, new ArrayList<Object>(processNode(n).values()));
                } else {
                    map.put(nodeName, list);
                }
            } else {
                map.put(nodeName, processNode(node));
            }
        }
        return map;
    }

    /** Generate a random password */
    protected String generatePassword() {
        return Long.toHexString(randomGenerator.nextLong());
    }
}
