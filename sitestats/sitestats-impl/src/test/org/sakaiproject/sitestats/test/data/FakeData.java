/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;


public class FakeData {
	// SITEs
	public final static String					SITE_A_ID			= "site-a-id";
	public final static String					SITE_A_REF			= "/site/site-a-id";
	public final static String					SITE_A_ALIAS		= "site-a-alias";
	public final static String					SITE_A_TARGET		= SITE_A_REF;
	public final static String					SITE_B_ID			= "site-b-id";
	public final static String					SITE_B_REF			= "/site/site-b-id";
	public final static String					SITE_C_ID			= "site-c-id";
	public final static String					SITE_C_REF			= "/site/site-c-id";
	public final static int						SITE_C_USER_COUNT	= 2002;
	
	// USERs
	public final static String					USER_A_ID			= "user-a";
	public final static String					USER_B_ID			= "user-b";
	public final static String					USER_ID_PREFIX		= "user-";

	// TOOLs & EVENTs
	public final static String					TOOL_CHAT			= "sakai.chat";
	public final static String					EVENT_CHATNEW		= "chat.new";
	public final static String					EVENT_CONTENTNEW	= "content.new";
	public final static String					EVENT_CONTENTREV	= "content.revise";
	public final static String					EVENT_CONTENTREAD	= "content.read";
	// anonymous events
	public final static String					EVENT_CONTENTDEL	= "content.delete";
	
	// EVENT LIST & EVENT MAP
	public final static Set<String>				EVENTIDS			= new HashSet<String>();
	public final static List<ToolInfo>			EVENT_REGISTRY		= new ArrayList<ToolInfo>();
	public final static List<ToolInfo>			EVENT_REGISTRY_RES	= new ArrayList<ToolInfo>();
	public final static List<ToolInfo>			EVENT_REGISTRY_CHAT	= new ArrayList<ToolInfo>();
	static{
		EVENTIDS.add(EVENT_CHATNEW); 
		EVENTIDS.add(EVENT_CONTENTNEW); 
		EVENTIDS.add(EVENT_CONTENTREAD); 
		EVENTIDS.add(EVENT_CONTENTREV); 
		EVENTIDS.add(EVENT_CONTENTDEL); 
		EVENTIDS.add(StatsManager.SITEVISIT_EVENTID);
	};
	public final static Map<String, ToolInfo>	EVENTID_TOOL_MAP	= new HashMap<String, ToolInfo>();
	static{
		ToolInfo chat = new ToolInfo("sakai.chat");
		chat.addEvent(new EventInfo(EVENT_CHATNEW));
		chat.setEventParserTip(new EventParserTip("contextId", "/", "3"));
		EVENTID_TOOL_MAP.put(EVENT_CHATNEW, chat);
		EVENT_REGISTRY.add(chat);
		EVENT_REGISTRY_CHAT.add(chat);

		ToolInfo resources = new ToolInfo(StatsManager.RESOURCES_TOOLID);
		resources.addEvent(new EventInfo(EVENT_CONTENTNEW));
		resources.addEvent(new EventInfo(EVENT_CONTENTREAD));
		resources.addEvent(new EventInfo(EVENT_CONTENTREV));
		resources.addEvent(new EventInfo(EVENT_CONTENTDEL));
		resources.setEventParserTip(new EventParserTip("contextId", "/", "3"));
		EVENTID_TOOL_MAP.put(EVENT_CONTENTNEW, resources);
		EVENTID_TOOL_MAP.put(EVENT_CONTENTREAD, resources);
		EVENTID_TOOL_MAP.put(EVENT_CONTENTREV, resources);
		EVENTID_TOOL_MAP.put(EVENT_CONTENTDEL, resources);
		EVENT_REGISTRY.add(resources);
		EVENT_REGISTRY_RES.add(resources);
	}

	// RESOURCEs
	public final static String					RES_MYWORKSPACE_A	= "/content/user/"+USER_A_ID+"/";
	public final static String					RES_MYWORKSPACE_B_F	= "/content/user/"+USER_B_ID+"/resource1";
	public final static String					RES_MYWORKSPACE_NO_F= "/content/user/no_user/resource1";
	public final static String					RES_ATTACH_SITE		= "/content/attachment/"+SITE_A_ID;
	public final static String					RES_ATTACH			= "/content/attachment/"+SITE_A_ID+"/Discussion/resource3";
	public final static String					RES_ATTACH_OLD		= "/content/attachment/"+SITE_A_ID+"/Choose File/resource3";
	public final static String					RES_ATTACH_OLD2		= "/content/attachment/"+SITE_A_ID+"/Choose File/Assignments/resource3";
	public final static String					RES_ROOT_SITE_A		= "/content/group/"+SITE_A_ID+"/";
	public final static String					RES_FILE_SITE_A		= "/content/group/"+SITE_A_ID+"/resource1";
	public final static String					RES_FOLDER_SITE_A	= "/content/group/"+SITE_A_ID+"/folder/";
	public final static String					RES_FILE2_SITE_A	= "/content/group/"+SITE_A_ID+"/folder/res2";
	public final static String					RES_DROPBOX_SITE_A	= "/content/group-user/"+SITE_A_ID+"/";
	public final static String					RES_DROPBOX_SITE_A_USER_A	= "/content/group-user/"+SITE_A_ID+"/"+USER_A_ID+"/";
	public final static String					RES_DROPBOX_SITE_A_USER_A_FILE	= "/content/group-user/"+SITE_A_ID+"/"+USER_A_ID+"/resource1";
	
}
