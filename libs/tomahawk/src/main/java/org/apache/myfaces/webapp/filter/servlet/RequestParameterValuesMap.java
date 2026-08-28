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
package org.apache.myfaces.webapp.filter.servlet;

import java.util.Enumeration;

import jakarta.servlet.ServletRequest;

/**
 * ServletRequest multi-value parameters as Map.
 * 
 * <p>
 * NOTE: This class was copied from myfaces impl 
 * org.apache.myfaces.context.servlet and it is
 * used by TomahawkFacesContextWrapper. By that reason, it could change
 * in the future.
 * </p>
 * 
 * @since 1.1.7
 * @author Anton Koinov (latest modification by $Author: lu4242 $)
 * @version $Revision: 691871 $ $Date: 2008-09-03 23:32:08 -0500 (Wed, 03 Sep 2008) $
 */
public class RequestParameterValuesMap extends AbstractAttributeMap
{
    private final ServletRequest _servletRequest;

    RequestParameterValuesMap(ServletRequest servletRequest)
    {
        _servletRequest = servletRequest;
    }

    protected Object getAttribute(String key)
    {
        return _servletRequest.getParameterValues(key);
    }

    protected void setAttribute(String key, Object value)
    {
        throw new UnsupportedOperationException(
            "Cannot set ServletRequest ParameterValues");
    }

    protected void removeAttribute(String key)
    {
        throw new UnsupportedOperationException(
            "Cannot remove ServletRequest ParameterValues");
    }

    protected Enumeration getAttributeNames()
    {
        return _servletRequest.getParameterNames();
    }
}