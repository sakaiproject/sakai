/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/antivirus/api/VirusFoundException.java $
 * $Id: VirusFoundException.java 68335 2009-10-29 08:18:43Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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
package org.sakaiproject.conditions.api;

import org.sakaiproject.event.api.Event;

/**
 * an interface for the Command pattern that uses a Sakai <code>Event</code> object in the <code>execute</code> method
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 *
 */
public interface EvaluationAction {
	
	/**
	 * execute this command with the given Sakai <code>Event</code>.
	 * @param e
	 * @param evalResult a boolean that can be used to choose whether to perform an action or not
	 * e.g. whether or not to make a file available in the Resources tool
	 * @throws Exception
	 */
	public void execute(Event e, boolean evalResult) throws Exception;

}
