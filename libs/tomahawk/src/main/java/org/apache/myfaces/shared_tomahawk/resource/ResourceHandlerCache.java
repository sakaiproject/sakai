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
package org.apache.myfaces.shared_tomahawk.resource;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.shared_tomahawk.util.ConcurrentLRUCache;
import org.apache.myfaces.shared_tomahawk.util.WebConfigParamUtils;

public class ResourceHandlerCache
{
    private static final Logger log = Logger
            .getLogger(ResourceHandlerCache.class.getName());

    private Boolean _resourceCacheEnabled = null;
    private volatile ConcurrentLRUCache<ResourceKey, ResourceValue> _resourceCacheMap = null;

    /**
     * Controls the size of the cache used to check if a resource exists or not. 
     * 
     * <p>See org.apache.myfaces.RESOURCE_HANDLER_CACHE_ENABLED for details.</p>
     */
    @JSFWebConfigParam(defaultValue = "500", since = "2.0.2", group="resources", 
            classType="java.lang.Integer", tags="performance")
    private static final String RESOURCE_HANDLER_CACHE_SIZE_ATTRIBUTE = 
        "org.apache.myfaces.RESOURCE_HANDLER_CACHE_SIZE";
    private static final int RESOURCE_HANDLER_CACHE_DEFAULT_SIZE = 500;

    /**
     * Enable or disable the cache used to "remember" if a resource handled by 
     * the default ResourceHandler exists or not.
     * 
     */
    @JSFWebConfigParam(defaultValue = "true", since = "2.0.2", group="resources", 
            expectedValues="true,false", tags="performance")
    private static final String RESOURCE_HANDLER_CACHE_ENABLED_ATTRIBUTE = 
        "org.apache.myfaces.RESOURCE_HANDLER_CACHE_ENABLED";
    private static final boolean RESOURCE_HANDLER_CACHE_ENABLED_DEFAULT = true;

    public ResourceValue getResource(String resourceName, String libraryName,
            String contentType, String localePrefix)
    {
        if (!isResourceCachingEnabled() || _resourceCacheMap == null)
        {
            return null;
        }

        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to get resource from cache for "
                    + resourceName);
        }

        ResourceKey key = new ResourceKey(resourceName, libraryName, contentType, localePrefix);

        return _resourceCacheMap.get(key);
    }
    
    public boolean containsResource(String resourceName, String libraryName, String contentType, String localePrefix)
    {
        if (!isResourceCachingEnabled() || _resourceCacheMap == null)
        {
            return false;
        }

        ResourceKey key = new ResourceKey(resourceName, libraryName, contentType, localePrefix);
        return _resourceCacheMap.get(key) != null;
    }

    public void putResource(String resourceName, String libraryName,
            String contentType, String localePrefix, ResourceMeta resource, ResourceLoader loader)
    {
        if (!isResourceCachingEnabled())
        {
            return;
        }

        if (log.isLoggable(Level.FINE))
        {
            log.log(Level.FINE, "Attemping to put resource to cache for "
                    + resourceName);
        }

        if (_resourceCacheMap == null)
        {
            if (log.isLoggable(Level.FINE))
            {
                log.log(Level.FINE, "Initializing resource cache map");
            }
            int maxSize = getMaxSize();
            _resourceCacheMap = new ConcurrentLRUCache<ResourceKey, ResourceValue>(
                    (maxSize * 4 + 3) / 3, maxSize);
        }

        _resourceCacheMap.put(new ResourceKey(resourceName, libraryName,
                contentType, localePrefix), new ResourceValue(resource, loader));
    }

    private boolean isResourceCachingEnabled()
    {
        if (_resourceCacheEnabled == null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();

            //first, check to make sure that ProjectStage is production, if not, skip caching
            if (!facesContext.isProjectStage(ProjectStage.Production))
            {
                _resourceCacheEnabled = Boolean.FALSE;
                return _resourceCacheEnabled;
            }

            ExternalContext externalContext = facesContext.getExternalContext();
            if (externalContext == null)
            {
                return false; //don't cache right now, but don't disable it yet either
            }

            //if in production, make sure that the cache is not explicitly disabled via context param
            _resourceCacheEnabled = WebConfigParamUtils.getBooleanInitParameter(externalContext, 
                    ResourceHandlerCache.RESOURCE_HANDLER_CACHE_ENABLED_ATTRIBUTE,
                    ResourceHandlerCache.RESOURCE_HANDLER_CACHE_ENABLED_DEFAULT);

            if (log.isLoggable(Level.FINE))
            {
                log.log(Level.FINE, "MyFaces Resource Caching Enabled="
                        + _resourceCacheEnabled);
            }
        }
        return _resourceCacheEnabled;
    }

    private int getMaxSize()
    {
        
        ExternalContext externalContext = FacesContext.getCurrentInstance()
                .getExternalContext();
        return WebConfigParamUtils.getIntegerInitParameter(externalContext, 
                RESOURCE_HANDLER_CACHE_SIZE_ATTRIBUTE, RESOURCE_HANDLER_CACHE_DEFAULT_SIZE);
    }

    public static class ResourceKey
    {
        private String resourceName;
        private String libraryName;
        private String contentType;
        private String localePrefix;

        public ResourceKey(String resourceName, String libraryName,
                String contentType, String localePrefix)
        {
            this.resourceName = resourceName;
            this.libraryName = libraryName;
            this.contentType = contentType;
            this.localePrefix = localePrefix;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            ResourceKey that = (ResourceKey) o;

            if (contentType != null ? !contentType.equals(that.contentType) : that.contentType != null)
            {
                return false;
            }
            if (libraryName != null ? !libraryName.equals(that.libraryName) : that.libraryName != null)
            {
                return false;
            }
            if (localePrefix != null ? !localePrefix.equals(that.localePrefix) : that.localePrefix != null)
            {
                return false;
            }
            if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = resourceName != null ? resourceName.hashCode() : 0;
            result = 31 * result + (libraryName != null ? libraryName.hashCode() : 0);
            result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
            result = 31 * result + (localePrefix != null ? localePrefix.hashCode() : 0);
            return result;
        }
    }

    public static class ResourceValue
    {
        private ResourceMeta resourceMeta;
        
        private ResourceLoader resourceLoader;

        public ResourceValue(ResourceMeta resourceMeta,
                ResourceLoader resourceLoader)
        {
            super();
            this.resourceMeta = resourceMeta;
            this.resourceLoader = resourceLoader;
        }

        public ResourceMeta getResourceMeta()
        {
            return resourceMeta;
        }

        public ResourceLoader getResourceLoader()
        {
            return resourceLoader;
        }
    }

}
