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

package org.sakaiproject.velocity.util;

import java.io.InputStream;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import jakarta.servlet.ServletContext;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * A Velocity {@link ResourceLoader} that loads templates from a webapp's {@link ServletContext},
 * replacing the {@code javax.servlet}-based official loaders, which cannot load under Jakarta EE.
 */
@Slf4j
public class WebappResourceLoader extends ResourceLoader {

    private static final long CACHE_EXPIRATION_IN_MILLIS = 60 * 1000;

    private ServletContext servletContext;
    private String[] paths;
    private Map<String, Date> templatePaths;

    @Override
    public void init(ExtProperties configuration) {
        log.debug("WebappResourceLoader : initialization starting.");

        Object attribute = rsvc.getApplicationAttribute(ServletContext.class.getName());
        if (!(attribute instanceof ServletContext)) {
            throw new IllegalStateException(
                    "WebappResourceLoader : unable to retrieve ServletContext application attribute");
        }
        servletContext = (ServletContext) attribute;

        Vector<String> configuredPaths = configuration.getVector("path", null);
        if (configuredPaths == null || configuredPaths.isEmpty()) {
            configuredPaths = new Vector<>();
            configuredPaths.add("/");
        }
        paths = new String[configuredPaths.size()];
        for (int i = 0; i < configuredPaths.size(); i++) {
            paths[i] = normalize(configuredPaths.get(i));
        }

        templatePaths = new HashMap<>();

        log.debug("WebappResourceLoader : initialization complete.");
    }

    private static String normalize(String path) {
        String normalized = path;
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }

    @Override
    public synchronized Reader getResourceReader(String name, String encoding) throws ResourceNotFoundException {
        if (name == null || name.length() == 0) {
            throw new ResourceNotFoundException("WebappResourceLoader : No template name provided");
        }

        String relativeName = name.startsWith("/") ? name.substring(1) : name;

        for (String path : paths) {
            InputStream result = servletContext.getResourceAsStream(path + relativeName);
            if (result != null) {
                templatePaths.put(name, new Date());
                try {
                    return buildReader(result, encoding);
                } catch (java.io.IOException e) {
                    throw new ResourceNotFoundException(
                            "WebappResourceLoader : Resource '" + name + "' could not be read.", e);
                }
            }
        }

        throw new ResourceNotFoundException("WebappResourceLoader : Resource '" + name + "' not found.");
    }

    private Date getCachedFileLastLoaded(String fileName) {
        return templatePaths.get(fileName);
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        Date fileLastLoaded = getCachedFileLastLoaded(resource.getName());
        if (fileLastLoaded == null) {
            return true;
        }
        return new Date().getTime() - fileLastLoaded.getTime() > CACHE_EXPIRATION_IN_MILLIS;
    }

    @Override
    public long getLastModified(Resource resource) {
        Date fileLastLoaded = getCachedFileLastLoaded(resource.getName());
        if (fileLastLoaded == null) {
            return 0;
        }
        return fileLastLoaded.getTime();
    }
}
