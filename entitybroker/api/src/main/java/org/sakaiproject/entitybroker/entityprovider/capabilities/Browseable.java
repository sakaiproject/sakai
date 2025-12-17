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
 * This is the root interface for browsing entities and does not do anything by itself,
 * you must add at least {@link BrowseableCollection} or {@link BrowseSearchable} interface to this <br/>
 * If your entities are nested then the nested ones will need to implement {@link BrowseNestable} <br/>
 * Entities which do not implement this will not appear in lists of entities which are being browsed <br/>
 * Internationalization keys:<br/>
 * {prefix}.browse = the name to show in the browse list for this entity <br/>
 * {prefix}.browse.description = the optional description that is viable to show in the browse list for this entity <br/>
 * <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 *  
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public abstract interface Browseable extends EntityProvider {

    public static final String BROWSE_TITLE_KEY = "browse";
    public static final String BROWSE_DESC_KEY = "browse.description";

    // this space intentionally left blank

}
