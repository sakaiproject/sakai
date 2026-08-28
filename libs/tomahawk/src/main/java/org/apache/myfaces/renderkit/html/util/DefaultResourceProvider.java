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
package org.apache.myfaces.renderkit.html.util;

import jakarta.servlet.ServletContext;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

/**
 * A class which provide the resource using the standard <code>class.getResource</code> lookup
 * stuff.
 *
 * @author imario (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (Thu, 03 Jul 2008) $
 */
public class DefaultResourceProvider implements ResourceProvider
{
    private final Class clazz;

    public DefaultResourceProvider(Class clazz)
    {
        this.clazz = clazz;
    }

    protected URL getResource(String resource)
    {
        resource = "resource/" + resource;
        return clazz.getResource(resource);
    }

    public boolean exists(ServletContext context, String resource)
    {
        return getResource(resource) != null;
    }

    public long getLastModified(ServletContext context, String resource) throws IOException
    {
        return getResource(resource).openConnection().getLastModified();
    }

    public int getContentLength(ServletContext context, String resource) throws IOException
    {
        return getResource(resource).openConnection().getContentLength();
    }

    public InputStream getInputStream(ServletContext context, String resource) throws IOException
    {
        return getResource(resource).openConnection().getInputStream();
    }

    public String getEncoding(ServletContext context, String resource) throws IOException
    {
        return null; //Tomahawk-877, this has to be null for now to avoid encoding issues
    }
}
