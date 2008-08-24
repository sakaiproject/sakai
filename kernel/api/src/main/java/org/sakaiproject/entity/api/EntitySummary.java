/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2007, 2008 Sakai Foundation
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

/**
 * 
 */
package org.sakaiproject.entity.api;

import java.util.Map;

import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;

/**
 * <p>
 * Services which implement EntitySummary declare themselves as willing and able to supply summary information for synoptics.
 * </p>
 */
public interface EntitySummary {
   
   Map getSummary(String summarizableReference, int items, int days) 
         throws IdUsedException, IdInvalidException, PermissionException;

   /**
    * Provide the string array of tool ids, for tools that we claim as manipulating our entities.
    * 
    * @return
    */
   String[] summarizableToolIds();
   /**
    * Get a sumerisable reference for the siteId and toolIdentifier
    * @param siteId
    * @param toolIdentifier
    * @return
    */
   String getSummarizableReference(String siteId, String toolIdentifier);
}
