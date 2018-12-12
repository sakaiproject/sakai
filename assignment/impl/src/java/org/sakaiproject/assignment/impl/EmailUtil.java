package org.sakaiproject.assignment.impl;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.util.ZipContentUtil;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;

@Slf4j
public class EmailUtil {
    private static final String A_HREF = "<a href=\"";
	private static final String CANNOTFIN_SITE = "cannotfin_site";
	private static final String CANT_GET_SITE_WITH_ID = "Can't get site with id = {}, {}";
	private static final String NOTI_SITE_URL = "noti.site.url";
	private static final String NOTI_SITE_TITLE = "noti.site.title";
	private static final String RELEASEGRADE = "releasegrade";
	private static final String SUBMISSION = "submission";
	private static final String HTML_HEADERS = "Content-Type: text/html\n\n";
	private static final String PLAIN_TEXT_HEADERS = "Content-Type: text/plain\n\n";
	public static final String HTML_END = "\n  </body>\n</html>\n";
	private static final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
    private static final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
    private static final String BOUNDARY_LINE = "\n\n--" + MULTIPART_BOUNDARY + "\n";
    private static final String TERMINATION_LINE = "\n\n--" + MULTIPART_BOUNDARY + "--\n\n";
    private static final String NEW_LINE = "<br />\n";
    private static final String INDENT = "    ";

    @Setter private AssignmentService assignmentService;
    @Setter private DeveloperHelperService developerHelperService;
    @Setter private EntityManager entityManager;
    @Setter private FormattedText formattedText;
    @Setter private ResourceLoader resourceLoader;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SiteService siteService;
    @Setter private UserDirectoryService userDirectoryService;

    public List<String> getHeaders(String receiverEmail, String submissionOrReleaseGrade) {
        List<String> rv = new ArrayList<>();

        rv.add("MIME-Version: 1.0");
        rv.add("Content-Type: multipart/alternative; boundary=\"" + MULTIPART_BOUNDARY + "\"");
        // set the subject
        rv.add(getSubject(submissionOrReleaseGrade));

        // from
        rv.add(getFrom());

        // to
        if (StringUtils.isNotBlank(receiverEmail)) {
            rv.add("To: " + receiverEmail);
        }

        return rv;
    }

    public String getSubject(String submissionOrReleaseGrade) {
        String subject;
        switch (submissionOrReleaseGrade) {
        case SUBMISSION:
            subject = resourceLoader.getString("noti.subject.content");
            break;
        case RELEASEGRADE:
            subject = resourceLoader.getString("noti.releasegrade.subject.content");
            break;
        default:
            subject = resourceLoader.getString("noti.releaseresubmission.subject.content");
            break;
        }
        return "Subject: " + subject;
    }

    public String getFrom() {
        return "From: " + "\"" + serverConfigurationService.getString("ui.service", "Sakai") + "\" <" + serverConfigurationService.getString("setup.request", "no-reply@" + serverConfigurationService.getServerName()) + ">";
    }

    public String getNotificationMessage(AssignmentSubmission s, String submissionOrReleaseGrade) {
        StringBuilder message = new StringBuilder();
        message.append(MIME_ADVISORY);
        message.append(BOUNDARY_LINE);
        message.append(PLAIN_TEXT_HEADERS);
        message.append(plainTextContent(s, submissionOrReleaseGrade));
        message.append(formattedText.convertFormattedTextToPlaintext(htmlContentAttachments(s)));
        message.append(BOUNDARY_LINE);
        message.append(HTML_HEADERS);
        message.append(htmlPreamble(submissionOrReleaseGrade));
        switch (submissionOrReleaseGrade) {
        case SUBMISSION:
            message.append(htmlContent(s));
            break;
        case RELEASEGRADE:
            message.append(htmlContentReleaseGrade(s));
            break;
        default:
            message.append(htmlContentReleaseResubmission(s));
            break;
        }
        message.append(htmlContentAttachments(s));
        message.append(HTML_END);
        message.append(TERMINATION_LINE);
        return message.toString();
    }

    public String plainTextContent(AssignmentSubmission s, String submissionOrReleaseGrade) {
    	switch (submissionOrReleaseGrade) {
        case SUBMISSION:
            return formattedText.convertFormattedTextToPlaintext(htmlContent(s));
        case RELEASEGRADE:
            return formattedText.convertFormattedTextToPlaintext(htmlContentReleaseGrade(s));
        default:
            return formattedText.convertFormattedTextToPlaintext(htmlContentReleaseResubmission(s));
        }
    }

    public String htmlPreamble(String submissionOrReleaseGrade) {
        StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
        buf.append("    \"http://www.w3.org/TR/html4/loose.dtd\">\n");
        buf.append("<html>\n");
        buf.append("  <head><title>");
        buf.append(getSubject(submissionOrReleaseGrade));
        buf.append("</title></head>\n");
        buf.append("  <body>\n");
        return buf.toString();
    }

