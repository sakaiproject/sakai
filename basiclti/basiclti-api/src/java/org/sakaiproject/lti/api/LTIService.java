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
	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/lti";

	/** getMappingModel */
	public String [] getMappingModel() ;

	/** getMappings */
	public Map<String, Object> getMappings(String search, int first, int last) ;

	/** insertMapping */
	String insertMapping(Properties newProps);

	/** insertTool */
	String insertTool(Properties newProps);

	// TODO: Should work if kernel-util moves to shared
	/** getResourceLoader 
	public ResourceLoader getResourceLoader();
	*/

	/** Model Descriptions for Foorm 
	 * You should probably retrieve these through getters in case there is some
	 * filtering in the service based on role/permission */

	public static String [] ADMIN_TOOL_MODEL = {
		"title:text:label=bl_title:required=true:maxlength=25",
		"toolid:id:label=bl_toolid:required=true:maxlength=16",
		"description:textarea:label=bl_description:required=true:rows=2:cols=25",
		"toolurl:url:label=bl_toolurl:required=true:maxlength=80",
		"resourcekey:text:label=bl_resourcekey:required=true:maxlength=80",
		"password:text:required=true:label=bl_password:maxlength=80",
		"preferheight:integer:label=bl_preferheight:maxlength=80",
		"allowpreferheight:radio:label=bl_allowpreferheight:choices=off,on",
		"launchinpopup:radio:label=bl_launchinpopup:choices=off,on,instructor",
		"debuglaunch:radio:label=bl_debuglaunch:choices=off,on,instructor",
		"sendname:radio:label=bl_sendname:choices=off,on,instructor",
		"sendemailaddr:radio:label=bl_sendemailaddr:choices=off,on,instructor",
		"acceptgrades:radio:label=bl_acceptgrades:choices=off,on",
		"allowroster:radio:label=bl_allowroster:choices=off,on,instructor",
		"allowsetting:radio:label=bl_allowsetting:choices=off,on,instructor",
		"allowcustomparameters:radio:label=bl_allowcustomparameters:choices=off,on",
		"customparameters:textarea:label=bl_customparameters:rows=5:cols=25",
		"organizationid:text:label=bl_organizationid:maxlength=80",
		"organizationurl:text:label=bl_organizationurl:maxlength=80",
		"organizationdescr:text:label=bl_organizationdescr:maxlength=80" };

	// This will be further reduced by the getters and the control row
	public static String [] INSTRUCTOR_TOOL_MODEL = {
		"title:text:label=bl_title:required=true:maxlength=25",
		"toolid:id:label=bl_toolid:required=true:maxlength=16",
		"description:textarea:label=bl_description:required=true:rows=2:cols=25",
		"toolurl:url:label=bl_toolurl:required=true:maxlength=80",
		"resourcekey:text:label=bl_resourcekey:required=true:maxlength=80",
		"password:text:required=true:label=bl_password:maxlength=80",
		"preferheight:integer:label=bl_preferheight:maxlength=80",
		"allowpreferheight:radio:label=bl_allowpreferheight:choices=off,on",
		"launchinpopup:radio:label=bl_launchinpopup:choices=off,on,content",
		"debuglaunch:radio:label=bl_debuglaunch:choices=off,on,content",
		"sendname:radio:label=bl_sendname:choices=off,on,content",
		"sendemailaddr:radio:label=bl_sendemailaddr:choices=off,on,content",
		"acceptgrades:radio:label=bl_acceptgrades:choices=off,on",
		"allowroster:radio:label=bl_allowroster:choices=off,on,content",
		"allowsetting:radio:label=bl_allowsetting:choices=off,on,content",
		"allowcustomparameters:radio:label=bl_allowcustomparameters:choices=off,on",
		"customparameters:textarea:label=bl_customparameters:rows=5:cols=25" };

	// For Instructors, this model is filtered down dynamically based on
	// Tool settings
	public static String [] ADMIN_CONTENT_MODEL = {
		"preferheight:integer:label=bl_preferheight:maxlength=80",
		"launchinpopup:radio:label=bl_launchinpopup:choices=off,on",
		"debuglaunch:radio:label=bl_debuglaunch:choices=off,on",
		"sendname:radio:label=bl_sendname:choices=off,on",
		"sendemailaddr:radio:label=bl_sendemailaddr:choices=off,on",
		"allowroster:radio:label=bl_allowroster:choices=off,on",
		"allowsetting:radio:label=bl_allowsetting:choices=off,on",
		"customparameters:textarea:label=bl_customparameters:rows=5:cols=25"} ; 

	public static String [] ADMIN_MAPPING_MODEL = {
		"matchpattern:text:label=bl_matchpattern:required=true:maxlength=80",
		"launchurl:url:label=bl_launchurl:required=true:maxlength=80"} ;
}
