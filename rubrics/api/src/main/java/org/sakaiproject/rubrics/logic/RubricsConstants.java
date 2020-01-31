/**********************************************************************************
 *
 * Copyright (c) 2018 The Sakai Foundation
 *
 * Original developers:
 *
 *   EDF
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

package org.sakaiproject.rubrics.logic;

public interface RubricsConstants {

	public static final String RBCS_TOOL = "sakai.rubrics";
	public static final String RBCS_TOOL_ASSIGNMENT = "sakai.assignment";
	public static final String RBCS_TOOL_FORUMS = "sakai.forums";
	public static final String RBCS_TOOL_GRADEBOOKNG = "sakai.gradebookng";
	public static final String RBCS_TOOL_SAMIGO = "sakai.samigo";

	public static final String RBCS_PREFIX = "rbcs-";
	public static final String RBCS_CONFIG_PREFIX = "config-";
	public static final String RBCS_CONFIG = RBCS_PREFIX + RBCS_CONFIG_PREFIX;
	public static final	String RBCS_ASSOCIATION_STATE_DETAILS = RBCS_PREFIX + "state-details";
	public static final String RBCS_ASSOCIATE_SUFFIX = "associate";
	public static final	String RBCS_ASSOCIATE = RBCS_PREFIX + RBCS_ASSOCIATE_SUFFIX;
	public static final String RBCS_LIST_SUFFIX = "rubricslist";
	public static final String RBCS_LIST = RBCS_PREFIX + RBCS_LIST_SUFFIX;
	public static final String RBCS_SOFT_DELETED = RBCS_PREFIX + "soft-deleted";

	//samigo custom props
	public static final	String RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX = "pub.";

	//forums custom props
	public static final	String RBCS_FORUM_ENTITY_PREFIX = "for.";
	public static final	String RBCS_TOPIC_ENTITY_PREFIX = "top.";
	public static final	String RBCS_MSG_ENTITY_PREFIX = "msg.";
	
}