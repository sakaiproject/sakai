/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/user/api/DisplayAdvisorUDP.java $
 * $Id: DisplayAdvisorUDP.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
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
package org.sakaiproject.user.api;

/**
 * An optional interface to override the default construction of user's sort names
 * @author David Horwitz
 * @author Nuno Fernandez
 */
public interface DisplaySortAdvisorUPD {

	
   
    
           /**
            * Compute a sort name for this user.
            * 
            * @param user
            *        The User object.
            * @return a sort name for this user, or null if the UDP is not advising on this one.
            */
          public String getSortName(User user);

}
