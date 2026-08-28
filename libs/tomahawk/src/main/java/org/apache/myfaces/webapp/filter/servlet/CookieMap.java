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
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * HttpServletRequest Cookies as Map.
 * <p>
 * NOTE: This class was copied from myfaces impl 
 * org.apache.myfaces.context.servlet and it is
 * used by TomahawkFacesContextWrapper. By that reason, it could change
 * in the future.
 * </p>
 * 
 * @since 1.1.7
 * @author Dimitry D'hondt
 * @author Anton Koinov
 * @version $Revision: 691871 $ $Date: 2008-09-03 23:32:08 -0500 (Wed, 03 Sep 2008) $
 */
public class CookieMap extends AbstractAttributeMap
{
    private static final Cookie[] EMPTY_ARRAY = new Cookie[0];

    final HttpServletRequest _httpServletRequest;

    CookieMap(HttpServletRequest httpServletRequest)
    {
        _httpServletRequest = httpServletRequest;
    }

    public void clear()
    {
        throw new UnsupportedOperationException(
            "Cannot clear HttpRequest Cookies");
    }

    public boolean containsKey(Object key)
    {
        Cookie[] cookies = _httpServletRequest.getCookies();
        if (cookies == null) return false;
        for (int i = 0, len = cookies.length; i < len; i++)
        {
            if (cookies[i].getName().equals(key))
            {
                return true;
            }
        }

        return false;
    }

    public boolean containsValue(Object findValue)
    {
        if (findValue == null)
        {
            return false;
        }

        Cookie[] cookies = _httpServletRequest.getCookies();
        if (cookies == null) return false;
        for (int i = 0, len = cookies.length; i < len; i++)
        {
            if (findValue.equals(cookies[i]))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isEmpty()
    {
        Cookie[] cookies = _httpServletRequest.getCookies();
        return cookies == null || cookies.length == 0;
    }

    public int size()
    {
        Cookie[] cookies = _httpServletRequest.getCookies();
        return cookies == null ? 0 : cookies.length;
    }

    public void putAll(Map t)
    {
        throw new UnsupportedOperationException();
    }


    protected Object getAttribute(String key)
    {
        Cookie[] cookies = _httpServletRequest.getCookies();
        if (cookies == null) return null;
        for (int i = 0, len = cookies.length; i < len; i++)
        {
            if (cookies[i].getName().equals(key))
            {
                return cookies[i];
            }
        }

        return null;
    }

    protected void setAttribute(String key, Object value)
    {
        throw new UnsupportedOperationException(
            "Cannot set HttpRequest Cookies");
    }

    protected void removeAttribute(String key)
    {
        throw new UnsupportedOperationException(
            "Cannot remove HttpRequest Cookies");
    }

    protected Enumeration getAttributeNames()
    {
        Cookie[] cookies = _httpServletRequest.getCookies();
        if (cookies == null)
        {
            return new CookieNameEnumeration(EMPTY_ARRAY);
        }
        else
        {
            return new CookieNameEnumeration(cookies);
        }
    }

    private static class CookieNameEnumeration implements Enumeration
    {
        private final Cookie[] _cookies;
        private final int _length;
        private int _index;

        public CookieNameEnumeration(Cookie[] cookies)
        {
            _cookies = cookies;
            _length = cookies.length;
        }

        public boolean hasMoreElements()
        {
            return _index < _length;
        }

        public Object nextElement()
        {
            return _cookies[_index++].getName();
        }
    }
}
