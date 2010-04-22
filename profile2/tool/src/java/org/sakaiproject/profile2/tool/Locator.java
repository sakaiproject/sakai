/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

package org.sakaiproject.profile2.tool;


/**
 * A simple class to lookup dependencies from the Application object.
 * 
 * The Application class must have getters and setters for these fields
 * and need to inject into the Application bean in WEB-INF/applicationContext.xml
 *
 *@deprecated use Spring Annotations for injection
 */

public class Locator {
	/*
	@Deprecated
	public static SakaiProxy getSakaiProxy()
    {
        ProfileApplication app = (ProfileApplication)RequestCycle.get().getApplication();
        return app.getSakaiProxy();
    }
	
	@Deprecated
	public static ProfileLogic getProfileLogic()
    {
		ProfileApplication app = (ProfileApplication)RequestCycle.get().getApplication();
        return app.getProfileLogic();
    }
	
	@Deprecated
	public static ProfileImageService getProfileImageService()
    {
		ProfileApplication app = (ProfileApplication)RequestCycle.get().getApplication();
        return app.getProfileImageService();
    }
    */
	
}
