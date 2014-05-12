/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.sakaiproject.portal.charon.velocity;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.webapp.api.WebappResourceManager;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 1/15/13
 * Time: 2:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class LibraryWebappLoader extends ResourceLoader {
    private static final long CACHE_EXPIRATION_IN_MILLIS = 60 * 1000;
    private WebappResourceManager libraryWebappResourceManager;

    protected HashMap templatePaths = null;

    /**
     * @param configuration the {@link ExtendedProperties} associated with this resource
     *                      loader.
     */
    @Override
    public void init(ExtendedProperties configuration) {
        rsvc.debug("WebappLoader : initialization starting.");

        getLibraryWebappResourceManager();

		/* init the template paths map */
        templatePaths = new HashMap();

        rsvc.debug("WebappLoader : initialization complete.");
    }

    /**
     * Get an InputStream so that the Runtime can build a template with it.
     *
     * @param name name of template to get
     * @return InputStream containing the template
     * @throws org.apache.velocity.exception.ResourceNotFoundException
     *          if template not found in classpath.
     */
    @Override
    public synchronized InputStream getResourceStream(String name)
            throws ResourceNotFoundException {
        InputStream result = null;

        if (name == null || name.length() == 0) {
            throw new ResourceNotFoundException(
                    "WebappLoader : No template name provided");
        }

		/*
         * since the paths always ends in '/', make sure the name never starts
		 * with one
		 */
        while (!name.startsWith("/")) {
            name = "/" + name;
        }

        String adjustedName = adjustName(name);

        Exception exception = null;
        try {
            result = getLibraryWebappResourceManager().getResourceAsStream(adjustedName);

				/* save the path and exit the loop if we found the template */
            if (result != null) {
                templatePaths.put(name, new Date());
            }
        } catch (Exception e) {                /* only save the first one for later throwing */
            if (exception == null) {
                exception = e;
            }
        }

		/* if we never found the template */
        if (result == null) {
            String msg;
            if (exception == null) {
                msg = "WebappLoader : Resource '" + name + "' not found.";
            } else {
                msg = exception.getMessage();
            }            /* convert to a general Velocity ResourceNotFoundException */
            throw new ResourceNotFoundException(msg);
        }

        return result;
    }

    private Date getCachedFileLastLoaded(String fileName) {
        return (Date) templatePaths.get(fileName);
    }

    /**
     * Checks to see if a resource has been deleted, moved or modified.
     *
     * @param resource Resource The resource to check for modification
     * @return boolean True if the resource has been modified
     */
    @Override
    public boolean isSourceModified(Resource resource) {

        // first, try getting the previously found file
        String fileName = resource.getName();
        Date fileLastLoaded = getCachedFileLastLoaded(fileName);
        if (fileLastLoaded == null) {
            return true;
        }

        if (new Date().getTime() - fileLastLoaded.getTime() > CACHE_EXPIRATION_IN_MILLIS) {
            return true;
        }

        return false;
    }

    /**
     * Checks to see when a resource was last modified
     *
     * @param resource Resource the resource to check
     * @return long The time when the resource was last modified or 0 if the
     *         file can't be read
     */
    @Override
    public long getLastModified(Resource resource) {
        String fileName = resource.getName();
        Date fileLastLoaded = getCachedFileLastLoaded(fileName);
        if (fileLastLoaded == null) {
            return 0;
        }
        return fileLastLoaded.getTime();

    }

    public WebappResourceManager getLibraryWebappResourceManager() {
        if (libraryWebappResourceManager == null) {
            libraryWebappResourceManager =
                    (WebappResourceManager) ComponentManager.get("org.sakaiproject.webapp.api.WebappResourceManager.library");
        }
        return libraryWebappResourceManager;
    }

    /**
     * adjust path to look in the skin folder inside the library webapp
     * @param name
     * @return
     */
    public String adjustName(String name) {
        //TODO look at current site, portal has not stored the placement state when we get called
        // so we can't use any of the normal methods to determine what site we are in
        String[] parts = name.split("/");
        return "/skin/" + getSkin() + "/" + parts[parts.length - 1];
    }

    /**
     * Do the getSkin, adjusting for the overall skin/templates for the portal.
     *
     * @return The skin
     */
    protected String getSkin() {
        String skin = ServerConfigurationService.getString("skin.default");
        String templates = ServerConfigurationService.getString("portal.templates", "neoskin");
        String prefix = ServerConfigurationService.getString("portal.neoprefix", "neo-");
        // Don't add the prefix twice
        if (StringUtils.equals("neoskin", templates) && !StringUtils.startsWith(skin, prefix)) {
        	skin = prefix + skin;
        }
        return skin;
    }


}


