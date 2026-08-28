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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

/**
 * <p>
 * NOTE: This class should be used(instantiated) only by 
 * TomahawkFacesContextWrapper. By that reason, it could change
 * in the future.
 * </p>
 * 
 * @since 1.1.8
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 782179 $ $Date: 2009-06-05 20:35:54 -0500 (Fri, 05 Jun 2009) $
 */
public class ActionRequestWrapper extends PortletRequestWrapper
    implements ActionRequest
{

    public ActionRequestWrapper(PortletRequest request)
    {
        super(request);
    }
    
    private ActionRequest _getActionRequest()
    {
        return (ActionRequest)super.getRequest();
    }

    public String getCharacterEncoding()
    {
        return _getActionRequest().getCharacterEncoding();
    }

    public int getContentLength()
    {
        return _getActionRequest().getContentLength();
    }

    public String getContentType()
    {
        return _getActionRequest().getContentType();
    }

    public InputStream getPortletInputStream() throws IOException
    {
        return _getActionRequest().getPortletInputStream();
    }

    public BufferedReader getReader() throws UnsupportedEncodingException,
            IOException
    {
        return _getActionRequest().getReader();
    }

    public void setCharacterEncoding(String s)
            throws UnsupportedEncodingException
    {
        _getActionRequest().setCharacterEncoding(s);
    }    

}
