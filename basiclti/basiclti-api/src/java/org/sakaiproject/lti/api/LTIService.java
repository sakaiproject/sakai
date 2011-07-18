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
	public Map<String,Object> getTool(String url);
	public boolean deleteTool(Long key);
	public Object updateTool(Long key, Map<String,Object> newProps);
	public Object updateTool(Long key, Properties newProps);
	public List<Map<String, Object>> getTools(String search, String order, int first, int last) ;
	
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
	        "SITE_ID:hidden:length=99",
		"preferheight:integer:label=bl_preferheight",
		"launchinpopup:radio:label=bl_launchinpopup:choices=off,on",
		"debuglaunch:radio:label=bl_debuglaunch:choices=off,on",
		"acceptgrades:radio:label=bl_acceptgrades:choices=off,on",
		"launchinpopup:radio:label=bl_launchinpopup:choices=off,on",
		"customparameters:textarea:label=bl_customparameters:rows=5:cols=25"} ; 

	static String [] TOOL_MODEL = {
		"id:key",
	        "SITE_ID:hidden:length=99",
		"title:text:label=bl_title:required=true:maxlength=255",
		"description:textarea:label=bl_description:required=true:rows=2:cols=25",
		"toolurl:url:label=bl_toolurl:required=true:maxlength=255",
		"resourcekey:text:label=bl_resourcekey:required=true:maxlength=255",
		"password:text:required=true:label=bl_password:maxlength=255",
		"preferheight:integer:label=bl_preferheight",
		"allowpreferheight:radio:label=bl_allowpreferheight:choices=off,on",
		"launchinpopup:radio:label=bl_launchinpopup:choices=off,on,content",
		"debuglaunch:radio:label=bl_debuglaunch:choices=off,on,content",
		"sendname:radio:label=bl_sendname:choices=off,on,content",
		"sendemailaddr:radio:label=bl_sendemailaddr:choices=off,on,content",
		"acceptgrades:radio:label=bl_acceptgrades:choices=off,on,content",
		"allowroster:radio:label=bl_allowroster:choices=off,on",
		"allowsetting:radio:label=bl_allowsetting:choices=off,on",
		"allowcustomparameters:radio:label=bl_allowcustomparameters:choices=off,on",
		"customparameters:textarea:label=bl_customparameters:rows=5:cols=25",
		"organizationid:text:label=bl_organizationid:maxlength=255",
		"organizationurl:text:label=bl_organizationurl:maxlength=255",
		"organizationdescr:text:label=bl_organizationdescr:maxlength=255"};

	static String [] MAPPING_MODEL = {
		"id:key",
		"matchpattern:url:label=bl_matchpattern:required=true:maxlength=255",
		"toolurl:url:label=bl_launchurl:required=true:maxlength=255"} ;
}