    public String htmlContent(AssignmentSubmission submission) {
        Assignment assignment = submission.getAssignment();
        String context = assignment.getContext();
        boolean isAnon = assignmentService.assignmentUsesAnonymousGrading(assignment);

        String siteTitle;
        String siteUrl;
        try {
            Site site = siteService.getSite(context);
            siteTitle = site.getTitle();
            siteUrl = site.getUrl();
        } catch (Exception e) {
            log.warn(CANT_GET_SITE_WITH_ID, context, e.getMessage());
            siteTitle = resourceLoader.getFormattedMessage(CANNOTFIN_SITE, context);
            siteUrl = "";
        }

        StringBuilder buffer = new StringBuilder();
        // site title and id
        buffer.append(resourceLoader.getString(NOTI_SITE_TITLE)).append(" ").append(siteTitle).append(NEW_LINE);
        buffer.append(resourceLoader.getString(NOTI_SITE_URL)).append(A_HREF).append(siteUrl).append("\">").append(siteUrl).append("</a>").append(NEW_LINE);
        // assignment title and due date
        buffer.append(resourceLoader.getString("assignment.title")).append(" ").append(assignment.getTitle()).append(NEW_LINE);

        buffer.append(resourceLoader.getString("noti.assignment.duedate")).append(" ").append(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(ZoneId.systemDefault()).format(assignment.getDueDate())).append(NEW_LINE).append(NEW_LINE);
        submittersContent(submission, isAnon, buffer);
        buffer.append(NEW_LINE).append(NEW_LINE);

        // submit time
        buffer.append(resourceLoader.getString("submission.id")).append(" ").append(submission.getId()).append(NEW_LINE);

        // submit time
        buffer.append(resourceLoader.getString("noti.submit.time")).append(" ").append(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(ZoneId.systemDefault()).format(submission.getDateSubmitted())).append(NEW_LINE).append(NEW_LINE);

        // submit text
        String text = StringUtils.trimToNull(submission.getSubmittedText());
        if (text != null) {
            buffer.append(resourceLoader.getString("gen.submittedtext")).append(NEW_LINE).append(NEW_LINE).append(Validator.escapeHtmlFormattedText(text)).append(NEW_LINE).append(NEW_LINE);
        }

        // attachment if any
        Set<String> attachments = submission.getAttachments();
        if (!attachments.isEmpty()) {
            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION) {
                buffer.append(resourceLoader.getString("gen.att.single"));
            } else {
                buffer.append(resourceLoader.getString("gen.att"));
            }
            buffer.append(NEW_LINE).append(NEW_LINE);

            //if this is a archive (zip etc) append the list of files in it
            attachments.stream().map(attachment -> entityManager.newReference(attachment)).forEach(reference -> {
                ResourceProperties properties = reference.getProperties();
                boolean isArchiveFile = isArchiveFile(reference);
                buffer.append(properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME))
                        .append(" (")
                        .append(reference.getProperties().getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH))
                        .append(isArchiveFile ? "):" : ")")
                        .append(NEW_LINE);
                if (isArchiveFile(reference)) {
                    buffer.append("<blockquote>\n");
                    buffer.append(getArchiveManifest(reference, true));
                    buffer.append("</blockquote>\n");
                }
            });
        }

        return buffer.toString();
    }
    
    private void submittersContent(AssignmentSubmission submission, boolean isAnon, StringBuilder buffer) {
		// submitter name and id
        String submitterNames = "";
        String submitterIds = "";
        for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
            try {
                User user = userDirectoryService.getUser(submitter.getSubmitter());
                if (!submitterNames.isEmpty()) {
                    submitterNames = submitterNames.concat("; ");
                    submitterIds = submitterIds.concat("; ");
                }
                submitterNames = submitterNames.concat((isAnon ? submission.getId() + " " + resourceLoader.getString("grading.anonymous.title") : user.getDisplayName()));
                submitterIds = submitterIds.concat(user.getDisplayId());
            } catch (UserNotDefinedException e) {
                log.warn("User not found with id = {}, {}", submitter.getSubmitter());
            }
        }
        buffer.append(resourceLoader.getString("noti.student")).append(" ").append(submitterNames);
        if (submitterIds.length() != 0 && !isAnon) {
            buffer.append("( ").append(submitterIds).append(" )");
        }
	}

    public String htmlContentReleaseGrade(AssignmentSubmission submission) {
        Assignment assignment = submission.getAssignment();
        String context = assignment.getContext();

        String siteTitle;
        String siteUrl;
        try {
            Site site = siteService.getSite(context);
            siteTitle = site.getTitle();
            siteUrl = site.getUrl();
        } catch (Exception e) {
        	log.warn(CANT_GET_SITE_WITH_ID, context, e.getMessage());
            siteTitle = resourceLoader.getFormattedMessage(CANNOTFIN_SITE, context);
            siteUrl = "";
        }

        StringBuilder buffer = new StringBuilder();
        // site title and id
        buffer.append(resourceLoader.getString(NOTI_SITE_TITLE)).append(" ").append(siteTitle).append(NEW_LINE);
        buffer.append(resourceLoader.getString(NOTI_SITE_URL)).append(A_HREF).append(siteUrl).append("\">").append(siteUrl).append("</a>").append(NEW_LINE);
        // notification text
        String linkToToolInSite = A_HREF + developerHelperService.getToolViewURL("sakai.assignment.grades", null, null, null) + "\">" + siteTitle + "</a>";
        buffer.append(resourceLoader.getFormattedMessage("noti.releasegrade.text", assignment.getTitle(), linkToToolInSite));

        return buffer.toString();
    }

    public String htmlContentReleaseResubmission(AssignmentSubmission submission) {
        Assignment assignment = submission.getAssignment();
        String context = assignment.getContext();

        String siteTitle;
        String siteUrl;
        try {
            Site site = siteService.getSite(context);
            siteTitle = site.getTitle();
            siteUrl = site.getUrl();
        } catch (Exception e) {
        	log.warn(CANT_GET_SITE_WITH_ID, context, e.getMessage());
            siteTitle = resourceLoader.getFormattedMessage(CANNOTFIN_SITE, context);
            siteUrl = "";
        }

        StringBuilder buffer = new StringBuilder();
        // site title and id
        buffer.append(resourceLoader.getString(NOTI_SITE_TITLE)).append(" ").append(siteTitle).append(NEW_LINE);
        buffer.append(resourceLoader.getString(NOTI_SITE_URL)).append(" ").append(A_HREF).append(siteUrl).append("\">").append(siteUrl).append("</a>").append(NEW_LINE);
        // notification text
        //Get the actual person that submitted, for a group submission just get the first person from that group (This is why the array is used)
        String userId = null;
        for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
            if (userId == null || submitter.getSubmittee()) {
                userId = submitter.getSubmitter();
            }
        }

        String linkToToolInSite = A_HREF + developerHelperService.getToolViewURL("sakai.assignment.grades", null, null, null) + "\">" + siteTitle + "</a>";
        if (assignmentService.canSubmit(assignment, userId)) {
            buffer.append(resourceLoader.getFormattedMessage("noti.releaseresubmission.text", assignment.getTitle(), linkToToolInSite));
        } else {
            buffer.append(resourceLoader.getFormattedMessage("noti.releaseresubmission.noresubmit.text", assignment.getTitle(), linkToToolInSite));
        }

        return buffer.toString();
    }

    public String htmlContentAttachments(AssignmentSubmission submission) {
        StringBuilder body = new StringBuilder();
        Set<String> feedbackAttachments = submission.getFeedbackAttachments();
        if (!feedbackAttachments.isEmpty()) {
            body.append(NEW_LINE).append(NEW_LINE);
            if (submission.getAssignment().getTypeOfSubmission() == Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION) {
                body.append(resourceLoader.getString("gen.att.single"));
            } else {
                body.append(resourceLoader.getString("gen.att"));
            }
            body.append(NEW_LINE);

            feedbackAttachments.stream().map(feedbackAttachment -> entityManager.newReference(feedbackAttachment)).forEach(reference -> {
                ResourceProperties properties = reference.getProperties();
                String attachmentName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
                String attachmentSize = properties.getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH);
                body.append(A_HREF).append(reference.getUrl()).append("\">").append(attachmentName).append(" (").append(attachmentSize).append(")").append("</a>");
                body.append(NEW_LINE);
            });
        }
        return body.toString();
    }

    public String getPlainTextNotificationMessage(AssignmentSubmission s, String submissionOrReleaseGrade) {
        return plainTextContent(s, submissionOrReleaseGrade);
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private boolean isArchiveFile(Reference reference) {
        return ".zip".equals(getFileExtension(reference));
    }

    /**
     * get a list of the files in the archive
     * @param r the entity reference for the archive file
     * @param indent specifies whether each line should be prefixed with 4 spaces
     * @return a formatted listing of the files in the archive
     */
    private String getArchiveManifest(Reference r, boolean indent) {
        String extension = getFileExtension(r);
        StringBuilder builder = new StringBuilder();
        // assume archive is empty until at least one entry is found
        boolean archiveIsEmpty = true;
        if (".zip".equals(extension)) {
            ZipContentUtil zipUtil = new ZipContentUtil();
            Map<String, Long> manifest = zipUtil.getZipManifest(r);
            Set<Map.Entry<String, Long>> set = manifest.entrySet();
            archiveIsEmpty = set.isEmpty();
            for (Map.Entry<String, Long> entry : set) {
                if (indent) {
                    builder.append(INDENT);
                }
                builder.append(entry.getKey()).append(" (").append(formatFileSize(entry.getValue())).append(")").append(NEW_LINE);
            }
        }

        if (archiveIsEmpty) {
            return INDENT + resourceLoader.getString("noti.archive.empty") + NEW_LINE;
        }
        return builder.toString();
    }

    private String getFileExtension(Reference reference) {
        ResourceProperties resourceProperties = reference.getProperties();
        String fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
        if (fileName.contains(".")) {
        	return fileName.substring(fileName.lastIndexOf('.'));
        }
        return null;
    }
}
