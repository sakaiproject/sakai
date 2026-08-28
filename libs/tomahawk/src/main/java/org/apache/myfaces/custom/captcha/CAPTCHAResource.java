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
package org.apache.myfaces.custom.captcha;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.shared_tomahawk.resource.ResourceHandlerSupport;


public class CAPTCHAResource extends Resource
{

    private String _captchaSessionKeyName;
    private ResourceHandlerSupport _resourceHandlerSupport;
    
    public CAPTCHAResource(ResourceHandlerSupport support, String captchaSessionKeyName, String resourceName)
    {
        super();
        _resourceHandlerSupport = support;
        _captchaSessionKeyName = captchaSessionKeyName;
        setLibraryName(CAPTCHAResourceHandlerWrapper.CAPTCHA_LIBRARY);
        setResourceName(resourceName);
        setContentType("image/png");
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        // Return an InputStream forces buffer the image, and really it is not necessary.
        // so, we just use CAPTCHAResourceHandlerWrapper to intercept requests and render
        // the image there directly.
        return null;
    }
    
    static class WrappedByteArrayOutputStream extends ByteArrayOutputStream{
        
        public WrappedByteArrayOutputStream(){
            super();
        }
        
        public WrappedByteArrayOutputStream(int size){
            super(size);                
        }
        
        private byte[] getInnerArray(){
            return buf; 
        }
        
        private int getInnerCount(){
            return count;
        }
    }

    @Override
    public String getRequestPath()
    {
        String path;
        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (_resourceHandlerSupport.isExtensionMapping())
        {
            path = ResourceHandler.RESOURCE_IDENTIFIER + '/' + 
                getResourceName() + _resourceHandlerSupport.getMapping();
        }
        else
        {
            String mapping = _resourceHandlerSupport.getMapping(); 
            path = ResourceHandler.RESOURCE_IDENTIFIER + '/' + getResourceName();
            path = (mapping == null) ? path : mapping + path;
        }
        path = path + "?ln=" + getLibraryName() + "&captchaSessionKeyName=" + _captchaSessionKeyName + "&dummyParameter=" + System.currentTimeMillis();

        return facesContext.getApplication().getViewHandler().getResourceURL(facesContext, path);
    }

    @Override
    public Map<String, String> getResponseHeaders()
    {
        return Collections.emptyMap();
    }

    @Override
    public URL getURL()
    {
        // This is a generated resource, so it does not have an inner url, return null
        return null;
    }

    @Override
    public boolean userAgentNeedsUpdate(FacesContext context)
    {
        return true;
    }
}
