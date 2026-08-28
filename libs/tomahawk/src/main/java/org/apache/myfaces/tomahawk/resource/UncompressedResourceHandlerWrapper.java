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
package org.apache.myfaces.tomahawk.resource;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.shared_tomahawk.resource.ResourceHandlerCache.ResourceValue;
import org.apache.myfaces.shared_tomahawk.resource.ResourceHandlerCache;
import org.apache.myfaces.shared_tomahawk.resource.ResourceHandlerSupport;
import org.apache.myfaces.shared_tomahawk.resource.ResourceImpl;
import org.apache.myfaces.shared_tomahawk.resource.ResourceLoader;
import org.apache.myfaces.shared_tomahawk.resource.ResourceMeta;
import org.apache.myfaces.shared_tomahawk.util.ClassUtils;

public class UncompressedResourceHandlerWrapper extends jakarta.faces.application.ResourceHandlerWrapper
{
    private ResourceHandler _delegate;
    
    private ResourceHandlerSupport _resourceHandlerSupport;

    private ResourceHandlerCache _resourceHandlerCache;
    
    public UncompressedResourceHandlerWrapper(ResourceHandler delegate)
    {
        super();
        _delegate = delegate;
    }

    @Override
    public Resource createResource(String resourceName)
    {
        return createResource(resourceName, null);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName)
    {
        return createResource(resourceName, libraryName, null);
    }

    public Resource createResource(String resourceName, String libraryName,
            String contentType)
    {
        Resource resource = null;
        
        // There will be no resource loaders if ProjectStage != Development
        if (getResourceHandlerSupport().getResourceLoaders().length > 0)
        {
            if (contentType == null)
            {
                //Resolve contentType using ExternalContext.getMimeType
                contentType = FacesContext.getCurrentInstance().getExternalContext().getMimeType(resourceName);
            }
            
            final String localePrefix = getLocalePrefixForLocateResource();
            
            if(getResourceLoaderCache().containsResource(resourceName, libraryName, contentType, localePrefix))
            {
                ResourceValue resourceValue = getResourceLoaderCache().getResource(resourceName, libraryName, contentType, localePrefix);
                resource = new ResourceImpl(resourceValue.getResourceMeta(), resourceValue.getResourceLoader(),
                        getResourceHandlerSupport(), contentType);
            }
            else
            {
                for (ResourceLoader loader : getResourceHandlerSupport()
                        .getResourceLoaders())
                {
                    ResourceMeta resourceMeta = deriveResourceMeta(loader,
                            resourceName, libraryName);
        
                    if (resourceMeta != null)
                    {
                        resource = new ResourceImpl(resourceMeta, loader,
                                getResourceHandlerSupport(), contentType);
                        
                        getResourceLoaderCache().putResource(resourceName, libraryName, contentType, localePrefix, resourceMeta, loader);
                        break;
                    }
                }
            }
            if (resource != null)
            {
                return resource;
            }
        }
        return getWrapped().createResource(resourceName, libraryName, contentType);
    }
    
