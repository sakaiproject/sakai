/*******************************************************************************
 *
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
 *******************************************************************************/
package edu.indiana.lib.twinpeaks.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.*;
import java.util.*;

@Slf4j
public class StatusUtils
{
	/**
	 * Set up initial status information
	 */
	public static void initialize(SessionContext sessionContext, String targets)
	{
		StringTokenizer parser 		= new StringTokenizer(targets, " \t,");
		ArrayList				dbList		= new ArrayList();
		HashMap					targetMap	= getNewStatusMap(sessionContext);

		/*
		 * Establish the DB list and initial (pre-LOGON) status
		 */
		while (parser.hasMoreTokens())
		{
			String 	db 				= parser.nextToken();
			HashMap emptyMap 	= new HashMap();

			/*
			 * Empty status entry
			 */
			emptyMap.put("STATUS", "INACTIVE");
			emptyMap.put("STATUS_MESSAGE", "<none>");

			emptyMap.put("HITS", "0");
			emptyMap.put("ESTIMATE", "0");
			emptyMap.put("MERGED", "0");
			/*
			 * Save
			 */
      dbList.add(db);
      targetMap.put(db, emptyMap);
		}
    /*
     * Search targets, global status
     */
		sessionContext.put("TARGETS", dbList);
		sessionContext.putInt("active", 0);

		sessionContext.put("STATUS", "INACTIVE");
		sessionContext.put("STATUS_MESSAGE", "<none>");
    /*
     * Initial totals
     */
		sessionContext.putInt("TOTAL_ESTIMATE", 0);
		sessionContext.putInt("TOTAL_HITS", 0);
		sessionContext.putInt("maxRecords", 0);

		/*
		 * Assume this search is synchronous.  An OSID that implements an
		 * asynchronous search will need to set the async flags manually after
		 * this code has finished.
		 */
		clearAsyncSearch(sessionContext);
		clearAsyncInit(sessionContext);
	}

	/**
	 * Get an iterator into the system status map
	 * @param sessionContext Active SessionContext
	 * @return Status map Iterator
	 */
	public static Iterator getStatusMapEntrySetIterator(SessionContext sessionContext)
	{
		HashMap statusMap = (HashMap) sessionContext.get("searchStatus");
		Set			entrySet	= Collections.EMPTY_SET;

		if (statusMap != null)
		{
			entrySet = statusMap.entrySet();
		}
		return entrySet.iterator();
	}

	/**
	 * Get the status entry for a specified target database
	 * @param sessionContext Active SessionContext
	 * @param target Database name
	 * @return Status Map for this target (null if none)
	 */
	public static HashMap getStatusMapForTarget(SessionContext sessionContext,
																					    String target)
	{
		HashMap statusMap = (HashMap) sessionContext.get("searchStatus");

		return (statusMap == null) ? null : (HashMap) statusMap.get(target);
	}

	/**
	 * Create a new status map
	 * @param sessionContext Active SessionContext
	 * @return Status Map for this target
	 */
	public static HashMap getNewStatusMap(SessionContext sessionContext)
	{
		HashMap statusMap = new HashMap();

		sessionContext.remove("searchStatus");
		sessionContext.put("searchStatus", statusMap);

		return statusMap;
	}

	/**
	 * Set global status (effects all target databases)
	 * @param sessionContext Active SessionContext
	 * @param status One of ERROR | DONE
	 * @param message Status text
	 */
	public static void setGlobalStatus(SessionContext sessionContext,
											 						   String status, String message)
	{
		/*
		 * Set global status
		 */
		sessionContext.put("STATUS", status);
		sessionContext.put("STATUS_MESSAGE", message);
		/*
		 * Per-target status
		 */
		for (Iterator iterator = StatusUtils.getStatusMapEntrySetIterator(sessionContext); iterator.hasNext(); )
		{
			Map.Entry entry 			= (Map.Entry) iterator.next();
			HashMap		targetMap 	= (HashMap) entry.getValue();

			targetMap.put("STATUS", status);
			targetMap.put("STATUS_MESSAGE", message);
		}
	}

	/**
	 * Set global error status (effects all target databases)
	 * @param sessionContext Active SessionContext
	 * @param message Expanded error text (null if none - produces "unknown")
	 */
	public static void setGlobalError(SessionContext sessionContext,
											 						  String message)
	{
		String	statusMessage = "*unknown*";

		if (!StringUtils.isNull(message))
		{
			statusMessage = message;
		}
		setGlobalStatus(sessionContext, "ERROR", statusMessage);
	}

	/**
	 * Set global error status (effects all target databases)
	 * @param sessionContext Active SessionContext
	 * @param error Error number
	 * @param message Expanded error text (null to omit expanded message)
	 */
	public static void setGlobalError(SessionContext sessionContext,
											 						  String error, String message)
	{
		String	statusMessage 	= "Error " + error;

		if (!StringUtils.isNull(message))
		{
			statusMessage += ": " + message;
		}
		setGlobalStatus(sessionContext, "ERROR", statusMessage);
	}

