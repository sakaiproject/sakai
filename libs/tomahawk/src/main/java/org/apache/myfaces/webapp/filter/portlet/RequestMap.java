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
package org.apache.myfaces.webapp.filter.portlet;

import java.util.Enumeration;
import java.util.Map;
import javax.portlet.PortletRequest;
import org.apache.myfaces.webapp.filter.servlet.AbstractAttributeMap;


/**
 * PortletRequest attributes Map.
 * <p>
 * NOTE: This class was copied from myfaces impl 
 * org.apache.myfaces.context.portlet and it is
 * used by TomahawkFacesContextWrapper. By that reason, it could change
 * in the future.
 * </p>
 * 
 * @since 1.1.7
 * @author  Stan Silvert (latest modification by $Author: lu4242 $)
 * @version $Revision: 691871 $ $Date: 2008-09-03 23:32:08 -0500 (Wed, 03 Sep 2008) $
 */
public class RequestMap extends AbstractAttributeMap
{
    final PortletRequest _portletRequest;

    RequestMap(PortletRequest portletRequest)
    {
        _portletRequest = portletRequest;
    }

    protected Object getAttribute(String key)
    {
        return _portletRequest.getAttribute(key);
    }

    protected void setAttribute(String key, Object value)
    {
        _portletRequest.setAttribute(key, value);
    }

    protected void removeAttribute(String key)
    {
        _portletRequest.removeAttribute(key);
    }

    protected Enumeration getAttributeNames()
    {
        return _portletRequest.getAttributeNames();
    }

    public void putAll(Map t)
    {
        throw new UnsupportedOperationException();
    }


    public void clear()
    {
        throw new UnsupportedOperationException();
    }    
}
