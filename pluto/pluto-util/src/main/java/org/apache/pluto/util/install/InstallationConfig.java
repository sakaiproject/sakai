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
package org.apache.pluto.util.install;

import org.apache.pluto.util.install.ServerConfig;

import java.util.List;
import java.util.Map;
import java.io.File;

/**
 * The installation configuration used to install Pluto to servlet container.
 */
public class InstallationConfig {

    /**
     * A list of depdencies that are shared amoungst web applications.
     */
    private List sharedDependencies;

    /**
     * A list of dependencies are are shared amoungst web applications.
     */
    private List endorsedDependencies;

    /**
     * A list of war files which are to be considered portal dependencies.
     */
    private Map portletApplications;

    /**
     * The location of the portal application being installed.
     */
    private File portalApplication;

    /**
     * The installation directory.
     */
    private File installationDirectory;

    private String portalContextPath;

    private ServerConfig serverConfig;


    public List getSharedDependencies() {
        return sharedDependencies;
    }

    public void setSharedDependencies(List sharedDependencies) {
        this.sharedDependencies = sharedDependencies;
    }

    public List getEndorsedDependencies() {
        return endorsedDependencies;
    }

    public void setEndorsedDependencies(List endorsedDependencies) {
        this.endorsedDependencies = endorsedDependencies;
    }

    public Map getPortletApplications() {
        return portletApplications;
    }

    public void setPortletApplications(Map portletApplications) {
        this.portletApplications = portletApplications;
    }

    public File getPortalApplication() {
        return portalApplication;
    }

    public void setPortalApplication(File portalApplication) {
        this.portalApplication = portalApplication;
    }

    public File getInstallationDirectory() {
        return installationDirectory;
    }

    public void setInstallationDirectory(File installationDirectory) {
        this.installationDirectory = installationDirectory;
    }

    public String getPortalContextPath() {
        return portalContextPath;
    }

    public void setPortalContextPath(String portalContextPath) {
        this.portalContextPath = portalContextPath;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

}
