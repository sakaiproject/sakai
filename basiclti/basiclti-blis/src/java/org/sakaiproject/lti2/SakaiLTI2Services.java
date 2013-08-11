
package org.sakaiproject.lti2;

import org.imsglobal.lti2.objects.*;

public class SakaiLTI2Services {

    public static Service_offered BasicOutcomes(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id(endpoint);    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat("application/vnd.sakai.lti.v1.Outcome+form");
		ret.setAction("POST");
		return ret;
    }

    public static Service_offered BasicRoster(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id(endpoint);    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat("application/vnd.sakai.lti.v1.Roster+form");
		return ret;
    }

    public static Service_offered BasicSettings(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id(endpoint);    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat("application/vnd.sakai.lti.v1.Settings+form");
		return ret;
    }

    public static Service_offered LORI_XML(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id(endpoint);    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat("application/vnd.sakai.lti.v1.LORI+xml");
		return ret;
    }

}
