/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.event.api;

import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;

/**
 * Allows an integration to be created between Sakai and a Learning Record Store (LRS)
 * 
 * Implementations of this which can be found (OR which are registered with the {@link LearningResourceStoreService})
 * will be called via the {@link #registerStatement(LRS_Statement)} method when valid learning events happen in Sakai
 * 
 * See https://jira.sakaiproject.org/browse/KNL-1042
 * 
 * Implementation Tips:
 * Use the ServerConfigurationService to load the configuration for your implementation.
 * Prefix all the config options with "lrs."+{your provider id}+"." (e.g. lrs.tincanapi-local.url=http://some.url/to/server).
 * 
 * http://en.wikipedia.org/wiki/Learning_Record_Store 
 * A Learning Record Store (LRS) is a data store that serve as a repository for learning records 
 * necessary for using the Experience API (XAPI). The Experience API (XAPI) is also known as "next-gen SCORM" 
 * or previously the TinCanAPI. The concept of the LRS was introduced to the e-learning industry in 2011, 
 * and is a shift to the way e-learning specifications function.
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public interface LearningResourceStoreProvider {

    /**
     * @return a unique id for this provider (e.g. "tincanapi-local"), used by the configuration handler and for logging purposes
     */
    public String getID();

    /**
     * Handle an incoming activity statement from the LMS (through the LRSS),
     * this will be called from within a dedicated thread to ensure it does not slow down other
     * parts of the system so there is no need to thread the implementation
     * 
     * @param statement the LRS statement representing the activity statement
     * @throws IllegalArgumentException if the input statement is invalid or cannot be handled
     * @throws RuntimeException if there is a FATAL failure
     */
    public void handleStatement(LRS_Statement statement);

}
