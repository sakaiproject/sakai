/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.PreferencesService;

/**
 * ResourceLoader provides an alternate implementation of org.util.ResourceBundle, dynamically selecting the prefered locale from either the user's session or from the user's sakai preferences
 * 
 * @author Sugiura, Tatsuki (University of Nagoya)
 */
@SuppressWarnings("rawtypes")
public class ResourceLoader extends DummyMap implements InternationalizedMessages
{
	protected static final Log M_log = LogFactory.getLog(ResourceLoader.class);

	// name of ResourceBundle
	protected String baseName = null;
	
	// Optional ClassLoader for ResourceBundle
	protected ClassLoader classLoader = null;

	// cached set of ResourceBundle objects
	protected Hashtable<Locale, ResourceBundle> bundles = new Hashtable<Locale, ResourceBundle>();

    // cached set of last time bundle was loaded
    protected Hashtable<Locale, Date> bundlesTimestamp = new Hashtable<Locale, Date>();

	// current user id
	protected String userId = null;
	
	// session key string for determining validity of ResourceBundle cache
	protected String LOCALE_SESSION_KEY = "sakai.locale.";

	// Debugging variables for displaying ResourceBundle name & property	
	protected String DEBUG_LOCALE = "en_US_DEBUG";
	private   String DBG_PREFIX = "** ";
	private   String DBG_SUFFIX = " **";

	private static Object LOCK = new Object();

	private static SessionManager sessionManager;
	protected static SessionManager getSessionManager() {
        if (sessionManager == null) {
            synchronized (LOCK) {
                sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
            }
        }
        return sessionManager;
	}

	private static PreferencesService preferencesService;
	protected static PreferencesService getPreferencesService() {
	    if (preferencesService == null) {
	        synchronized (LOCK) {
	            preferencesService = (PreferencesService) ComponentManager.get(PreferencesService.class);
            }
	    }
	    return preferencesService;
	}

	/**
	 * Default constructor (may be used to find user's default locale 
	 *                      without specifying a bundle)
	 */
	public ResourceLoader()
	{
	}

	/**
	 * Constructor: set baseName
	 * 
	 * @param name
	 *        default ResourceBundle base filename
	 */
	public ResourceLoader(String name)
	{
		this.baseName = name;
	}

	/**
	 * Constructor: set baseName
	 * 
	 * @param name default ResourceBundle base filename
	 * @param classLoader ClassLoader for ResourceBundle 
	 */
	public ResourceLoader(String name, ClassLoader classLoader)
	{
		this.baseName = name;
		this.classLoader = classLoader;
	}

	/**
	 * Constructor: specified userId, specified baseName 
	 *              (either may be null)
	 * 
	 * @param userId user's internal sakai id (e.g. user.getId())
	 * @param name  default ResourceBundle base filename
	 */
	public ResourceLoader(String userId, String name)
	{
		this.userId = userId; 
		this.baseName = name; 
	}

	/**
	 ** Return ResourceBundle properties as if Map.entrySet() 
	 **/
    public Set entrySet()
	{
		return getBundleAsMap().entrySet();
	}

	/**
	 * * Return (generic object) value for specified property in current locale specific ResourceBundle
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle * *
	 * @return value for specified property key
	 */
	public Object get(Object key)
	{
		return getString(key.toString());
	}

   /**
    ** Return formatted message based on locale-specific pattern
    **
    ** @param key maps to locale-specific pattern in properties file
    ** @param args parameters to format and insert according to above pattern
    ** @return  formatted message
    ** 
    ** @author Sugiura, Tatsuki (University of Nagoya)
    ** @author Jean-Francois Leveque (Universite Pierre et Marie Curie - Paris 6)
    **
    **/
	public String getFormattedMessage(String key, Object[] args)
	{
		if ( getLocale().toString().equals(DEBUG_LOCALE) )
			return formatDebugPropertiesString( key );
			
		String pattern = (String) get(key);
		if (M_log.isDebugEnabled()) {
			M_log.debug("getFormattedMessage(key,args) bundle name=" +
			this.baseName + ", locale=" + getLocale().toString() +
			", key=" + key + ", pattern=" + pattern);
		}
			
		return (new MessageFormat(pattern, getLocale())).format(args, new StringBuffer(), null).toString();
	}

