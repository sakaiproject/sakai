package org.sakaibrary.xserver.session;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * MetasearchSessionManager is a Singleton class designed for session
 * management in metasearching applications.  It makes use of ehcache
 * and MetasearchSession objects indexed by globally unique identifiers
 * to hold all session state for individual sessions.
 * 
 * @author gbhatnag
 */
public class MetasearchSessionManager implements java.io.Serializable {
  /* constants */
  private static final String EHCACHE_CONFIG_XML = "/ehcache/ehcache.xml";
  private static final String CACHE_NAME = "metasearchSessionCache";
  private static final org.apache.commons.logging.Log LOG =
		org.apache.commons.logging.LogFactory.getLog(
				"org.sakaibrary.osid.repository.xserver.session.MetasearchSessionManager" );

  /* private static variables */
  private static MetasearchSessionManager metasearchSessionManager;
  private static CacheManager cacheManager;
  private static Cache cache;

  /**
   * Private constructor to ensure only one MetasearchSessionManager
   * is instantiated.  Initializes ehcache components CacheManager
   * and Cache.
   */
  private MetasearchSessionManager() {
    // get a static CacheManager, configured using EHCACHE_CONFIG_XML file
    java.io.InputStream is = this.getClass().getResourceAsStream( EHCACHE_CONFIG_XML );

    try {
      cacheManager = CacheManager.create( is );
      is.close();

      // add the cache to the CacheManager if it doesn't already exist
      if( !cacheManager.cacheExists( CACHE_NAME ) ) {
        // create a cache using ehcache 1.1 constructor
        Cache temp = new Cache( CACHE_NAME,
            50,
            true,
            false,
            0L,
            900L,
            false,
            120L );
        cacheManager.addCache( temp );
      }

      // get cache for use
      cache = cacheManager.getCache( CACHE_NAME );
    } catch( CacheException ce ) {
      LOG.warn( "MetasearchSessionManager() failed to create CacheManager or Cache", ce );
    } catch( java.io.IOException ioe ) {
      LOG.warn( "MetasearchSessionManager() failed to close ehcache input stream" );
    }

    LOG.info( "ehcache session initiated properly." );
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
        LOG.warn( "putMetasearchSession(): putting MetasearchSession into " +
            "ehcache with mismatched guids..." );
      }

      // the following puts if guid is new, updates if guid is old
      cache.put( new Element( guid, ms ) );
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
      Element element = null;
      try {
        element = cache.get( guid );
      } catch( CacheException ce ) {
        LOG.warn( "MetasearchSessionManager.getMetasearchSession()" +
            " cannot get cache with guid: " + guid, ce );
      }
      
      // element could have expired
      boolean isExpired = ( element == null ) ? true : cache.isExpired( element );
    
      return isExpired ? null : ( MetasearchSession )element.getValue();
    }
  }