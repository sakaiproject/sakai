/**
 * Copyright (c) 2023 The Apereo Foundation
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
package org.sakaiproject.messaging.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MicrosoftMessage {
	public enum Topic {
		CREATE_ELEMENT,
		DELETE_ELEMENT,
		MODIFY_ELEMENT,
		ADD_MEMBER_TO_AUTHZGROUP,
		REMOVE_MEMBER_FROM_AUTHZGROUP,
		TEAM_CREATION,
		CHANGE_LISTEN_GROUP_EVENTS
	}
	
	public enum Action {
		CREATE, DELETE, ADD, REMOVE, REMOVE_ALL, ENABLE, DISABLE, UNPUBLISH;
	}
	public enum Type {
		SITE, GROUP, TEAM;
	}
	
	private Action action;
	private Type type;
	private String reference;
	private String siteId;
	private String groupId;
	private String userId;
	private boolean owner;
	private int status;
	@Builder.Default
	private boolean force = false;
	
	//custom builder
	public static class MicrosoftMessageBuilder {
		private static Pattern sitePattern = Pattern.compile("^/site/([^/]+)$");
		private static Pattern groupPattern = Pattern.compile("^/site/([^/]+)/group/([^/]+)$");
		
		//fill type, siteId and groupId based on reference
		public MicrosoftMessageBuilder reference(String reference) {
			this.reference = reference;
			
			Matcher matcher = sitePattern.matcher(reference);
			if(matcher.find()) {
				this.type = Type.SITE;
				this.siteId = matcher.group(1);
			} else {
				matcher = groupPattern.matcher(reference);
				if(matcher.find()) {
					this.type = Type.GROUP;
					this.siteId = matcher.group(1);
					this.groupId = matcher.group(2);
				}
			}
			return this;
		}
	}
}
