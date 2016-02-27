/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.basiclti.util;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.casa.CASAUtil;
import org.tsugi.lti2.LTI2Config;

import org.sakaiproject.lti2.SakaiLTI2Config;
import org.sakaiproject.lti2.SakaiLTI2Base;

import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * Some Sakai Utility code for IMS CASA
 * This is mostly code to support the Sakai conventions for 
 * dealing with CASA.
 */
@SuppressWarnings("deprecation")
public class SakaiCASAUtil {

	private static Log M_log = LogFactory.getLog(SakaiCASAUtil.class);

	public static JSONObject getCASAEntry(String toolRegistration)
	{
		Tool theTool = ToolManager.getTool(toolRegistration);
		if ( theTool == null ) return null;
		LTI2Config cnf = new SakaiLTI2Config();
		boolean sample = false;
		if ( cnf.getGuid() == null ) {
			cnf = new SakaiLTI2Base();
			sample = true;
		}

		JSONObject jsonResponse = new JSONObject();
		JSONObject identity = new JSONObject();
		identity.put("product_instance_guid", cnf.getService_owner_id());
		identity.put("originator_id", cnf.getService_owner_id());
		identity.put("id", toolRegistration);
		jsonResponse.put("identity", identity);
		JSONObject use = new JSONObject();
		use.put(CASAUtil.TITLE_SCHEMA,theTool.getTitle());
		use.put(CASAUtil.TEXT_SCHEMA,theTool.getDescription());
		JSONArray contact = new JSONArray();
		JSONObject name = new JSONObject();
		name.put("name", cnf.getService_owner_owner_name());
		name.put("email", cnf.getService_owner_support_email());
		contact.add(name);
		use.put(CASAUtil.CONTACT_SCHEMA, contact);
		use.put(CASAUtil.ICON_SCHEMA, "https://www.apereo.org/sites/all/themes/apereo/images/apereo-logo-white-bg.png");

		JSONObject launch = new JSONObject();
		launch.put("launch_url", ServerConfigurationService.getServerUrl() + "/imsblis/provider/"+toolRegistration);
		use.put(CASAUtil.LAUNCH_SCHEMA, launch);

		JSONObject original = new JSONObject();
		original.put("use", use);
		original.put("timestamp", "2015-01-02T22:17:00.371Z");
		original.put("uri", ServerConfigurationService.getServerUrl());
		original.put("share", Boolean.TRUE);
		original.put("propagate", Boolean.TRUE);
		jsonResponse.put("original", original);

		return jsonResponse;
	}

}
