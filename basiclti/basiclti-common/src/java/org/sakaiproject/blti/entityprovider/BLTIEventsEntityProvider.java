/**
 * Copyright (c) 2009-2011 The Apereo Foundation
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
package org.sakaiproject.blti.entityprovider;

import java.util.Locale;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Statisticable;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

/**
 * An entity provider to register Basic LTI events with SiteStats 
 * 
 */
public class BLTIEventsEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Statisticable, Describeable {

	public final static String PREFIX = "basiclti-events";
	public final static String TOOL_ID = "sakai.basiclti";

	public final static String[] EVENT_KEYS = new String[] {
		"basiclti.launch",
			"basiclti.config"
	};



	public String getEntityPrefix() {
		return PREFIX;
	}

	public String getAssociatedToolId() {
		return TOOL_ID;
	}

	public String[] getEventKeys() {
		return EVENT_KEYS;
	}

	public Map<String, String> getEventNames(Locale locale) {
		return null;
	}

}
