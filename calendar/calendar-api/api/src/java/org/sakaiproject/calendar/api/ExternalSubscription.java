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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.calendar.api;

public interface ExternalSubscription
{

	/** Get subscription name */
	public String getSubscriptionName();

	/** Set subscription name */
	public void setSubscriptionName(String subscriptionName);

	/** Get subscription URL */
	public String getSubscriptionUrl();

	/** Set subscription URL */
	public void setSubscriptionUrl(String subscriptionUrl);

	/** Get context (site id) of external subscription */
	public String getContext();

	/** Set context (site id) of external subscription */
	public void setContext(String context);

	/** Get Reference of external subscription */
	public String getReference();

	/** Get Calendar object of external subscription */
	public Calendar getCalendar();

	/** Set Calendar object for external subscription */
	public void setCalendar(Calendar calendar);

	/** Check if external subscription is an institutional subscription */
	public boolean isInstitutional();

	/** Mark this external subscription as an institutional subscription */
	public void setInstitutional(boolean isInstitutional);

}
