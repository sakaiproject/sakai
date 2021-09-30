/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.assignment.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.util.ZipContentUtil;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

@Slf4j
public class EmailUtil {

    private static final String NEW_LINE = "<br />\n";
    private static final String INDENT = "    ";

    @Setter private AssignmentService assignmentService;
    @Setter private EntityManager entityManager;
    @Setter private FormattedText formattedText;
    @Setter private ResourceLoader resourceLoader;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SiteService siteService;
    @Setter private UserDirectoryService userDirectoryService;

    public Map<String, Object> getReleaseGradeReplacements(Assignment a, String siteId) {

        Map<String, Object> map = new HashMap<>();
        try {
            Site site = siteService.getSite(siteId);
            map.put("siteTitle", site.getTitle());
            map.put("siteUrl", site.getUrl());
            map.put("assignmentTitle", a.getTitle());
            map.put("assignmentUrl", getAssignmentUrl(a));
            map.put("bundle", resourceLoader);
        } catch (Exception e) {
            log.warn("Failed to get email replacements", e);
        }
        return map;
    }

    public Map<String, Object> getSubmissionReplacements(AssignmentSubmission submission) {

        Map<String, Object> replacements = new HashMap<>();

        Assignment assignment = submission.getAssignment();
        String context = assignment.getContext();
        boolean isAnon = assignmentService.assignmentUsesAnonymousGrading(assignment);

        try {
            Site site = siteService.getSite(context);
            replacements.put("siteTitle", site.getTitle());
            replacements.put("siteUrl", site.getUrl());
        } catch (Exception e) {
            log.warn("Can't get site with id = {}, {}", context, e.getMessage());
            replacements.put("siteTitle", resourceLoader.getFormattedMessage("cannotfin_site", context));
            replacements.put("siteUrl", "");
        }

        replacements.put("assignmentTitle", assignment.getTitle());
        replacements.put("assignmentUrl", getAssignmentUrl(assignment));
        replacements.put("hideDueDate", assignment.getHideDueDate());
        if(!assignment.getHideDueDate()) {
            replacements.put("dueDate", assignmentService.getUsersLocalDateTimeString(assignment.getDueDate()));
        }
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
        replacements.put("submitterNames", submitterNames);
        if (!isAnon) {
            replacements.put("submitterIds", submitterIds);
        }

        // submit time
        replacements.put("submissionId", submission.getId());

        // submit time
	    replacements.put("submittedDate", assignmentService.getUsersLocalDateTimeString(submission.getDateSubmitted()));

        // submit text
        String text = StringUtils.trimToNull(submission.getSubmittedText());
        if (text != null) {
            replacements.put("submittedText", text);
        }

        // attachment if any
        Set<String> attachments = submission.getAttachments();
        if (!attachments.isEmpty()) {
            if (assignment.getTypeOfSubmission() == Assignment.SubmissionType.SINGLE_ATTACHMENT_SUBMISSION) {
                replacements.put("attachmentType", resourceLoader.getString("gen.att.single"));
            } else {
                replacements.put("attachmentType", resourceLoader.getString("gen.att"));
            }

            //if this is a archive (zip etc) append the list of files in it
            StringBuffer buffer = attachments.stream().map(att -> entityManager.newReference(att)).collect(StringBuffer::new, (sb, ref) -> {

                ResourceProperties properties = ref.getProperties();
                boolean isArchiveFile = isArchiveFile(ref);
                sb.append(properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME))
                    .append(" (")
                    .append(ref.getProperties().getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH))
                    .append(isArchiveFile ? "):" : ")")
                    .append(NEW_LINE);
                if (isArchiveFile(ref)) {
                    sb.append("<blockquote>\n");
                    sb.append(getArchiveManifest(ref, true));
                    sb.append("</blockquote>\n");
                }
            }, StringBuffer::append);
            replacements.put("attachmentsBlock", buffer.toString());
        }

        replacements.put("bundle", resourceLoader);

        return replacements;
    }

    public Map<String, Object> getReleaseResubmissionReplacements(AssignmentSubmission submission) {

        Map<String, Object> replacements = new HashMap<>();

        Assignment assignment = submission.getAssignment();
        String context = assignment.getContext();

        try {
            Site site = siteService.getSite(context);
            replacements.put("siteTitle", site.getTitle());
            replacements.put("siteUrl", site.getUrl());
        } catch (Exception e) {
            log.warn("Can't get site with id = {}, {}", context, e.getMessage());
            replacements.put("siteTitle", resourceLoader.getFormattedMessage("cannotfin_site", context));
            replacements.put("siteUrl", "");
        }

        //Get the actual person that submitted, for a group submission just get the first person from that group (This is why the array is used)
        String userId = null;
        for (AssignmentSubmissionSubmitter submitter : submission.getSubmitters()) {
            if (userId == null || submitter.getSubmittee()) {
                userId = submitter.getSubmitter();
            }
        }

        String linkToToolInSite = "<a href=\"" + getAssignmentUrl(assignment) + "\">" + replacements.get("siteTitle") + "</a>";
        replacements.put("assignmentUrl", linkToToolInSite);
        replacements.put("assignmentTitle", assignment.getTitle());
        replacements.put("canSubmit", assignmentService.canSubmit(assignment, userId));
        replacements.put("bundle", resourceLoader);

        return replacements;
    }

    private String getAssignmentUrl(Assignment assignment) {

        String ref = AssignmentReferenceReckoner.reckoner()
                        .id(assignment.getId())
                        .context(assignment.getContext())
                        .subtype("a")
                        .reckon()
                        .getReference();

        Optional<String> url = entityManager.getUrl(ref, Entity.UrlType.PORTAL);

        if (url.isPresent()) {
            return url.get();
        } else {
            log.warn("Failed to get url for assignment {}", assignment.getId());
            return "";
        }
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
            String extension = fileName.substring(fileName.lastIndexOf("."));
            return extension;
        }
        return null;
    }
}
