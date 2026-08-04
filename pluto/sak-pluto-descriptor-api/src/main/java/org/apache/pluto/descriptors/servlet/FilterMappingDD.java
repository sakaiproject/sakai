/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.descriptors.servlet;

import java.util.List;

/**
 * FilterMapping configuration as contained within the
 * web.xml Deployment Descriptor.
 * @version $Id: FilterMappingDD.java 157475 2005-03-14 22:13:18Z ddewolf $
 * @since Feb 28, 2005
 */
public class FilterMappingDD {

    private String filterName;
    private String servletName;
    private List urlPattern;
    private List dispatchers;

    public FilterMappingDD() {

    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public List getUrlPatterns() {
        return urlPattern;
    }

    public void setUrlPatterns(List urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public List getDispatchers() {
        return dispatchers;
    }

    public void setDispatchers(List dispatchers) {
        this.dispatchers = dispatchers;
    }

}

