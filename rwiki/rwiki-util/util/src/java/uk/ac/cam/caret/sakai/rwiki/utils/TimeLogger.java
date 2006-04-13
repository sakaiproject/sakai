/**
 * 
 */
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
