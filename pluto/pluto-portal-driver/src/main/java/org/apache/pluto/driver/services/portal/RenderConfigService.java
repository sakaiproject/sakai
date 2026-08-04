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
package org.apache.pluto.driver.services.portal;

import java.util.List;

/**
 * Service interface defining methods necessary for
 * a provider wishing to manage page administration.
 *
 * @since Aug 10, 2005
 */
public interface RenderConfigService extends DriverConfigurationService {

    /**
     * Retrieve an ordered list of all PageConfig instances.
     * @return list of all PageConfig instances.
     */
    List getPages();

    /**
     * Retrieve the PageConfig for the default
     * page.
     * @return PageConfig instance of the default page.
     */
    PageConfig getDefaultPage();

    /**
     * Retrieve the PageConfig for the given
     * page id.
     *
     * @param id
     * @return PageConfig instance for the specified id.
     */
    PageConfig getPage(String id);

    //added for page admin portlet
    public void addPage(PageConfig pageConfig);
    public void removePage(PageConfig pageConfig);
        
}
