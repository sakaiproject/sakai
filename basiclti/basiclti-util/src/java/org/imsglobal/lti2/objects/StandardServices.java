
package org.imsglobal.lti2.objects;

public class StandardServices {

    public static Service_offered LTI2Registration(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id(endpoint);    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat("application/vnd.ims.lti.v2.ToolProxy+json");
		ret.setAction("POST");
		return ret;
    }

    public static Service_offered LTI1Outcomes(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id(endpoint);    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat("application/vnd.ims.lti.v1.Outcome+xml");
		ret.setAction("POST");
		return ret;
    }

}
