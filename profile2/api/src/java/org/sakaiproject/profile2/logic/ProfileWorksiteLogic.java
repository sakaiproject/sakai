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
/**
 * 
 */
package org.sakaiproject.profile2.logic;

import java.util.Collection;

import org.sakaiproject.profile2.model.Person;

/**
 * Logic interface for creating worksites from Profile2.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public interface ProfileWorksiteLogic {

	/**
	 * Create a new worksite for the specified owner and list of members.
	 * Members will be e-mailed notifications if requested
	 * 
	 * @param siteTitle the title for the new site.
	 * @param ownerId the user creating the worksite (must have
	 *            <code>site.add</code> permission).
	 * @param members the members of the worksite (must be connections of the
	 *            user creating the worksite).
	 * @param notifyByEmail <code>true</code> if members should be notified of
	 *            the new site by email, otherwise <code>false</code>.
	 */
	public boolean createWorksite(String siteTitle, String ownerId, Collection<Person> members, boolean notifyByEmail);
}
