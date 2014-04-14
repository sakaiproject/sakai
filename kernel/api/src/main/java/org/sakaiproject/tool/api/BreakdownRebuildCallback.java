/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2014 The Apereo Foundation.
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
package org.sakaiproject.tool.api;

import javax.servlet.http.HttpSession;

/**
 * This supports the special case where session data rebuild cannot happen immediately
 * for some reason and has to be deferred. Anything implementing this will not
 * have the rebuild called immediately when the session is being rebuilt but will instead
 * use the callback defined in this class to trigger the rebuild on demand
 * (as determined by the implementer of this class)
 */
public interface BreakdownRebuildCallback<T> extends Breakdownable<T> {

    /**
     * Allows this implementation to optionally handle stashing the cached data
     * which cannot be rebuilt into the session immediately
     *
     * @param storedData the container holding the serialized data from the distributed store
     * @param attributeKey the session attribute key name that was supposed to hold this storedData
     * @param session the session the data was supposed to be placed into (might not be the user session, may be a tool or context session)
     * @return true if the data was handled in this implementation (or is no longer relevant) and the service can dump it,
     *         false if the data needs to be stashed in the service (for a very limited time)
     */
    public boolean makeStash(StoreableBreakdown storedData, String attributeKey, HttpSession session);
    
}
