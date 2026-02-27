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
package org.sakaiproject.lessonbuildertool.ccexport;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.util.ResourceLoaderMessageSource;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LessonsExport {

    @Setter private CCUtils ccUtils;
    @Setter private SimplePageToolDao simplePageToolDao;

    private ResourceLoaderMessageSource messageSource;

    public LessonsExport() {
        messageSource = new ResourceLoaderMessageSource();
        messageSource.setBasename("lessons");
    }

    public SimplePageItem outputLessonPage(CCConfig ccConfig, ZipPrintStream out, Long pageId, String title, int indent, boolean showNext) {
        SimplePageItem next = null;
        boolean multipleNext = false;

        ccConfig.getPagesDone().add(pageId);

        ccUtils.outputIndent(out, indent);
        out.println("<item identifier=\"page_" + pageId + "\">");
        ccUtils.outputIndent(out, indent + 2);
        out.println("<title>" + StringEscapeUtils.escapeXml11(title) + "</title>");

        List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);
        for (SimplePageItem item : items) {
            if (item.getNextPage()) {
                if (next == null) {
                    next = item;
                } else if (!multipleNext) {
                    next = null;
                    multipleNext = true;
                }
            }
        }

        for (SimplePageItem item : items) {
            String sakaiId;
            CCResourceItem res = null;
            String itemString = null;
            String urlTitle = null;

            switch (item.getType()) {
                case SimplePageItem.PAGE:
                    Long pId = Long.valueOf(item.getSakaiId());
                    if (ccConfig.getPagesDone().contains(pId)) {
                        ccConfig.getResults().add(messageSource.getMessage("simplepage.exportcc-pagealreadydone", null, ccConfig.getLocale()).replace("{1}", title).replace("{2}", item.getName()));
                    } else if ((next != null) && (item.getId() == next.getId())) {
                        if (showNext) {
                            SimplePageItem n = outputLessonPage(ccConfig, out, pId, item.getName(), indent + 2, false);
                            while ((n != null) && (!ccConfig.getPagesDone().contains(pId = Long.valueOf(n.getSakaiId())))) {
                                n = outputLessonPage(ccConfig, out, pId, n.getName(), indent + 2, false);
                            }
                            if ((n != null) && (ccConfig.getPagesDone().contains(pId))) {
                                ccConfig.getResults().add(messageSource.getMessage("simplepage.exportcc-pagealreadydone", null, ccConfig.getLocale()).replace("{1}", title).replace("{2}", item.getName()));
                            }
                        }
                    } else {
                        outputLessonPage(ccConfig, out, pId, item.getName(), indent + 2, true);
                    }
                    break;
                case SimplePageItem.MULTIMEDIA:
                    String embedCode = item.getAttribute("multimediaEmbedCode");
                    if (embedCode != null && embedCode.length() > 0) {
                        sakaiId = "/text/" + item.getId();
                        String location = "attachments/item-" + item.getId() + ".html";
                        res = new CCResourceItem(sakaiId, ccConfig.getResourceId(), location, null, null, null);
                        ccConfig.getFileMap().put(sakaiId, res);
                        ccConfig.getEmbedMap().put(item.getId(), ccUtils.relFixup(ccConfig, embedCode, res));
                        // item won't have a title, so we have to specify one. But with an embed code
                        // there's no useful title. So just use generic text.
                        urlTitle = messageSource.getMessage("simplepage.importcc-embedtitle", null, ccConfig.getLocale());
                        break;
                    }
                    String oembed = item.getAttribute("multimediaUrl");
                    if (oembed != null && oembed.length() > 0) {
                        // we've already done outputAllFiles, so this code is simply
                        // to get the <resource> output.
                        String location = "attachments/item-" + item.getId() + ".xml";
                        // first argument is dummy in this case, since it's not in the file system
                        res = ccConfig.addFile(oembed, location);
                        res.setLink(true);
                        res.setUrl(oembed);
                        res.setTitle(item.getName());
                        if (StringUtils.isBlank(title)) res.setTitle(oembed);
                        // queue this to output the XML link file
                        ccConfig.getLinkSet().add(res);
                        // no actual item, so we need to supply a title. Use the URL
                        urlTitle = oembed;
                        break;
                    }
                case SimplePageItem.RESOURCE:
                    res = ccConfig.getFileMap().get(item.getSakaiId());
                    break;
                case SimplePageItem.ASSIGNMENT:
                    sakaiId = item.getSakaiId();
                    if (sakaiId.indexOf("/", 1) < 0) {
                        sakaiId = "assignment/" + sakaiId;
                    } else {
                        sakaiId = sakaiId.substring(1);
                    }
                    res = ccConfig.getAssignmentMap().get(sakaiId);
                    break;
                case SimplePageItem.ASSESSMENT:
                    sakaiId = item.getSakaiId();
                    if (sakaiId.indexOf("/", 1) < 0) {
                        sakaiId = "sam_pub/" + sakaiId;
                    } else {
                        sakaiId = sakaiId.substring(1);
                    }
                    res = ccConfig.getSamigoMap().get(sakaiId);
                    break;
                case SimplePageItem.SCORM:
                    break;
                case SimplePageItem.TEXT:
                    res = ccConfig.getFileMap().get("/text/" + item.getId());
                    break;
                case SimplePageItem.FORUM:
                    res = ccConfig.getForumsMap().get(item.getSakaiId().substring(1));
                    break;
                case SimplePageItem.BLTI:
                    res = ccConfig.getBltiMap().get(item.getSakaiId().substring(1));
                    break;
                case SimplePageItem.COMMENTS:
                case SimplePageItem.STUDENT_CONTENT:
                case SimplePageItem.QUESTION:
                case SimplePageItem.PEEREVAL:
                    switch (item.getType()) {
                        case SimplePageItem.COMMENTS:
                            itemString = messageSource.getMessage("simplepage.comments-section", null, ccConfig.getLocale());
                            break;
                        case SimplePageItem.STUDENT_CONTENT:
                            itemString = messageSource.getMessage("simplepage.student-content", null, ccConfig.getLocale());
                            break;
                        case SimplePageItem.QUESTION:
                            itemString = messageSource.getMessage("simplepage.questionName", null, ccConfig.getLocale());
                            break;
                        case SimplePageItem.PEEREVAL:
                            itemString = messageSource.getMessage("simplepage.peerEval-secotion", null, ccConfig.getLocale());
                            break;
                        case SimplePageItem.BREAK:
                            itemString = messageSource.getMessage("simplepage.break", null, ccConfig.getLocale());
                            break;
                        default:
                            break;
                    }
                    ccConfig.getResults().add(messageSource.getMessage("simplepage.exportcc-bad-type", null, ccConfig.getLocale()).replace("{1}", title).replace("{2}", item.getName()).replace("{3}", itemString));
                    break;
                default:
                    break;
            }
            if (res != null) {
                ccUtils.outputIndent(out, indent + 2);
                out.println("<item identifier=\"item_" + item.getId() + "\" identifierref=\"" + res.getResourceId() + "\">");
                String iTitle = item.getName();

                if (StringUtils.isBlank(iTitle)) {
                    if (urlTitle != null) {
                        iTitle = urlTitle;
                    } else {
                        iTitle = messageSource.getMessage("simplepage.importcc-texttitle", null, ccConfig.getLocale());
                    }
                }
                ccUtils.outputIndent(out, indent + 4);
                out.println("<title>" + StringEscapeUtils.escapeXml11(iTitle) + "</title>");
                // output Sakai-specific information, if any
                outputItemMetadata(out, indent, item);
                ccUtils.outputIndent(out, indent + 2);
                out.println("</item>");
            }
        }
        ccUtils.outputIndent(out, indent);
        out.println("</item>");
        if (showNext) return null;
        return next;
    }

    public void outputItemMetadata(ZipPrintStream out, int indent, SimplePageItem item) {
        // inline types
        switch (item.getType()) {
            case SimplePageItem.MULTIMEDIA:
                String mmDisplayType = item.getAttribute("multimediaDisplayType");
                if (StringUtils.isBlank(mmDisplayType)) mmDisplayType = "2";
                ccUtils.outputIndent(out, indent + 4);
                out.println("<metadata>");
                ccUtils.outputIndent(out, indent + 6);
                out.println("<lom:lom>");
                ccUtils.outputIndent(out, indent + 8);
                out.println("<lom:general>");
                ccUtils.outputIndent(out, indent + 10);
                out.println("<lom:structure>");
                ccUtils.outputIndent(out, indent + 12);
                out.println("<lom:source>inline.lessonbuilder.sakaiproject.org</lom:source>");
                ccUtils.outputIndent(out, indent + 12);
                out.println("<lom:value>true</lom:value>");
                ccUtils.outputIndent(out, indent + 12);
                out.println("<lom:source>mmDisplayType.lessonbuilder.sakaiproject.org</lom:source>");
                ccUtils.outputIndent(out, indent + 12);
                out.println("<lom:value>" + mmDisplayType + "</lom:value>");
                ccUtils.outputIndent(out, indent + 10);
                out.println("</lom:structure>");
                ccUtils.outputIndent(out, indent + 8);
                out.println("</lom:general>");
                ccUtils.outputIndent(out, indent + 6);
                out.println("</lom:lom>");
                ccUtils.outputIndent(out, indent + 4);
                out.println("</metadata>");
                break;
            case SimplePageItem.TEXT:
                ccUtils.outputIndent(out, indent + 4);
                out.println("<metadata>");
                ccUtils.outputIndent(out, indent + 6);
                out.println("<lom:lom>");
                ccUtils.outputIndent(out, indent + 8);
                out.println("<lom:general>");
                ccUtils.outputIndent(out, indent + 10);
                out.println("<lom:structure>");
                ccUtils.outputIndent(out, indent + 12);
                out.println("<lom:source>inline.lessonbuilder.sakaiproject.org</lom:source>");
                ccUtils.outputIndent(out, indent + 12);
                out.println("<lom:value>true</lom:value>");
                ccUtils.outputIndent(out, indent + 10);
                out.println("</lom:structure>");
                ccUtils.outputIndent(out, indent + 8);
                out.println("</lom:general>");
                ccUtils.outputIndent(out, indent + 6);
                out.println("</lom:lom>");
                ccUtils.outputIndent(out, indent + 4);
                out.println("</metadata>");
                break;
            default:
                break;
        }
    }
}
