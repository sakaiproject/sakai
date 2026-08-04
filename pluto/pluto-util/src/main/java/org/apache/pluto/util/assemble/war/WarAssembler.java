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
package org.apache.pluto.util.assemble.war;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.pluto.util.UtilityException;
import org.apache.pluto.util.assemble.AbstractArchiveAssembler;
import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.assemble.io.JarStreamingAssembly;

/**
 *
 * @version 1.0
 * @since Nov 8, 2004
 */
public class WarAssembler extends AbstractArchiveAssembler {
    // Constructor -------------------------------------------------------------

    /**
     * Default no-arg constructor.
     */
    public WarAssembler() {
    	// Do nothing.
    }


    // Assembler Impl ----------------------------------------------------------

    public void assembleInternal(AssemblerConfig config) 
        throws UtilityException, IOException {
        
        this.assembleWar(config.getSource(), config.getDestination(), config.getDispatchServletClass());
        
    }

    /**
     * Reads the source JAR copying entries to the dest JAR. The web.xml and portlet.xml are cached
     * and after the entire archive is copied (minus the web.xml) a re-written web.xml is generated
     * and written to the destination JAR.
     */
    protected void assembleWar(File source, File dest, String dispatchServletClass) throws IOException {
        final JarInputStream jarIn = new JarInputStream(new FileInputStream(source));
        //Create the output JAR stream, copying the Manifest
        final Manifest manifest = jarIn.getManifest();
        //TODO add pluto notes to the Manifest?
        
        final JarOutputStream jarOut;
        if (manifest != null) {
            jarOut = new JarOutputStream(new FileOutputStream(dest), manifest);
        }
        else {
            jarOut = new JarOutputStream(new FileOutputStream(dest));
        }
        
        try {            
            JarStreamingAssembly.assembleStream(jarIn, jarOut, dispatchServletClass);
        } finally {
            jarIn.close();
            jarOut.close();
        }
    }

}

