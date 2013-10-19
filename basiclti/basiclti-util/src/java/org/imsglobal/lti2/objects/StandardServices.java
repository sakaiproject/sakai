
package org.imsglobal.lti2.objects;

public class StandardServices {

    public static final String FORMAT_TOOLPROXY = "application/vnd.ims.lti.v2.toolproxy+json";
    public static final String FORMAT_TOOLPROXY_ID = "application/vnd.ims.lti.v2.toolproxy.id+json";
    public static final String FORMAT_TOOLSETTINGS = "application/vnd.ims.lti.v2.toolsettings+json";
    public static final String FORMAT_TOOLSETTINGS_SIMPLE = "application/vnd.ims.lti.v2.toolsettings.simple+json";
    public static final String FORMAT_RESULT = "application/vnd.ims.lis.v2.result+json";

    public static final String TOOL_PROXY_GUID = "{tool_proxy_guid}";
    public static final String LINK_ID = "{link_id}";
    public static final String SOURCEDID = "{sourcedId}";

    public static Service_offered LTI2Registration(String endpoint) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(endpoint);
		ret.set_id("tcp:ToolProxy.collection");    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat(FORMAT_TOOLPROXY);
		ret.setAction("POST");
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/ToolProxy/{tool_proxy_guid}",
    public static Service_offered LTI2ProxyItem(String resources) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(resources+"/ToolProxy/{tool_proxy_guid}");
		ret.set_id("tcp:ToolProxy.item");
		ret.set_type("RestService");
		ret.setFormat(FORMAT_TOOLPROXY);
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/Result/{sourcedId}",
    public static Service_offered LTI2ResultItem(String resources) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(resources+"/Result/{sourcedId}");
		ret.set_id("tcp:Result.item");    // TODO: Is this right see 5.6 in the docs
		ret.set_type("RestService");
		ret.setFormat(FORMAT_RESULT);
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/ToolProxy/{tool_proxy_guid}/custom",
    public static Service_offered LTI2ToolProxySettings(String resources) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(resources+"/ToolProxy/{tool_proxy_guid}/custom");
		ret.set_id("tcp:ToolProxySettings");    
		ret.set_type("RestService");
		ret.setFormat(new String[] {FORMAT_TOOLSETTINGS, FORMAT_TOOLSETTINGS_SIMPLE});
		ret.setAction(new String[] {"GET", "PUT"});
		return ret;
    }

    // "endpoint" : "http://lms.example.com/resources/links/{link_id}/custom",
    public static Service_offered LTI2LtiLinkSettings(String resources) {
		Service_offered ret = new Service_offered();
		ret.setEndpoint(resources+"/links/{link_id}/custom");
		ret.set_id("tcp:LtiLinkSettings");    
		ret.set_type("RestService");
		ret.setFormat(new String[] {FORMAT_TOOLSETTINGS, FORMAT_TOOLSETTINGS_SIMPLE});
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
