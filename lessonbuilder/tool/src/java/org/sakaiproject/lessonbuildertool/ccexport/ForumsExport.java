/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lessonbuildertool.ccexport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.api.FormattedText;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForumsExport {

    @Setter private AssignmentExport assignmentExport;
    @Setter private CCUtils ccUtils;
    @Setter private ContentHostingService contentHostingService;
    @Setter private FormattedText formattedText;
    @Setter private MessageForumsForumManager forumManager;
    @Setter private SiteService siteService;

    private List<CCForumItem> getItemsInSite(String siteId) {
        List<CCForumItem> ccForumItems = new ArrayList<>();

        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException iue) {
            // if site doesn't exist, it seems silly to try any more tools
            log.debug("Could not access site {}, {}", siteId, iue.toString());
            return null;
        }

        if (site.getToolForCommonId("sakai.forums") == null) {
            return null;
        }

        for (DiscussionForum forum : forumManager.getForumsForSite(siteId)) {
            if (!forum.getDraft()) {
                for (DiscussionTopic topic : (Set<DiscussionTopic>) forum.getTopicsSet()) {
                    if (topic.getDraft().equals(Boolean.FALSE)) {
                        CCForumItem ccForumItem = new CCForumItem();
                        ccForumItem.setId(LessonEntity.FORUM_TOPIC + "/" + topic.getId());

                        List<Attachment> attachments = topic.getAttachments();
                        for (Attachment attachment : attachments) {
                            String sakaiId = attachment.getAttachmentId();
                            ccForumItem.getAttachments().add(new CCForumAttachment(sakaiId, sakaiId));
                        }
                        ccForumItems.add(ccForumItem);
                    }
                }
            }
        }
        return ccForumItems;
    }

    public List<String> getEntitiesInSite(CCConfig ccConfig) {

        List<String> list = new ArrayList<>();
        String siteId = ccConfig.getSiteId();
        String siteRef = "/group/" + siteId + "/";
        List<CCForumItem> ccForumItems = Optional.ofNullable(getItemsInSite(siteId)).orElseGet(ArrayList::new);

        for (CCForumItem ccForumItem : ccForumItems) {
            list.add(ccForumItem.getId());

            List<CCForumAttachment> attachments = ccForumItem.getAttachments();
            for (CCForumAttachment attachment : attachments) {
                // this code is to identify attachments that aren't in the normal
                // site resources. In that case we have to make a copy of it
                String url = null;
                // if it is a URL, need the URL rather than copying the file
                String logical = attachment.getLogical();
                String physical = attachment.getPhysical();
                if (!physical.startsWith("///")) {
                    try {
                        ContentResource res = contentHostingService.getResource(physical);
                        if (ccUtils.isLink(res)) {
                            url = new String(res.getContent());
                        }
                    } catch (Exception e) {
                        log.debug("Could not access resource {}, {}", physical, e.toString());
                    }
                }

                if (url == null && !physical.startsWith(siteRef)) {  // if in resources, already included
                    int lastSlash = logical.lastIndexOf("/");
                    String lastAtom = logical.substring(lastSlash + 1);
                    ccConfig.addFile(physical, "attachments/" + ccForumItem.getId() + "/" + lastAtom, null);
                }
            }
        }
        return list;
    }

    private CCForumItem getContents(String forumRef) {

        if (!forumRef.startsWith(LessonEntity.FORUM_TOPIC + "/")) {
            return null;
        }

        int i = forumRef.indexOf("/");
        String forumString = forumRef.substring(i + 1);
        Long forumId = new Long(forumString);

        Topic topic = forumManager.getTopicById(true, forumId);
        if (topic == null) return null;

        CCForumItem ccForumItem = new CCForumItem();

        ccForumItem.setId(forumRef);
        ccForumItem.setTitle(topic.getTitle());
        String text = topic.getExtendedDescription();  // html
        if (StringUtils.isBlank(text)) {
            text = topic.getShortDescription();
            if (StringUtils.isNotBlank(text)) {
                text = formattedText.convertPlaintextToFormattedText(text);
            }
        }
        text = StringUtils.trimToEmpty(text);
        ccForumItem.setText(text);

        List<Attachment> attachments = topic.getAttachments();
        for (Attachment attachment : attachments) {
            String sakaiId = attachment.getAttachmentId();
            ccForumItem.getAttachments().add(new CCForumAttachment(sakaiId, sakaiId));
        }

        return ccForumItem;
    }

    public boolean outputEntity(CCConfig ccConfig, String forumRef, ZipPrintStream out, CCResourceItem ccResourceItem, CCVersion ccVersion) {

        CCForumItem ccForumItem = getContents(forumRef);

        // according to the spec, attachments must be Learnning Object web content. That is, they can
        // be files but not URLs, and they must be in a special directory for this forum topic.
        // Since we need to be able to support URLs, don't include any attachments. Instead,
        // append it as URLs at the end of the document.
        // However none of their examples actually work this way. So I reimplemented it using actual attachments.

        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        switch (ccVersion) {
            case V11:
                out.println("<topic xmlns=\"http://www.imsglobal.org/xsd/imsccv1p1/imsdt_v1p1\"");
                out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p1/imsdt_v1p1 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_imsdt_v1p1.xsd\">");
                break;
            case V13:
                out.println("<topic xmlns=\"http://www.imsglobal.org/xsd/imsccv1p3/imsdt_v1p3\"");
                out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p3/imsdt_v1p3 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_imsdt_v1p3.xsd\">");
                break;
            default:
                out.println("<topic xmlns=\"http://www.imsglobal.org/xsd/imsccv1p2/imsdt_v1p2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p2/imsdt_v1p2 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_imsdt_v1p2.xsd\">");
                break;
        }
        out.println("  <title>" + StringEscapeUtils.escapeXml11(ccForumItem.getTitle()) + "</title>");

        boolean useAttachments = !ccForumItem.getAttachments().isEmpty();
        List<String> attachments = new ArrayList<>();

        // see if we can use <attachments>. We can't if any of the attachments are a URL
        // construct a new list, which is SakaiIds, in case we need to use outputAttachments
        for (CCForumAttachment attachment : ccForumItem.getAttachments()) {
            String sakaiId = attachment.getPhysical();
            if (sakaiId.startsWith("/content/")) {
                sakaiId = sakaiId.substring("/content".length());
            }
            attachments.add(sakaiId);

            // if it is a URL, need the URL rather than copying the file
            if (!sakaiId.startsWith("///")) {
                try {
                    ContentResource resource = contentHostingService.getResource(sakaiId);
                    if (ccUtils.isLink(resource)) {
                        useAttachments = false;
                    }
                } catch (Exception e) {
                    log.debug("Could not access resource {}, {}", sakaiId, e.toString());
                }
            }
        }

        String text = ccUtils.fixup(ccConfig,"<div>" + ccForumItem.getText() + " </div>", ccResourceItem);

        if (useAttachments) {
            out.println("  <text texttype=\"text/html\">" + text + StringEscapeUtils.escapeXml11(assignmentExport.outputAttachments(ccConfig, ccResourceItem, attachments, "$IMS-CC-FILEBASE$../")) + "</text>");
        } else {
            out.println("  <text texttype=\"text/html\">" + text + "</text>");
        }

        if (useAttachments) {
            out.println("  <attachments>");

            for (CCForumAttachment attachment : ccForumItem.getAttachments()) {
                String physical = attachment.getPhysical();
                String URL = null;
                if (!physical.startsWith("///")) {
                    try {
                        ContentResource res = contentHostingService.getResource(physical);
                        if (ccUtils.isLink(res)) {
                            URL = new String(res.getContent());
                        }
                    } catch (Exception e) {
                        log.debug("Could not access resource {}, {}", physical, e.toString());
                    }
                }

                // the spec doesn't seem to ask for URL encoding on file names
                if (URL == null) {
                    URL = "../" + ccConfig.getLocation(physical);
                    URL = StringEscapeUtils.escapeXml11(URL.replaceAll("//", "/"));
                }
                out.println("    <attachment href=\"" + URL + "\"/>");
                ccConfig.addDependency(ccResourceItem, physical);
            }

            out.println("  </attachments>");
        }
        out.println("</topic>");

        return true;
    }

}