	/**
	 * Set all status value to "search complete" (effects all target databases)
	 * @param sessionContext Active SessionContext
	 */
	public static void setAllComplete(SessionContext sessionContext)
	{
		setGlobalStatus(sessionContext, "DONE", "Search complete");
	}

	/**
	 * Update the hit count for this target (database)
	 * @param sessionContext Active SessionContext
	 * @param target Database name
	 * @return Updated hit count
	 */
	public static int updateHits(SessionContext sessionContext,
															 String target)
	{
		Map				targetMap;
		String		hits;
		int				total, totalHits, estimate;

		if (StringUtils.isNull(target))
		{
			throw new SearchException("No target database to update");
		}

		if ((targetMap = getStatusMapForTarget(sessionContext, target)) == null)
		{
			throw new SearchException("No status map found for target database " + target);
		}
  	log.debug("Map for target " + target + ": " + targetMap);
		/*
		 * Update total hits from this search source
		 */
		hits 	= (String) targetMap.get("HITS");
		total = Integer.parseInt(hits) + 1;
		targetMap.put("HITS", String.valueOf(total));

    totalHits = sessionContext.getInt("TOTAL_HITS") + 1;
		sessionContext.putInt("TOTAL_HITS", totalHits);
		/*
		 * Have we collected all available results?
		 */
		estimate = Integer.parseInt((String) targetMap.get("ESTIMATE"));
		if (estimate == total)
		{
			int active = sessionContext.getInt("active");
			/*
			 * If this is the last active source, mark everything DONE
			 */
			if (--active <= 0)
			{
				setAllComplete(sessionContext);
			}
			else
			{	/*
				 * Just this source is finished
				 */
				targetMap.put("STATUS", "DONE");
				targetMap.put("STATUS_MESSAGE", "Search complete");
			}
			sessionContext.putInt("active", active);
		}
		return total;
	}

	/**
	 * Fetch the estimated hits for a specified target (database)
	 * @param sessionContext Active SessionContext
	 * @param target Database name
	 * @return Updated hit count
	 */
	public static int getEstimatedHits(SessionContext sessionContext,
															 			 String target)
	{
		Map			targetMap;
		String	estimate;


		if ((targetMap = getStatusMapForTarget(sessionContext, target)) == null)
		{
			throw new SearchException("No status map for target database " + target);
		}

		estimate = (String) targetMap.get("ESTIMATE");
		return Integer.parseInt(estimate);
	}

	/**
	 * Fetch the number of remaining hits (all targets)
	 * @param sessionContext Active SessionContext
	 * @return Remaining hits
	 */
	public static int getAllRemainingHits(SessionContext sessionContext)
	{
		int estimate  = sessionContext.getInt("TOTAL_ESTIMATE");
		int hits      = sessionContext.getInt("TOTAL_HITS");
		int remaining = estimate - hits;

		return (remaining > 0) ? remaining : 0;
	}

	/**
	 * Fetch the number of active (searching) targets
	 * @param sessionContext Active SessionContext
	 * @return Count of active targets
	 */
	public static int getActiveTargetCount(SessionContext sessionContext)
	{
		return sessionContext.getInt("active");
	}

	/**
	 * Is this an asynchronous search?
	 * @param sessionContext Active SessionContext
	 * @return true if so
	 */
	public static boolean isAsyncSearch(SessionContext sessionContext)
	{
		return "TRUE".equals(sessionContext.get("ASYNC_SEARCH"));
	}

	/**
	 * Clear the asynchronous search flag
	 * @param sessionContext Active SessionContext
	 */
	public static void clearAsyncSearch(SessionContext sessionContext)
	{
		sessionContext.put("ASYNC_SEARCH", "FALSE");
	}

	/**
	 * Indicate an asynchronous search
	 * @param sessionContext Active SessionContext
	 */
	public static void setAsyncSearch(SessionContext sessionContext)
	{
		sessionContext.put("ASYNC_SEARCH", "TRUE");
	}

	/**
	 * Clear the asynchronous initialization flag
	 * @param sessionContext Active SessionContext
	 */
	public static void clearAsyncInit(SessionContext sessionContext)
	{
		sessionContext.put("ASYNC_INIT", "FALSE");
	}

	/**
	 * Indicate asynchronous initialization in progress
	 * @param sessionContext Active SessionContext
	 */
	public static void setAsyncInit(SessionContext sessionContext)
	{
		sessionContext.put("ASYNC_INIT", "TRUE");
	}

	/**
	 * Still performing asynchronous init?
	 * @param sessionContext Active SessionContext
	 * @return true if so
	 */
	public static boolean doingAsyncInit(SessionContext sessionContext)
	{
		return "TRUE".equals(sessionContext.get("ASYNC_INIT"));
	}
}