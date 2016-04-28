/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaibrary.xserver.session;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;

/**
 * MetasearchSessionManager is a Singleton class designed for session
 * management in metasearching applications.  It makes use of cache
 * and MetasearchSession objects indexed by globally unique identifiers
 * to hold all session state for individual sessions.
 * 
 * @author gbhatnag
 */
@Slf4j
public class MetasearchSessionManager implements java.io.Serializable {
  /* constants */
  private static final String CACHE_NAME = "org.sakaibrary.xserver.session.MetasearchSession";

  /* private static variables */
  private static MemoryService memoryService = (MemoryService)ComponentManager.get(MemoryService.class);
  private static MetasearchSessionManager metasearchSessionManager;
  private static Cache cache;

  /**
   * Private constructor to ensure only one MetasearchSessionManager
   * is instantiated.  Initializes cache components CacheManager
   * and Cache.
   */
  private MetasearchSessionManager() {
	  if ( cache == null ) {
	    cache = memoryService.getCache(CACHE_NAME);
	
	    log.info( "MetasearchSessionManager cache session initiated properly." );
	  }
  }

    /**
     * Gets the Singleton instance of MetasearchSessionManager
     * 
     * @return an instance of MetasearchSessionManager
     */
    public static synchronized MetasearchSessionManager getInstance() {
      if( metasearchSessionManager == null ) {
        metasearchSessionManager = new MetasearchSessionManager();
      }

      return metasearchSessionManager;
    }

    /**
     * Puts the MetasearchSession object into the MetasearchSessionManager
     * cache indexed by the guid.  If the guid already exists, the
     * MetasearchSession object is updated with the given object.
     * 
     * @param guid a globally unique identifier String
     * @param ms the MetasearchSession object to be put/updated in the
     * MetasearchSessionManager cache.
     */
    public void putMetasearchSession( String guid,
        MetasearchSession ms ) {
      // given guid and ms.getGuid() should match -- TODO new Exception Type?
      if( !ms.getGuid().equals( guid ) ) {
        log.warn( "putMetasearchSession(): putting MetasearchSession into " +
            "cache with mismatched guids..." );
      }

      // the following puts if guid is new, updates if guid is old
      cache.put( guid, ms );
    }

    /**
     * Gets the MetasearchSession object out of the MetasearchSessionManager
     * cache indexed by the guid.
     * 
     * @param guid a globally unique identifier String
     * @return the MetasearchSession object if it exists and has not expired,
     * otherwise, null
     */
    public MetasearchSession getMetasearchSession( String guid ) {
      try {
        MetasearchSession ms = (MetasearchSession) cache.get( guid );
        return ms;
      } catch( Exception ce ) {
        log.warn( "MetasearchSessionManager.getMetasearchSession()" +
            " cannot get cache with guid: " + guid, ce );
      }
      
      return null;
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
		MetasearchSessionManager.cache = cache;
	}
  }
