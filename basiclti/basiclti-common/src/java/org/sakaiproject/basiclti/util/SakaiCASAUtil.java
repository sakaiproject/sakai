/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2016- Charles R. Severance
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

import org.tsugi.casa.objects.Launch;
import org.tsugi.casa.objects.Use;
import org.tsugi.casa.objects.Contact;
import org.tsugi.casa.objects.Original;
import org.tsugi.casa.objects.Identity;
import org.tsugi.casa.objects.Application;
import org.tsugi.lti2.LTI2Config;

import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lti2.SakaiLTI2Base;
import org.sakaiproject.lti2.SakaiLTI2Config;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * Some Sakai Utility code for IMS CASA
 * This is mostly code to support the Sakai conventions for 
 * dealing with CASA.
 */
@SuppressWarnings("deprecation")
public class SakaiCASAUtil {

	public static Application getCASAEntry(String toolRegistration)
	{
		Tool theTool = ToolManager.getTool(toolRegistration);
		if ( theTool == null ) return null;
		LTI2Config cnf = new SakaiLTI2Config();
		boolean sample = false;
		if ( cnf.getGuid() == null ) {
			cnf = new SakaiLTI2Base();
			sample = true;
		}

		Launch launch = new Launch();
		launch.setLaunch_url(SakaiBLTIUtil.getOurServerUrl() + "/imsblti/provider/"+toolRegistration);

		Use use = new Use(launch);
		// TODO: Fix this by generating all the PNGs for Sakai icons
		use.setIcon_url("https://www.apereo.org/sites/all/themes/apereo/images/apereo-logo-white-bg.png");
		use.setTitle(theTool.getTitle());

		String desc = theTool.getDescription();
		if ( desc == null ) desc = theTool.getTitle();
		if ( desc == null ) desc = "";
                String note = ServerConfigurationService.getString("casa.provider.note", null);
		if ( note != null ) {
			desc = desc.trim();
			if ( ! desc.endsWith(".") ) desc += ".";
			desc += " "+note;
		}
		use.setText(desc);
		use.addContact(new Contact(cnf.getService_owner_owner_name(), cnf.getService_owner_support_email()));

		Original orig = new Original(use);
		orig.setUri(SakaiBLTIUtil.getOurServerUrl());
		orig.setPropagate(Boolean.TRUE);
		orig.setShare(Boolean.TRUE);
		// TODO: Fix this when I know the rules
		orig.setTimestamp("2015-01-02T22:17:00.371Z");

		Identity identity = new Identity(cnf.getService_owner_id(), toolRegistration);

		Application app = new Application(identity,orig);
		return app;
	}

}
