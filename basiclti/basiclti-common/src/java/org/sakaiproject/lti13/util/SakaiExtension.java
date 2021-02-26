package org.sakaiproject.lti13.util;

import java.util.Properties;
import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.tsugi.ags2.objects.LineItem;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated("com.googlecode.jsonschema2pojo")

// ext_sakai_academic_session: OTHER
// ext_sakai_launch_presentation_css_url_list: http://localhost:8080/library/skin/tool_base.css,http://localhost:8080/library/skin/morpheus-default/tool.css?version=49b21ca5
// ext_sakai_role: maintain
// ext_sakai_server: http://localhost:8080
// ext_sakai_serverid: MacBook-Pro-92.local
//
public class SakaiExtension extends LineItem {

	@JsonProperty("sakai_launch_presentation_css_url_list")
	public String sakai_launch_presentation_css_url_list;

	@JsonProperty("sakai_academic_session")
	public String sakai_academic_session;

	// SAK-44886
	@JsonProperty("sakai_role")
	public String sakai_role;

	@JsonProperty("sakai_server")
	public String sakai_server;

	@JsonProperty("sakai_serverid")
	public String sakai_serverid;

	public void copyFromPost(Properties ltiProps) {
		this.sakai_launch_presentation_css_url_list = ltiProps.getProperty("ext_sakai_launch_presentation_css_url_list");
		this.sakai_academic_session = ltiProps.getProperty("ext_sakai_academic_session");
		this.sakai_role = ltiProps.getProperty("ext_sakai_role");
		this.sakai_server = ltiProps.getProperty("ext_sakai_server");
		this.sakai_serverid = ltiProps.getProperty("ext_sakai_serverid");
	}

}

