/**
 * Copyright (c) 2010-2017 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational
* Community License, Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.roster.api;

/**
 * <code>RosterFunctions</code>. The following supported functions/permissions
 * are from the original Sakai Roster tool:
 * 
 * <ul>
 * <li>roster.viewallmembers</li>
 * <li>roster.viewhidden</li>
 * <li>roster.export</li>
 * <li>roster.viewgroup</li>
 * <li>roster.viewenrollmentstatus</li>
 * <li>roster.viewprofile</li>
 * </ul>
 * 
 * The following functions/permissions are no longer supported:
 * 
 * <ul>
 * <li>roster.viewofficialphoto</li>
 * </ul>
 * 
 * Please see the following URL for further documentation:
 * 
 * <a href="http://confluence.sakaiproject.org/display/RSTR/Roster2">http://
 * confluence.sakaiproject.org/display/RSTR/Roster2</a>
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public interface RosterFunctions {

	public static final String ROSTER_FUNCTION_PREFIX = "roster.";
	
	public static final String ROSTER_FUNCTION_VIEWALL = ROSTER_FUNCTION_PREFIX + "viewallmembers";
	public static final String ROSTER_FUNCTION_VIEWHIDDEN = ROSTER_FUNCTION_PREFIX + "viewhidden";
	public static final String ROSTER_FUNCTION_EXPORT = ROSTER_FUNCTION_PREFIX + "export";
	public static final String ROSTER_FUNCTION_VIEWGROUP = ROSTER_FUNCTION_PREFIX + "viewgroup";
	public static final String ROSTER_FUNCTION_VIEWENROLLMENTSTATUS = ROSTER_FUNCTION_PREFIX + "viewenrollmentstatus";
	public static final String ROSTER_FUNCTION_VIEWPROFILE = ROSTER_FUNCTION_PREFIX + "viewprofile";
	public static final String ROSTER_FUNCTION_VIEWEMAIL = ROSTER_FUNCTION_PREFIX + "viewemail";
	public static final String ROSTER_FUNCTION_VIEWOFFICIALPHOTO = ROSTER_FUNCTION_PREFIX + "viewofficialphoto";
	public static final String ROSTER_FUNCTION_VIEWSITEVISITS = ROSTER_FUNCTION_PREFIX + "viewsitevisits";
	public static final String ROSTER_FUNCTION_VIEWUSERPROPERTIES = ROSTER_FUNCTION_PREFIX + "viewuserproperties";
	
}
