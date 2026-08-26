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
package org.apache.pluto.core;

import junit.framework.TestCase;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.PortletWindowID;

import javax.portlet.WindowState;
import javax.portlet.PortletMode;


public class ContainerInvocationTest extends TestCase
{
    private static PortletContainerImpl container = 
        new PortletContainerImpl("test", null, null);

    public void testSetInvocation()
    {
        ContainerInvocation.setInvocation(container, new PortletWindow() {

            public PortletWindowID getId() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getContextPath() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getPortletName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public WindowState getWindowState() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public PortletMode getPortletMode() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });

    }

    public void testClearInvocation()
    {
        ContainerInvocation.clearInvocation();
    }

}
