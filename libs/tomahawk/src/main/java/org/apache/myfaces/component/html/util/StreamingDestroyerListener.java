/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.component.html.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.apache.myfaces.shared_tomahawk.config.MyfacesConfig;

public class StreamingDestroyerListener implements ServletContextListener 
{

    public void contextInitialized(ServletContextEvent event)
    {
        //Only initialize a StreamingThreadManager if StreamingAddResource is used
        String addResourceClass = MyfacesConfig.getAddResourceClassFromServletContext(event.getServletContext());
        
        if (addResourceClass != null && addResourceClass.equals(StreamingAddResource.class.getName()))
        {
            StreamingThreadManager manager = new StreamingThreadManager();
            event.getServletContext().setAttribute(StreamingThreadManager.KEY,
                    manager);
            manager.init();
        }
    }

    public void contextDestroyed(ServletContextEvent event)
    {
        StreamingThreadManager manager = (StreamingThreadManager) event.getServletContext().getAttribute(StreamingThreadManager.KEY);
        if (manager != null)
        {
            manager.destroy();
        }
    }
}
