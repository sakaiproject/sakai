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
import java.io.IOException;
import java.io.InputStream;

/**
 * A class which can provide the resource itself
 *
 * @author imario (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (Thu, 03 Jul 2008) $
 */
public interface ResourceProvider
{
    /**
     * check if the resource exists
     */
    public boolean exists(ServletContext context, String resource);

    /**
     * get the content length of the resource
     */
    public int getContentLength(ServletContext context, String resource) throws IOException;

    /**
     * get the last modified time of the resource
     */
    public long getLastModified(ServletContext context, String resource) throws IOException;

    /**
     * get the input stream of the resource
     */
    public InputStream getInputStream(ServletContext context, String resource) throws IOException;

    /**
     * get resource encoding
     */
    public String getEncoding(ServletContext context, String resource) throws IOException;
}
