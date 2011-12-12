/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/branches/SAK-18678/api/src/main/java/org/sakaiproject/site/api/Site.java $
 * $Id: Site.java 81275 2010-08-14 09:24:56Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.component.cover.ComponentManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

import java.util.*;
import java.util.Map.Entry;


/**
 * An extension of ResourceBundle which gets the bundle data in the normal way and then
 * consults with the MessageBundleService for addition data.
 * Any values returned by the MessageBundleService will override values returned from
 * the normal ResourceBundle.
 *
 * Also provides a method to index new bundle data by interacting with the MessageBundleService
 * to do the real work.
 *
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: May 7, 2009
 * Time: 11:03:59 AM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class DbResourceBundle extends ResourceBundle {
    protected Locale locale;
    protected Map entries = new HashMap();
    protected String baseName;

    protected static Log log = LogFactory.getLog(DbResourceBundle.class);

    private DbResourceBundle(String baseName, Locale locale) {
        this.baseName = baseName;
        this.locale = locale;
    }

    private static Object LOCK = new Object();

    private static ThreadLocalManager threadLocalManager;
    protected static ThreadLocalManager getThreadLocalManager() {
        if (threadLocalManager == null) {
            synchronized (LOCK) {
                ThreadLocalManager component = (ThreadLocalManager) ComponentManager.get(ThreadLocalManager.class);
                if (component == null) {
                    throw new IllegalStateException("Unable to find the ThreadLocalManager using the ComponentManager");
                } else {
                    threadLocalManager = component;
                }
            }
        }
        return threadLocalManager;
    }

    /**
     *
     * @param baseName
     * @param locale
     * @param classLoader
     * @return ResourceBundle with values from classpath loading, and overridden by any values
     *         retrieved from the MessageBundleService
     */
    static public ResourceBundle addResourceBundle(String baseName, Locale locale, ClassLoader classLoader) {
        DbResourceBundle newBundle = new DbResourceBundle(baseName, locale);
        String context =  (String) getThreadLocalManager().get(org.sakaiproject.util.RequestFilter.CURRENT_CONTEXT);
		try
		{
            if (context != null) {
                Map bundleValues = getMessageBundleService().getBundle(baseName, context, locale);
                Iterator<Entry<String,String>> bundleEntryIter = bundleValues.entrySet().iterator();
                while (bundleEntryIter.hasNext()) {
                	Entry<String,String> entry = bundleEntryIter.next();
                    String key = entry.getKey();
                    String value = entry.getValue();
                    newBundle.addProperty(key, value);
                }
            }
            ResourceBundle loadedBundle = null;
            try
            {
                if ( classLoader == null )
                    loadedBundle = ResourceBundle.getBundle(baseName, locale);
                else
                    loadedBundle = ResourceBundle.getBundle(baseName, locale, classLoader);
            }
            catch (NullPointerException e)
            {
            } // ignore

            Enumeration<String> keys = loadedBundle.getKeys();
            while (keys.hasMoreElements()){
                String key = keys.nextElement();
                if (newBundle.handleGetObject(key) == null) {
                    newBundle.addProperty(key, loadedBundle.getString(key));
                }
            }
        }
		catch (Exception e)
		{
            log.error("problem loading bundle: " +baseName + " locale: " + locale.toString() + " " + e.getMessage());
		}

		return newBundle;
    }

    protected void addProperty(String name, String value) {
        entries.put(name, value);
    }

    public Locale getLocale() {
        return locale;
    }

    protected Object handleGetObject(String key) {
        return entries.get(key);
    }

    public Enumeration<String> getKeys() {
        return Collections.enumeration(entries.keySet());
    }

    /**
     * imports or updates bundle data via the MessageBundleService
     * @param baseName
     * @param newBundle
     * @param loc
     * @param classLoader
     */
    public static void indexResourceBundle(String baseName, ResourceBundle newBundle, Locale loc, ClassLoader classLoader) {
        // serves as the moduleName
        String context =  (String) getThreadLocalManager().get(org.sakaiproject.util.RequestFilter.CURRENT_CONTEXT);
        if (context == null) return;
        MessageBundleService messageBundleService = getMessageBundleService();
        messageBundleService.saveOrUpdate(baseName, context, newBundle, loc);

    }

    private static MessageBundleService getMessageBundleService() {
        return (MessageBundleService) ComponentManager.get(MessageBundleService.class.getName());
    }


}
