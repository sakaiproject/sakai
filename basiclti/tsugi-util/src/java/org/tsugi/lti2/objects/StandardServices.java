
package org.tsugi.lti2.objects;

public class StandardServices {

    public static final String TOOLPROXY_FORMAT = "application/vnd.ims.lti.v2.toolproxy+json";
    public static final String TOOLSETTINGS_CONTEXT = "http://purl.imsglobal.org/ctx/lti/v2/ToolSettings";
    public static final String TOOLSETTINGS_FORMAT = "application/vnd.ims.lti.v2.toolsettings+json";
    public static final String TOOLSETTINGS_SIMPLE_FORMAT = "application/vnd.ims.lti.v2.toolsettings.simple+json";

    public static final String TOOLPROXY_ID_CONTEXT = "http://purl.imsglobal.org/ctx/lti/v2/ToolProxyId";
    public static final String TOOLPROXY_ID_FORMAT = "application/vnd.ims.lti.v2.toolproxy.id+json";
    public static final String TOOLPROXY_ID_TYPE = "ToolProxy";

	public static final String RESULT_FORMAT = "application/vnd.ims.lis.v2.result+json";
	public static final String RESULT_CONTEXT = "http://purl.imsglobal.org/ctx/lis/v2/Result";
	public static final String RESULT_TYPE = "Result";

    public static Service_offered LTI2Registration(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:ToolProxy.collection");    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat(TOOLPROXY_FORMAT);
		ret.setAction("POST");
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/ToolProxy/{tool_proxy_guid}",
    public static Service_offered LTI2ProxyItem(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:ToolProxy.item");
		ret.set_type("RestService");
		ret.setFormat(TOOLPROXY_FORMAT);
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/Result/{sourcedId}",
    public static Service_offered LTI2ResultItem(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:Result.item");    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat(RESULT_FORMAT);
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/ToolProxy/{tool_proxy_guid}/custom",
    public static Service_offered LTI2ToolProxySettings(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:ToolProxySettings");    
		ret.set_type("RestService");
		ret.setFormat(new String[] {TOOLSETTINGS_FORMAT, TOOLSETTINGS_SIMPLE_FORMAT});
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/links/{link_id}/custom",
    public static Service_offered LTI2LtiLinkSettings(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:LtiLinkSettings");    
		ret.set_type("RestService");
		ret.setFormat(new String[] {TOOLSETTINGS_FORMAT, TOOLSETTINGS_SIMPLE_FORMAT});
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

    public static Service_offered LTI1Outcomes(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:LTI_1_1_ResultService");
		ret.set_type("RestService");
		ret.setFormat("application/vnd.ims.lti.v1.outcome+xml");
		ret.setAction("POST");
		return ret;
    }


}
