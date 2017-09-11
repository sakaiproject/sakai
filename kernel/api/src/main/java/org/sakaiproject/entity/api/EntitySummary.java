/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

package org.sakaiproject.entity.api;

import java.util.Map;

import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;

/**
 * <p>
 * Services which implement EntitySummary declare themselves as willing and able to supply summary information for synoptics.
 * At the moment this is used to produce an RSS feed from the portal.
 * </p>
 * @see Summary
 */
public interface EntitySummary {
   
   /**
    * Get a summary for the supplied reference.
    * @param summarizableReference
    * @param items
    * @param days
    * @return A map containing keys from {@see Summary} and string values.
    * @throws IdUsedException
    * @throws IdInvalidException
    * @throws PermissionException
    */
   Map<String, String> getSummary(String summarizableReference, int items, int days) 
         throws IdUsedException, IdInvalidException, PermissionException;

   /**
    * Provide the string array of tool ids, for tools that we claim as manipulating our entities.
    * 
    * @return An array of tool IDs, eg "sakai.announcement".
    */
   String[] summarizableToolIds();
   
   /**
    * Get a summerizable reference for the siteId and toolIdentifier
    * @param siteId The site ID to get the summary for.
    * @param toolIdentifier The tool ID, eg "sakai.announcement".
    * @return A String that can be passed to {@see #getSummary(String, int, int)}.
    */
   String getSummarizableReference(String siteId, String toolIdentifier);
}
