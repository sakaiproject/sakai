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
package org.apache.pluto.util.install.file.jetty;

import org.apache.commons.io.FileUtils;
import org.apache.pluto.util.install.InstallationConfig;
import org.apache.pluto.util.install.file.FileSystemInstaller;
import org.apache.pluto.util.UtilityException;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class Jetty5FileSystemInstaller extends FileSystemInstaller {

    protected File getEndorsedDir(InstallationConfig config) {
        File installationDirectory = config.getInstallationDirectory();
        return new File(installationDirectory, "ext");
    }

    protected File getSharedDir(InstallationConfig config) {
        File installationDirectory = config.getInstallationDirectory();
        // Jetty 5.1 provides commons-logging.  Should be a nicer way
        // for installers to indicate what dependencies are provided by the
        // servlet container.
        if ( new File(config.getInstallationDirectory(), "ext/commons-logging.jar").exists()) {
            for (Iterator iter = config.getSharedDependencies().iterator(); iter.hasNext();) {
                File dep = (File) iter.next();
                if (dep.getPath().indexOf("commons-logging-api") != -1) {
                    iter.remove();
                }
            }
        }
        return new File(installationDirectory, "ext");
    }

    protected File getWebAppDir(InstallationConfig config) {
        File installationDirectory = config.getInstallationDirectory();
        return new File(
                installationDirectory, "webapps"
        );
    }

    protected File getConfigurationDir(InstallationConfig config) {
        File installationDirectory = config.getInstallationDirectory();
        String engine = "Catalina";
        String host = config.getServerConfig().getHost();
        return new File(installationDirectory, "conf/" + engine + "/" + host);
    }

    public void uninstall(InstallationConfig config) {
    }

    public void deploy() {

    }

    public boolean isValidInstallationDirectory(File installDir) {
        File serverConfig = new File(installDir, "etc/jetty.xml");
        return serverConfig.exists();
    }

    /**
     * NOTE: Order is important.  If the server is running, we want to
     * make sure that the correct order is preserved
     * <p/>
     * 1) Install endorsed dependencies
     * 2) Install shared dependencies
     * 4) Prep Time
     * -- Create a domain directory for the portal
     * -- Init the configs holder
     * 5) Install the Portlet Applications
     * 6) Install the Portal Application
     * 7) Finally, install the configs
     *
     * @param config
     * @throws org.apache.pluto.util.UtilityException
     *
     */
    public void install(InstallationConfig config) throws UtilityException {
        File endorsedDir = getEndorsedDir(config);
        File sharedDir = getSharedDir(config);
        File domainDir = getWebAppDir(config);
        domainDir.mkdirs();
        File contextConfigurationDirectory = getConfigurationDir(config);

        try {

            // Jetty Doesn't need 'em
            //copyFilesToDirectory(config.getEndorsedDependencies(), endorsedDir);

            copyFilesToDirectory(config.getSharedDependencies(), sharedDir);

            Iterator it = config.getPortletApplications().values().iterator();
            while (it.hasNext()) {
                File portletApp = (File) it.next();
                FileUtils.copyFileToDirectory(portletApp, domainDir);
            }

            FileUtils.copyFileToDirectory(config.getPortalApplication(), domainDir);


//            it = config.getPortletApplications().entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry entry = (Map.Entry) it.next();
//                String context = entry.getKey().toString();
//                File portletApp = (File) entry.getValue();
//
//                File deployed = new File(domainDir, portletApp.getName());
//                String contents = getPortletApplicationConfig(context, deployed);
//                //FileWriter out = new FileWriter(
//                //        new File(contextConfigurationDirectory, context + ".xml"));
//                out.write(contents);
//                out.flush();
//                out.close();
//            }

//            File xmlFile = new File(contextConfigurationDirectory, config.getPortalContextPath() + ".xml");
//            FileWriter out = new FileWriter(xmlFile);
//            out.write(getPortalApplicationConfig(config));
//            out.flush();
//            out.close();
        }
        catch (IOException io) {
            throw new UtilityException(io);
        }
    }

    private String getPortalApplicationConfig(InstallationConfig config) {
        File domainDir = this.getWebAppDir(config);
        String war = domainDir.getAbsolutePath() + File.separatorChar +
                config.getPortalApplication().getName();
        String contextPath = config.getPortalContextPath();
        return getConfigContents(war, contextPath);
    }

    private String getPortletApplicationConfig(String contextPath, File file) {
        String war = file.getAbsolutePath();
        return getConfigContents(war, contextPath);
    }

    public void writeConfiguration(InstallationConfig config) {

    }

    private String getConfigContents(String war, String contextPath) {
        return "JettyConfigContents: war=" + war + "contextPath=" + contextPath;
    }
}
