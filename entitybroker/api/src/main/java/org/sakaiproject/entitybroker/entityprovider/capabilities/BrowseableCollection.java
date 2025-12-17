/**
 * $Id$
 * $URL$
 * Browseable.java - entity-broker - Aug 3, 2008 9:20:34 AM - azeckoski
 **************************************************************************
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
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * This indicates that this entity will participate in browse functionality for entities,
 * For example, it will provide lists of entities which are visible to users in locations
 * which can be looked through and selected<br/>
 * Entities which do not implement this will not appear in lists of entities which are being browsed<br/>
 * This is the convention interface and simply uses the results of calls to {@link CollectionResolvable} to
 * provide lists of entities for browsing, the implementor should be sure that ordering and limiting are supported
 * for the provided search object in their implementation of 
 * {@link CollectionResolvable#getEntities(org.sakaiproject.entitybroker.EntityReference, org.sakaiproject.entitybroker.entityprovider.search.Search)}<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * See {@link Browseable} for the i18n keys <br/>
 * This extends {@link CollectionResolvable}, use the {@link BrowseSearchable} interface if you require more control
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface BrowseableCollection extends Browseable, CollectionResolvable {

   // this space intentionally left blank

}
