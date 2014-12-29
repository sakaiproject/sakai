/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.log.api;

/**
 * <p>
 * LogConfigurationManager provides runtime control over and access to our logging message.
 * </p>
 */
public interface LogConfigurationManager
{
	/**
	 * Set the logging level for a logger
	 * 
	 * @param level
	 *        The logging level - one of: OFF | TRACE | DEBUG | INFO | WARN | ERROR | FATAL | ALL
	 *        The logger name (such as "org.sakaiproject")
	 * @return true if successful, false if not (could be a bad level, or a logger that does not exist)
	 * @throws LogPermissionException
	 *         if the current end user does not have permission to set the log level.
	 */
	boolean setLogLevel(String level, String logger) throws LogPermissionException;
}