    /**
     * This method try to create a ResourceMeta for a specific resource
     * loader. If no library, or resource is found, just return null,
     * so the algorithm in createResource can continue checking with the 
     * next registered ResourceLoader. 
     */
    protected ResourceMeta deriveResourceMeta(ResourceLoader resourceLoader,
            String resourceName, String libraryName)
    {
        String localePrefix = getLocalePrefixForLocateResource();
        String resourceVersion = null;
        String libraryVersion = null;
        ResourceMeta resourceId = null;
        
        //1. Try to locate resource in a localized path
        if (localePrefix != null)
        {
            if (null != libraryName)
            {
                String pathToLib = localePrefix + '/' + libraryName;
                libraryVersion = resourceLoader.getLibraryVersion(pathToLib);

                if (null != libraryVersion)
                {
                    String pathToResource = localePrefix + '/'
                            + libraryName + '/' + libraryVersion + '/'
                            + resourceName;
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }
                else
                {
                    String pathToResource = localePrefix + '/'
                            + libraryName + '/' + resourceName;
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }

                if (!(resourceVersion != null && ResourceLoader.VERSION_INVALID.equals(resourceVersion)))
                {
                    resourceId = resourceLoader.createResourceMeta(localePrefix, libraryName,
                            libraryVersion, resourceName, resourceVersion);
                }
            }
            else
            {
                resourceVersion = resourceLoader
                        .getResourceVersion(localePrefix + '/'+ resourceName);
                if (!(resourceVersion != null && ResourceLoader.VERSION_INVALID.equals(resourceVersion)))
                {               
                    resourceId = resourceLoader.createResourceMeta(localePrefix, null, null,
                            resourceName, resourceVersion);
                }
            }

            if (resourceId != null)
            {
                URL url = resourceLoader.getResourceURL(resourceId);
                if (url == null)
                {
                    resourceId = null;
                }
            }            
        }
        
        //2. Try to localize resource in a non localized path
        if (resourceId == null)
        {
            if (null != libraryName)
            {
                libraryVersion = resourceLoader.getLibraryVersion(libraryName);

                if (null != libraryVersion)
                {
                    String pathToResource = (libraryName + '/' + libraryVersion
                            + '/' + resourceName);
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }
                else
                {
                    String pathToResource = (libraryName + '/'
                            + resourceName);
                    resourceVersion = resourceLoader
                            .getResourceVersion(pathToResource);
                }

                if (!(resourceVersion != null && ResourceLoader.VERSION_INVALID.equals(resourceVersion)))
                {               
                    resourceId = resourceLoader.createResourceMeta(null, libraryName,
                            libraryVersion, resourceName, resourceVersion);
                }
            }
            else
            {
                resourceVersion = resourceLoader
                        .getResourceVersion(resourceName);
                
                if (!(resourceVersion != null && ResourceLoader.VERSION_INVALID.equals(resourceVersion)))
                {               
                    resourceId = resourceLoader.createResourceMeta(null, null, null,
                            resourceName, resourceVersion);
                }
            }

            if (resourceId != null)
            {
                URL url = resourceLoader.getResourceURL(resourceId);
                if (url == null)
                {
                    resourceId = null;
                }
            }            
        }
        
        return resourceId;
    }
    
    protected String getLocalePrefixForLocateResource()
    {
        String localePrefix = null;
        FacesContext context = FacesContext.getCurrentInstance();

        String bundleName = context.getApplication().getMessageBundle();

        if (null != bundleName)
        {
            Locale locale = context.getApplication().getViewHandler()
                    .calculateLocale(context);

            ResourceBundle bundle = ResourceBundle
                    .getBundle(bundleName, locale, ClassUtils.getContextClassLoader());

            if (bundle != null)
            {
                try
                {
                    localePrefix = bundle.getString(ResourceHandler.LOCALE_PREFIX);
                }
                catch (MissingResourceException e)
                {
                    // Ignore it and return null
                }
            }
        }
        return localePrefix;
    }

    
    @Override
    public ResourceHandler getWrapped()
    {
        return _delegate;
    }
    
    /**
     * @param resourceHandlerSupport
     *            the resourceHandlerSupport to set
     */
    public void setResourceHandlerSupport(
            ResourceHandlerSupport resourceHandlerSupport)
    {
        _resourceHandlerSupport = resourceHandlerSupport;
    }

    /**
     * @return the resourceHandlerSupport
     */
    protected ResourceHandlerSupport getResourceHandlerSupport()
    {
        if (_resourceHandlerSupport == null)
        {
            _resourceHandlerSupport = new UncompressedResourceHandlerSupport();
        }
        return _resourceHandlerSupport;
    }
    
    private ResourceHandlerCache getResourceLoaderCache()
    {
        if (_resourceHandlerCache == null)
            _resourceHandlerCache = new ResourceHandlerCache();
        return _resourceHandlerCache;
    }
}
