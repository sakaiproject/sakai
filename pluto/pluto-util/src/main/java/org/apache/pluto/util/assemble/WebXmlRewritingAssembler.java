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
package org.apache.pluto.util.assemble;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pluto.util.assemble.io.JarStreamingAssembly;
import org.apache.pluto.util.assemble.io.WebXmlStreamingAssembly;

/**
 * @version $Revision: 539020 $
 * @todo fix direct dependency on pluto-descriptor-impl
 */
public abstract class WebXmlRewritingAssembler implements Assembler {
    
    //protected final JarStreamingAssembly jarAssembler = new JarStreamingAssembly();
    //protected final WebXmlStreamingAssembly webXmlAssembler = new WebXmlStreamingAssembly();

    /**
     * Updates the webapp descriptor by injecting portlet wrapper servlet
     * definitions and mappings.
     *
     * @param webXmlIn  input stream to the webapp descriptor, it will be closed before the web xml is written out.
     * @param portletXmlIn  input stream to the portlet app descriptor, it will be closed before the web xml is written out.
     * @param webXmlOut output stream to the webapp descriptor, it will be flushed and closed.
     * @param dispatchServletClass The name of the servlet class to use for
     *                         handling portlet requests
     * @throws IOException
     */
    protected void updateWebappDescriptor(InputStream webXmlIn,
                                              InputStream portletXmlIn,
                                              OutputStream webXmlOut,
                                              String dispatchServletClass)
    throws IOException {

        WebXmlStreamingAssembly.assembleStream(webXmlIn, portletXmlIn, webXmlOut, dispatchServletClass);
        
    }
}
