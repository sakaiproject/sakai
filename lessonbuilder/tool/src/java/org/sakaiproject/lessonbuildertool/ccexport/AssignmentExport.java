/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

package org.sakaiproject.lessonbuildertool.ccexport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.util.api.FormattedText;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentExport {

    @Setter private AssignmentService assignmentService;
    @Setter private CCUtils ccUtils;
    @Setter private ContentHostingService contentHostingService;
    @Setter private FormattedText formattedText;

    private List<CCAssignmentItem> getItemsInSite(String siteId) {
        List<CCAssignmentItem> list = new ArrayList<>();

        for (Assignment assignment : assignmentService.getAssignmentsForContext(siteId)) {
            if (!assignment.getDraft()) {
                Set<String> attachments = assignment.getAttachments();
                String instructions = assignment.getInstructions();

                CCAssignmentItem item = new CCAssignmentItem();
                item.setId(LessonEntity.ASSIGNMENT + "/" + assignment.getId());
                item.setInstructions(instructions);
                item.getAttachments().addAll(attachments);
                list.add(item);
            }
        }
        return list;
    }


    // find topics in site, but organized by forum
    public List<String> getEntitiesInSite(CCConfig ccConfig) {

        List<String> list = new ArrayList<>();
        String siteId = ccConfig.getSiteId();
        String siteRef = "/group/" + siteId + "/";

        List<CCAssignmentItem> items = getItemsInSite(siteId);

        for (CCAssignmentItem item : items) {

            List<String> attachments = item.getAttachments();

            // special case. one attachment and nothing else.
            // just export the attachment.
            // removed this code. Interaction with version 1.3 has made this
            // too complex to test and maintain.

            list.add(item.getId());

            // arrange to include the attachments
            for (String sakaiId : attachments) {
                if (sakaiId.startsWith("/content/")) sakaiId = sakaiId.substring("/content".length());

                String url = null;
                // if it is a URL, need the URL rather than copying the file
                try {
                    ContentResource resource = contentHostingService.getResource(sakaiId);
                    if (ccUtils.isLink(resource)) {
                        url = new String(resource.getContent());
                    }
                } catch (Exception e) {
                    log.debug("Could not access resource {}, {}", sakaiId, e.toString());
                }

                // if attachment isn't a file in resources, arrange for it to be included
                if (url == null && !sakaiId.startsWith(siteRef)) {  // if in resources, already included
                    String assignmentId = item.getId();
                    int i = assignmentId.indexOf("/");
                    assignmentId = assignmentId.substring(i + 1);
                    int lastSlash = sakaiId.lastIndexOf("/");
                    String lastAtom = sakaiId.substring(lastSlash + 1);
                    ccConfig.addFile(sakaiId, "attachments/" + assignmentId + "/" + lastAtom, null);
                }
            }
        }

        return list;
    }

    private CCAssignmentItem getContents(String assignmentRef) {

        if (!assignmentRef.startsWith(LessonEntity.ASSIGNMENT + "/")) {
            return null;
        }

        CCAssignmentItem ret = new CCAssignmentItem();

        int i = assignmentRef.indexOf("/");
        String assignmentId = assignmentRef.substring(i + 1);
        ret.setId(assignmentId);

        Assignment assignment = null;

        try {
            assignment = assignmentService.getAssignment(assignmentId);
        } catch (Exception e) {
            log.info("Could not access assignment {}, {}", assignmentId, e.getMessage());
            return null;
        }

        ret.setTitle(assignment.getTitle());
        ret.setInstructions(assignment.getInstructions());

        Assignment.GradeType typeOfGrade = assignment.getTypeOfGrade();
        // in Sakai only point-based goes to gradebook.
        // in CC we have gradeable with optional point value
        // I've chosen to specify a point value only for question with a point value

        switch (typeOfGrade) {
            case SCORE_GRADE_TYPE:
                Integer scaleFactor = assignment.getScaleFactor() != null ? assignment.getScaleFactor() : assignmentService.getScaleFactor();
                String aux = assignmentService.getMaxPointGradeDisplay(scaleFactor, assignment.getMaxGradePoint());
                String decSeparator = formattedText.getDecimalSeparator();
                double maxPoints = 0.0;
                try {
                    maxPoints = Double.valueOf(StringUtils.replace(aux, decSeparator, "."));
                } catch (NumberFormatException e ) {
                    log.error("Could not parse max points [{}] as a Double, {}", aux, e.getMessage());
                }
                ret.setMaxPoints(maxPoints);
                ret.setForPoints(true);
            case LETTER_GRADE_TYPE:
            case PASS_FAIL_GRADE_TYPE:
            case CHECK_GRADE_TYPE:
                ret.setGradable(true);
        }

        Assignment.SubmissionType typeOfSubmission = assignment.getTypeOfSubmission();
        switch (typeOfSubmission) {
            case TEXT_ONLY_ASSIGNMENT_SUBMISSION:
                ret.setAllowText(true);
                break;
            case ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION:
                ret.setAllowFile(true);
                break;
            case TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION:
                ret.setAllowText(true);
                ret.setAllowFile(true);
                break;
        }

        ret.getAttachments().addAll(assignment.getAttachments());
        return ret;
    }

    public boolean outputEntity(CCConfig ccConfig, String assignmentRef, ZipPrintStream out, CCResourceItem ccResourceItem) {

        CCAssignmentItem contents = getContents(assignmentRef);
        if (contents == null) return false;

        String instructions = ccUtils.relFixup(ccConfig, contents.getInstructions(), ccResourceItem);

        List<String> attachments = contents.getAttachments();

        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
        out.println("<body>");
        if (instructions != null && !instructions.trim().equals("")) {
            out.println("<div>");
            out.println(instructions);
            out.println("</div> ");
        }


        out.println(outputAttachments(ccConfig, ccResourceItem, attachments, "../../"));

        out.println("</body>");
        out.println("</html>");

        return true;
    }


    // this is weird, because it's a web content, not a learning application. So we need to produce an HTML file
    // with instructions and relative references to any attachments

    public String outputAttachments(CCConfig ccConfig, CCResourceItem ccResourceItem, List<String> attachments, String prefix) {

        StringBuilder out = new StringBuilder();

        if (attachments.size() > 0) {
            out.append("<ul>\n");

            for (String sakaiId : attachments) {
                String URL = null;

                if (sakaiId.startsWith("/content/")) {
                    sakaiId = sakaiId.substring("/content".length());
                }

                // if it is a URL, need the URL rather than copying the file
                if (!sakaiId.startsWith("///")) {
                    try {
                        ContentResource res = contentHostingService.getResource(sakaiId);
                        if (ccUtils.isLink(res)) {
                            URL = new String(res.getContent());
                        }
                    } catch (Exception e) {
                        log.debug("Could not access resource {}, {}", sakaiId, e.getMessage());
                    }
                }

                String location = ccConfig.getLocation(sakaiId);
                int lastSlash = sakaiId.lastIndexOf("/");
                String lastAtom = sakaiId.substring(lastSlash + 1);

                // assumption here is that if the user entered a URL, it's in valid syntax
                // if we generate it from file location, it needs to be escaped
                if (URL != null) {
                    out.append("<li><a href=\"").append(URL).append("\">").append(StringEscapeUtils.escapeHtml4(URL)).append("</a>\n");
                } else {
                    URL = prefix + formattedText.escapeUrl(location);  // else it's in the normal site content
                    URL = URL.replaceAll("//", "/");
                    out.append("<li><a href=\"").append(URL).append("\">").append(StringEscapeUtils.escapeHtml4(lastAtom)).append("</a><br/>\n");
                    ccConfig.addDependency(ccResourceItem, sakaiId);
                }
            }
            out.append("</ul>\n");
        }

        return out.toString();

    }

    public boolean outputEntity2(CCConfig ccConfig, String assignmentRef, ZipPrintStream out, CCResourceItem ccResourceItem) {

        CCAssignmentItem contents = getContents(assignmentRef);
        if (contents == null) return false;

        String title = contents.getTitle();

        // relFixup is for stuff that's in an actual HTML file, fixup for stuff in an XML descriptor
        String instructions = ccUtils.fixup(ccConfig, contents.getInstructions(), ccResourceItem);
        List<String> attachments = contents.getAttachments();

        // the spec doesn't allow URLs in attachments, so if any of our attachments are URLs,
        // put the attachments as a list inside the instructions
        boolean useAttachments = (attachments.size() > 0);
        for (String sakaiId : attachments) {
            if (sakaiId.startsWith("/content/")) {
                sakaiId = sakaiId.substring("/content".length());
            }

            String URL = null;
            // if it is a URL, need the URL rather than copying the file
            try {
                ContentResource res = contentHostingService.getResource(sakaiId);
                if (ccUtils.isLink(res)) {
                    useAttachments = false;
                    break;
                }
            } catch (Exception e) {
                log.debug("Could not access resource {}, {}", sakaiId, e.getMessage());
            }
        }

        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<assignment xmlns=\"http://www.imsglobal.org/xsd/imscc_extensions/assignment\"");
        out.println("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        out.println("     xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imscc_extensions/assignment http://www.imsglobal.org/profile/cc/cc_extensions/cc_extresource_assignmentv1p0_v1p0.xsd\"");
        out.println("  identifier=\"" + contents.getId() + "\">");

        if (StringUtils.isBlank(title)) title = "Assignment";
        out.println("  <title>" + StringEscapeUtils.escapeXml11(title) + "</title>");
        if (useAttachments || attachments.size() == 0) {
            out.println("  <text texttype=\"text/html\">" + instructions + "</text>");
        } else {
            out.println("  <text texttype=\"text/html\">" + StringEscapeUtils.escapeXml11("<div>") + instructions + StringEscapeUtils.escapeXml11(outputAttachments(ccConfig, ccResourceItem, attachments, "$IMS-CC-FILEBASE$../") + "</div>") + "</text>");
        }
        // spec requires an instructor text even though we don't normally have one.
        out.println("<instructor_text texttype=\"text/plain\"></instructor_text>");
        out.println("<gradable" + (contents.isForPoints() ? (" points_possible=\"" + contents.getMaxPoints() + "\"") : "") + ">" + contents.isGradable() + "</gradable>");

        if (useAttachments) {
            out.println("  <attachments>");

            for (String sakaiId : attachments) {
                if (sakaiId.startsWith("/content/")) {
                    sakaiId = sakaiId.substring("/content".length());
                }

                String URL = null;
                // if it is a URL, need the URL rather than copying the file
                try {
                    ContentResource res = contentHostingService.getResource(sakaiId);
                    if (ccUtils.isLink(res)) {
                        URL = new String(res.getContent());
                    }
                } catch (Exception e) {
                    log.debug("Could not access resource {}, {}", sakaiId, e.getMessage());
                }

                String location = ccConfig.getLocation(sakaiId);

                if (URL != null) {
                    out.println("    <attachment href=\"" + StringEscapeUtils.escapeXml11(URL) + "\" role=\"All\" />");
                } else {
                    URL = "../" + location;  // else it's in the normal site content
                    URL = URL.replaceAll("//", "/");
                    out.println("    <attachment href=\"" + StringEscapeUtils.escapeXml11(URL) + "\" role=\"All\" />");
                    ccConfig.addDependency(ccResourceItem, sakaiId);
                }
            }
            out.println("  </attachments>");
        }
        out.println("  <submission_formats>");
        // our text input is HTML
        if (contents.isAllowText())
            out.println("    <format  type=\"html\" />");
        // file input allows both file and URL
        if (contents.isAllowFile()) {
            out.println("    <format  type=\"file\" />");
            out.println("    <format  type=\"url\" />");
        }
        out.println("  </submission_formats>");

        out.println("</assignment>");

        return true;
    }
}
