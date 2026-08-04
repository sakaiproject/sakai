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
package org.apache.pluto.maven;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.pluto.util.install.ServerConfig;
import org.apache.pluto.util.install.InstallationConfig;
import org.apache.pluto.util.install.PortalInstallerFactory;
import org.apache.pluto.util.install.PortalInstaller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * Abstract Mojo for installation tasks.
 *
 * @since 07/29/2005
 */
public abstract class AbstractManagementMojo extends AbstractPlutoMojo {

    /**
     * @parameter expression="${domain}"
     */
    protected String domain = "PlutoDomain";

    /**
     * @parameter expression="${server}"
     */
    protected String server = "PlutoServer";

    /**
     * @parameter expression="${host}"
     */
    protected String host = "localhost";

    /**
     * @parameter expression="${port}"
     */
    protected int port;

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    protected ArtifactResolver artifactResolver;

    /**
     * @parameter expression="${localRepository}
     */
    protected ArtifactRepository artifactRepository;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    protected List remoteRepositories;

    /**
     * @parameter expression="${ctx}" default-value="pluto"
     *
     */
    protected String portalContext;

    /**
     * @parameter expression="${pom.currentVersion} default="1.0-SNAPSHOT"
     */
    protected String version;

    /**
     *  at parameter expression="${portletApps}"
     */
    protected Map portletApps = new HashMap();

    protected AbstractManagementMojo() {
    	// Do nothing.
    }

    protected List getSharedDependencies() throws ArtifactNotFoundException, ArtifactResolutionException {
       return getDependencies(InstallationDependency.getSharedDependencies());
    }

    protected List getEndorsedDependencies() throws ArtifactNotFoundException, ArtifactResolutionException {
       return getDependencies(InstallationDependency.getEndorsedDependencies());
    }

    private List getDependencies(Collection artifacts) throws ArtifactNotFoundException, ArtifactResolutionException {
        List list = new ArrayList();
        Iterator it = artifacts.iterator();
        while(it.hasNext()) {
            InstallationDependency dep = (InstallationDependency)it.next();
            Artifact artifact = artifactFactory.createArtifactWithClassifier(
                    dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), dep.getType(), null
            );

            artifactResolver.resolve(artifact, remoteRepositories, artifactRepository);
            if(artifact.getFile() == null) {
                getLog().warn("Unable to find file for artifact: "+artifact.getArtifactId());
            }

            list.add(artifact.getFile());
        }
        return list;
    }

    protected ServerConfig getServerConfig() {
        ServerConfig config = new ServerConfig();
        config.setDomain(domain);
        config.setHost(host);
        config.setPort(port);
        config.setServer(server);
        return config;
    }

    protected PortalInstaller getHandler() {
        return PortalInstallerFactory.getAppServerHandler(installationDirectory);
    }

    protected InstallationConfig createInstallationConfig() throws ArtifactNotFoundException, ArtifactResolutionException {
        InstallationConfig config = new InstallationConfig();
        config.setInstallationDirectory(installationDirectory);
        config.setPortalContextPath(portalContext);
        config.setPortalApplication(getPortalApplication());
        config.setPortletApplications(getPortletApplications());
        config.setEndorsedDependencies(getEndorsedDependencies());
        config.setSharedDependencies(getSharedDependencies());
        config.setServerConfig(getServerConfig());
        return config;
    }

    private File getPortalApplication() throws ArtifactNotFoundException, ArtifactResolutionException  {
        InstallationDependency dep = InstallationDependency.PORTAL;
        Artifact artifact = artifactFactory.createBuildArtifact(
           dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), dep.getType()
        );
        artifactResolver.resolve(artifact, remoteRepositories, artifactRepository);
        return artifact.getFile();
    }

    private Map getPortletApplications() throws ArtifactNotFoundException, ArtifactResolutionException {
        Map files = new HashMap();
        InstallationDependency dep = InstallationDependency.TESTSUITE;
        Artifact artifact = artifactFactory.createBuildArtifact(
                dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), dep.getType()
        );
        artifactResolver.resolve(artifact, remoteRepositories, artifactRepository);

        files.put("testsuite", artifact.getFile());
        /*
        Iterator apps = portletApps.iterator();
        while(apps.hasNext()) {
            //files.add(artifactFactory.createBuildArtifact(
            //    InstallMojo.GROUP_ID, apps.next().toString(), version, "war"
            //).getFile());
        }
        */
        return files;
    }

    protected void doValidate() throws Exception {
        if(installationDirectory == null || !installationDirectory.exists()) {
            throw new MojoExecutionException("A valid installation directory must be provided in order to install pluto.");

        }
    }
}
