/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.component.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handle any default sakai.properties values that need to be set dynamically.
 */
public class DynamicDefaultSakaiProperties extends Properties {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(DynamicDefaultSakaiProperties.class);
	
	public void init() {
		try {
			String defaultServerId = InetAddress.getLocalHost().getHostName();
			this.put("serverId", defaultServerId);
			if (log.isDebugEnabled()) log.debug("Set serverId to " + defaultServerId);
		} catch (UnknownHostException e) {
			if (log.isDebugEnabled()) log.debug(e);
		}
	}
}
