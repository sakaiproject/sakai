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
package org.apache.pluto.util.assemble.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.pluto.util.UtilityException;
import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.assemble.WebXmlRewritingAssembler;

/**
 *
 * @version 1.0
 * @since Nov 8, 2004
 */
public class FileAssembler extends WebXmlRewritingAssembler {
    // Constructor -------------------------------------------------------------

    /**
     * Default no-arg constructor.
     */
    public FileAssembler() {
    	// Do nothing.
    }


    // Assembler Impl ----------------------------------------------------------

    public void assemble(AssemblerConfig config) throws UtilityException {
        try {
            final File webappDescriptor = config.getWebappDescriptor();
            InputStream webXmlIn = new FileInputStream(webappDescriptor);

            final File portletDescriptor = config.getPortletDescriptor();
            InputStream portletXmlIn = new FileInputStream(portletDescriptor);

            final File destinationDescriptor = config.getDestination();
            if (webappDescriptor.equals(destinationDescriptor)) {
                final File tempXml = File.createTempFile(webappDescriptor.getName() + ".", ".tmp");
                final FileOutputStream webXmlOut = new FileOutputStream(tempXml);

                this.updateWebappDescriptor(webXmlIn, portletXmlIn, webXmlOut, config.getDispatchServletClass());

                //Move the temp file to the destination location
                destinationDescriptor.delete();
                // renameTo() is impl-specific
                boolean success = tempXml.renameTo(destinationDescriptor);
                if (! success) {
                    FileUtils.copyFile( tempXml, destinationDescriptor );
                }
            }
            else {
                destinationDescriptor.getParentFile().mkdirs();
                final FileOutputStream webXmlOut = new FileOutputStream(destinationDescriptor);
                this.updateWebappDescriptor(webXmlIn, portletXmlIn, webXmlOut, config.getDispatchServletClass());
            }
        } catch (IOException ex) {
            throw new UtilityException(ex.getMessage(), ex, null);
        }
    }
}

