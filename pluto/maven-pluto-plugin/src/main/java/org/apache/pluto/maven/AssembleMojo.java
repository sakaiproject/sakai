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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.pluto.util.UtilityException;
import org.apache.pluto.util.assemble.Assembler;
import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.assemble.AssemblerFactory;

/**
 * The AssembleMojo is responsible for assembling a web application for deployment
 * into the Pluto portlet container.  Assembly, in this context, is the process of
 * updating a web application's WEB-INF/web.xml with Pluto specific parameters for 
 * deployment in Pluto.   
 * <p>
 * This Mojo is able to operate on individual descriptors by specifying 
 * <code>portletXml</code>, <code>webXml</code>, and <code>webXmlDestination</code>.
 * If your project uses standard Maven 2 directory layouts, the defaults will
 * provide proper values.
 * <p/>
 * Example Maven 2 <code>pom.xml</code> usage:
 * <pre>
 * &lt;project&gt;
 *   ...
 *   &lt;build&gt;
 *      &lt;plugins&gt;
 *          &lt;plugin&gt;
 *              &lt;groupId&gt;org.apache.pluto&lt;/groupId&gt;
 *              &lt;artifactId&gt;maven-pluto-plugin&lt;/artifactId&gt;
 *          &lt;/plugin&gt;
 *      &lt;/plugins&gt;
 *   &lt;/build&gt;
 *   ...
 * &lt;/project&gt;
 * </pre>
 * <p>
 * This Mojo can also operate on entire WAR or EAR archive files by specifying
 * a list of archive path names in <code>archives</code>.
 * <p/>
 * Example Maven 2 <code>pom.xml</code> usage:
 * <pre>
 * &lt;project&gt;
 *   ...
 *   &lt;build&gt;
 *      &lt;plugins&gt;
 *          &lt;plugin&gt;
 *              &lt;groupId&gt;org.apache.pluto&lt;/groupId&gt;
 *              &lt;artifactId&gt;maven-pluto-plugin&lt;/artifactId&gt;
 *              &lt;executions&gt;
 *                  &lt;execution&gt;
 *                      &lt;phase&gt;package&lt;/phase&gt;
 *                      &lt;goals&gt;
 *                          &lt;goal&gt;assemble&lt;/goal&gt;
 *                      &lt;/goals&gt;
 *                      &lt;configuration&gt;
 *                          &lt;assemblyOutputDirectory&gt;${project.build.directory}/assembled-wars&lt;/assemblyOutputDirectory&gt;
 *                          &lt;archives&gt;
 *                              &lt;assembleArchive&gt;
 *                                  ${project.build.directory}/wartoassemble.war
 *                              &lt;/assembleArchive&gt;
 *                              &lt;assembleArchive&gt;
 *                                  ${project.build.directory}/anotherwartoassemble.war
 *                              &lt;/assembleArchive&gt;
 *                          &lt;/archives&gt;
 *                      &lt;/configuration&gt;
 *                  &lt;/execution&gt;
 *              &lt;/executions&gt;
 *          &lt;/plugin&gt;
 *      &lt;/plugins&gt;
 *   &lt;/build&gt;
 *   ...
 * &lt;/project&gt;
 * </pre>
 * 
 * @since Jul 30, 2005
 * @see org.apache.pluto.util.assemble.Assembler 
 * 
 * @goal assemble
 * @description prepares a web application as a portlet application
 * @phase process-resources
 */
public class AssembleMojo extends AbstractPortletMojo {

	// Private Member Variables ------------------------------------------------

    /**
     * The portlet application descriptor (<code>WEB-INF/portlet.xml</code>).
     * @parameter expression="${basedir}/src/main/webapp/WEB-INF/portlet.xml"
     * @required
     */
    private File portletXml;

    /**
     * The original webapp descriptor (<code>WEB-INF/web.xml</code>).
     * @parameter expression="${basedir}/src/main/webapp/WEB-INF/web.xml"
     * @required
     */
    private File webXml;

    /**
     * The file to which the updated webapp descriptor is written.
     * @parameter expression="${project.build.directory}/pluto-resources/web.xml"
     */
    private File webXmlDestination;

    /**
     * The name of the dispatch servlet class to use
     * @parameter
     */
    private String dispatchServletClass;
    
    /**
     * A list of archive files to assemble.  Only EAR and WAR file
     * types are supported.  
     * <p/>
     * Each value in the list is the absolute pathname to the 
     * archive being assembled.
     * <p/>
     * This parameter is mutually exclusive with portletXml, webXml, 
     * and webXmlDestination parameters.  
     * 
     * @parameter alias="warFiles"
     */
    private List archives;

    /**
     * @deprecated see archives parameter
     * @parameter
     */
    private List warFiles;
    
    /**
     * Destination directory the assembled files are written out to.
     * @parameter alias="warFilesDestination" expression="${project.build.directory}/pluto-assembled-wars"
     */
    private File assemblyOutputDirectory;
    
    /**
     * Destination directory the assembled files are written out to.
     * @parameter
     * @deprecated see assemblyOutputDirectory parameter
     */
    private File warFilesDestination;

    // AbstractPlutoMojo Impl --------------------------------------------------

