/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.util.cover;

import java.util.Set;

import org.sakaiproject.component.cover.ComponentManager;

public class LinkMigrationHelper {

	private static org.sakaiproject.util.api.LinkMigrationHelper thisLinkMigrationHelper=null;

	private static org.sakaiproject.util.api.LinkMigrationHelper getLinkMigrationHelper(){
		if(thisLinkMigrationHelper==null){
			thisLinkMigrationHelper = (org.sakaiproject.util.api.LinkMigrationHelper) ComponentManager.get(org.sakaiproject.util.api.LinkMigrationHelper.class);
		}
		return thisLinkMigrationHelper;
	}
	
	public static String bracketAndNullifySelectedLinks(String m) throws Exception {
		
		return getLinkMigrationHelper().bracketAndNullifySelectedLinks(m);
	}
	
	
	public static String migrateAllLinks(Set entrySet, String msgBody){
		return getLinkMigrationHelper().migrateAllLinks(entrySet, msgBody);
	}
	
	public static String migrateOneLink(String fromContextRef, String targetContextRef, String msgBody){
		
		return getLinkMigrationHelper().migrateOneLink(fromContextRef, targetContextRef, msgBody);
	}
}
