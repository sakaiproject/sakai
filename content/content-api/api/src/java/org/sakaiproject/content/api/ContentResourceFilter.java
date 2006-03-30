/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.api;

public interface ContentResourceFilter {

   /**
    * Implement this method to control which resources are allowed
    * to be selected.  Implementation should inspect the resource and
    * return true if the resource should be selectable and false if not.
    * @param contentResource resource to test
    * @return true if resource should be selectable, false if not
    */
   public boolean allowSelect(ContentResource contentResource);

   /**
    * Implement this method to control which resources are viewable.
    * Implementation should inspect the resource and
    * return true if the resource should be presented in the list
    * and false if not.
    * @param contentResource resource to test
    * @return true if resource should be viewable, false if not
    */
   public boolean allowView(ContentResource contentResource);
}
