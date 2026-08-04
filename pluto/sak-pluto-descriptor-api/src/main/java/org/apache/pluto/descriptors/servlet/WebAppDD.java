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
import java.util.Iterator;
import java.util.List;

/**
 * WebApplication configuration as contained
 * within the web.xml Deployment Descriptor.
 *
 * @version $Id: WebAppDD.java 157475 2005-03-14 22:13:18Z ddewolf $
 * @since Feb 28, 2005
 */
public class WebAppDD {

    private IconDD icon;
    private String displayName;
    private String description;
    private List contextParams = new ArrayList();
    private List filters = new ArrayList();
    private List filterMappings = new ArrayList();
    private List listeners = new ArrayList();
    private List servlets = new ArrayList();
    private List servletMappings = new ArrayList();
    private SessionConfigDD sessionConfig;
    private List mimeMappings = new ArrayList();
    private WelcomeFileListDD welcomeFileList;
    private List errorPages = new ArrayList();
    private List taglibs = new ArrayList();
    private JspConfigDD jspConfig;
    private List resourceRefs = new ArrayList();
    private List securityConstraints = new ArrayList();
    private LoginConfigDD loginConfig;
    private List securityRoles = new ArrayList();
    private List envEntrys = new ArrayList();
    private List ejbRefs = new ArrayList();
    // Default to servletVersion 2.3.  If a <web-app>
    // element is present with a version attribute,
    // the Castor mapping will update this field.
    private String servletVersion = "2.3";
    // Default to false.  If a <web-app>
    // contains a <distributable/> element, then
    // Castor will update this field to true.
    private DistributableDD distributableDD = new DistributableDD();
    // Needed to generate the correct default
    // namespace URI in Castor
    private String xmlNs = null;

    public WebAppDD() {

    }

    public IconDD getIcon() {
        return icon;
    }

    public void setIcon(IconDD icon) {
        this.icon= icon;
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

    public boolean isDistributable() {
        return distributableDD.isDistributable().booleanValue();
    }

    public DistributableDD getDistributable() {
        return distributableDD;
    }

    public void setDistributable() {
        this.distributableDD.setDistributable(true);
    }

    public void setDistributable(boolean distributable) {
        this.distributableDD.setDistributable(distributable);
    }

    /**
     * Retrieve the context parameters.
     * @return InitParamDD instances.
     */
    public List getContextParams() {
        return contextParams;
    }

    public void setContextParams(List contextParams) {
        this.contextParams = contextParams;
    }

    public List getFilters() {
        return filters;
    }

    public void setFilters(List filters) {
        this.filters = filters;
    }

    public List getFilterMappings() {
        return filterMappings;
    }

    public void setFilterMappings(List filterMappings) {
        this.filterMappings = filterMappings;
    }

    public List getListeners() {
        return listeners;
    }

    public void setListeners(List listeners) {
        this.listeners = listeners;
    }

    public List getServlets() {
        return servlets;
    }

    public void setServlets(List servlets) {
        this.servlets = servlets;
    }

    public List getServletMappings() {
        return servletMappings;
    }

    public void setServletMappings(List servletMappings) {
        this.servletMappings = servletMappings;
    }

    public SessionConfigDD getSessionConfig() {
        return sessionConfig ;
    }

    public void setSessionConfig(SessionConfigDD sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    public List getMimeMappings() {
        return mimeMappings;
    }

    public void setMimeMappings(List mimeMappings) {
        this.mimeMappings = mimeMappings;
    }

    public WelcomeFileListDD getWelcomeFileList() {
        return welcomeFileList;
    }

    public void setWelcomeFileList(WelcomeFileListDD welcomeFileList) {
        this.welcomeFileList = welcomeFileList;
    }

    public List getErrorPages() {
        return errorPages;
    }

    public void setErrorPages(List errorPages) {
        this.errorPages = errorPages;
    }

    public List getTaglibs() {
        return taglibs;
    }

    public void setTaglibs(List taglibs) {
        this.taglibs = taglibs;
    }

    public List getResourceRefs() {
        return resourceRefs;
    }

    public void setResourceRefs(List resourceRefs) {
        this.resourceRefs = resourceRefs;
    }

    public List getSecurityConstraints() {
        return securityConstraints;
    }

    public void setSecurityConstraints(List securityConstraints) {
        this.securityConstraints = securityConstraints;
    }

    public LoginConfigDD getLoginConfig() {
        return loginConfig;
    }

    public void setLoginConfig(LoginConfigDD loginConfig) {
        this.loginConfig = loginConfig;
    }

    public List getSecurityRoles() {
        return securityRoles;
    }

    public void setSecurityRoles(List securityRoles) {
        this.securityRoles = securityRoles;
    }

    public List getEnvEntrys() {
        return envEntrys;
    }

    public void setEnvEntrys(List envEntrys) {
        this.envEntrys = envEntrys;
    }

    public List getEjbRefs() {
        return ejbRefs;
    }

    public void setEjbRefs(List ejbRefs) {
        this.ejbRefs = ejbRefs;
    }


    public String getServletVersion() {
        return servletVersion;
    }

    public void setServletVersion(String servletVersion) {
        this.servletVersion = servletVersion;
    }
    
    public String getXmlNs() {
        return this.xmlNs;
    }

    public void setXmlNs(String xmlNs) {
        this.xmlNs = xmlNs;
    }

    public JspConfigDD getJspConfig()
    {
        return jspConfig;
    }

    public void setJspConfig( JspConfigDD jspConfig )
    {
        this.jspConfig = jspConfig;
    }
    
// Helpers

    public ServletDD getServlet(String name) {
        ArrayList set = new ArrayList(servlets);
        Iterator it = set.iterator();
        ServletDD dd;
        while(name!=null && it.hasNext()) {
            dd = (ServletDD)it.next();
            if(name.equals(dd.getServletName())) {
                return dd;
            }
        }
        return null;
    }

    public ServletMappingDD getServletMapping(String uri) {
        ArrayList set = new ArrayList(servletMappings);
        Iterator it = set.iterator();
        ServletMappingDD dd;
        while(uri!=null && it.hasNext()) {
            dd = (ServletMappingDD)it.next();
            if(uri.equals(dd.getUrlPattern())) {
                return dd;
            }
        }
        return null;
    }

}

