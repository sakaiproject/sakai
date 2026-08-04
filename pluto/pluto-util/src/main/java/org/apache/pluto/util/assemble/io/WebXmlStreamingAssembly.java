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
package org.apache.pluto.util.assemble.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.pluto.descriptors.common.InitParamDD;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.services.PortletAppDescriptorService;
import org.apache.pluto.descriptors.services.WebAppDescriptorService;
import org.apache.pluto.descriptors.services.castor.PortletAppDescriptorServiceImpl;
import org.apache.pluto.descriptors.services.castor.WebAppDescriptorServiceImpl;
import org.apache.pluto.descriptors.servlet.LoadOnStartupDD;
import org.apache.pluto.descriptors.servlet.ServletDD;
import org.apache.pluto.descriptors.servlet.ServletMappingDD;
import org.apache.pluto.descriptors.servlet.WebAppDD;
import org.apache.pluto.util.assemble.Assembler;

/** 
 * Utility class responsible for accepting web.xml and portlet.xml as InputStreams, and assembling
 * the web.xml to an OutputStream.
 */
public class WebXmlStreamingAssembly {
    
    /**
     * Assembles the web.xml represented by the <code>InputStream</code>.
     * 
     * @param webXmlIn the unassembled web.xml file
     * @param portletXmlIn the corresponding portlet.xml file
     * @param assembledWebXmlOut the assembled web.xml file
     * @param dispatchServletClass the dispatch servlet
     * @throws IOException
     */
    public static void assembleStream(InputStream webXmlIn,
                                InputStream portletXmlIn, 
                                OutputStream assembledWebXmlOut, 
                                String dispatchServletClass) 
    throws IOException {
        if (dispatchServletClass == null ||
                dispatchServletClass.length() == 0 ||
                dispatchServletClass.trim().length() == 0) {
            dispatchServletClass = Assembler.DISPATCH_SERVLET_CLASS;
        }

      WebAppDescriptorService descriptorSvc = new WebAppDescriptorServiceImpl();  
      PortletAppDescriptorService portletAppDescriptorSvc = new PortletAppDescriptorServiceImpl();
        
        WebAppDD webAppDDIn = descriptorSvc.read(webXmlIn);
        PortletAppDD portletAppDD = portletAppDescriptorSvc.read(portletXmlIn);
        portletXmlIn.close();

        for (Iterator it = portletAppDD.getPortlets().iterator();
                it.hasNext(); ) {

            // Read portlet definition.
            PortletDD portlet = (PortletDD) it.next();
            String name = portlet.getPortletName();

            ServletDD servlet = new ServletDD();
            servlet.setServletName(name);

            servlet.setServletClass(dispatchServletClass);

            InitParamDD initParam = new InitParamDD();
            initParam.setParamName("portlet-name");
            initParam.setParamValue(name);
            servlet.getInitParams().add(initParam);

            LoadOnStartupDD onStartup = new LoadOnStartupDD();
            onStartup.setPriority(1);
            servlet.setLoadOnStartup(onStartup);

            ServletMappingDD servletMapping = new ServletMappingDD();
            servletMapping.setServletName(name);
            servletMapping.setUrlPattern("/PlutoInvoker/" + name);

            webAppDDIn.getServlets().add(servlet);
            webAppDDIn.getServletMappings().add(servletMapping);

        }

        descriptorSvc.write(webAppDDIn, assembledWebXmlOut);
        
    }
}
