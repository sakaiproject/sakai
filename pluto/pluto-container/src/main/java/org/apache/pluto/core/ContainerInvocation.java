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

import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletWindow;

/**
 * <font style="color: red">Whenever possible,
 *  use of this class should be avoided. It is intended for
 *  use by objects which can NOT be instantiated and managed
 *  by the container.
 * </font>
 *
 * Provides static access to the currently executing container.
 * This is critical for instance of the PortletContext and PortletConfig
 * to be able to access container specific services.
 *
 *
 * @since 1.1
 *
 */
public class ContainerInvocation {

    private static final InheritableThreadLocal CONTAINERS =
        new InheritableThreadLocal();

    /**
     * Retrieve the container which is currently
     * executing.
     *
     * @return the currently executing portlet container.
     */
    public static ContainerInvocation getInvocation() {
        return (ContainerInvocation)CONTAINERS.get();
    }

    /**
     * Set the container currently under execution.
     *
     * @param container the container invoked
     * @param window used in the invocation
     */
    public static void setInvocation(PortletContainer container, PortletWindow window) {
        CONTAINERS.set(new ContainerInvocation(window, container));
    }

    public static void clearInvocation() {
        CONTAINERS.set(null);
    }


    private PortletWindow window;
    private PortletContainer container;

    public ContainerInvocation(PortletWindow window, PortletContainer container) {
        this.window = window;
        this.container = container;
    }

    public PortletWindow getPortletWindow() {
        return window;
    }

    public void setWindow(PortletWindow window) {
        this.window = window;
    }

    public PortletContainer getPortletContainer() {
        return container;
    }

    public void setContainer(PortletContainer container) {
        this.container = container;
    }
}
