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

import java.util.ArrayList;
import java.util.List;

/**
 * Filter DD() uration as contained within the
 * web.xml Deployment Descriptor.
 *
 * @version $Id: ServletDD.java 156743 2005-03-10 05:50:30Z ddewolf $
 * @since Feb 28, 2005
 */
public class ServletDD {

    private String servletName;
    private String servletClass;
    private String displayName;
    private String description;
    private String jspFile;
    private LoadOnStartupDD loadOnStartup;

    private IconDD icon;
    private List initParams = new ArrayList();
    private List securityRoleRefs = new ArrayList();

    public ServletDD() {

    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getJspFile() {
        return jspFile;
    }

    public void setJspFile(String jspFile) {
        this.jspFile = jspFile;
    }

    public LoadOnStartupDD getLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(LoadOnStartupDD loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
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

    public List getSecurityRoleRefs() {
        return securityRoleRefs;
    }

    public void setSecurityRoleRefs(List securityRoleRefs) {
        this.securityRoleRefs = securityRoleRefs;
    }

}

