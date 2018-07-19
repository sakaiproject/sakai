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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.advisors.ContentReviewSiteAdvisor;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.service.BaseContentReviewService;
import org.sakaiproject.contentreview.service.ContentReviewQueueService;
import org.sakaiproject.contentreview.turnitin.util.TurnitinAPIUtil;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TurnitinReviewServiceImpl extends BaseContentReviewService {

	public static final String TURNITIN_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String SERVICE_NAME = "Turnitin";

	// Site property to enable or disable use of Turnitin for the site
	private static final String TURNITIN_SITE_PROPERTY = "turnitin";

	final static long LOCK_PERIOD = 12000000;

	private boolean studentAccountNotified = true;

	private int sendSubmissionNotification = 0;

	private Long maxRetry = null;

	// note that the assignment id actually has to be unique globally so use
	// this as a prefix
	// eg. assignid = defaultAssignId + siteId
	private String defaultAssignId = null;

	private String defaultClassPassword = null;

	private List<String> enabledSiteTypes;

	// Define Turnitin's acceptable file extensions and MIME types, order of these arrays DOES matter
	private final String[] DEFAULT_ACCEPTABLE_FILE_EXTENSIONS = new String[] {
		".doc",
		".docx",
		".xls",
		".xls",
		".xls",
		".xls",
		".xlsx",
		".ppt",
		".ppt",
		".ppt",
		".ppt",
		".pptx",
		".pps",
		".pps",
		".ppsx",
		".pdf",
		".ps",
		".eps",
		".txt",
		".html",
		".htm",
		".wpd",
		".wpd",
		".odt",
		".rtf",
		".rtf",
		".rtf",
		".rtf",
		".hwp"
	};
	private final String[] DEFAULT_ACCEPTABLE_MIME_TYPES = new String[] {
		"application/msword",
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
		"application/excel",
		"application/vnd.ms-excel",
		"application/x-excel",
		"application/x-msexcel",
		"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
		"application/mspowerpoint",
		"application/powerpoint",
		"application/vnd.ms-powerpoint",
		"application/x-mspowerpoint",
		"application/vnd.openxmlformats-officedocument.presentationml.presentation",
		"application/mspowerpoint",
		"application/vnd.ms-powerpoint",
		"application/vnd.openxmlformats-officedocument.presentationml.slideshow",
		"application/pdf",
		"application/postscript",
		"application/postscript",
		"text/plain",
		"text/html",
		"text/html",
		"application/wordperfect",
		"application/x-wpwin",
		"application/vnd.oasis.opendocument.text",
		"text/rtf",
		"application/rtf",
		"application/x-rtf",
		"text/richtext",
		"application/x-hwp"
	};

	// Sakai.properties overriding the arrays above
	private final String PROP_ACCEPT_ALL_FILES = "turnitin.accept.all.files";

	private final String PROP_ACCEPTABLE_FILE_EXTENSIONS = "turnitin.acceptable.file.extensions";
	private final String PROP_ACCEPTABLE_MIME_TYPES = "turnitin.acceptable.mime.types";

	// A list of the displayable file types (ie. "Microsoft Word", "WordPerfect document", "Postscript", etc.)
	private final String PROP_ACCEPTABLE_FILE_TYPES = "turnitin.acceptable.file.types";

	private final String KEY_FILE_TYPE_PREFIX = "file.type";

	/**
	 * If set to true in properties, will result in 3 random digits being
	 * appended to the email name. In other words, adrian.r.fish@gmail.com will
	 * become something like adrian.r.fish593@gmail.com
	 */
	private boolean spoilEmailAddresses = false;

	/** Prefer system profile email addresses */
	private boolean preferSystemProfileEmail = true;

	/** Use guest account eids as email addresses */
	private boolean preferGuestEidEmail = true;

	@Setter
	private TurnitinAccountConnection turnitinConn;

	@Setter
	private EntityManager entityManager;

	@Setter
	private ContentHostingService contentHostingService;

	@Setter
	private SakaiPersonManager sakaiPersonManager;

	@Setter
	private UserDirectoryService userDirectoryService;

	@Setter
	private SiteService siteService;

	@Setter
	private PreferencesService preferencesService;

	@Setter
	private TurnitinContentValidator turnitinContentValidator;

	@Setter
	private GradebookService gradebookService;

	@Setter
	private GradebookExternalAssessmentService gradebookExternalAssessmentService;

	@Setter
	private SecurityService securityService;

	@Setter
	private SessionManager sessionManager;

	@Setter
	private ContentReviewSiteAdvisor siteAdvisor;
	
	@Setter
	private ToolManager toolManager;

	@Setter
	ContentReviewQueueService crqs;

	public void init() {

		studentAccountNotified = turnitinConn.isStudentAccountNotified();
		sendSubmissionNotification = turnitinConn.getSendSubmissionNotification();
		maxRetry = turnitinConn.getMaxRetry();
		defaultAssignId = turnitinConn.getDefaultAssignId();
		defaultClassPassword = turnitinConn.getDefaultClassPassword();

		spoilEmailAddresses = serverConfigurationService.getBoolean("turnitin.spoilEmailAddresses", false);
		preferSystemProfileEmail = serverConfigurationService.getBoolean("turnitin.preferSystemProfileEmail", true);
		preferGuestEidEmail = serverConfigurationService.getBoolean("turnitin.preferGuestEidEmail", true);

		enabledSiteTypes = Arrays
				.asList(ArrayUtils.nullToEmpty(serverConfigurationService.getStrings("turnitin.sitetypes")));

		log.info("init(): spoilEmailAddresses=" + spoilEmailAddresses + " preferSystemProfileEmail="
				+ preferSystemProfileEmail + " preferGuestEidEmail=" + preferGuestEidEmail);

		if (enabledSiteTypes != null && !enabledSiteTypes.isEmpty()) {
			log.info("Turnitin is enabled for site types: " + StringUtils.join(enabledSiteTypes, ","));
		}

		if (!turnitinConn.isUseSourceParameter()) {
			if (serverConfigurationService.getBoolean("turnitin.updateAssingments", false))
				doAssignments();
		}
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	/**
	 * Allow Turnitin for this site?
	 */
	@Override
	public boolean isSiteAcceptable(Site s) {

		if (s == null) {
			return false;
		}

		log.debug("isSiteAcceptable: " + s.getId() + " / " + s.getTitle());

		// Delegated to another bean
		if (siteAdvisor != null) {
			return siteAdvisor.siteCanUseReviewService(s);
		}

		// Check site property
		ResourceProperties properties = s.getProperties();

		String prop = (String) properties.get(TURNITIN_SITE_PROPERTY);
		if (prop != null) {
			log.debug("Using site property: " + prop);
			return Boolean.parseBoolean(prop);
		}

		// Check list of allowed site types, if defined
		if (enabledSiteTypes != null && !enabledSiteTypes.isEmpty()) {
			log.debug("Using site type: " + s.getType());
			return enabledSiteTypes.contains(s.getType());
		}

		// No property set, no restriction on site types, so allow
		return true;
	}

	public String getIconCssClassforScore(int score, String contentId) {
		if (score == 0) {
			return "contentReviewIconThreshold-5";
		} else if (score < 25) {
			return "contentReviewIconThreshold-4";
		} else if (score < 50) {
			return "contentReviewIconThreshold-3";
		} else if (score < 75) {
			return "contentReviewIconThreshold-2";
		} else {
			return "contentReviewIconThreshold-1";
		}
	}

	/**
	 * This uses the default Instructor information or current user.
	 *
	 * @see org.sakaiproject.contentreview.impl.BaseReviewServiceImpl#getReviewReportInstructor(java.lang.String)
	 */
	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {

		Optional<ContentReviewItem> matchingItem = crqs.getQueuedItem(getProviderId(), contentId);

		if (!matchingItem.isPresent()) {
			log.debug("Content " + contentId + " has not been queued previously");
			throw new QueueException("Content " + contentId + " has not been queued previously");
		}

		// check that the report is available
		// TODO if the database record does not show report available check with
		// turnitin (maybe)

		ContentReviewItem item = matchingItem.get();

		if (item.getStatus().compareTo(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
			log.debug("Report not available: " + item.getStatus());
			throw new ReportException("Report not available: " + item.getStatus());
		}

		// report is available - generate the URL to display

		String oid = item.getExternalId();
		String fid = "6";
		String fcmd = "1";
		String cid = item.getSiteId();
		String assignid = defaultAssignId + item.getSiteId();
		String utp = "2";

		Map params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "fid", fid, "fcmd", fcmd, "assignid",
				assignid, "cid", cid, "oid", oid, "utp", utp);

		params.putAll(getInstructorInfo(item.getSiteId()));

		return turnitinConn.buildTurnitinURL(params);
	}

	public String getReviewReportStudent(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {

		Optional<ContentReviewItem> matchingItem = crqs.getQueuedItem(getProviderId(), contentId);

		if (!matchingItem.isPresent()) {
			log.debug("Content " + contentId + " has not been queued previously");
			throw new QueueException("Content " + contentId + " has not been queued previously");
		}

		// check that the report is available
		// TODO if the database record does not show report available check with
		// turnitin (maybe)

		ContentReviewItem item = matchingItem.get();
		if (item.getStatus().compareTo(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
			log.debug("Report not available: " + item.getStatus());
			throw new ReportException("Report not available: " + item.getStatus());
		}

		// report is available - generate the URL to display

		String oid = item.getExternalId();
		String fid = "6";
		String fcmd = "1";
		String cid = item.getSiteId();
		String assignid = defaultAssignId + item.getSiteId();

		User user = userDirectoryService.getCurrentUser();

		// USe the method to get the correct email
		String uem = getEmail(user);
		String ufn = getUserFirstName(user);
		String uln = getUserLastName(user);
		String uid = item.getUserId();
		String utp = "1";

		Map params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "fid", fid, "fcmd", fcmd, "assignid",
				assignid, "uid", uid, "cid", cid, "oid", oid, "uem", uem, "ufn", ufn, "uln", uln, "utp", utp);

		return turnitinConn.buildTurnitinURL(params);
	}

	public String getReviewReport(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {

		// first retrieve the record from the database to get the externalId of
		// the content
		log.warn("Deprecated Methog getReviewReport(String contentId) called");
		return this.getReviewReportInstructor(contentId, assignmentRef, userId);
	}

	private Optional<ContentReviewItem> getItemByContentId(String contentId) {
		return crqs.getQueuedItem(getProviderId(), contentId);
	}

	/**
	 * Get additional data from String if available
	 * 
	 * @return array containing site ID, Task ID, Task Title
	 */
	private String[] getAssignData(String data) {
		String[] assignData = null;
		try {
			if (data.contains("#")) {
				assignData = data.split("#");
			}
		} catch (Exception e) {
		}
		return assignData;
	}

	public String getInlineTextId(String assignmentReference, String userId, long submissionTime) {
		return "";
	}

	public boolean acceptInlineAndMultipleAttachments() {
		return false;
	}

	public int getReviewScore(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException, Exception {
		ContentReviewItem item = null;
		try {
			Optional<ContentReviewItem> matchingItem = getItemByContentId(contentId);
			if (!matchingItem.isPresent()) {
				log.debug("Content " + contentId + " has not been queued previously");
			}
			item = matchingItem.get();
			if (item.getStatus().compareTo(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
				log.debug("Report not available: " + item.getStatus());
			}
		} catch (Exception e) {
			log.error("(getReviewScore)" + e);
		}

		String[] assignData = null;
		try {
			assignData = getAssignData(contentId);
		} catch (Exception e) {
			log.error("(assignData)" + e);
		}

		String siteId = "", taskId = "", taskTitle = "";
		Map<String, Object> data = new HashMap<String, Object>();
		if (assignData != null) {
			siteId = assignData[0];
			taskId = assignData[1];
			taskTitle = assignData[2];
		} else {
			siteId = item.getSiteId();
			taskId = item.getTaskId();
			taskTitle = getAssignmentTitle(taskId);
			data.put("assignment1", "assignment1");
		}
		// Sync Grades
		if (turnitinConn.getUseGradeMark()) {
			try {
				data.put("siteId", siteId);
				data.put("taskId", taskId);
				data.put("taskTitle", taskTitle);
				syncGrades(data);
			} catch (Exception e) {
				log.error("Error syncing grades. " + e);
			}
		}

		return item.getReviewScore().intValue();
	}

	/**
	 * Check if grade sync has been run already for the specified site
	 * 
	 * @param sess
	 *            Current Session
	 * @param taskId
	 * @return
	 */
	public boolean gradesChecked(Session sess, String taskId) {
		String sessSync = "";
		try {
			sessSync = sess.getAttribute("sync").toString();
			if (sessSync.equals(taskId)) {
				return true;
			}
		} catch (Exception e) {
			// log.error("(gradesChecked)"+e);
		}
		return false;
	}

	/**
	 * Check if the specified user has the student role on the specified site.
	 * 
	 * @param siteId
	 *            Site ID
	 * @param userId
	 *            User ID
	 * @return true if user has student role on the site.
	 */
	public boolean isUserStudent(String siteId, String userId) {
		boolean isStudent = false;
		try {
			Set<String> studentIds = siteService.getSite(siteId).getUsersIsAllowed("section.role.student");
			List<User> activeUsers = userDirectoryService.getUsers(studentIds);
			for (int i = 0; i < activeUsers.size(); i++) {
				User user = activeUsers.get(i);
				if (userId.equals(user.getId())) {
					return true;
				}
			}
		} catch (Exception e) {
			log.info("(isStudentUser)" + e);
		}
		return isStudent;
	}

	/**
	 * Return the Gradebook item associated with an assignment.
	 * 
	 * @param data
	 *            Map containing Site/Assignment IDs
	 * @return Associated gradebook item
	 */
	public Assignment getAssociatedGbItem(Map data) {
		Assignment assignment = null;
		String taskId = data.get("taskId").toString();
		String siteId = data.get("siteId").toString();
		String taskTitle = data.get("taskTitle").toString();

		pushAdvisor();
		try {
			List<Assignment> allGbItems = gradebookService.getAssignments(siteId);
			for (Assignment assign : allGbItems) {
				// Match based on External ID / Assignment title
				if (taskId.equals(assign.getExternalId()) || assign.getName().equals(taskTitle)) {
					assignment = assign;
					break;
				}
			}
		} catch (Exception e) {
			log.error("(allGbItems)" + e.toString());
		} finally {
			popAdvisor();
		}
		return assignment;
	}

	/**
	 * Check Turnitin for grades and write them to the associated gradebook
	 * 
	 * @param data
	 *            Map containing relevant IDs (site ID, Assignment ID, Title)
	 */
	public void syncGrades(Map<String, Object> data) {
		// Get session and check if gardes have already been synced
		Session sess = sessionManager.getCurrentSession();
		boolean runOnce = gradesChecked(sess, data.get("taskId").toString());
		boolean isStudent = isUserStudent(data.get("siteId").toString(), sess.getUserId());

		if (turnitinConn.getUseGradeMark() && runOnce == false && isStudent == false) {
			log.info("Syncing Grades with Turnitin");

			String siteId = data.get("siteId").toString();
			String taskId = data.get("taskId").toString();

			HashMap<String, Integer> reportTable = new HashMap<String, Integer>();
			HashMap<String, String> additionalData = new HashMap<String, String>();
			String tiiUserId = "";

			String assign = taskId;
			if (data.containsKey("assignment1")) {
				// Assignments 1 uses the actual title whereas Assignments 2
				// uses the ID
				assign = getAssignmentTitle(taskId);
			}

			// Run once
			sess.setAttribute("sync", taskId);

			// Get students enrolled on class in Turnitin
			Map<String, Object> enrollmentInfo = getAllEnrollmentInfo(siteId);

			// Get Associated GB item
			Assignment assignment = getAssociatedGbItem(data);

			// List submissions call
			Map params = new HashMap();
			params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "fid", "10", "fcmd", "2", "tem",
					getTEM(siteId), "assign", assign, "assignid", taskId, "cid", siteId, "ctl", siteId, "utp", "2");
			params.putAll(getInstructorInfo(siteId));

			Document document = null;
			try {
				document = turnitinConn.callTurnitinReturnDocument(params);
			} catch (TransientSubmissionException e) {
				log.error(e.getMessage());
			} catch (SubmissionException e) {
				log.warn("SubmissionException error. " + e.getMessage());
			}
			Element root = document.getDocumentElement();
			if (((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData().trim()
					.compareTo("72") == 0) {
				NodeList objects = root.getElementsByTagName("object");
				String grade = "";
				log.debug(objects.getLength() + " objects in the returned list");

				for (int i = 0; i < objects.getLength(); i++) {
					tiiUserId = ((CharacterData) (((Element) (objects.item(i))).getElementsByTagName("userid").item(0)
							.getFirstChild())).getData().trim();
					additionalData.put("tiiUserId", tiiUserId);
					// Get GradeMark Grade
					try {
						grade = ((CharacterData) (((Element) (objects.item(i))).getElementsByTagName("score").item(0)
								.getFirstChild())).getData().trim();
						reportTable.put("grade" + tiiUserId, Integer.valueOf(grade));
					} catch (Exception e) {
						// No score returned
						grade = "";
					}

					if (!grade.equals("")) {
						// Update Grade ----------------
						if (gradebookService.isGradebookDefined(siteId)) {
							writeGrade(assignment, data, reportTable, additionalData, enrollmentInfo);
						}
					}
				}
			} else {
				log.debug("Report list request not successful");
				log.debug(document.getTextContent());
			}
		}
	}

	/**
	 * Check if a grade returned from Turnitin is greater than the max points
	 * for an assignment. If so then set to max points. (Grade is unchanged in
	 * Turnitin)
	 * 
	 * @param grade
	 *            Grade returned from Turnitin
	 * @param assignment
	 * @return
	 */
	public String processGrade(String grade, Assignment assignment) {
		String processedGrade = "";
		try {
			int gradeVal = Integer.parseInt(grade);
			if (gradeVal > assignment.getPoints()) {
				processedGrade = Double.toString(assignment.getPoints());
				log.info("Grade exceeds maximum point value for this assignment(" + assignment.getName()
						+ ") Setting to Max Points value");
			} else {
				processedGrade = grade;
			}
		} catch (NumberFormatException e) {
			log.warn("Error parsing grade");
		} catch (Exception e) {
			log.warn("Error processing grade");
		}
		return processedGrade;
	}

	/**
	 * Write a grade to the gradebook for the current specified user
	 * 
	 * @param assignment
	 * @param data
	 * @param reportTable
	 * @param additionalData
	 * @param enrollmentInfo
	 * @return
	 */
	public boolean writeGrade(Assignment assignment, Map<String, Object> data, HashMap reportTable,
			HashMap additionalData, Map enrollmentInfo) {
		boolean success = false;
		String grade = null;
		String siteId = data.get("siteId").toString();
		String currentStudentUserId = additionalData.get("tiiUserId").toString();
		String tiiExternalId = "";

		if (!enrollmentInfo.isEmpty()) {
			if (enrollmentInfo.containsKey(currentStudentUserId)) {
				tiiExternalId = enrollmentInfo.get(currentStudentUserId).toString();
				log.info("tiiExternalId: " + tiiExternalId);
			}
		} else {
			return false;
		}

		// Check if the returned grade is greater than the maximum possible
		// grade
		// If so then set to the maximum grade
		grade = processGrade(reportTable.get("grade" + currentStudentUserId).toString(), assignment);

		pushAdvisor();
		try {
			if (grade != null) {
				try {
					if (data.containsKey("assignment1")) {
						gradebookExternalAssessmentService.updateExternalAssessmentScore(siteId,
								assignment.getExternalId(), tiiExternalId, grade);
					} else {
						gradebookService.setAssignmentScoreString(siteId, data.get("taskTitle").toString(),
								tiiExternalId, grade, "SYNC");
					}
					log.info("UPDATED GRADE (" + grade + ") FOR USER (" + tiiExternalId + ") IN ASSIGNMENT ("
							+ assignment.getName() + ")");
					success = true;
				} catch (GradebookNotFoundException e) {
					log.error("Error update grade GradebookNotFoundException " + e.toString());
				} catch (Exception e) {
					log.error("Error update grade " + e.toString());
				}
			}
		} catch (Exception e) {
			log.error("Error setting grade " + e.toString());
		} finally {
			popAdvisor();
		}
		return success;
	}

	/**
	 * Get a list of students enrolled on a class in Turnitin
	 * 
	 * @param siteId
	 *            Site ID
	 * @return Map containing Students turnitin / Sakai ID
	 */
	public Map getAllEnrollmentInfo(String siteId) {
		Map params = new HashMap();
		Map<String, String> enrollmentInfo = new HashMap();
		String tiiExternalId = "";// the ID sakai stores
		String tiiInternalId = "";// Turnitin internal ID
		User user = null;
		Map instructorInfo = getInstructorInfo(siteId, true);
		try {
			user = userDirectoryService.getUser(instructorInfo.get("uid").toString());
		} catch (UserNotDefinedException e) {
			log.error("(getAllEnrollmentInfo)User not defined. " + e);
		}
		params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "fid", "19", "fcmd", "5", "tem",
				getTEM(siteId), "ctl", siteId, "cid", siteId, "utp", "2", "uid", user.getId(), "uem", getEmail(user),
				"ufn", user.getFirstName(), "uln", user.getLastName());
		Document document = null;
		try {
			document = turnitinConn.callTurnitinReturnDocument(params);
		} catch (Exception e) {
			log.warn("Failed to get enrollment data using user: " + user.getDisplayName(), e);
		}

		Element root = document.getDocumentElement();
		if (((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData().trim()
				.compareTo("93") == 0) {
			NodeList objects = root.getElementsByTagName("student");
			for (int i = 0; i < objects.getLength(); i++) {
				tiiExternalId = ((CharacterData) (((Element) (objects.item(i))).getElementsByTagName("uid").item(0)
						.getFirstChild())).getData().trim();
				tiiInternalId = ((CharacterData) (((Element) (objects.item(i))).getElementsByTagName("userid").item(0)
						.getFirstChild())).getData().trim();
				enrollmentInfo.put(tiiInternalId, tiiExternalId);
			}
		}
		return enrollmentInfo;
	}

	public void pushAdvisor() {
		securityService.pushAdvisor(new SecurityAdvisor() {

			public SecurityAdvisor.SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvisor.SecurityAdvice.ALLOWED;
			}
		});
	}

	public void popAdvisor() {
		securityService.popAdvisor();
	}

	/**
	 * private methods
	 */
	private String encodeParam(String name, String value, String boundary) {
		return "--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n";
	}

	/**
	 * This method was originally private, but is being made public for the
	 * moment so we can run integration tests. TODO Revisit this decision.
	 *
	 * @param siteId
	 * @throws SubmissionException
	 * @throws TransientSubmissionException
	 */
	@SuppressWarnings("unchecked")
	public void createClass(String siteId) throws SubmissionException, TransientSubmissionException {
		log.debug("Creating class for site: " + siteId);

		String cpw = defaultClassPassword;
		String ctl = siteId;
		String fcmd = "2";
		String fid = "2";
		String utp = "2"; // user type 2 = instructor
		String cid = siteId;

		Document document = null;

		Map params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "cid", cid, "cpw", cpw, "ctl", ctl,
				"fcmd", fcmd, "fid", fid, "utp", utp);

		params.putAll(getInstructorInfo(siteId));

		document = turnitinConn.callTurnitinReturnDocument(params);

		Element root = document.getDocumentElement();
		String rcode = ((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData().trim();

		if (((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData().trim()
				.compareTo("20") == 0
				|| ((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData().trim()
						.compareTo("21") == 0) {
			log.debug("Create Class successful");
		} else {
			if ("218".equals(rcode) || "9999".equals(rcode)) {
				throw new TransientSubmissionException("Create Class not successful. Message: "
						+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData()
								.trim()
						+ ". Code: " + ((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild()))
								.getData().trim());
			} else {
				throw new SubmissionException("Create Class not successful. Message: "
						+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData()
								.trim()
						+ ". Code: " + ((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild()))
								.getData().trim());
			}
		}
	}

	/**
	 * This returns the String that will be used as the Assignment Title in Turn
	 * It In.
	 *
	 * The current implementation here has a few interesting caveats so that it
	 * will work with both, the existing Assignments 1 integration, and the new
	 * Assignments 2 integration under development.
	 *
	 * We will check and see if the taskId starts with /assignment/. If it does
	 * we will look up the Assignment Entity on the legacy Entity bus. (not the
	 * entitybroker). This needs some general work to be made generally modular
	 * ( and useful for more than just Assignments 1 and 2 ). We will need to
	 * look at some more concrete use cases and then factor it accordingly in
	 * the future when the next scenerio is required.
	 *
	 * Another oddity is that to get rid of our hard dependency on Assignments 1
	 * we are invoking the getTitle method by hand. We probably need a mechanism
	 * to register a title handler or something as part of the setup process for
	 * new services that want to be reviewable.
	 *
	 * @param taskId
	 * @return
	 */
	private String getAssignmentTitle(String taskId) {
		String togo = taskId;
		if (taskId.startsWith("/assignment/")) {
			try {
				Reference ref = entityManager.newReference(taskId);
				log.debug("got ref " + ref + " of type: " + ref.getType());
				EntityProducer ep = ref.getEntityProducer();

				Entity ent = ep.getEntity(ref);
				log.debug("got entity " + ent);
				if(ent != null){
					String title = scrubSpecialCharacters(ent.getClass().getMethod("getTitle").invoke(ent).toString());
					log.debug("Got reflected assignment title from entity " + title);
					togo = URLDecoder.decode(title, "UTF-8");
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		// Turnitin requires Assignment titles to be at least two characters
		// long
		if (togo.length() == 1) {
			togo = togo + "_";
		}

		return togo;

	}

	private String scrubSpecialCharacters(String title) {

		try {
			if (title.contains("&")) {
				title = title.replace('&', 'n');
			}
			if (title.contains("%")) {
				title = title.replace("%", "percent");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return title;
	}

	/**
	 * @param siteId
	 * @param taskId
	 * @throws SubmissionException
	 * @throws TransientSubmissionException
	 */
	public void createAssignment(String siteId, String taskId)
			throws SubmissionException, TransientSubmissionException {
		createAssignment(siteId, taskId, null);
	}

	/**
	 * Works by fetching the Instructor User info based on defaults or current
	 * user.
	 *
	 * @param siteId
	 * @param taskId
	 * @return
	 * @throws SubmissionException
	 * @throws TransientSubmissionException
	 */
	@SuppressWarnings("unchecked")
	public Map getAssignment(String siteId, String taskId) throws SubmissionException, TransientSubmissionException {
		String taskTitle = getAssignmentTitle(taskId);

		Map params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "assign", taskTitle, "assignid", taskId,
				"cid", siteId, "ctl", siteId, "fcmd", "7", "fid", "4", "utp", "2");

		params.putAll(getInstructorInfo(siteId));

		return turnitinConn.callTurnitinReturnMap(params);
	}

	public void addTurnitinInstructor(Map userparams) throws SubmissionException, TransientSubmissionException {
		Map params = new HashMap();
		params.putAll(userparams);
		params.putAll(turnitinConn.getBaseTIIOptions());
		params.put("fid", "1");
		params.put("fcmd", "2");
		params.put("utp", "2");
		turnitinConn.callTurnitinReturnMap(params);
	}

	/**
	 * Creates or Updates an Assignment
	 *
	 * This method will look at the current user or default instructor for it's
	 * user information.
	 *
	 *
	 * @param siteId
	 * @param taskId
	 * @param extraAsnnOpts
	 * @throws SubmissionException
	 * @throws TransientSubmissionException
	 */
	@SuppressWarnings("unchecked")
	public void createAssignment(String siteId, String taskId, Map extraAsnnOpts)
			throws SubmissionException, TransientSubmissionException {

		// get the assignment reference
		String taskTitle = "";
		if (extraAsnnOpts.containsKey("title")) {
			taskTitle = extraAsnnOpts.get("title").toString();
		} else {
			getAssignmentTitle(taskId);
		}
		log.debug("Creating assignment for site: " + siteId + ", task: " + taskId + " tasktitle: " + taskTitle);

		SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
		dform.applyPattern(TURNITIN_DATETIME_FORMAT);
		Calendar cal = Calendar.getInstance();
		// set this to yesterday so we avoid timezone problems etc
		// TII-143 seems this now causes problems may need a finner tweak than 1
		// day like midnight +1 min or something
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 1);
		// cal.add(Calendar.DAY_OF_MONTH, -1);
		String dtstart = dform.format(cal.getTime());
		String today = dtstart;

		// set the due dates for the assignments to be in 5 month's time
		// turnitin automatically sets each class end date to 6 months after it
		// is created
		// the assignment end date must be on or before the class end date

		String fcmd = "2"; // new assignment
		boolean asnnExists = false;
		// If this assignment already exists, we should use fcmd 3 to update it.
		Map tiiresult = this.getAssignment(siteId, taskId);
		if (tiiresult.get("rcode") != null && tiiresult.get("rcode").equals("85")) {
			fcmd = "3";
			asnnExists = true;
		}

		/*
		 * Some notes about start and due dates. This information is accurate as
		 * of Nov 12, 2009 and was determined by testing and experimentation
		 * with some Sash scripts.
		 *
		 * A turnitin due date, must be after the start date. This makes sense
		 * and follows the logic in both Assignments 1 and 2.
		 *
		 * When *creating* a new Turnitin Assignment, the start date must be
		 * todays date or later. The format for dates only includes the day, and
		 * not any specific times. I believe that, in order to make up for time
		 * zone differences between your location and the turnitin cloud, it can
		 * be basically the current day anywhere currently, with some slack. For
		 * instance I can create an assignment for yesterday, but not for 2 days
		 * ago. Doing so causes an error.
		 *
		 * However! For an existing turnitin assignment, you appear to have the
		 * liberty of changing the start date to sometime in the past. You can
		 * also change an assignment to have a due date in the past as long as
		 * it is still after the start date.
		 *
		 * So, to avoid errors when syncing information, or adding turnitin
		 * support to new or existing assignments we will:
		 *
		 * 1. If the assignment already exists we'll just save it.
		 *
		 * 2. If the assignment does not exist, we will save it once using
		 * todays date for the start and due date, and then save it again with
		 * the proper dates to ensure we're all tidied up and in line.
		 *
		 * Also, with our current class creation, due dates can be 5 years out,
		 * but not further. This seems a bit lower priortity, but we still
		 * should figure out an appropriate way to deal with it if it does
		 * happen.
		 *
		 */

		// TODO use the 'secret' function to change this to longer
		cal.add(Calendar.MONTH, 5);
		String dtdue = dform.format(cal.getTime());
		log.debug("Set date due to: " + dtdue);
		if (extraAsnnOpts != null && extraAsnnOpts.containsKey("dtdue")) {
			dtdue = extraAsnnOpts.get("dtdue").toString();
			log.debug("Settign date due from external to: " + dtdue);
			extraAsnnOpts.remove("dtdue");
		}

		String fid = "4"; // function id
		String utp = "2"; // user type 2 = instructor
		String s_view_report = "1";
		if (extraAsnnOpts != null && extraAsnnOpts.containsKey("s_view_report")) {
			s_view_report = extraAsnnOpts.get("s_view_report").toString();
			extraAsnnOpts.remove("s_view_report");
		}

		// erater
		String erater = (serverConfigurationService.getBoolean("turnitin.option.erater.default", false)) ? "1" : "0";
		String ets_handbook = "1";
		String ets_dictionary = "en";
		String ets_spelling = "1";
		String ets_style = "1";
		String ets_grammar = "1";
		String ets_mechanics = "1";
		String ets_usage = "1";

		try {
			if (extraAsnnOpts != null && extraAsnnOpts.containsKey("erater")) {
				erater = extraAsnnOpts.get("erater").toString();
				extraAsnnOpts.remove("erater");

				ets_handbook = extraAsnnOpts.get("ets_handbook").toString();
				extraAsnnOpts.remove("ets_handbook");

				ets_dictionary = extraAsnnOpts.get("ets_dictionary").toString();
				extraAsnnOpts.remove("ets_dictionary");

				ets_spelling = extraAsnnOpts.get("ets_spelling").toString();
				extraAsnnOpts.remove("ets_spelling");

				ets_style = extraAsnnOpts.get("ets_style").toString();
				extraAsnnOpts.remove("ets_style");

				ets_grammar = extraAsnnOpts.get("ets_grammar").toString();
				extraAsnnOpts.remove("ets_grammar");

				ets_mechanics = extraAsnnOpts.get("ets_mechanics").toString();
				extraAsnnOpts.remove("ets_mechanics");

				ets_usage = extraAsnnOpts.get("ets_usage").toString();
				extraAsnnOpts.remove("ets_usage");
			}
		} catch (Exception e) {
			log.info("(createAssignment)erater extraAsnnOpts. " + e);
		}

		String cid = siteId;
		String assignid = taskId;
		String ctl = siteId;

		Map params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "assign", taskTitle, "assignid",
				assignid, "cid", cid, "ctl", ctl, "dtdue", dtdue, "dtstart", dtstart, "fcmd", "3", "fid", fid,
				"s_view_report", s_view_report, "utp", utp, "erater", erater, "ets_handbook", ets_handbook,
				"ets_dictionary", ets_dictionary, "ets_spelling", ets_spelling, "ets_style", ets_style, "ets_grammar",
				ets_grammar, "ets_mechanics", ets_mechanics, "ets_usage", ets_usage);

		// Save instructorInfo up here to reuse for calls in this
		// method, since theoretically getInstructorInfo could return
		// different instructors for different invocations and we need
		// the same one since we're using a session id.
		Map instructorInfo = getInstructorInfo(siteId);
		params.putAll(instructorInfo);

		if (extraAsnnOpts != null) {
			for (Object key : extraAsnnOpts.keySet()) {
				if (extraAsnnOpts.get(key) == null) {
					continue;
				}
				params = TurnitinAPIUtil.packMap(params, key.toString(), extraAsnnOpts.get(key).toString());
			}
		}

		// We only need to use a session id if we are creating this
		// assignment for the first time.
		String sessionid = null;
		Map sessionParams = null;

		if (!asnnExists) {
			// Try adding the user in case they don't exist TII-XXX
			addTurnitinInstructor(instructorInfo);

			sessionParams = turnitinConn.getBaseTIIOptions();
			sessionParams.putAll(instructorInfo);
			sessionParams.put("utp", utp);
			sessionid = TurnitinSessionFuncs.getTurnitinSession(turnitinConn, sessionParams);

			Map firstparams = new HashMap();
			firstparams.putAll(params);
			firstparams.put("session-id", sessionid);
			firstparams.put("dtstart", today);

			// Make the due date in the future
			Calendar caldue = Calendar.getInstance();
			caldue.add(Calendar.MONTH, 5);
			String dtdue_first = dform.format(caldue.getTime());
			firstparams.put("dtdue", dtdue_first);

			log.debug("date due is: " + dtdue);
			log.debug("Start date: " + today);
			firstparams.put("fcmd", "2");
			Document firstSaveDocument = turnitinConn.callTurnitinReturnDocument(firstparams);
			Element root = firstSaveDocument.getDocumentElement();
			int rcode = new Integer(
					((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData().trim())
							.intValue();
			if ((rcode > 0 && rcode < 100) || rcode == 419) {
				log.debug("Create FirstDate Assignment successful");
				log.debug("tii returned "
						+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData()
								.trim()
						+ ". Code: " + rcode);
			} else {
				log.debug("FirstDate Assignment creation failed with message: "
						+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData()
								.trim()
						+ ". Code: " + rcode);
				// log.debug(root);
				throw new TransientSubmissionException("FirstDate Create Assignment not successful. Message: "
						+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData()
								.trim()
						+ ". Code: " + rcode, Integer.valueOf(rcode));
			}
		}
		log.debug("going to attempt second update");
		if (sessionid != null) {
			params.put("session-id", sessionid);
		}
		Document document = turnitinConn.callTurnitinReturnDocument(params);

		Element root = document.getDocumentElement();
		int rcode = new Integer(
				((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData().trim())
						.intValue();
		if ((rcode > 0 && rcode < 100) || rcode == 419) {
			log.debug("Create Assignment successful");
			log.debug("tii returned "
					+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData().trim()
					+ ". Code: " + rcode);
		} else {
			log.debug("Assignment creation failed with message: "
					+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData().trim()
					+ ". Code: " + rcode);
			// log.debug(root);
			throw new TransientSubmissionException("Create Assignment not successful. Message: "
					+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData().trim()
					+ ". Code: " + rcode, Integer.valueOf(rcode));
		}

		if (sessionid != null) {
			TurnitinSessionFuncs.logoutTurnitinSession(turnitinConn, sessionid, sessionParams);
		}
	}

	/**
	 * Currently public for integration tests. TODO Revisit visibility of
	 * method.
	 *
	 * @param userId
	 * @param uem
	 * @param siteId
	 * @throws SubmissionException
	 */
	public void enrollInClass(String userId, String uem, String siteId)
			throws SubmissionException, TransientSubmissionException {

		String uid = userId;
		String cid = siteId;

		String ctl = siteId;
		String fid = "3";
		String fcmd = "2";
		String tem = getTEM(cid);

		User user;
		try {
			user = userDirectoryService.getUser(userId);
		} catch (Exception t) {
			throw new SubmissionException("Cannot get user information", t);
		}

		log.debug("Enrolling user " + user.getEid() + "(" + userId + ")  in class " + siteId);

		String ufn = getUserFirstName(user);
		if (ufn == null) {
			throw new SubmissionException("User has no first name");
		}

		String uln = getUserLastName(user);
		if (uln == null) {
			throw new SubmissionException("User has no last name");
		}

		String utp = "1";

		Map params = new HashMap();
		params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "fid", fid, "fcmd", fcmd, "cid", cid, "tem",
				tem, "ctl", ctl, "dis", studentAccountNotified ? "0" : "1", "uem", uem, "ufn", ufn, "uln", uln, "utp",
				utp, "uid", uid);

		Document document = turnitinConn.callTurnitinReturnDocument(params);

		Element root = document.getDocumentElement();

		String rMessage = ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData();
		String rCode = ((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData();
		if ("31".equals(rCode)) {
			log.debug("Results from enrollInClass with user + " + userId + " and class title: " + ctl + ".\n"
					+ "rCode: " + rCode + " rMessage: " + rMessage);
		} else {
			// certain return codes need to be logged
			log.warn("Results from enrollInClass with user + " + userId + " and class title: " + ctl + ". " + "rCode: "
					+ rCode + ", rMessage: " + rMessage);
			// TODO for certain types we should probably throw an exception here
			// and stop the proccess
		}

	}

	/*
	 * Get the next item that needs to be submitted
	 *
	 */
	private Optional<ContentReviewItem> getNextItemInSubmissionQueue() {
		return crqs.getNextItemInQueueToSubmit(getProviderId());
	}

	public void processQueue() {

		log.info("Processing submission queue");
		int errors = 0;
		int success = 0;

		Optional<ContentReviewItem> nextItem = null;
		while ((nextItem = getNextItemInSubmissionQueue()).isPresent()) {
			ContentReviewItem item = nextItem.get();
			
			log.debug("Attempting to submit content: " + item.getContentId() + " for user: "
					+ item.getUserId() + " and site: " + item.getSiteId());

			if (item.getRetryCount() == null) {
				item.setRetryCount(Long.valueOf(0));
				item.setNextRetryTime(this.getNextRetryTime(0));
				crqs.update(item);
			} else if (item.getRetryCount().intValue() > maxRetry) {
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED_CODE);
				crqs.update(item);
				errors++;
				continue;
			} else {
				long l = item.getRetryCount().longValue();
				l++;
				item.setRetryCount(Long.valueOf(l));
				item.setNextRetryTime(this.getNextRetryTime(Long.valueOf(l)));
				crqs.update(item);
			}

			User user;

			try {
				user = userDirectoryService.getUser(item.getUserId());
			} catch (UserNotDefinedException e1) {
				log.error("Submission attempt unsuccessful - User not found.", e1);
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				crqs.update(item);
				errors++;
				continue;
			}

			String uem = getEmail(user);
			if (uem == null) {
				log.error("User: " + user.getEid() + " has no valid email");
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS_CODE);
				item.setLastError("no valid email");
				crqs.update(item);
				errors++;
				continue;
			}

			String ufn = getUserFirstName(user);
			if (ufn == null || ufn.equals("")) {
				log.error("Submission attempt unsuccessful - User has no first name");
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS_CODE);
				item.setLastError("has no first name");
				crqs.update(item);
				errors++;
				continue;
			}

			String uln = getUserLastName(user);
			if (uln == null || uln.equals("")) {
				log.error("Submission attempt unsuccessful - User has no last name");
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS_CODE);
				item.setLastError("has no last name");
				crqs.update(item);
				errors++;
				continue;
			}

			if (!turnitinConn.isUseSourceParameter()) {
				try {
					createClass(item.getSiteId());
				} catch (SubmissionException t) {
					log.error("Submission attempt unsuccessful: Could not create class", t);
					item.setLastError("Class creation error: " + t.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
					crqs.update(item);
					errors++;
					continue;
				} catch (TransientSubmissionException tse) {
					item.setLastError("Class creation error: " + tse.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
					crqs.update(item);
					errors++;
					continue;
				}
			}

			try {
				enrollInClass(item.getUserId(), uem, item.getSiteId());
			} catch (Exception t) {
				log.error("Submission attempt unsuccessful: Could not enroll user in class", t);

				if (t.getClass() == IOException.class) {
					item.setLastError("Enrolment error: " + t.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
				} else {
					item.setLastError("Enrolment error: " + t.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
				}
				crqs.update(item);
				errors++;
				continue;
			}

			if (!turnitinConn.isUseSourceParameter()) {
				try {
					Map tiiresult = this.getAssignment(item.getSiteId(), item.getTaskId());
					if (tiiresult.get("rcode") != null && !tiiresult.get("rcode").equals("85")) {
						createAssignment(item.getSiteId(), item.getTaskId());
					}
				} catch (SubmissionException se) {
					item.setLastError("Assign creation error: " + se.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
					if (se.getErrorCode() != null) {
						item.setErrorCode(se.getErrorCode());
					}
					crqs.update(item);
					errors++;
					continue;
				} catch (TransientSubmissionException tse) {
					item.setLastError("Assign creation error: " + tse.getMessage());
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
					if (tse.getErrorCode() != null) {
						item.setErrorCode(tse.getErrorCode());
					}

					crqs.update(item);
					errors++;
					continue;

				}
			}

			// get all the info for the api call
			// we do this before connecting so that if there is a problem we can
			// jump out - saves time
			// these errors should probably be caught when a student is enrolled
			// in a class
			// but we check again here to be sure

			String fcmd = "2";
			String fid = "5";

			// to get the name of the initial submited file we need the title
			ContentResource resource = null;
			ResourceProperties resourceProperties = null;
			String fileName = null;
			try {
				try {
					resource = contentHostingService.getResource(item.getContentId());

				} catch (IdUnusedException e4) {
					// ToDo we should probably remove these from the Queue
					log.warn("IdUnusedException: no resource with id " + item.getContentId());
					crqs.delete(item);
					errors++;
					continue;
				}
				resourceProperties = resource.getProperties();
				fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
				fileName = escapeFileName(fileName, resource.getId());
			} catch (PermissionException e2) {
				log.error("Submission failed due to permission error.", e2);
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				item.setLastError("Permission exception: " + e2.getMessage());
				crqs.update(item);
				errors++;
				continue;
			} catch (TypeException e) {
				log.error("Submission failed due to content Type error.", e);
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
				item.setLastError("Type Exception: " + e.getMessage());
				crqs.update(item);
				errors++;
				continue;
			}

			// TII-97 filenames can't be longer than 200 chars
			if (fileName != null && fileName.length() >= 200) {
				fileName = truncateFileName(fileName, 198);
			}

			String userEid = item.getUserId();
			try {
				userEid = userDirectoryService.getUserEid(item.getUserId());
			} catch (UserNotDefinedException unde) {
				// nothing realy to do?
			}

			String ptl = userEid + ":" + fileName;
			String ptype = "2";

			String uid = item.getUserId();
			String cid = item.getSiteId();
			String assignid = item.getTaskId();

			// TODO ONC-1292 How to get this, and is it still required with
			// src=9?
			String tem = getTEM(cid);

			String utp = "1";

			log.debug("Using Emails: tem: " + tem + " uem: " + uem);

			String assign = getAssignmentTitle(item.getTaskId());
			String ctl = item.getSiteId();

			Map params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "assignid", assignid, "uid", uid,
					"cid", cid, "assign", assign, "ctl", ctl, "dis",
					Integer.valueOf(sendSubmissionNotification).toString(), "fcmd", fcmd, "fid", fid, "ptype", ptype,
					"ptl", ptl, "tem", tem, "uem", uem, "ufn", ufn, "uln", uln, "utp", utp, "resource_obj", resource);

			Document document = null;
			try {
				document = turnitinConn.callTurnitinReturnDocument(params, true);
			} catch (TransientSubmissionException e) {
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
				item.setLastError(
						"Error Submitting Assignment for Submission: " + e.getMessage() + ". Assume unsuccessful");
				crqs.update(item);
				errors++;
				continue;
			} catch (SubmissionException e) {
				item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
				item.setLastError(
						"Error Submitting Assignment for Submission: " + e.getMessage() + ". Assume unsuccessful");
				crqs.update(item);
				errors++;
				continue;
			}

			Element root = document.getDocumentElement();

			String rMessage = ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild()))
					.getData();
			String rCode = ((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData();

			if (rCode == null)
				rCode = "";
			else
				rCode = rCode.trim();

			if (rMessage == null)
				rMessage = rCode;
			else
				rMessage = rMessage.trim();

			if (rCode.compareTo("51") == 0) {
				String externalId = ((CharacterData) (root.getElementsByTagName("objectID").item(0).getFirstChild()))
						.getData().trim();
				if (externalId != null && externalId.length() > 0) {
					log.debug("Submission successful");
					item.setExternalId(externalId);
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
					item.setRetryCount(Long.valueOf(0));
					item.setLastError(null);
					item.setErrorCode(null);
					item.setDateSubmitted(new Date());
					success++;
					crqs.update(item);
				} else {
					log.warn("invalid external id");
					item.setLastError("Submission error: no external id received");
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
					errors++;
					crqs.update(item);
				}
			} else {
				log.debug("Submission not successful: "
						+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData()
								.trim());

				if (rMessage.equals("User password does not match user email") || "1001".equals(rCode)
						|| "".equals(rMessage) || "413".equals(rCode) || "1025".equals(rCode) || "250".equals(rCode)) {
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
					log.warn("Submission not successful. It will be retried.");
					errors++;
				} else if (rCode.equals("423")) {
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS_CODE);
					errors++;

				} else if (rCode.equals("301")) {
					// this took a long time
					log.warn("Submission not successful due to timeout. It will be retried.");
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY, 22);
					item.setNextRetryTime(cal.getTime());
					errors++;

				} else {
					log.error("Submission not successful. It will NOT be retried.");
					item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_NO_RETRY_CODE);
					errors++;
				}
				item.setLastError("Submission Error: " + rMessage + "(" + rCode + ")");
				item.setErrorCode(Integer.valueOf(rCode));
				crqs.update(item);

			}
			// release the lock so the reports job can handle it
			getNextItemInSubmissionQueue();
		}

		log.info("Submission queue run completed: " + success + " items submitted, " + errors + " errors.");
	}

	public String escapeFileName(String fileName, String contentId) {
		log.debug("origional filename is: " + fileName);
		if (fileName == null) {
			// use the id
			fileName = contentId;
		} else if (fileName.length() > 199) {
			fileName = fileName.substring(0, 199);
		}
		log.debug("fileName is :" + fileName);
		try {
			fileName = URLDecoder.decode(fileName, "UTF-8");
			// in rare cases it seems filenames can be double encoded
			while (fileName.indexOf("%20") > 0 || fileName.contains("%2520")) {
				try {
					fileName = URLDecoder.decode(fileName, "UTF-8");
				} catch (IllegalArgumentException eae) {
					log.warn("Unable to decode fileName: " + fileName + ", using contentId: " + contentId);
					// as the result is likely to cause a MD5 exception use the
					// ID
					return contentId;
					/*
					 * currentItem.setStatus(ContentReviewItem.
					 * SUBMISSION_ERROR_NO_RETRY_CODE);
					 * currentItem.setLastError("FileName decode exception: " +
					 * fileName); dao.update(currentItem);
					 * releaseLock(currentItem); errors++; throw new
					 * SubmissionException("Can't decode fileName!");
					 */
				}

			}
		} catch (IllegalArgumentException eae) {
			log.warn("Unable to decode fileName: " + fileName + ", using contentId: " + contentId);
			return contentId;
		} catch (UnsupportedEncodingException e) {
			log.warn(e.getMessage(), e);
		}

		fileName = fileName.replace(' ', '_');
		// its possible we have double _ as a result of this lets do some
		// cleanup
		fileName = StringUtils.replace(fileName, "__", "_");

		log.debug("fileName is :" + fileName);
		return fileName;
	}

	private String truncateFileName(String fileName, int i) {
		// get the extension for later re-use
		String extension = "";
		if (fileName.contains(".")) {
			extension = fileName.substring(fileName.lastIndexOf("."));
		}

		fileName = fileName.substring(0, i - extension.length());
		fileName = fileName + extension;

		return fileName;
	}

	public void checkForReports() {
		checkForReportsBulk();
	}

	/*
	 * Fetch reports on a class by class basis
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public void checkForReportsBulk() {

		SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
		dform.applyPattern(TURNITIN_DATETIME_FORMAT);

		log.info("Fetching reports from Turnitin");

		// get the list of all items that are waiting for reports
		List<ContentReviewItem> awaitingReport = crqs.getAwaitingReports(getProviderId());

		Iterator<ContentReviewItem> listIterator = awaitingReport.iterator();
		HashMap<String, Integer> reportTable = new HashMap<String, Integer>();

		log.debug("There are " + awaitingReport.size() + " submissions awaiting reports");

		ContentReviewItem currentItem;
		while (listIterator.hasNext()) {
			currentItem = (ContentReviewItem) listIterator.next();

			// has the item reached its next retry time?
			if (currentItem.getNextRetryTime() == null)
				currentItem.setNextRetryTime(new Date());

			if (currentItem.getNextRetryTime().after(new Date())) {
				// we haven't reached the next retry time
				log.info("next retry time not yet reached for item: " + currentItem.getId());
				crqs.update(currentItem);
				continue;
			}

			if (currentItem.getRetryCount() == null) {
				currentItem.setRetryCount(Long.valueOf(0));
				currentItem.setNextRetryTime(this.getNextRetryTime(0));
			} else if (currentItem.getRetryCount().intValue() > maxRetry) {
				currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_EXCEEDED_CODE);
				crqs.update(currentItem);
				continue;
			} else {
				log.debug("Still have retries left, continuing. ItemID: " + currentItem.getId());
				// Moving down to check for report generate speed.
				// long l = currentItem.getRetryCount().longValue();
				// l++;
				// currentItem.setRetryCount(Long.valueOf(l));
				// currentItem.setNextRetryTime(this.getNextRetryTime(Long.valueOf(l)));
				// dao.update(currentItem);
			}

			if (currentItem.getExternalId() == null || currentItem.getExternalId().equals("")) {
				currentItem.setStatus(Long.valueOf(4));
				crqs.update(currentItem);
				continue;
			}

			if (!reportTable.containsKey(currentItem.getExternalId())) {
				// get the list from turnitin and see if the review is available

				log.debug("Attempting to update hashtable with reports for site " + currentItem.getSiteId());

				String fcmd = "2";
				String fid = "10";

				try {
					User user = userDirectoryService.getUser(currentItem.getUserId());
				} catch (Exception e) {
					log.error("Unable to look up user: " + currentItem.getUserId() + " for contentItem: "
							+ currentItem.getId(), e);
				}

				String cid = currentItem.getSiteId();
				String tem = getTEM(cid);

				String utp = "2";

				String assignid = currentItem.getTaskId();

				String assign = currentItem.getTaskId();
				String ctl = currentItem.getSiteId();

				// TODO FIXME Current sgithens
				// Move the update setRetryAttempts to here, and first call and
				// check the assignment from TII to see if the generate until
				// due is enabled. In that case we don't want to waste retry
				// attempts and should just continue.
				try {
					// TODO FIXME This is broken at the moment because we need
					// to have a userid, but this is assuming it's coming from
					// the thread, but we're in a quartz job.
					// Map curasnn = getAssignment(currentItem.getSiteId(),
					// currentItem.getTaskId());
					// TODO FIXME Parameterize getAssignment method to take user
					// information
					Map getAsnnParams = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "assign",
							getAssignmentTitle(currentItem.getTaskId()), "assignid", currentItem.getTaskId(), "cid",
							currentItem.getSiteId(), "ctl", currentItem.getSiteId(), "fcmd", "7", "fid", "4", "utp",
							"2");

					getAsnnParams.putAll(getInstructorInfo(currentItem.getSiteId()));

					Map curasnn = turnitinConn.callTurnitinReturnMap(getAsnnParams);

					if (curasnn.containsKey("object")) {
						Map curasnnobj = (Map) curasnn.get("object");
						String reportGenSpeed = (String) curasnnobj.get("generate");
						String duedate = (String) curasnnobj.get("dtdue");
						SimpleDateFormat retform = ((SimpleDateFormat) DateFormat.getDateInstance());
						retform.applyPattern(TURNITIN_DATETIME_FORMAT);
						Date duedateObj = null;
						try {
							if (duedate != null) {
								duedateObj = retform.parse(duedate);
							}
						} catch (ParseException pe) {
							log.warn("Unable to parse turnitin dtdue: " + duedate, pe);
						}
						if (reportGenSpeed != null && duedateObj != null && reportGenSpeed.equals("2")
								&& duedateObj.after(new Date())) {
							log.info("Report generate speed is 2, skipping for now. ItemID: " + currentItem.getId());
							// If there was previously a transient error for
							// this item, reset the status
							if (ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE.equals(currentItem.getStatus())) {
								currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_AWAITING_REPORT_CODE);
								currentItem.setLastError(null);
								currentItem.setErrorCode(null);
								crqs.update(currentItem);
							}
							continue;
						} else {
							log.debug("Incrementing retry count for currentItem: " + currentItem.getId());
							long l = currentItem.getRetryCount().longValue();
							l++;
							currentItem.setRetryCount(Long.valueOf(l));
							currentItem.setNextRetryTime(this.getNextRetryTime(Long.valueOf(l)));
							crqs.update(currentItem);
						}
					}
				} catch (SubmissionException e) {
					log.error("Unable to check the report gen speed of the asnn for item: " + currentItem.getId(), e);
				} catch (TransientSubmissionException e) {
					log.error("Unable to check the report gen speed of the asnn for item: " + currentItem.getId(), e);
				}

				Map params = new HashMap();
				// try {
				params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "fid", fid, "fcmd", fcmd, "tem", tem,
						"assign", assign, "assignid", assignid, "cid", cid, "ctl", ctl, "utp", utp);
				params.putAll(getInstructorInfo(currentItem.getSiteId()));

				Document document = null;

				try {
					document = turnitinConn.callTurnitinReturnDocument(params);
				} catch (TransientSubmissionException e) {
					log.warn("Update failed due to TransientSubmissionException error: " + e.toString(), e);
					currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE);
					currentItem.setLastError(e.getMessage());
					crqs.update(currentItem);
					break;
				} catch (SubmissionException e) {
					log.warn("Update failed due to SubmissionException error: " + e.toString(), e);
					currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_REPORT_ERROR_RETRY_CODE);
					currentItem.setLastError(e.getMessage());
					crqs.update(currentItem);
					break;
				}

				Element root = document.getDocumentElement();
				if (((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData().trim()
						.compareTo("72") == 0) {
					log.debug("Report list returned successfully");

					NodeList objects = root.getElementsByTagName("object");
					String objectId;
					String similarityScore;
					String overlap = "";
					log.debug(objects.getLength() + " objects in the returned list");
					for (int i = 0; i < objects.getLength(); i++) {
						similarityScore = ((CharacterData) (((Element) (objects.item(i)))
								.getElementsByTagName("similarityScore").item(0).getFirstChild())).getData().trim();
						objectId = ((CharacterData) (((Element) (objects.item(i))).getElementsByTagName("objectID")
								.item(0).getFirstChild())).getData().trim();
						if (similarityScore.compareTo("-1") != 0) {
							overlap = ((CharacterData) (((Element) (objects.item(i))).getElementsByTagName("overlap")
									.item(0).getFirstChild())).getData().trim();
							reportTable.put(objectId, Integer.valueOf(overlap));
						} else {
							reportTable.put(objectId, Integer.valueOf(-1));
						}

						log.debug("objectId: " + objectId + " similarity: " + similarityScore + " overlap: " + overlap);
					}
				} else {
					log.debug("Report list request not successful");
					log.debug(document.getTextContent());

				}
			}

			int reportVal;
			// check if the report value is now there (there may have been a
			// failure to get the list above)
			if (reportTable.containsKey(currentItem.getExternalId())) {
				reportVal = ((Integer) (reportTable.get(currentItem.getExternalId()))).intValue();
				log.debug("reportVal for " + currentItem.getExternalId() + ": " + reportVal);
				if (reportVal != -1) {
					currentItem.setReviewScore(reportVal);
					currentItem.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE);
					currentItem.setDateReportReceived(new Date());
					crqs.update(currentItem);
					log.debug("new report received: " + currentItem.getExternalId() + " -> "
							+ currentItem.getReviewScore());
				}
			}
		}

		log.info("Finished fetching reports from Turnitin");
	}

	// returns null if no valid email exists
	public String getEmail(User user) {

		String uem = null;

		// Check account email address
		String account_email = null;

		if (isValidEmail(user.getEmail())) {
			account_email = user.getEmail().trim();
		}

		// Lookup system profile email address if necessary
		String profile_email = null;
		if (account_email == null || preferSystemProfileEmail) {
			SakaiPerson sp = sakaiPersonManager.getSakaiPerson(user.getId(), sakaiPersonManager.getSystemMutableType());
			if (sp != null && isValidEmail(sp.getMail())) {
				profile_email = sp.getMail().trim();
			}
		}

		// Check guest accounts and use eid as the email if preferred
		if (this.preferGuestEidEmail && isValidEmail(user.getEid())) {
			uem = user.getEid();
		}

		if (uem == null && preferSystemProfileEmail && profile_email != null) {
			uem = profile_email;
		}

		if (uem == null && account_email != null) {
			uem = account_email;
		}

		// Randomize the email address if preferred
		if (spoilEmailAddresses && uem != null) {
			// Scramble it
			String[] parts = uem.split("@");

			String emailName = parts[0];

			Random random = new Random();
			int int1 = random.nextInt();
			int int2 = random.nextInt();
			int int3 = random.nextInt();

			emailName += (int1 + int2 + int3);

			uem = emailName + "@" + parts[1];

			if (log.isDebugEnabled())
				log.debug("SCRAMBLED EMAIL:" + uem);
		}

		log.debug("Using email " + uem + " for user eid " + user.getEid() + " id " + user.getId());
		return uem;
	}

	/**
	 * Is this a valid email the service will recognize
	 * 
	 * @param email
	 * @return
	 */
	private boolean isValidEmail(String email) {

		// TODO: Use a generic Sakai utility class (when a suitable one exists)

		if (email == null || email.equals(""))
			return false;

		email = email.trim();
		// must contain @
		if (email.indexOf("@") == -1)
			return false;

		// an email can't contain spaces
		if (email.indexOf(" ") > 0)
			return false;

		// use commons-validator
		EmailValidator validator = EmailValidator.getInstance();
		if (validator.isValid(email))
			return true;

		return false;
	}

	// Methods for updating all assignments that exist
	public void doAssignments() {
		log.info("About to update all turnitin assignments");
		
		List<ContentReviewItem> items = crqs.getAllContentReviewItemsGroupedBySiteAndTask(getProviderId());
		
		for (ContentReviewItem item : items) {
			try {
				updateAssignment(item.getSiteId(), item.getTaskId());
			} catch (SubmissionException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Update Assignment. This method is not currently called by Assignments 1.
	 */
	public void updateAssignment(String siteId, String taskId) throws SubmissionException {
		log.info("updateAssignment(" + siteId + " , " + taskId + ")");
		// get the assignment reference
		String taskTitle = getAssignmentTitle(taskId);
		log.debug("Creating assignment for site: " + siteId + ", task: " + taskId + " tasktitle: " + taskTitle);

		SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
		dform.applyPattern(TURNITIN_DATETIME_FORMAT);
		Calendar cal = Calendar.getInstance();
		// set this to yesterday so we avoid timezpne problems etc
		cal.add(Calendar.DAY_OF_MONTH, -1);
		String dtstart = dform.format(cal.getTime());

		// set the due dates for the assignments to be in 5 month's time
		// turnitin automatically sets each class end date to 6 months after it
		// is created
		// the assignment end date must be on or before the class end date

		// TODO use the 'secret' function to change this to longer
		cal.add(Calendar.MONTH, 5);
		String dtdue = dform.format(cal.getTime());

		String fcmd = "3"; // new assignment
		String fid = "4"; // function id
		String utp = "2"; // user type 2 = instructor
		String s_view_report = "1";

		// erater
		String erater = "0";
		String ets_handbook = "1";
		String ets_dictionary = "en";
		String ets_spelling = "1";
		String ets_style = "1";
		String ets_grammar = "1";
		String ets_mechanics = "1";
		String ets_usage = "1";

		String cid = siteId;
		String assignid = taskId;
		String assign = taskTitle;
		String ctl = siteId;

		String assignEnc = assign;
		try {
			if (assign.contains("&")) {
				// log.debug("replacing & in assingment title");
				assign = assign.replace('&', 'n');

			}
			assignEnc = assign;
			log.debug("Assign title is " + assignEnc);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		Map params = TurnitinAPIUtil.packMap(turnitinConn.getBaseTIIOptions(), "assign", assignEnc, "assignid",
				assignid, "cid", cid, "ctl", ctl, "dtdue", dtdue, "dtstart", dtstart, "fcmd", fcmd, "fid", fid,
				"s_view_report", s_view_report, "utp", utp, "erater", erater, "ets_handbook", ets_handbook,
				"ets_dictionary", ets_dictionary, "ets_spelling", ets_spelling, "ets_style", ets_style, "ets_grammar",
				ets_grammar, "ets_mechanics", ets_mechanics, "ets_usage", ets_usage);

		params.putAll(getInstructorInfo(siteId));

		Document document = null;

		try {
			document = turnitinConn.callTurnitinReturnDocument(params);
		} catch (TransientSubmissionException tse) {
			log.error("Error on API call in updateAssignment siteid: " + siteId + " taskid: " + taskId, tse);
			return;
		} catch (SubmissionException se) {
			log.error("Error on API call in updateAssignment siteid: " + siteId + " taskid: " + taskId, se);
			return;
		}

		Element root = document.getDocumentElement();
		int rcode = new Integer(
				((CharacterData) (root.getElementsByTagName("rcode").item(0).getFirstChild())).getData().trim())
						.intValue();
		if ((rcode > 0 && rcode < 100) || rcode == 419) {
			log.debug("Create Assignment successful");
		} else {
			log.debug("Assignment creation failed with message: "
					+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData().trim()
					+ ". Code: " + rcode);
			throw new SubmissionException("Create Assignment not successful. Message: "
					+ ((CharacterData) (root.getElementsByTagName("rmessage").item(0).getFirstChild())).getData().trim()
					+ ". Code: " + rcode);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.contentreview.service.ContentReviewService#
	 * isAcceptableContent(org.sakaiproject.content.api.ContentResource)
	 */
	public boolean isAcceptableContent(ContentResource resource) {
		return turnitinContentValidator.isAcceptableContent(resource);
	}

	public String[] getAcceptableFileExtensions() {
		String[] extensions = serverConfigurationService.getStrings(PROP_ACCEPTABLE_FILE_EXTENSIONS);
		if (extensions != null && extensions.length > 0) {
			return extensions;
		}
		return DEFAULT_ACCEPTABLE_FILE_EXTENSIONS;
	}

	// TII-157	--bbailla2
	public String[] getAcceptableMimeTypes() {
		String[] mimeTypes = serverConfigurationService.getStrings(PROP_ACCEPTABLE_MIME_TYPES);
		if (mimeTypes != null && mimeTypes.length > 0) {
			return mimeTypes;
		}
		return DEFAULT_ACCEPTABLE_MIME_TYPES;
	}

	// TII-157	--bbailla2
	public String [] getAcceptableFileTypes() {
		return serverConfigurationService.getStrings(PROP_ACCEPTABLE_FILE_TYPES);
	}

	// TII-157	--bbailla2
	/**
	 * Inserts (key, value) into a Map<String, Set<String>> such that value is inserted into the value Set associated with key.
	 * The value set is implemented as a TreeSet, so the Strings will be in alphabetical order
	 * Eg. if we insert (a, b) and (a, c) into map, then map.get(a) will return {b, c}
	 */
	private void appendToMap(Map<String, SortedSet<String>> map, String key, String value) {
		SortedSet<String> valueList = map.get(key);
		if (valueList == null) {
			valueList = new TreeSet<>();
			map.put(key, valueList);
		}
		valueList.add(value);
	}
	/**
	 * find the next time this item should be tried
	 * 
	 * @param retryCount
	 * @return
	 */
	private Date getNextRetryTime(long retryCount) {
		int offset = 5;

		if (retryCount > 9 && retryCount < 20) {

			offset = 10;

		} else if (retryCount > 19 && retryCount < 30) {
			offset = 20;
		} else if (retryCount > 29 && retryCount < 40) {
			offset = 40;
		} else if (retryCount > 39 && retryCount < 50) {
			offset = 80;
		} else if (retryCount > 49 && retryCount < 60) {
			offset = 160;
		} else if (retryCount > 59) {
			offset = 220;
		}

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, offset);
		return cal.getTime();
	}

	/**
	 * Gets a first name for a user or generates an initial from the eid
	 * 
	 * @param user
	 *            a sakai user
	 * @return the first name or at least an initial if possible, "X" if no fn
	 *         can be made
	 */
	private String getUserFirstName(User user) {
		String ufn = user.getFirstName().trim();
		if (ufn == null || ufn.equals("")) {
			boolean genFN = (boolean) serverConfigurationService.getBoolean("turnitin.generate.first.name", true);
			if (genFN) {
				String eid = user.getEid();
				if (eid != null && eid.length() > 0) {
					ufn = eid.substring(0, 1);
				} else {
					ufn = "X";
				}
			}
		}
		return ufn;
	}

	/**
	 * Get user last Name. If turnitin.generate.last.name is set to true last
	 * name is anonamised
	 * 
	 * @param user
	 * @return
	 */
	private String getUserLastName(User user) {
		String uln = user.getLastName().trim();
		if (uln == null || uln.equals("")) {
			boolean genLN = serverConfigurationService.getBoolean("turnitin.generate.last.name", false);
			if (genLN) {
				String eid = user.getEid();
				if (eid != null && eid.length() > 0) {
					uln = eid.substring(0, 1);
				} else {
					uln = "X";
				}
			}
		}
		return uln;
	}

	public String getLocalizedStatusMessage(String messageCode, String userRef) {

		String userId = EntityReference.getIdFromRef(userRef);
		ResourceLoader resourceLoader = new ResourceLoader(userId, "turnitin");
		return resourceLoader.getString(messageCode);
	}

	public String getReviewError(String contentId) {
		return getLocalizedReviewErrorMessage(contentId);
	}

	public String getLocalizedStatusMessage(String messageCode) {
		return getLocalizedStatusMessage(messageCode, userDirectoryService.getCurrentUser().getReference());
	}

	public String getLocalizedStatusMessage(String messageCode, Locale locale) {
		// TODO not sure how to do this with the sakai resource loader
		return null;
	}

	public String getLocalizedReviewErrorMessage(String contentId) {
		log.debug("Returning review error for content: " + contentId);

		Optional<ContentReviewItem> item = crqs.getQueuedItem(getProviderId(), contentId);

		if (item.isPresent()) {
			// its possible the error code column is not populated
			Integer errorCode = item.get().getErrorCode();
			if (errorCode != null) {
				return getLocalizedStatusMessage(errorCode.toString());
			}
			return item.get().getLastError();
		}

		log.debug("Content " + contentId + " has not been queued previously");
		return null;
	}

	private String getTEM(String cid) {
		if (turnitinConn.isUseSourceParameter()) {
			return getInstructorInfo(cid).get("uem").toString();
		} else {
			return turnitinConn.getDefaultInstructorEmail();
		}
	}

	/**
	 * This will return a map of the information for the instructor such as uem,
	 * username, ufn, etc. If the system is configured to use src9 provisioning,
	 * this will draw information from the current thread based user. Otherwise
	 * it will use the default Instructor information that has been configured
	 * for the system.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map getInstructorInfo(String siteId) {

		log.debug("Getting instructor info for site " + siteId);

		Map togo = new HashMap();
		if (!turnitinConn.isUseSourceParameter()) {
			togo.put("uem", turnitinConn.getDefaultInstructorEmail());
			togo.put("ufn", turnitinConn.getDefaultInstructorFName());
			togo.put("uln", turnitinConn.getDefaultInstructorLName());
			togo.put("uid", turnitinConn.getDefaultInstructorId());
		} else {
			String INST_ROLE = "section.role.instructor";
			User inst = null;
			try {
				Site site = siteService.getSite(siteId);
				User user = userDirectoryService.getCurrentUser();

				log.debug("Current user: " + user.getId());

				if (site.isAllowed(user.getId(), INST_ROLE)) {
					inst = user;
				} else {
					Set<String> instIds = getActiveInstructorIds(INST_ROLE, site);
					if (instIds.size() > 0) {
						inst = userDirectoryService.getUser((String) instIds.toArray()[0]);
					}
				}
			} catch (IdUnusedException e) {
				log.error("Unable to fetch site in getAbsoluteInstructorInfo: " + siteId, e);
			} catch (UserNotDefinedException e) {
				log.error("Unable to fetch user in getAbsoluteInstructorInfo", e);
			}

			if (inst == null) {
				log.error("Instructor is null in getAbsoluteInstructorInfo");
			} else {
				togo.put("uem", getEmail(inst));
				togo.put("ufn", inst.getFirstName());
				togo.put("uln", inst.getLastName());
				togo.put("uid", inst.getId());
				togo.put("username", inst.getDisplayName());
			}
		}

		return togo;
	}

	@SuppressWarnings("unchecked")
	public Map getInstructorInfo(String siteId, boolean ignoreUseSource) {
		Map togo = new HashMap();
		if (!turnitinConn.isUseSourceParameter() && ignoreUseSource == false) {
			togo.put("uem", turnitinConn.getDefaultInstructorEmail());
			togo.put("ufn", turnitinConn.getDefaultInstructorFName());
			togo.put("uln", turnitinConn.getDefaultInstructorLName());
			togo.put("uid", turnitinConn.getDefaultInstructorId());
		} else {
			String INST_ROLE = "section.role.instructor";
			User inst = null;
			try {
				Site site = siteService.getSite(siteId);
				User user = userDirectoryService.getCurrentUser();
				if (site.isAllowed(user.getId(), INST_ROLE)) {
					inst = user;
				} else {
					Set<String> instIds = getActiveInstructorIds(INST_ROLE, site);
					if (instIds.size() > 0) {
						inst = userDirectoryService.getUser((String) instIds.toArray()[0]);
					}
				}
			} catch (IdUnusedException e) {
				log.error("Unable to fetch site in getAbsoluteInstructorInfo: " + siteId, e);
			} catch (UserNotDefinedException e) {
				log.error("Unable to fetch user in getAbsoluteInstructorInfo", e);
			}

			if (inst == null) {
				log.error("Instructor is null in getAbsoluteInstructorInfo");
			} else {
				togo.put("uem", getEmail(inst));
				togo.put("ufn", inst.getFirstName());
				togo.put("uln", inst.getLastName());
				togo.put("uid", inst.getId());
				togo.put("username", inst.getDisplayName());
			}
		}

		return togo;
	}

	private Set<String> getActiveInstructorIds(String INST_ROLE, Site site) {

		log.debug("Getting active instructor IDs for permission " + INST_ROLE + " in site " + site.getId());

		Set<String> instIds = site.getUsersIsAllowed(INST_ROLE);

		// the site could contain references to deleted users
		List<User> activeUsers = userDirectoryService.getUsers(instIds);
		Set<String> ret = new HashSet<String>();
		for (int i = 0; i < activeUsers.size(); i++) {
			User user = activeUsers.get(i);
			// Ignore users who do not have a first and/or last name set or do
			// not have
			// a valid email address, as this will cause a TII API call to fail
			if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty() && user.getLastName() != null
					&& !user.getLastName().trim().isEmpty() && getEmail(user) != null) {
				ret.add(user.getId());
			}
		}

		return ret;
	}

	@Override
	public boolean allowAllContent() {
		// Turntin reports errors when content is submitted that it can't check originality against. So we will block unsupported content.
		return serverConfigurationService.getBoolean(PROP_ACCEPT_ALL_FILES, false);
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableExtensionsToMimeTypes() {
		Map<String, SortedSet<String>> acceptableExtensionsToMimeTypes = new HashMap<>();
		String[] acceptableFileExtensions = getAcceptableFileExtensions();
		String[] acceptableMimeTypes = getAcceptableMimeTypes();
		int min = Math.min(acceptableFileExtensions.length, acceptableMimeTypes.length);
		for (int i = 0; i < min; i++) {
			appendToMap(acceptableExtensionsToMimeTypes, acceptableFileExtensions[i], acceptableMimeTypes[i]);
		}

		return acceptableExtensionsToMimeTypes;
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableFileTypesToExtensions() {
		Map<String, SortedSet<String>> acceptableFileTypesToExtensions = new LinkedHashMap<>();
		String[] acceptableFileTypes = getAcceptableFileTypes();
		String[] acceptableFileExtensions = getAcceptableFileExtensions();
		if (acceptableFileTypes != null && acceptableFileTypes.length > 0) {
			// The acceptable file types are listed in sakai.properties. Sakai.properties takes precedence.
			int min = Math.min(acceptableFileTypes.length, acceptableFileExtensions.length);
			for (int i = 0; i < min; i++) {
				appendToMap(acceptableFileTypesToExtensions, acceptableFileTypes[i], acceptableFileExtensions[i]);
			}
		}
		else {
			/*
			 * acceptableFileTypes not specified in sakai.properties (this is normal).
			 * Use ResourceLoader to resolve the file types.
			 * If the resource loader doesn't find the file extenions, log a warning and return the [missing key...] messages
			 */
			ResourceLoader resourceLoader = new ResourceLoader("turnitin");
			for( String fileExtension : acceptableFileExtensions ) {
				String key = KEY_FILE_TYPE_PREFIX + fileExtension;
				if (!resourceLoader.getIsValid(key)) {
					log.warn("While resolving acceptable file types for Turnitin, the sakai.property " + PROP_ACCEPTABLE_FILE_TYPES + " is not set, and the message bundle " + key + " could not be resolved. Displaying [missing key ...] to the user");
				}
				String fileType = resourceLoader.getString(key);
				appendToMap( acceptableFileTypesToExtensions, fileType, fileExtension );
			}
		}

		return acceptableFileTypesToExtensions;
	}

	@Override
	public void queueContent(String userId, String siteId, String taskId, List<ContentResource> content)
			throws QueueException {

		log.debug("Method called queueContent()");

		if (content == null || content.isEmpty()) {
			return;
		}

		if (userId == null) {
			log.debug("Using current user");
			userId = userDirectoryService.getCurrentUser().getId();
		}

		if (siteId == null) {
			log.debug("Using current site");
			siteId = toolManager.getCurrentPlacement().getContext();
		}

		if (taskId == null) {
			log.debug("Generating default taskId");
			taskId = siteId + " " + "defaultAssignment";
		}

		log.debug("Adding content from site " + siteId + " and user: " + userId + " for task: " + taskId
				+ " to submission queue");
		crqs.queueContent(getProviderId(), userId, siteId, taskId, content);
	}

	@Override
	public Long getReviewStatus(String contentId) throws QueueException {
		return crqs.getReviewStatus(getProviderId(), contentId);
	}

	@Override
	public Date getDateQueued(String contextId) throws QueueException {
		return crqs.getDateQueued(getProviderId(), contextId);
	}

	@Override
	public Date getDateSubmitted(String contextId) throws QueueException, SubmissionException {
		return crqs.getDateSubmitted(getProviderId(), contextId);
	}

	@Override
	public List<ContentReviewItem> getReportList(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		return crqs.getContentReviewItems(getProviderId(), siteId, taskId);
	}

	@Override
	public List<ContentReviewItem> getReportList(String siteId)
			throws QueueException, SubmissionException, ReportException {
		return getReportList(siteId, null);
	}

	@Override
	public List<ContentReviewItem> getAllContentReviewItems(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		return crqs.getContentReviewItems(getProviderId(), siteId, taskId);
	}

	@Override
	public void resetUserDetailsLockedItems(String userId) {
		crqs.resetUserDetailsLockedItems(getProviderId(), userId);
	}

	@Override
	public boolean allowResubmission() {
		return true;
	}

	@Override
	public void removeFromQueue(String contentId) {
		crqs.removeFromQueue(getProviderId(), contentId);
	}

	@Override
	public ContentReviewItem getContentReviewItemByContentId(String contentId){
		Optional<ContentReviewItem> cri = getItemByContentId(contentId);
		if(cri.isPresent()){
			ContentReviewItem item = cri.get();
			
			//TII specific work
		
			// Sync Grades
			if (turnitinConn.getUseGradeMark()) {
				try {				
					String[] assignData = getAssignData(contentId);
					String siteId = "", taskId = "", taskTitle = "";
					Map<String, Object> data = new HashMap<String, Object>();
					if (assignData != null) {
						siteId = assignData[0];
						taskId = assignData[1];
						taskTitle = assignData[2];
					} else {
						siteId = item.getSiteId();
						taskId = item.getTaskId();
						taskTitle = getAssignmentTitle(taskId);
						data.put("assignment1", "assignment1");
					}
					data.put("siteId", siteId);
					data.put("taskId", taskId);
					data.put("taskTitle", taskTitle);
					syncGrades(data);
				} catch (Exception e) {
					log.error("Error syncing grades. " + e);
				}
			}

			return item;
		} else {
			log.debug("Content " + contentId + " has not been queued previously");
		}
		return null;
	}

	@Override
	public String getEndUserLicenseAgreementLink(String userId) {
		return null;
	}

	@Override
	public Instant getEndUserLicenseAgreementTimestamp() {
		return null;
	}

	@Override
	public String getEndUserLicenseAgreementVersion() {
		return null;
	}
	
	@Override
	public void webhookEvent(HttpServletRequest request, int providerId, Optional<String> customParam) {
		//Auto-generated method stub
	}
}
