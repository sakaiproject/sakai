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


/**
 * This test ensures that the version attribute of a servlet 2.3 descriptor
 * is being read from and written to properly by Castor.
 *
 * @since Mar 3, 2007
 * @version $Id: Servlet23WebAppDescriptorTest.java 607452 2007-12-29 17:52:38Z esm $
 * @todo JavaDoc
 */
public class Servlet23WebAppDescriptorTest extends AbstractVersionedWebAppDescriptorTest
{

    private static final String DESCRIPTOR = "/servlet-2.3-webapp-descriptor.xml";
    private static final String EXPECTED_DESCRIPTOR = "/servlet-2.3-expected-webapp-descriptor.xml";
    
    
    /* (non-Javadoc)
     * @see org.sakaiproject.pluto.descriptors.services.castor.AbstractVersionedWebAppDescriptorTest#getDescriptorPath()
     */
    protected String getDescriptorPath() {
        return DESCRIPTOR;
    }
    
    /* (non-Javadoc)
     * @see org.sakaiproject.pluto.descriptors.services.castor.AbstractVersionedWebAppDescriptorTest#getExpectedDescriptorPath()
     */
    protected String getExpectedDescriptorPath() {
        return EXPECTED_DESCRIPTOR;
    }
    
    /* (non-Javadoc)
     * @see org.sakaiproject.pluto.descriptors.services.castor.AbstractVersionedWebAppDescriptorTest#getDescriptorVersion()
     */
    protected String getDescriptorVersion() {
        return "2.3";
    }
}
