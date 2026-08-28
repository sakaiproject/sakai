/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.stylesheet;

import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import java.io.Serializable;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;

/**
 * Loads, filters and then caches any resource available to the webapp.
 * <p>
 * The resource can then be retrieved from the cache when desired. In
 * particular, it can be retrieved via a URL that invokes the Tomahawk
 * ExtensionsFilter and TextResourceFilterProvider classes.
 * <p>
 * The "filtering" process looks for any strings of form #{...} in the
 * resource, and executes it as an EL expression. The original expression
 * is then replaced with the return value of the expression.
 * <p>
 * Note that evaluation of EL expressions happens <i>only once</i>, when
 * the resource is loaded into the cache for the first time. And the EL
 * expressions are evaluated using the context of whatever faces request
 * happens to be active at the time. EL expressions in filtered resources
 * should therefore not reference any request-scoped or session-scoped values
 * as the cached result would be unpredictable.
 * <p>
 * This class is a per-webapp singleton, accessed via the getInstance method.
 *
 * @author imario
 */
public class TextResourceFilter implements Serializable
{
    private static final Log log = LogFactory.getLog(TextResourceFilter.class);

    private final static String CONTEXT_KEY = TextResourceFilter.class.getName() + ".INSTANCE";

    // A cache of all the resources ever loaded via getOrCreateFilteredResource
    private final Map filteredResources = Collections.synchronizedMap(new TreeMap());

    public static class ResourceInfo
    {
        private final long lastModified;
        private final String text;

        protected ResourceInfo(long lastModified, String text)
        {
            this.lastModified = lastModified;
            this.text = text;
        }

        public long getLastModified()
        {
            return lastModified;
        }

        public String getText()
        {
            return text;
        }

        public int getSize()
        {
            return text.length();
        }
    }

    protected TextResourceFilter()
    {
    }

    protected static TextResourceFilter create()
    {
        return new TextResourceFilter();
    }

    /**
     * Return the application-singleton instance of this class.
     */
    public static TextResourceFilter getInstance(ServletContext context)
    {
        synchronized(TextResourceFilter.class)
        {
            TextResourceFilter filterText = (TextResourceFilter) context.getAttribute(CONTEXT_KEY);
            if (filterText == null)
            {
                filterText = create();
                context.setAttribute(CONTEXT_KEY, filterText);
            }
            return filterText;
        }
    }

    /**
     * Return the application-singleton instance of this class.
     */
    public static TextResourceFilter getInstance(FacesContext context)
    {
        Map appMap = context.getExternalContext().getApplicationMap();
        synchronized(TextResourceFilter.class)
        {
            TextResourceFilter filterText = (TextResourceFilter) appMap.get(CONTEXT_KEY);
            if (filterText == null)
            {
                filterText = create();
                appMap.put(CONTEXT_KEY, filterText);
            }
            return filterText;
        }
    }

    /**
     * Return the cached content for the specified resource.
     * <p>
     * If the resource is not already in the cache (due to an earlier call to
     * getOrCreateFilteredResource) then null is returned.
     * <p>
     * The path param is a simple key that must match the value passed to
     * an earlier call to getOrCreateFilteredResource.
     */
    public ResourceInfo getFilteredResource(String path)
    {
        ResourceInfo filteredResource = (ResourceInfo) filteredResources.get(path);
        if (filteredResource == null)
        {
            return null;
        }
        return filteredResource;
    }

    /**
     * Load, filter and cache the specified resource (if it isn't already cached).
     * <p>
     * Note: This method is not synchronized for performance reasons (the map is).
     * The worst case is that we filter a resource twice the first time which is not
     * a problem.
     * <P>
     * The path param must start with a slash, and is interpreted as a path relative
     * to the webapp root (not the current page).
     */
    public ResourceInfo getOrCreateFilteredResource(FacesContext context, String path) throws IOException
    {
        if (!path.startsWith("/"))
        {
            throw new IllegalArgumentException("Path must start with a slash, but was: " + path);
        }

        ResourceInfo filteredResource = getFilteredResource(path);
        if (filteredResource != null)
        {
            return filteredResource;
        }

        //Tomcat ASF Bugzilla � Bug 43241
        //ServletContext.getResourceAsStream() does not follow API spec
        //ALL resources must start with '/' 
        String text = RendererUtils.loadResourceFile(context, path);
        if (text == null)
        {
            // avoid loading the errorneous resource over and over again
            text = "";
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new StringReader(text.toString()));

            String line;
            while ((line = reader.readLine()) != null)
            {
                int pos = line.indexOf("#{");
                if (pos > -1 && line.indexOf("}", pos) > -1)
                {
                    line = RendererUtils.getStringValue(context, context.getApplication().createValueBinding(line));
                }

                if (line != null)
                {
                    writer.println(line);
                }
            }
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    log.warn(e.getLocalizedMessage(), e);
                }
            }

            writer.close();
        }

        filteredResource = new ResourceInfo(System.currentTimeMillis(), stringWriter.toString());
        filteredResources.put(path, filteredResource);

        return filteredResource;
    }
}
