
package org.imsglobal.lti2.objects;

public class StandardServices {

    public static Service_offered LTI2Registration(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:ToolProxy.collection");    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat("application/vnd.ims.lti.v2.toolproxy+json");
		ret.setAction("POST");
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/ToolProxy/{tool_proxy_guid}",
    public static Service_offered LTI2ProxyItem(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:ToolProxy.item");    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat("application/vnd.ims.lti.v2.toolproxy+json");
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

    public static Service_offered LTI1Outcomes(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id(endpoint);    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat("application/vnd.ims.lti.v1.outcome+xml");
		ret.setAction("POST");
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/Result/{sourcedId}",
    public static Service_offered LTI2ResultItem(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:Result.item");    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat("application/vnd.ims.lis.v2.result+json");
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/links/{link_id}/custom",
    public static Service_offered LTI2LtiLinkSettings(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:LtiLinkSettings");    
		ret.set_type("RestService");
		ret.setFormat(new String[] {"application/vnd.ims.lti.v2.toolsettings+json", 
            "application/vnd.ims.lti.v2.toolsettings.simple+json"});
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/ToolProxy/{tool_proxy_guid}/custom",
    public static Service_offered LTI2ToolProxySettings(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:ToolProxySettings");    
		ret.set_type("RestService");
		ret.setFormat(new String[] {"application/vnd.ims.lti.v2.toolsettings+json", 
            "application/vnd.ims.lti.v2.toolsettings.simple+json"});
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

}
