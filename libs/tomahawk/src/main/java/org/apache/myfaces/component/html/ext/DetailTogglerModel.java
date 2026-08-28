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
package org.apache.myfaces.component.html.ext;

/**
 * This interface define a contract to manipulate the toggle state of components
 * like t:dataTable with detailStamp enabled. An object implementing this interface
 * is set on request scope under the key defined by varDetailToggler.
 * 
 * @author Leonardo Uribe
 */
public interface DetailTogglerModel
{
    /**
     * Collapse all details of all rows in all pages.
     */
    public void collapseAllDetails();

    /**
     * Expand all details of the rows displayed on the current page.
     */
    public void collapseAllPageDetails();
    
    /**
     * Expand all details of all rows in all pages.
     */
    public void expandAllDetails();
    
    /**
     * Expand all details of the rows displayed on the current page.
     */
    public void expandAllPageDetails();
    
    /**
     * Return true if the current detail row is expanded.
     *
     * @return true if the current detail row is expanded.
     */
    public boolean isCurrentDetailExpanded();

    /**
     * Change the status of the current detail row from collapsed to expanded or
     * viceversa.
     */
    public void toggleDetail();
    
    /**
     * true|false - true if the detailStamp should be expanded by default. default: false
     * 
     */
    public boolean isDetailStampExpandedDefault();
    
}