	/**
	 * Access some named configuration value as an int.
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle
	 * @param dflt
	 *        The value to return if not found.
	 * @return The property value with this name, or the default value if not found.
	 */
	public int getInt(String key, int dflt)
	{
		String value = getString(key);

		if (value.length() == 0) return dflt;

		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			// ignore
			return dflt;
		}
	}
   
	/**
	 ** Return user's prefered locale
	 **	 First: return locale from Sakai user preferences, if available
	 **	 Second: return locale from user session, if available
	 **	 Last: return system default locale
	 **
	 ** @return user's Locale object
	**
	** @author Sugiura, Tatsuki (University of Nagoya)
	** @author Jean-Francois Leveque (Universite Pierre et Marie Curie - Paris 6)
	 **/
	public Locale getLocale()
	{			 
		 Locale loc = null;
		 try
		 {
			 // check if locale is requested for specific user
			 if ( this.userId != null )
			 {
				 loc = getLocale( this.userId );
			 }
			 
			 else
			 {
				 loc = (Locale) getSessionManager().getCurrentSession().getAttribute(LOCALE_SESSION_KEY+getSessionManager().getCurrentSessionUserId());
				 
				 // The locale is not in the session. 
				 // Look for it and set in session
				 if (loc == null) 
					 loc = setContextLocale(null);
			 }
		 }
		 //FIXME NPE's should not be explicitly caught - rather check the session above for null
		 catch(NullPointerException e) 
		 {
			if (M_log.isWarnEnabled()) {
				M_log.warn("getLocale() swallowing NPE");
				e.printStackTrace();
			}
			 // The locale is not in the session. 
			 // Look for it and set in session
			 loc = setContextLocale(null);
		 } 

		if (loc == null) {
			M_log.info("getLocale() Locale not found in preferences or session, returning default");
			loc = Locale.getDefault();
		}

		 return loc;
	}
	
	/**
	 ** This method formats a debugging string using the properties key.
	 ** This allows easy identification of context for properties keys, and
	 ** also highlights any hard-coded text, when the debug locale is selected
	 **/
	protected String formatDebugPropertiesString( String key )
	{
		StringBuilder dbgPropertiesString = new StringBuilder(DBG_PREFIX);
		dbgPropertiesString.append( this.baseName );
		dbgPropertiesString.append( " " );
		dbgPropertiesString.append( key );
		dbgPropertiesString.append( DBG_SUFFIX );
		return dbgPropertiesString.toString();
	}

	/**
	 ** Get user's preferred locale (or null if not set)
	 ***/
	public Locale getLocale(String userId)
	{
		return getPreferencesService().getLocale(userId);
	}
	
	/**
	 ** Sets user's prefered locale in context
	 **	 First: sets  locale from Sakai user preferences, if available
	 **	 Second: sets locale from user session, if available
	 **	 Last: sets system default locale
	 **
	 ** @return user's Locale object
	**
	** @author Sugiura, Tatsuki (University of Nagoya)
	** @author Jean-Francois Leveque (Universite Pierre et Marie Curie - Paris 6)
	 **/
	public Locale setContextLocale(Locale loc)
	{		 
		//	 First : find locale from Sakai user preferences, if available
		if (loc == null) 
		{
			try
			{
				loc = getLocale( getSessionManager().getCurrentSessionUserId() );
			}
			catch (Exception e)
			{
				if (M_log.isWarnEnabled()) {
					M_log.warn("setContextLocale(Locale) swallowing Exception");
					e.printStackTrace();
				}
			} // ignore and continue
		}
			  
		// Second: find locale from user browser session, if available
		if (loc == null)
		{
			try
			{
				loc = (Locale) getSessionManager().getCurrentSession().getAttribute("locale");
			}
			catch (NullPointerException e)
			{
				if (M_log.isWarnEnabled()) {
					M_log.warn("setContextLocale(Locale) swallowing NPE");
					e.printStackTrace();
				}
			} // ignore and continue
		}

		// Last: find system default locale
		if (loc == null)
		{
			// fallback to default.
			loc = Locale.getDefault();
			if (M_log.isDebugEnabled()) {
				M_log.debug("setContextLocale(Locale), default locale");
			}
		}
		else if (!Locale.getDefault().getLanguage().equals("en") && loc.getLanguage().equals("en") && !loc.toString().equals(DEBUG_LOCALE))
		{
			// Tweak for English: en is default locale. It has no suffix in filename.
			loc = new Locale("");
			if (M_log.isDebugEnabled()) {
				M_log.debug("setContextLocale(Locale), Tweak for English");
			}
		}

		//Write the sakai locale in the session	
		try 
		{
			String sessionUser = getSessionManager().getCurrentSessionUserId();
			if ( sessionUser != null )
				getSessionManager().getCurrentSession().setAttribute(LOCALE_SESSION_KEY+sessionUser,loc);
		}
		catch (Exception e) 
		{
			if (M_log.isWarnEnabled()) {
				M_log.warn("setContextLocale(Locale) swallowing Exception");
				e.printStackTrace();
			}
		} //Ignore and continue
		 
		return loc;		  
	}

	/**
	 ** Returns true if the given key is defined, otherwise false
	 **/
	public boolean getIsValid( String key )
	{
		try
		{
			String value = getBundle().getString(key);
			return value != null;
		}
		catch (MissingResourceException e)
		{
			return false;
		}
	} 
	
	/**
	 * Return string value for specified property in current locale specific ResourceBundle
	 * 
	 * @param key property key to look up in current ResourceBundle
	 * @return String value for specified property key
	 */
	public String getString(String key)
	{
	    if ( getLocale().toString().equals(DEBUG_LOCALE) ) {
	        return formatDebugPropertiesString( key );
	    }

	    try
	    {
	        String value = getBundle().getString(key);
	        if (M_log.isDebugEnabled()) {
	            M_log.debug("getString(key) bundle name=" + this.baseName +
	                    ", locale=" + getLocale().toString() + ", key=" +
	                    key + ", value=" + value);
	        }
	        return value;

	    }
	    catch (MissingResourceException e)
	    {
	        if (M_log.isWarnEnabled()) {
	            M_log.warn("bundle \'"+baseName +"\'  missing key: \'" + key 
	                    + "\'  from: " + e.getStackTrace()[3] ); // 3-deep gets us out of ResourceLoader
	        }
	        return "[missing key (mre): " + baseName + " " + key + "]";
	    }
	    catch (NullPointerException e)
	    {
	        if (M_log.isWarnEnabled()) {
	            M_log.warn("bundle \'"+baseName +"\'  null pointer exception: \'" + key 
	                    + "\'  from: " + e.getStackTrace()[3] ); // 3-deep gets us out of ResourceLoader
	        }
	        return "[missing key (npe): " + baseName + " " + key + "]";			
	    }
	    catch (ClassCastException e)
	    {
	        if (M_log.isWarnEnabled()) {
	            M_log.warn("bundle \'"+baseName +"\'  class cast exception: \'" + key 
	                    + "\'  from: " + e.getStackTrace()[3] ); // 3-deep gets us out of ResourceLoader
	        }
	        return "[missing key (clc): " + baseName + " " + key + "]";						
	    }
	}

	/**
	 * Return string value for specified property in current locale specific ResourceBundle
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle
	 * @param dflt
	 *        the default value to be returned in case the property is missing
	 * @return String value for specified property key
	 */
	public String getString(String key, String dflt)
	{
	    if ( getLocale().toString().equals(DEBUG_LOCALE) )
	        return formatDebugPropertiesString( key );

	    try
	    {
	        return getBundle().getString(key);
	    }
	    catch (MissingResourceException e)
	    {
	        return dflt;
	    }
	    catch (NullPointerException e)
	    {
	        return dflt;
	    }
	    catch(ClassCastException e)
	    {
	        return dflt;
	    }
	}

	/**
	 * Access some named property values as an array of strings. The name is the base name. name + ".count" must be defined to be a positive integer - how many are defined. name + "." + i (1..count) must be defined to be the values.
	 * 
	 * @param key
	 *        property key to look up in current ResourceBundle
	 * @return The property value with this name, or the null if not found.
	 *
	 * @author Sugiura, Tatsuki (University of Nagoya)
	 * @author Jean-Francois Leveque (Universite Pierre et Marie Curie - Paris 6)
	 *
	 */
	public String[] getStrings(String key)
	{
		if ( getLocale().toString().equals(DEBUG_LOCALE) )
			return new String[] { formatDebugPropertiesString(key) };
			
		// get the count
		int count = getInt(key + ".count", 0);
		if (count > 0)
		{
			String[] rv = new String[count];
			for (int i = 1; i <= count; i++)
			{
				String value = "";
				try
				{
					value = getBundle().getString(key + "." + i);
				}
				catch (MissingResourceException e)
				{
					if (M_log.isWarnEnabled()) {
						M_log.warn("getStrings(" + key + ") swallowing MissingResourceException for String " + i);
						e.printStackTrace();
					}
					// ignore the exception
				}
				rv[i - 1] = value;
			}
			return rv;
		}

		return null;
	}


	/**
	 ** Return Resour�ceBundle properties as if Map.keySet() 
	 **/
	public Set keySet()
	{
		return getBundleAsMap().keySet();
	}

	/**
	 * * Clear bundles hashmap
	 */
	public void purgeCache()
	{
		this.bundles = new Hashtable<Locale, ResourceBundle>();
		M_log.debug("purge bundle cache");
	}

	/**
	 * Set baseName
	 * 
	 * @param name
	 *        default ResourceBundle base filename
	 */
	public void setBaseName(String name)
	{
		if (M_log.isDebugEnabled()) {
			M_log.debug("set baseName=" + name);
		}
		this.baseName = name;
	}

	/**
	 ** Return ResourceBundle properties as if Map.values() 
	 **/
	public Collection values()
	{
		return getBundleAsMap().values();
	}

	/**
	 * Return ResourceBundle for user's preferred locale
	 * 
	 * @return user's ResourceBundle object
	 */
	protected ResourceBundle getBundle()
	{
        if (ServerConfigurationService.getBoolean("load.bundles.from.db", false)) {
            return getBundleFromDb();
        }
		Locale loc = getLocale();
		ResourceBundle bundle = (ResourceBundle) this.bundles.get(loc);
		if (bundle == null)
		{
			if (M_log.isDebugEnabled()) {
				M_log.debug("Load bundle name=" + this.baseName + ", locale=" + getLocale().toString());
			}
			bundle = loadBundle(loc);
		}
		return bundle;
	}

    /**
     *
     * @return ResourceBundle from the cache or retrieved from the MessageService data store
     */
	protected ResourceBundle getBundleFromDb()
	{
		Locale loc = getLocale();
        //TODO consider using a better caching method here
		ResourceBundle bundle = (ResourceBundle) this.bundles.get(loc);
        Date timeStamp = (Date) this.bundlesTimestamp.get(loc);
		if ((timeStamp == null || timeStamp.getTime() + ServerConfigurationService.getInt("load.bundles.from.db.timeout", 30000) < new Date().getTime() ) )
		{
			M_log.debug("Load bundle name=" + this.baseName + ", locale=" + getLocale().toString());
			bundle = loadBundle(loc);
		}
		return bundle;
	}

	/**
	 ** Return the ResourceBundle properties as a Map object
	 **/
	protected Map<Object, Object> getBundleAsMap()
	{
		Map<Object, Object> bundle = new Hashtable<Object, Object>();

		for (Enumeration e = getBundle().getKeys(); e.hasMoreElements();)
		{
			Object key = e.nextElement();
			bundle.put(key, getBundle().getObject((String) key));
		}

		return bundle;
	}

	/**
	 * Return ResourceBundle for specified locale
	 * 
	 * @param loc
	 *
	 * @return locale specific ResourceBundle
	 *         (or empty ListResourceBundle in case of error)
	 */
	protected ResourceBundle loadBundle(Locale loc)
	{
        if (ServerConfigurationService.getBoolean("load.bundles.from.db", false)) {
            return loadBundleFromDb(loc);
        }
		ResourceBundle newBundle = null;
		try
		{
			if ( this.classLoader == null )
				newBundle = ResourceBundle.getBundle(this.baseName, loc);
			else
				newBundle = ResourceBundle.getBundle(this.baseName, loc, this.classLoader);


		}
        catch (NullPointerException e)
        {
        } // ignore


		setBundle(loc, newBundle);
		return newBundle;
	}

	/**
	 * Return ResourceBundle for specified locale.  Returns values from the classpath
     * overridden with any values found in the database via the MessageService.  Also,
     * responsible for indexing any new or changes values.
	 *
	 * @param loc
	 *        properties bundle * *
	 * @return locale specific ResourceBundle
	 *         (or empty ListResourceBundle in case of error)
	 */
	protected ResourceBundle loadBundleFromDb(Locale loc)
	{
		ResourceBundle newBundle = null;
		try
		{
            newBundle = DbResourceBundle.addResourceBundle(this.baseName, loc, this.classLoader);
		}
		catch (Exception e)
		{
			if (M_log.isWarnEnabled()) {
				M_log.warn("loadBundle "+baseName+" "+loc.toString(), e );
			}
			throw new MissingResourceException("ResourceLoader.loadBundle failed",
														  "", "" );
		}

        DbResourceBundle.indexResourceBundle(this.baseName, newBundle, loc, this.classLoader);

		setBundle(loc, newBundle);
		return newBundle;
	}

	/**
	 * Add loc (key) and bundle (value) to this.bundles hash
	 * 
	 * @param loc
	 *        Language/Region Locale *
	 * @param bundle
	 *        properties bundle
	 */
	protected void setBundle(Locale loc, ResourceBundle bundle)
	{
		if (loc == null || bundle == null) 
			return;
		this.bundles.put(loc, bundle);
        this.bundlesTimestamp.put(loc, new Date());
	}
}

@SuppressWarnings({ "rawtypes" })
abstract class DummyMap implements Map
{
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key)
	{
		return true;
	}

	public boolean containsValue(Object value)
	{
		throw new UnsupportedOperationException();
	}

	public Set entrySet()
	{
		throw new UnsupportedOperationException();
	}

	public abstract Object get(Object key);

	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}

	public Set keySet()
	{
		throw new UnsupportedOperationException();
	}

	public Object put(Object arg0, Object arg1)
	{
		throw new UnsupportedOperationException();
	}

	public void putAll(Map arg0)
	{
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	public int size()
	{
		throw new UnsupportedOperationException();
	}

	public Collection values()
	{
		throw new UnsupportedOperationException();
	}
}
