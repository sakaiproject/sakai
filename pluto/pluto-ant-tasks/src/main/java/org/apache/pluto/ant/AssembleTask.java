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
package org.apache.pluto.ant;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.pluto.util.UtilityException;
import org.apache.pluto.util.assemble.Assembler;
import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.assemble.AssemblerFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * TODO JavaDoc
 *
 * @version 1.0
 * @since Nov 23, 2005
 */
public class AssembleTask extends Task {
    
    /**
     * Path to the portlet descriptor 
     * (normally <code>WEB-INF/portlet.xml</code>)
     * <p/>
     * If <code>webapp</code> is specified, this File will
     * be resolved relative to <code>webapp</code>
     */
    private File portletxml;

    /**
     * Path to the unassembled servlet descriptor 
     * (normally <code>WEB-INF/web.xml</code>)
     * <p/>
     * If <code>webapp</code> is specified, this File will
     * be resolved relative to <code>webapp</code>
     */
    private File webxml;

    /**
     * Path the assembled servlet descriptor will
     * be written to.
     */
    private File destfile;

    /** 
     * The base directory of the exploded web application to assemble.
     * If set, <code>webXml</code> and <code>portletXml</code>
     * will be resolved relative to this directory.
     */
    private File webapp;

    /**
     * Path to the archive to assemble.  EAR and WAR 
     * packaging formats are supported.
     */
    private File archive;

    /**
     * Destination directory the assembled archives
     * are written out to.
     */
    private File destdir;

    private final Collection archiveFileSets = new LinkedList();

    public File getPortletxml() {
        if(webapp != null)
            return new File(webapp, "WEB-INF/portlet.xml");
        return portletxml;
    }

    public void setPortletxml(File portletxml) {
        this.portletxml = portletxml;
    }

    public File getWebxml() {
        if(webapp != null)
            return new File(webapp, "WEB-INF/web.xml");
        return webxml;
    }

    public void setWebxml(File webxml) {
        this.webxml = webxml;
    }

    public File getDestfile() {
        if(destfile != null)
            return destfile;
        return getWebxml();
    }

    public void setDestfile(File destfile) {
        this.destfile = destfile;
    }

    public File getWebapp() {
        return webapp;
    }

    public void setWebapp(File webapp) {
        this.webapp = webapp;
    }

    /**
     * Note this methods remains to support
     * backwards compatiblity.
     * 
     * @deprecated see <code>getArchive()</code>
     */
    public File getWar() {
        return this.archive;
    }

    /**
     * Note this methods remains to support
     * backwards compatiblity.
     * 
     * @param war
     * @deprecated see <code>setArchive(File)</code>
     */
    public void setWar(File war) {
        this.archive = war;
    }
    
    public File getArchive() {
        return this.archive;
    }
    
    public void setArchive(File archive) {
        this.archive = archive;
    }

    public File getDestdir() {
        if (destdir == null) {
            return (archive != null ? archive.getParentFile() : null);
        }
        return this.destdir;
    }

    public void setDestdir(File destDir) {
        this.destdir = destDir;
    }

    /**
     * Note this method remains to support 
     * backwards compatiblity.
     * 
     * @param fileSet
     * @deprecated use addArchives instead
     */
    public void addWars(FileSet fileSet) {
        this.archiveFileSets.add(fileSet);
    }
    
    public void addArchives(FileSet fileSet) {
        this.archiveFileSets.add(fileSet);
    }

