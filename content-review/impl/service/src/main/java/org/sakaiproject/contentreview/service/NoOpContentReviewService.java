package org.sakaiproject.contentreview.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.site.api.Site;

public class NoOpContentReviewService implements ContentReviewService {
	private static final String SERVICE_NAME = "NOOP";

	@Override
	public void queueContent(String userId, String siteId, String taskId, List<ContentResource> content)
			throws QueueException {
	}

	@Override
	public int getReviewScore(String contentId, String taskId, String userId)
			throws QueueException, ReportException, Exception {
		return 0;
	}

	@Override
	public String getReviewReport(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		return null;
	}

	@Override
	public String getReviewReportStudent(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		return null;
	}

	@Override
	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		return null;
	}

	@Override
	public Long getReviewStatus(String contentId) throws QueueException {
		return null;
	}

	@Override
	public Date getDateQueued(String contextId) throws QueueException {
		return null;
	}

	@Override
	public Date getDateSubmitted(String contextId) throws QueueException, SubmissionException {
		return null;
	}

	@Override
	public void processQueue() {
	}

	@Override
	public void checkForReports() {
	}

	@Override
	public List<ContentReviewItem> getReportList(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		return null;
	}

	@Override
	public List<ContentReviewItem> getReportList(String siteId)
			throws QueueException, SubmissionException, ReportException {
		return null;
	}

	@Override
	public List<ContentReviewItem> getAllContentReviewItems(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		return null;
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public void resetUserDetailsLockedItems(String userId) {
	}

	@Override
	public boolean allowAllContent() {
		return false;
	}

	@Override
	public boolean isAcceptableContent(ContentResource resource) {
		return false;
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableExtensionsToMimeTypes() {
		return new HashMap<String, SortedSet<String>>();
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableFileTypesToExtensions() {
		return new HashMap<String, SortedSet<String>>();
	}

	@Override
	public boolean isSiteAcceptable(Site site) {
		return false;
	}

	@Override
	public String getIconCssClassforScore(int score, String contentId) {
		return "contentReviewIconNoService";
	}

	@Override
	public boolean allowResubmission() {
		return false;
	}

	@Override
	public void removeFromQueue(String ContentId) {
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode, String userRef) {
		return "There is no content review service configured, please see your administrator";
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode) {
		return "There is no content review service configured, please see your administrator";
	}

	@Override
	public String getReviewError(String contentId) {
		return "There is no content review service configured, please see your administrator";
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode, Locale locale) {
		return "There is no content review service configured, please see your administrator";
	}

	@Override
	public Map getAssignment(String siteId, String taskId) throws SubmissionException, TransientSubmissionException {
		return null;
	}

	@Override
	public void createAssignment(String siteId, String taskId, Map extraAsnnOpts)
			throws SubmissionException, TransientSubmissionException {
	}
}
