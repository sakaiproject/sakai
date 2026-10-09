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

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.myfaces.webapp.filter.MultipartRequestWrapper;


/**
 * This filters is mandatory for the use of many components.
 * It handles the Multipart requests (for file upload)
 * It's used by the components that need javascript libraries
 *
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller </a>
 * @version $Revision: 1379874 $ $Date: 2012-09-01 17:13:08 -0500 (Sat, 01 Sep 2012) $
 */
public class MultipartFilter implements Filter
{

    private long uploadMaxSize = 100 * 1024 * 1024; // 100 MB
    
    private long uploadMaxFileSize = 100 * 1024 * 1024; // 10 MB

    private int uploadThresholdSize = 1 * 1024 * 1024; // 1 MB

    private String uploadRepositoryPath = null; //standard temp directory
    
    private boolean cacheFileSizeErrors = false;

    public void init(FilterConfig filterConfig)
    {
        uploadMaxFileSize = resolveSize(filterConfig.getInitParameter("uploadMaxFileSize"), uploadMaxFileSize);
        String param = filterConfig.getInitParameter("uploadMaxSize");
        if (param != null)
        {
            uploadMaxSize = resolveSize(param, uploadMaxSize);
        }
        else
        {
            //If not set, default to uploadMaxFileSize
            uploadMaxSize = resolveSize(param, uploadMaxFileSize);
        }
        uploadThresholdSize = resolveSize(filterConfig.getInitParameter("uploadThresholdSize"), uploadThresholdSize);
        uploadRepositoryPath = filterConfig.getInitParameter("uploadRepositoryPath");
        cacheFileSizeErrors = getBooleanValue(filterConfig.getInitParameter("cacheFileSizeErrors"), false);
    }


    private int resolveSize(String param, int defaultValue)
    {
        int numberParam = defaultValue;

        if (param != null)
        {
            param = param.toLowerCase();
            int factor = 1;
            String number = param;

            if (param.endsWith("g"))
            {
                factor = 1024 * 1024 * 1024;
                number = param.substring(0, param.length() - 1);
            } else if (param.endsWith("m"))
            {
                factor = 1024 * 1024;
                number = param.substring(0, param.length() - 1);
            } else if (param.endsWith("k"))
            {
                factor = 1024;
                number = param.substring(0, param.length() - 1);
            }

            numberParam = Integer.parseInt(number) * factor;
        }
        return numberParam;
    }
    
    private long resolveSize(String param, long defaultValue)
    {
        long numberParam = defaultValue;

        if (param != null)
        {
            param = param.toLowerCase();
            long factor = 1;
            String number = param;

            if (param.endsWith("g"))
            {
                factor = 1024 * 1024 * 1024;
                number = param.substring(0, param.length() - 1);
            } else if (param.endsWith("m"))
            {
                factor = 1024 * 1024;
                number = param.substring(0, param.length() - 1);
            } else if (param.endsWith("k"))
            {
                factor = 1024;
                number = param.substring(0, param.length() - 1);
            }

            numberParam = Long.parseLong(number) * factor;
        }
        return numberParam;
    }
    
    private static boolean getBooleanValue(String initParameter, boolean defaultVal)
    {
        if(initParameter == null || initParameter.trim().length()==0)
            return defaultVal;

        return (initParameter.equalsIgnoreCase("on") || initParameter.equals("1") || initParameter.equalsIgnoreCase("true"));
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        if (!(response instanceof HttpServletResponse))
        {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // For multipart/form-data requests
        if (JakartaServletFileUpload.isMultipartContent(httpRequest))
        {
            chain.doFilter(new MultipartRequestWrapper(httpRequest, uploadMaxFileSize, 
                    uploadThresholdSize, uploadRepositoryPath, uploadMaxSize , cacheFileSizeErrors), response);
        } else
        {
            chain.doFilter(request, response);
        }
    }


    public void destroy()
    {
        // NoOp
    }
}
