
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
