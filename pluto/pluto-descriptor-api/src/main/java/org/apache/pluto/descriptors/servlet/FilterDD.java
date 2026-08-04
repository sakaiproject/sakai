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

import org.apache.pluto.descriptors.common.IconDD;

import java.util.List;

/**
 * Filter DD() uration as contained within the
 * web.xml Deployment Descriptor.
 *
 * @version $Id: FilterDD.java 156636 2005-03-09 12:16:31Z cziegeler $
 * @since Feb 28, 2005
 */
public class FilterDD {

    private String filterName;
    private String filterClass;
    private String displayName;

    private String description;
    private IconDD icon;
    private List initParams;

    public FilterDD() {

    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IconDD getIcon() {
        return icon;
    }

    public void setIcon(IconDD icon) {
        this.icon = icon;
    }

    public List getInitParams() {
        return initParams;
    }

    public void setInitParams(List initParams) {
        this.initParams = initParams;
    }

    public boolean isValid() {
        return filterName != null && filterClass != null;
    }
}

