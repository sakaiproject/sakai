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
package org.apache.myfaces.component.html.util;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.myfaces.renderkit.html.util.ResourceHandler;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Mathias Broekelmann
 *
 */
public class ParameterResourceHandler implements ResourceHandler
{
    private final Map _parameters;
    private final Class _resourceLoaderClass;

    private Integer _hashCode;

    /**
     * @param resourceLoaderClass
     * @param parameters
     */
    public ParameterResourceHandler(Class resourceLoaderClass, Map parameters)
    {
        _resourceLoaderClass = resourceLoaderClass;
        _parameters = parameters;
    }

    /**
     * @see org.apache.myfaces.renderkit.html.util.ResourceHandler#getResourceLoaderClass()
     */
    public Class getResourceLoaderClass()
    {
        return _resourceLoaderClass;
    }

    /**
     * @see org.apache.myfaces.renderkit.html.util.ResourceHandler#getResourceUri(jakarta.faces.context.FacesContext)
     */
    public String getResourceUri(FacesContext context)
    {
        if (_parameters != null && !_parameters.isEmpty())
        {
            StringBuffer sb = new StringBuffer();
            sb.append("?");
            for (Iterator iter = _parameters.entrySet().iterator(); iter.hasNext();)
            {
                Map.Entry entry = (Map.Entry) iter.next();
                sb.append(entry.getKey());
                sb.append("=");
                if (entry.getValue() != null)
                {
                    try
                    {
                        // encode the value to make it safe to be passed through the url
                        // the best we can do here is to use the same encoding than the response writer
                        String encoding = context.getResponseWriter().getCharacterEncoding();
                        if (encoding == null)
                        {
                            // or fallback to UTF-8 (which makes the most sense)
                            encoding = "UTF-8";
                        }
                        sb.append(URLEncoder.encode(entry.getValue().toString(), encoding));
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        throw new FacesException(e);
                    }
                }
                if (iter.hasNext())
                {
                    sb.append("&");
                }
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ParameterResourceHandler)
        {
            ParameterResourceHandler other = (ParameterResourceHandler) obj;
            return new EqualsBuilder().append(_parameters, other._parameters).isEquals();
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        if (_hashCode == null)
        {
            _hashCode = new Integer(new HashCodeBuilder().append(_parameters).toHashCode());
        }
        return _hashCode.intValue();
    }
}
