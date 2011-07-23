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
import java.util.Map;
import java.util.Properties;

import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.user.api.User;
// TODO: Should work with kernel-util moved to shared
// import org.sakaiproject.util.ResourceLoader;


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
	/* This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/lti";
	
	public boolean isAdmin();
	public boolean isMaintain();

	/* Mapping */
	public String [] getMappingModel() ;
	public Object insertMapping(Properties newProps);
	public  Map<String,Object> getMapping(Long key);
	public boolean deleteMapping(Long key);
	public Object updateMapping(Long key, Map<String, Object> newProps);
	public Object updateMapping(Long key, Properties newProps);
	public List<Map<String, Object>> getMappings(String search, String order, int first, int last) ;
	
	/* Tool */
	public String [] getToolModel() ;
	public Object insertTool(Properties newProps);
	public Map<String,Object> getTool(Long key);
	public Map<String,Object> getToolNoAuthz(Long key);
	public Map<String,Object> getTool(String url);
	public boolean deleteTool(Long key);
	public Object updateTool(Long key, Map<String,Object> newProps);
	public Object updateTool(Long key, Properties newProps);
	public List<Map<String, Object>> getTools(String search, String order, int first, int last) ;

        public String checkMapping(String url);

	/* Content */
	public String [] getContentModel(Long tool_id) ;
	public Object insertContent(Properties newProps);
	public Map<String,Object> getContent(Long key);
	public Map<String,Object> getContentNoAuthz(Long key);
	public boolean deleteContent(Long key);
	public Object updateContent(Long key, Map<String,Object> newProps);
	public Object updateContent(Long key, Properties newProps);
	public List<Map<String, Object>> getContents(String search, String order, int first, int last) ;
        public String getContentLaunch(Map<String,Object> content);
        public void filterContent(Map<String,Object> content, Map<String,Object> tool);
	
	public String formOutput(Object row, String fieldInfo);
	public String formOutput(Object row, String [] fieldInfo);
	public String formInput(Object row, String fieldInfo);
	public String formInput(Object row, String [] fieldInfo);

	/** Model Descriptions for Foorm 
	 * You should probably retrieve these through getters in case there is some
	 * filtering in the service based on role/permission */
	 
	 // For Instructors, this model is filtered down dynamically based on
	// Tool settings
	static String [] CONTENT_MODEL = {
	        "id:key",
                "tool_id:integer:hidden=true",
	        "SITE_ID:text:maxlength=99:label=bl_content_site_id:role=admin",
		"title:text:label=bl_content_title:required=true:maxlength=255",
		"description:textarea:label=bl_description:rows=2:cols=25:maxlength=4096:",
		"frameheight:integer:label=bl_frameheight",
		"newpage:radio:label=bl_newpage:choices=off,on",
		"debug:radio:label=bl_debug:choices=off,on",
		"custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=1024",
                "launch:url:hidden=true:maxlength=255",
                "xmlimport:text:hidden=true:maxlength=16384",
                "created_at:autodate",
                "updated_at:autodate"
        } ; 

	static String [] TOOL_MODEL = {
		"id:key",
		"title:text:label=bl_title:required=true:maxlength=255",
		"description:textarea:label=bl_description:rows=2:cols=25:maxlength=4096:",
                "status:radio:label=bl_status:choices=enable,disable",
		"launch:url:label=bl_launch:required=true:maxlength=255",
		"consumerkey:text:label=bl_consumerkey:required=true:maxlength=255",
		"secret:text:required=true:label=bl_secret:maxlength=255",
	        "SITE_ID:text:maxlength=99:role=admin",
		"frameheight:integer:label=bl_frameheight",
		"allowframeheight:radio:label=bl_allowframeheight:choices=off,on",
		"newpage:radio:label=bl_newpage:choices=off,on,content",
		"debug:radio:label=bl_debug:choices=off,on,content",
		"sendname:radio:label=bl_sendname:choices=off,on",
		"sendemailaddr:radio:label=bl_sendemailaddr:choices=off,on",
		"allowcustom:radio:label=bl_allowcustom:choices=off,on",
		"custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=1024",
		"organizationid:text:label=bl_organizationid:maxlength=255",
		"organizationurl:text:label=bl_organizationurl:maxlength=255",
		"organizationdescr:text:label=bl_organizationdescr:maxlength=255",
                "created_at:autodate",
                "updated_at:autodate"
        };

	static String [] MAPPING_MODEL = {
		"id:key",
		"matchpattern:url:label=bl_matchpattern:required=true:maxlength=255",
		"launch:url:label=bl_launchurl:required=true:maxlength=255",
                "note:text:label=bl_note:maxlength=255",
                "created_at:autodate",
                "updated_at:autodate"
        } ;
}
