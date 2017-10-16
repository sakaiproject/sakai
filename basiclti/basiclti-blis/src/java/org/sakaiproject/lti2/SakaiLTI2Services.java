/**
 * Copyright (c) 2010-2016 The Apereo Foundation
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

package org.sakaiproject.lti2;

import org.tsugi.lti2.objects.*;

public class SakaiLTI2Services {

    public static Service_offered BasicOutcomes(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:SakaiOutcomeForm");
		ret.set_type("RestService");
		ret.setFormat("application/vnd.sakai.lti.v1.outcome+form");
		ret.setAction("POST");
		return ret;
    }

    public static Service_offered BasicRoster(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:SakaiRosterForm");
		ret.set_type("RestService");
		ret.setFormat("application/vnd.sakai.lti.v1.roster+form");
		ret.setAction("POST");
		return ret;
    }

    public static Service_offered BasicSettings(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:SakaiSettingsForm");
		ret.set_type("RestService");
		ret.setFormat("application/vnd.sakai.lti.v1.settings+form");
		ret.setAction("POST");
		return ret;
    }

}
