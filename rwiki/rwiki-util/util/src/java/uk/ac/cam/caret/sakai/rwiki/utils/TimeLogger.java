/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package uk.ac.cam.caret.sakai.rwiki.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 */
public class TimeLogger
{
	private static Log log = LogFactory.getLog(TimeLogger.class);

	private TimeLogger()
	{
	}

	private static boolean logFullResponse = false;

	private static boolean logResponse = false;

	public static void printTimer(String name, long start, long end)
	{
		if (logFullResponse)
		{
			log.info("TIMER:" + name + ";" + (end - start) + ";" + end + ";");
		}
		else
		{
			log.debug("TIMER:" + name + ";" + (end - start) + ";" + end + ";");
		}
	}

	/**
	 * @return Returns the logFullResponse.
	 */
	public static boolean getLogFullResponse()
	{
		return logFullResponse;
	}

	/**
	 * @param logFullResponse
	 *        The logFullResponse to set.
	 */
	public static void setLogFullResponse(boolean logFullResponse)
	{
		TimeLogger.logFullResponse = logFullResponse;
	}

	/**
	 * @return Returns the logResponse.
	 */
	public static boolean getLogResponse()
	{
		return logResponse;
	}

	/**
	 * @param logResponse
	 *        The logResponse to set.
	 */
	public static void setLogResponse(boolean logResponse)
	{
		TimeLogger.logResponse = logResponse;
	}
}