    protected void doExecute() throws MojoExecutionException {       
        
        // Log parameter values.
    	Log log = getLog();
        if (log.isInfoEnabled()) {
            if (archives == null || archives.isEmpty()) {
                log.info("Reading web.xml from :" + webXml.getAbsolutePath());
                log.info("Reading portlet.xml from: " + portletXml.getAbsolutePath());
                log.info("Writing web.xml to: " + webXmlDestination.getAbsolutePath());
            }               
        }
        
        try {
            // Assemble portlet app by updating web.xml.
            if (archives == null || archives.isEmpty()) {        
                AssemblerConfig config = createAssemblerConfig();
                Assembler assembler = AssemblerFactory.getFactory()
            		    .createAssembler(config);
                assembler.assemble(config);
            } else {
                for (Iterator i = archives.iterator(); i.hasNext();) {
                    File archive = new File(i.next().toString());
                    if (log.isInfoEnabled()) {
                        log.info("Assembling archive file " + archive.getAbsolutePath() + 
                                " to directory " + assemblyOutputDirectory.getAbsolutePath());
                    }                
                    AssemblerConfig config = createArchiveAssemblerConfig(archive, assemblyOutputDirectory);
                    Assembler assembler = AssemblerFactory.getFactory()
                        .createAssembler(config);
                    assembler.assemble(config);
                }
            }
        } catch (UtilityException e) {
            log.error("Assembly failed: " + e.getMessage(), e);
        }
    }

    protected void doValidate() throws MojoExecutionException {
        Log log = getLog();
        
        // Support for the old 'warFiles' mojo parameter.  Apparently
        // the alias for the 'archives' parameter doesn't work properly.
        if (! (warFiles == null || warFiles.isEmpty()) ) {
            log.warn( "'warFiles' parameter is deprecated.  Use 'archives' parameter instead." );
            if ( archives == null ) {
                archives = new ArrayList();
            }
            archives.addAll( warFiles );
        }
        
        // Warn if the old 'warFilesDestination' mojo parameter is used
        if ( warFilesDestination != null ) {
            log.warn( "'warFilesDestination' parameter is deprecated.  Use 'assemblyOutputDirectory' instead." );
            assemblyOutputDirectory = warFilesDestination;
        }
        
        // If a list of war files are supplied:
        //   1) webXml, portletXml, and webXmlDestination parameters are ignored
        //   2) verify the files in the List exist.
        //   3) verify the destination is a directory, or create it if it doesn't exist.
        
        // A list of files was supplied so we ignore other parameters. 
        if (archives != null && !archives.isEmpty()) {
            if (webXml != null) {
                log.debug("archives parameter and webXml parameter are mutually exclusive.  Ignoring webXml parameter.");
            }
            if (portletXml != null) {
                log.debug("archives parameter and portletXml parameter are mutually exclusive.  Ignoring portletXml parameter.");
            }
            if (webXmlDestination != null) {
                log.debug("archives parameter and webXmlDestination parameter are mutually exclusive.  Ignoring webXmlDestination parameter.");
            }
            
            // verify each file can be found
            for (Iterator i = archives.iterator(); i.hasNext();) {
                File f = new File(i.next().toString());
                if (!f.exists()) {
                    log.warn("File " + f.getAbsolutePath() + " does not exist.");
                    i.remove();
                    continue;
                }
                if (!f.canRead()) {
                    log.warn("File " + f.getAbsolutePath() + " exists but cannot be read.");
                    i.remove();
                    continue;
                }
            }
            
            // check to see if the warFiles list is now empty
            if (archives.isEmpty()) {
                throw new MojoExecutionException("No war files could be installed due errors.");
            }
             
            // check to see if the dest dir exists or create it.
            if (!assemblyOutputDirectory.exists()) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating destination directory for assembled war files: " + assemblyOutputDirectory.getAbsolutePath());
                }
                try {                    
                    if(!assemblyOutputDirectory.mkdirs()) {
                        throw new MojoExecutionException("Unable to create destination directory for assembled war files: " + 
                                assemblyOutputDirectory.getAbsolutePath());
                    }                        
                } catch (SecurityException e) {
                    throw new MojoExecutionException("Unable to create destination directory for assembled war files: " + e.getMessage(), e);
                }
            } else {
                if (!assemblyOutputDirectory.isDirectory()) {
                    throw new MojoExecutionException("Specified destination for assembled war files " +
                            assemblyOutputDirectory.getAbsolutePath() + " is not a directory!");
                }
                if (!assemblyOutputDirectory.canRead()||!assemblyOutputDirectory.canWrite()) {
                    throw new MojoExecutionException("Unable to read or write to destination directory for assembed war files.  " +
                            "Check permissions on the directory " + assemblyOutputDirectory.getAbsolutePath());
                }
            }
            
        // A list of archive files was not provided, so use the other parameters instead.
            
        } else {            
            if (webXml == null || !webXml.exists()) {
                throw new MojoExecutionException("Web application descriptor must be a valid web.xml");
            }
            if (portletXml == null || !portletXml.exists()) {
                throw new MojoExecutionException("Portlet descriptor must be a valid portlet.xml");
            }
        }
    }

    // Private Methods ---------------------------------------------------------

    private AssemblerConfig createAssemblerConfig() {
        AssemblerConfig config = new AssemblerConfig();
        config.setPortletDescriptor(portletXml);
        config.setWebappDescriptor(webXml);
        config.setDestination(webXmlDestination);
        config.setDispatchServletClass(dispatchServletClass);
        return config;
    }
    
    private AssemblerConfig createArchiveAssemblerConfig(File archiveToAssemble, File destinationDirectory) {
        AssemblerConfig config = new AssemblerConfig();
        config.setDispatchServletClass(dispatchServletClass);
        config.setSource(archiveToAssemble);
        config.setDestination(destinationDirectory);
        return config;
    }

}
