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
package org.sakaiproject.profile2.service;

import java.util.List;

import org.sakaiproject.profile2.model.Person;

/**
 * A facade on the various logic component methods to improve backwards compatibility with
 * clients of the older Profile2 apis. See PRFL-551.
 * 
 * @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
public interface ProfileService {
	public abstract List<Person> getConnectionsForUser(final String userUuid);
}
