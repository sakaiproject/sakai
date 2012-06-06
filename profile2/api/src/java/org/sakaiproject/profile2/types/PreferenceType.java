/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.types;

/**
 * These are the types of preferences in Profile2.
 * <p>
 * We use these when checking if different options are enabled.
 * <p>
 * See also {@link EmailType}.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * @since 1.5
 *
 */
public enum PreferenceType {

	EMAIL_NOTIFICATION_REQUEST,
	EMAIL_NOTIFICATION_CONFIRM,
	EMAIL_NOTIFICATION_MESSAGE_NEW,
	EMAIL_NOTIFICATION_MESSAGE_REPLY,
	EMAIL_NOTIFICATION_WALL_EVENT_NEW,
	EMAIL_NOTIFICATION_WALL_POST_MY_NEW,
	EMAIL_NOTIFICATION_WALL_POST_CONNECTION_NEW,
	EMAIL_NOTIFICATION_WALL_STATUS_NEW,
	EMAIL_NOTIFICATION_WORKSITE_NEW,
	EMAIL_NOTIFICATION_PROFILE_CHANGE,
	OFFICIAL_IMAGE,
	GRAVATAR_IMAGE,
	KUDOS_RATING,
	PICTURES,
	ONLINE_STATUS;
	
}
