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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringEscapeUtils;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lti.api.LTIService;

@Slf4j
public class BltiExport {
    private static SimplePageToolDao simplePageToolDao;
    static MessageLocator messageLocator = null;

    protected static LTIService ltiService = null;

    public void setSimplePageToolDao(Object dao) {
	simplePageToolDao = (SimplePageToolDao)dao;
    }

    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    public void init() {
	log.info("init()");

	if (ltiService == null) {
	    Object service = ComponentManager.get("org.sakaiproject.lti.api.LTIService");
	    if (service == null) {
		log.info("can't find LTI Service -- disabling LTI support");
		return;
	    }
	    ltiService = (LTIService)service;
	    log.info("LTI Export initialized");
	}
    }

    public void destroy() {
	log.info("destroy()");
    }

    public List<String> getEntitiesInSite(String siteId, CCExport bean) {
	List<String> ret = new ArrayList();
	if (ltiService == null)
	    return ret;
	List<Map<String,Object>> contents = null;
	try {
	    contents = ltiService.getContents(null, null, 0, 0, siteId);
	} catch (Exception e) {
	    // this should never happen, but we saw it once
	    return null;
	}
		
	for (Map<String,Object> content : contents) {
	    Long id = getLong(content.get(LTIService.LTI_ID));
	    if (id.longValue() != -1L && entityReal(id, siteId)) {
		ret.add("blti/" + id);
	    }
	}
	return ret;
    }

    // we saw a weird site where this wasn't true. Admin had changed accessibility of tool
    public boolean entityReal(Long bkey, String siteId) {
	Map content = ltiService.getContent(bkey, siteId);
	if (content == null)
	    return false;
	Long toolKey = getLongNull(content.get(LTIService.LTI_TOOL_ID));
	if (toolKey == null)
	    return false;
	Map tool = ltiService.getTool(toolKey, siteId);
	if (tool == null)
	    return false;
	return true;
    }

    public boolean outputEntity(String bltiRef, ZipPrintStream out, PrintStream errStream, CCExport bean, CCExport.Resource resource, int version) {
	int i = bltiRef.indexOf("/");
	String id = bltiRef.substring(i + 1);

	Long bkey = getLong(id);
	Map content = ltiService.getContent(bkey, bean.siteId);
	if (content == null)
	    return false;
	Long toolKey = getLongNull(content.get(LTIService.LTI_TOOL_ID));
	if (toolKey == null)
	    return false;
	Map tool = ltiService.getTool(toolKey, bean.siteId);
	if (tool == null)
	    return false;
	
	String title = (String)tool.get(LTIService.LTI_TITLE);
	String launch_url = (String)tool.get(LTIService.LTI_LAUNCH);
	String custom1 = (String)tool.get(LTIService.LTI_CUSTOM);
	if (content.get(LTIService.LTI_TITLE) != null)
	    title = (String)content.get(LTIService.LTI_TITLE);
	String custom2 = (String)content.get(LTIService.LTI_CUSTOM);

	List<String> custom = new ArrayList<String>();
	if ((custom1 != null) && (!custom1.trim().equals("")))
	    custom.addAll(Arrays.asList(custom1.split("\n")));
	if ((custom2 != null) && (!custom2.trim().equals(""))) {
	    custom.addAll(Arrays.asList(custom2.split("\n")));
	}
	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	switch (version) {
	case CCExport.V11:
	    out.println("<cartridge_basiclti_link");
	    out.println("      xmlns=\"http://www.imsglobal.org/xsd/imslticc_v1p0\"");
	    out.println("      xmlns:blti=\"http://www.imsglobal.org/xsd/imsbasiclti_v1p0\"");
	    out.println("      xmlns:lticm=\"http://www.imsglobal.org/xsd/imslticm_v1p0\"");
	    out.println("      xmlns:lticp=\"http://www.imsglobal.org/xsd/imslticp_v1p0\"");
	    out.println("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
	    out.println("      xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imslticc_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticc_v1p0.xsd http://www.imsglobal.org/xsd/imslticp_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticp_v1p0.xsd http://www.imsglobal.org/xsd/imslticm_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticm_v1p0.xsd http://www.imsglobal.org/xsd/imsbasiclti_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imsbasiclti_v1p0p1.xsd\">");
	    break;
	case CCExport.V13:
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
	}
	
	out.println("      <blti:title>" + StringEscapeUtils.escapeXml(title) + "</blti:title>");

	if (custom.size() > 0) {
	    out.println("      <blti:custom>");

	    for (String attr : custom) {
		int k = attr.indexOf("=");
		if (k >= 0) {
		    String key = attr.substring(0, k).trim();
		    String value = attr.substring(k + 1).trim();
		    out.println("        <lticm:property name=\"" + StringEscapeUtils.escapeXml(key) + "\">" + StringEscapeUtils.escapeXml(value) + "</lticm:property>");
		}
	    }
	    out.println("      </blti:custom>");
	}
	out.println("      <blti:launch_url>" + StringEscapeUtils.escapeXml(launch_url) + "</blti:launch_url>");
	out.println("      <blti:vendor>");
	out.println("        <lticp:code>" + StringEscapeUtils.escapeXml(ServerConfigurationService.getServerName()) + "</lticp:code>");
	out.println("        <lticp:name>" + StringEscapeUtils.escapeXml(ServerConfigurationService.getString("ui.institution", "Sakai")) + "</lticp:name>");
	out.println("      </blti:vendor>");
	out.println("</cartridge_basiclti_link>");
	return true;
    }

    public Long getLong(Object key) {
	Long retval = getLongNull(key);
	if (retval != null)
	    return retval;
	return new Long(-1L);
    }

    public Long getLongNull(Object key) {
	if (key == null)
	    return null;
	if ((key instanceof Number))
	    return new Long(((Number)key).longValue());
	if ((key instanceof String)) {
	    try {
		return new Long((String)key);
	    } catch (Exception e) {
		return null;
	    }
	}
	return null;
    }
}