    public void execute() throws BuildException {

        validateArgs();

        try {
            if (this.archiveFileSets.size() > 0) {
                for (final Iterator fileSetItr = this.archiveFileSets.iterator(); fileSetItr.hasNext();) {
                    final FileSet fileSet = (FileSet)fileSetItr.next();
                    final DirectoryScanner directoryScanner = fileSet.getDirectoryScanner(this.getProject());

                    final File basedir = directoryScanner.getBasedir();
                    final String[] includedFiles = directoryScanner.getIncludedFiles();

                    for (int index = 0; index < includedFiles.length; index++) {
                        AssemblerConfig config = new AssemblerConfig();

                        final File archiveSource = new File(basedir, includedFiles[index]);
                        config.setSource(archiveSource);
                        config.setDestination(getDestdir());

                        this.log("Assembling '" + archiveSource + "' to '" + getDestdir() + "'");
                        Assembler assembler = AssemblerFactory.getFactory().createAssembler(config);
                        assembler.assemble(config);
                    }
                }
            }
            else {
                AssemblerConfig config = new AssemblerConfig();

                final File archiveSource = getArchive();
                if (archiveSource != null) {
                    config.setSource(archiveSource);
                    config.setDestination(getDestdir());
                    this.log("Assembling '" + archiveSource + "' to '" + getDestdir() + "'");
                }
                else {
                    config.setPortletDescriptor(getPortletxml());
                    config.setWebappDescriptor(getWebxml());
                    config.setDestination(getDestfile());
                    this.log("Assembling '" + getWebxml() + "' to '" + getDestfile() + "'");
                }

                Assembler assembler = AssemblerFactory.getFactory().createAssembler(config);
                assembler.assemble(config);
            }
        }

        catch(UtilityException ue) {
            throw new BuildException(ue);
        }
    }

    private void validateArgs() throws BuildException {
        //Check if running with webapp arg
        if(webapp != null) {
            if(!webapp.exists()) {
               throw new BuildException("webapp "+webapp.getAbsolutePath()+ " does not exist");
            }

            if (archive != null) {
                throw new BuildException("archive (or war) should not be specified if webapp is specified");
            }
            if (this.archiveFileSets.size() > 0) {
                throw new BuildException("archive (or wars) should not be specified if webapp is specified");
            }
            // TODO check this
            if (destdir != null) {
                throw new BuildException("destfile should not be specified if webapp is specified");
            }

            return;
        }

        //Check if running with war arg
        if (archive != null) {
            if(!archive.exists()) {
                throw new BuildException("Archive file "+archive.getAbsolutePath()+ " does not exist");
            }

            if (this.archiveFileSets.size() > 0) {
                throw new BuildException("archives (or wars) should not be specified if archive (or war) is specified");
            }
            if (webapp != null) {
                throw new BuildException("webapp should not be specified if archive (or war) is specified");
            }
            if (destfile != null) {
                throw new BuildException("destfile should not be specified if archive (or war) is specified");
            }
            if (portletxml != null) {
                throw new BuildException("portletxml should not be specified if archive (or war) is specified");
            }
            if (webxml != null) {
                throw new BuildException("webxml should not be specified if archive (or war) is specified");
            }

            return;
        }

        //Check if running with archives or wars arg
        if (this.archiveFileSets.size() > 0) {
            if (archive != null) {
                throw new BuildException("archives (or wars) should not be specified if archive (or war) is specified");
            }
            if (webapp != null) {
                throw new BuildException("webapp should not be specified if archives (or wars) is specified");
            }
            if (destfile != null) {
                throw new BuildException("destfile should not be specified if archives (or wars) is specified");
            }
            if (portletxml != null) {
                throw new BuildException("portletxml should not be specified if archive (or wars) is specified");
            }
            if (webxml != null) {
                throw new BuildException("webxml should not be specified if archives (or wars) is specified");
            }

            return;
        }

        //Check if running with portletxml && webxml args
        if(portletxml == null || !portletxml.exists()) {
            throw new BuildException("portletxml "+portletxml+" does not exist");
        }
        if(webxml == null || !webxml.exists()) {
            throw new BuildException("webxml "+webxml + " does not exist");
        }
        if (archive != null) {
            throw new BuildException("archive (or war) should not be specified if portletxml and webxml are specified");
        }
        if (this.archiveFileSets.size() > 0) {
            throw new BuildException("archives (or wars) should not be specified if portletxml and webxml are specified");
        }
        if (destdir != null) {
            // TODO check this
            throw new BuildException("destfile should not be specified if portletxml and webxml are aspecified");
        }
    }
}
