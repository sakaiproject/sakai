/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.calendar.api;

public interface ExternalSubscriptionDetails extends ExternalSubscription {

	enum State { LOADED, FAILED, UNKNOWN}

	/** Get context (site id) of external subscription or the institutional ID if central one */
	String getContext();

	/** Get subscription URL */
	String getSubscriptionUrl();

	/** Get Calendar object of external subscription */
	Calendar getCalendar();

	/** Check if external subscription is an institutional subscription */
	boolean isInstitutional();

	/**
	 * @return Is this subscription sucessfully loading some data.
	 */
	State getState();

	/** Owner of calendar subscription **/
	String getUserId();
	
	/** Time Zone Id of user, is used when calendar has not time zone defined */
	String getTzid();
}
