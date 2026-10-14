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
package org.apache.myfaces.component;

/**
 * Behavioral interface.
 * By default, displayValueOnly is false, and the components have the default behaviour.
 * When displayValueOnly is true, the renderer should not render any input widget.
 * Only the text corresponding to the component's value should be rendered instead.
 * 
 * org.apache.myfaces.shared_tomahawk.component.DisplayValueOnlyCapable 
 * are available on shared, but we need something on tomahawk
 * to define and generate properties.
 * 
 * @since 1.1.7
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 *
 */
public interface DisplayValueOnlyAware
{
    /**
     *  If true, renders only the value of the component, 
     *  but no input widget. Default is false.
     * 
     * @JSFProperty
     */
    public Boolean getDisplayValueOnly();

    public void setDisplayValueOnly(Boolean b);
    
    /**
     * Style used when displayValueOnly is true.
     * 
     * @JSFProperty
     */
    public String getDisplayValueOnlyStyle();
    
    public void setDisplayValueOnlyStyle(String displayValueOnlyStyle);
    
    /**
     * Style class used when displayValueOnly is true.
     * 
     * @JSFProperty
     */
    public String getDisplayValueOnlyStyleClass();
    
    public void setDisplayValueOnlyStyleClass(String displayValueOnlyStyleClass);    
    
}
