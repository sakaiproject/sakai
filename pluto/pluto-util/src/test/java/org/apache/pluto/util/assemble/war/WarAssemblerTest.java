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
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.pluto.util.assemble.Assembler;
import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.assemble.ArchiveBasedAssemblyTest;

/**
 * @version $Revision: 607455 $
 */
public class WarAssemblerTest extends ArchiveBasedAssemblyTest {

    private static final String portletResourceNoManifest = "/org/apache/pluto/util/assemble/war/WarDeployerTestPortletNoManifest.war";
    private static final String portletResource = "/org/apache/pluto/util/assemble/war/WarDeployerTestPortlet.war";
    private File portletFile = null;    
    
    
    protected void setUp() throws Exception {
        final URL portletUrl = this.getClass().getResource(portletResource);
        this.portletFile = new File(portletUrl.getFile());
    }

    protected void tearDown() throws Exception {
        this.portletFile = null;
    }

    public void testAssembleToNewDirectory() throws Exception {
        AssemblerConfig config = new AssemblerConfig();

        config.setSource(this.portletFile);

        final File tempDir = getTempDir();
        config.setDestination(tempDir);

        WarAssembler assembler = new WarAssembler();
        assembler.assemble(config);

        //How to validate it worked?
    }

    public void testAssembleOverSelf() throws Exception {
        AssemblerConfig config = new AssemblerConfig();

        final File portletCopy = File.createTempFile(this.portletFile.getName() + ".", ".war");
        portletCopy.deleteOnExit();
        FileUtils.copyFile(this.portletFile, portletCopy);

        config.setSource(portletCopy);
        config.setDestination(portletCopy.getParentFile());

        WarAssembler assembler = new WarAssembler();
        assembler.assemble(config);

        //How to validate it worked?
    }
    
    public void testAssembleWithNoManifest() throws Exception {
        
        final File warNoManifest = new File( this.getClass().getResource(portletResourceNoManifest).getFile() );
        assertNotNull( "Unable to locate the test war file with no manifest.", warNoManifest );
        
        final File tempDir = getTempDir();
        final AssemblerConfig config = new AssemblerConfig();
        config.setSource( warNoManifest );
        config.setDestination(tempDir);
        
        WarAssembler assembler = new WarAssembler();
        assembler.assemble(config);        
    }

    private File getTempDir() throws IOException {
        final File tempFile = File.createTempFile("DoesNotMatter", ".tmp");
        tempFile.delete();
        final File tempDir = tempFile.getParentFile();
        return tempDir;
    }

    protected Assembler getAssemblerUnderTest() {
        return new WarAssembler();
    }

    protected File getFileToAssemble() {
        return portletFile;
    }
}

