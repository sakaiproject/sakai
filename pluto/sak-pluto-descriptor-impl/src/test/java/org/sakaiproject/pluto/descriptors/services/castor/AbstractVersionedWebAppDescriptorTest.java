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
package org.sakaiproject.pluto.descriptors.services.castor;

import java.io.InputStream;

import junit.framework.TestCase;

import org.sakaiproject.pluto.descriptors.services.WebAppDescriptorService;
import org.sakaiproject.pluto.descriptors.servlet.WebAppDD;

/**
 * Base test for reading specific versions of the webapp descriptor xml.
 *
 * @since Mar 3, 2007
 * @version $Id: AbstractVersionedWebAppDescriptorTest.java 611006 2008-01-11 01:21:16Z esm $
 */
public abstract class AbstractVersionedWebAppDescriptorTest extends TestCase
{
    private WebAppDescriptorService underTest = null;

    protected void setUp() throws Exception
    {
        underTest = new WebAppDescriptorServiceImpl();
    }

    protected final InputStream getDescriptorStream() {
        final String descriptorPath = this.getDescriptorPath();
        return this.getRequiredResource(descriptorPath);
    }
    
    protected final InputStream getRequiredResource(String path) {
        final InputStream resource = this.getClass().getResourceAsStream(path);
        assertNotNull(resource);
        return resource;
    }

    protected void tearDown() throws Exception
    {
        underTest = null;
    }
    
    protected abstract String getDescriptorPath();
    
    protected abstract String getDescriptorVersion();

    public final void testRead() throws Exception
    {
        final InputStream descriptorStream = this.getDescriptorStream();
        WebAppDD webappdd = underTest.read(descriptorStream);
        assertNotNull(webappdd);
        assertEquals(this.getDescriptorVersion(), webappdd.getServletVersion());
    }
}
