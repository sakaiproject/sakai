/**********************************************************************************
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
**********************************************************************************/
package edu.indiana.lib.twinpeaks.util;

import java.lang.*;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;


/**
 * Expose the session context block
 */
@Slf4j
public class SessionContext
{
	/**
	 * Cache name
	 */
	private final static String	CACHENAME			= "org.sakaiproject.sakaibrary.http-osid-sessioncache";
	/**
	 * Items held in memory
	 */
	private static int CACHE_MEMORY_ELEMENTS 	= 50;
	/**
	 * Cache entry maximum time-to-live (seconds).  Zero is infinite.
	 */
	private static int CACHE_TTL							= 12 * (60 * 60);
	/**
	 * Cache entry idle time (seconds)
	 */
	private static int CACHE_IDLE_TIME				= 15 * 60;
	/**
	 * Session id (passed by caller)
	 */
	private String _sessionId 								= null;
	/**
	 * Local storage for name=value pairs
	 */
	private HashMap	_parameterMap							= null;
	
	/**
	 * The cache to use, since the cache manager is managed by Sakai, this
	 * has to be injected and set on startup of the parent component
	 */
	private static Cache cache;

	private static MemoryService memoryService = (MemoryService)ComponentManager.get(MemoryService.class);


  /**
   * Private constructor - set up the cache as required
   */
  private SessionContext(String id)
  {
		HashMap hashmap;

		try
		{
			if ( cache == null ) {
				cache = memoryService.getCache(CACHENAME);
				log.debug(CACHENAME + ":" + cache.toString());
			}
			/*
			 * Fetch the session cache (create a new one if necessary)
			 */
			hashmap = (HashMap) cache.get(id);
			log.debug("cache.get(" + id + ") finds " + hashmap);

			if (hashmap == null)
		{
				cache.put(id, new HashMap());

				log.debug("HashMap() created for id " + id);

			}
		}
		catch (Exception exception)
		{
			throw new SearchException(exception.toString());
		}

		_sessionId = id;
 		_parameterMap = hashmap;
	}

  /**
   * Get a Session Context instance
   */
  public static SessionContext getInstance(String id)
  {
		return new SessionContext(id);
	}

	/**
	 * Normalize a parameter name (add a simple prefix to the parameter name; this
	 *														 differentiates our names from those generated
	 *														 by ehcache)
	 * @param name Parameter name
	 * @return Normalized name
	 */
	private static String normalize(String name) {
		return "_" + name.trim();
	}

  /**
   * Fetch a value
   * @param name Attribute name
   * @return Requested value
   */
  public Object get(String name) {
    return _parameterMap.get(normalize(name));
  }

  /**
   * Set a name=value pair
   * @param name Attribute name
   * @param value Attribute value
   */
  public void put(String name, Object value) {
    _parameterMap.put(normalize(name), value);
  }

  /**
   * Fetch an int value
   * @param name Attribute name
   * @return Requested value
   */
  public int getInt(String name) {
    String value = (String) _parameterMap.get(normalize(name));

    try
    {
    	return Integer.parseInt(value);
		}
		catch (Exception NumberFormatException)
		{
			throw new SearchException("Invalid number: " + value);
		}
  }

  /**
   * Set a name=value pair
   * @param name Attribute name
   * @param value Attribute value
   */
  public void putInt(String name, int value) {
    _parameterMap.put(normalize(name), String.valueOf(value));
  }

  /**
   * Delete a name=value pair
   * @param name Attribute name
   */
  public void remove(String name) {
    _parameterMap.remove(normalize(name));
  }

	/*
	 * Helpers for the "search source" handlers
	 */

  /**
   * Construct a session-wide unique name (unique within a browser session)
   * @parameter The parent (calling) Object
   */
	public static String uniqueSessionName(Object parent) {
    /*
     * With ehcache, we don't need the following level of "unique detail":
     *
     *   return StringUtils.replace(parent.getClass().getName(), "\\.", ":");
     */
    return "";
  }

	/**
	 * @return the cache
	 */
	public static Cache getCache()
	{
		return cache;
	}
	
	/**
	 * @param cache the cache to set
	 */
	public static void setCache(Cache cache)
	{
		SessionContext.cache = cache;
	}
}
