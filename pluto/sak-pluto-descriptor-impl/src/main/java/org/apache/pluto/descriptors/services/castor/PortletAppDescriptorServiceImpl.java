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
package org.apache.pluto.descriptors.services.castor;

import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.services.Constants;
import org.apache.pluto.descriptors.services.PortletAppDescriptorService;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Abstract Implementation of the Web Application Deployment
 * Descriptor service.  Provides default implementation of
 * the service; requiring only that subclasses provide the
 * input streams to/from the actual descriptor.
 *
 * @version $Id: PortletAppDescriptorServiceImpl.java 156743 2005-03-10 05:50:30Z ddewolf $
 * @since Mar 5, 2005
 */
public class PortletAppDescriptorServiceImpl
    extends AbstractCastorDescriptorService
    implements PortletAppDescriptorService {

    public static final String PORTLET_XML_MAPPING =
        "castor-portlet-xml-mapping.xml";

    /**
     * Read the Web Application Deployment Descriptor.
     *
     * @return WebAppDD instance representing the descriptor.
     * @throws java.io.IOException
     */
    public PortletAppDD read(InputStream in) throws IOException {
        PortletAppDD portlet =
            (PortletAppDD) readInternal(in);
        return portlet;
    }

    /**
     * Write the deployment descriptor.
     * @param portlet
     * @throws java.io.IOException
     */
    public void write(PortletAppDD portlet, OutputStream out) throws IOException {
        writeInternal(portlet, out);
    }

    /**
     * Retrieve the Web Application Deployment
     * descriptor's public Id.
     * @return
     */
    protected String getPublicId() {
        return Constants.PORLTET_XML_PUBLIC_ID;
    }

    /**
     * Retrieve the Web Application Deployment
     * descriptor's DTD uri.
     * @return
     */
    protected String getDTDUri() {
        return Constants.PORTLET_XML_DTD;
    }

    /**
     * Read and Retrieve the Web Application's Castor Mapping
     * resource.
     *
     * @return
     * @throws java.io.IOException
     * @throws org.exolab.castor.mapping.MappingException
     */
    protected Mapping getCastorMapping()
    throws IOException, MappingException {
        URL url = getClass().getResource(PORTLET_XML_MAPPING);
        if(url == null) {
            throw new NullPointerException(
                    "Configuration Error.  Resource: "+PORTLET_XML_MAPPING+" not found."
            );
        }
        Mapping mapping = new Mapping();
        mapping.loadMapping(url);
        return mapping;
    }

    protected boolean getIgnoreExtraElements() {
        return true;
    }
}

