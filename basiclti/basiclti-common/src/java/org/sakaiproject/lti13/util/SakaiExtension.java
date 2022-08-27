/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
 package org.sakaiproject.lti13.util;

import java.util.Properties;
import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated("com.googlecode.jsonschema2pojo")

// ext_sakai_academic_session: OTHER
// ext_sakai_launch_presentation_css_url_list: http://localhost:8080/library/skin/tool_base.css,http://localhost:8080/library/skin/morpheus-default/tool.css?version=49b21ca5
// ext_sakai_role: maintain
// ext_sakai_server: http://localhost:8080
// ext_sakai_serverid: MacBook-Pro-92.local
// ext_sakai_eid: person@example.com
//
public class SakaiExtension extends org.tsugi.jackson.objects.JacksonBase {

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

	@JsonProperty("sakai_eid")
	public String sakai_eid;

	public void copyFromPost(Properties ltiProps) {
		this.sakai_launch_presentation_css_url_list = ltiProps.getProperty("ext_sakai_launch_presentation_css_url_list");
		this.sakai_academic_session = ltiProps.getProperty("ext_sakai_academic_session");
		this.sakai_role = ltiProps.getProperty("ext_sakai_role");
		this.sakai_server = ltiProps.getProperty("ext_sakai_server");
		this.sakai_serverid = ltiProps.getProperty("ext_sakai_serverid");
		this.sakai_eid = ltiProps.getProperty("ext_sakai_eid");
	}

}

