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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BLTIExport {

    @Setter private LTIService ltiService;

    public List<String> getEntitiesInSite(String siteId) {
        List<String> list = new ArrayList<>();
        List<Map<String, Object>> contents;
        try {
            contents = ltiService.getContents(null, null, 0, 0, siteId);
        } catch (Exception e) {
            // this should never happen, but we saw it once
            log.warn("Could not get LTI ContentItem's for site {}, {}", siteId, e.toString());
            contents = new ArrayList<>();
        }

        for (Map<String, Object> content : contents) {
            Long id = getLong(content.get(LTIService.LTI_ID));
            if (id != -1L && validEntity(id, siteId)) {
                list.add("blti/" + id);
            }
        }
        return list;
    }

    public boolean outputEntity(String siteId, String ltiReference, ZipPrintStream out, CCVersion ccVersion) {
        int i = ltiReference.indexOf("/");
        String id = ltiReference.substring(i + 1);

        Map<String, Object> content = ltiService.getContent(getLong(id), siteId);
        if (content == null) return false;
        Long toolKey = getLongNull(content.get(LTIService.LTI_TOOL_ID));
        if (toolKey == null) return false;
        Map<String, Object> tool = ltiService.getTool(toolKey, siteId);
        if (tool == null) return false;

        String title = (String) tool.get(LTIService.LTI_TITLE);
        String launch_url = (String) tool.get(LTIService.LTI_LAUNCH);
        String custom1 = (String) tool.get(LTIService.LTI_CUSTOM);
        if (content.get(LTIService.LTI_TITLE) != null) {
            title = (String) content.get(LTIService.LTI_TITLE);
        }

        List<String> custom = new ArrayList<>();
        if ((custom1 != null) && (!custom1.trim().equals(""))) {
            custom.addAll(Arrays.asList(custom1.split("\n")));
        }

        String custom2 = (String) content.get(LTIService.LTI_CUSTOM);
        if ((custom2 != null) && (!custom2.trim().equals(""))) {
            custom.addAll(Arrays.asList(custom2.split("\n")));
        }
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        switch (ccVersion) {
            case V11:
                out.println("<cartridge_basiclti_link");
                out.println("      xmlns=\"http://www.imsglobal.org/xsd/imslticc_v1p0\"");
                out.println("      xmlns:blti=\"http://www.imsglobal.org/xsd/imsbasiclti_v1p0\"");
                out.println("      xmlns:lticm=\"http://www.imsglobal.org/xsd/imslticm_v1p0\"");
                out.println("      xmlns:lticp=\"http://www.imsglobal.org/xsd/imslticp_v1p0\"");
                out.println("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
                out.println("      xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imslticc_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticc_v1p0.xsd http://www.imsglobal.org/xsd/imslticp_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticp_v1p0.xsd http://www.imsglobal.org/xsd/imslticm_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticm_v1p0.xsd http://www.imsglobal.org/xsd/imsbasiclti_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imsbasiclti_v1p0p1.xsd\">");
                break;
            case V13:
                out.println("<cartridge_basiclti_link");
                out.println("      xmlns=\"http://www.imsglobal.org/xsd/imslticc_v1p0\"");
                out.println("      xmlns:blti=\"http://www.imsglobal.org/xsd/imsbasiclti_v1p0\"");
                out.println("      xmlns:lticm=\"http://www.imsglobal.org/xsd/imslticm_v1p0\"");
                out.println("      xmlns:lticp=\"http://www.imsglobal.org/xsd/imslticp_v1p0\"");
                out.println("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
                out.println("      xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imslticc_v1p3 http://www.imsglobal.org/xsd/lti/ltiv1p3/imslticc_v1p3.xsd http://www.imsglobal.org/xsd/imslticp_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticp_v1p0.xsd http://www.imsglobal.org/xsd/imslticm_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticm_v1p0.xsd http://www.imsglobal.org/xsd/imsbasiclti_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imsbasiclti_v1p0p1.xsd\">");
                break;
            default:
                out.println("<cartridge_basiclti_link xmlns=\"http://www.imsglobal.org/xsd/imslticc_v1p0\"");
                out.println("      xmlns:blti = \"http://www.imsglobal.org/xsd/imsbasiclti_v1p0\"");
                out.println("      xmlns:lticm =\"http://www.imsglobal.org/xsd/imslticm_v1p0\"");
                out.println("      xmlns:lticp =\"http://www.imsglobal.org/xsd/imslticp_v1p0\"");
                out.println("      xmlns:xsi = \"http://www.w3.org/2001/XMLSchema-instance\"");
                out.println("      xsi:schemaLocation = \"http://www.imsglobal.org/xsd/imslticc_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticc_v1p0p1.xsd http://www.imsglobal.org/xsd/imsbasiclti_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imsbasiclti_v1p0p1.xsd  http://www.imsglobal.org/xsd/imslticm_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticm_v1p0.xsd http://www.imsglobal.org/xsd/imslticp_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticp_v1p0.xsd\">");
                break;
        }

        out.println("      <blti:title>" + StringEscapeUtils.escapeXml11(title) + "</blti:title>");

        if (custom.size() > 0) {
            out.println("      <blti:custom>");

            for (String attr : custom) {
                int k = attr.indexOf("=");
                if (k >= 0) {
                    String key = attr.substring(0, k).trim();
                    String value = attr.substring(k + 1).trim();
                    out.println("        <lticm:property name=\"" + StringEscapeUtils.escapeXml11(key) + "\">" + StringEscapeUtils.escapeXml11(value) + "</lticm:property>");
                }
            }
            out.println("      </blti:custom>");
        }
        out.println("      <blti:launch_url>" + StringEscapeUtils.escapeXml11(launch_url) + "</blti:launch_url>");
        out.println("      <blti:vendor>");
        out.println("        <lticp:code>" + StringEscapeUtils.escapeXml11(ServerConfigurationService.getServerName()) + "</lticp:code>");
        out.println("        <lticp:name>" + StringEscapeUtils.escapeXml11(ServerConfigurationService.getString("ui.institution", "Sakai")) + "</lticp:name>");
        out.println("      </blti:vendor>");
        out.println("</cartridge_basiclti_link>");
        return true;
    }

    // we saw a weird site where this wasn't true. Admin had changed accessibility of tool
    private boolean validEntity(Long key, String siteId) {
        Map<String, Object> content = ltiService.getContent(key, siteId);
        if (content == null) return false;
        Long toolKey = getLongNull(content.get(LTIService.LTI_TOOL_ID));
        if (toolKey == null) return false;
        Map<String, Object> tool = ltiService.getTool(toolKey, siteId);
        return tool != null;
    }

    private Long getLong(Object key) {
        Long keyLong = getLongNull(key);
        if (keyLong != null) return keyLong;
        return -1L;
    }

    private Long getLongNull(Object key) {
        if (key == null) {
            return null;
        } else if ((key instanceof Number)) {
            return ((Number) key).longValue();
        } else if ((key instanceof String)) {
            try {
                return new Long((String) key);
            } catch (NumberFormatException nfe) {
                log.debug(nfe.toString());
                return null;
            }
        } else {
            return null;
        }
    }
}
