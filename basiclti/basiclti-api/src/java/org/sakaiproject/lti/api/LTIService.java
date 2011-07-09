/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.lti.api;

import java.util.List;

import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * A LTIService does things for LTI
 * </p>
 * <p>
 * Location is a combination of site id, (optional) page id and (optional) tool id
 * </p>
 */
public interface LTIService
{
	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/basiclti";

	/** Name for the event of establishing presence at a location. */
	static final String EVENT_PRESENCE = "pres.begin";

	/** Name for the event of ending presence at a location. */
	static final String EVENT_ABSENCE = "pres.end";

}
