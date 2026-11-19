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
 * 
 * @since 1.1.7
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 704778 $ $Date: 2008-10-14 23:27:46 -0500 (Tue, 14 Oct 2008) $
 *
 */
public interface MessageProperties
{

    /**
     *  If present, instead of rendering the message summary, 
     *  a MessageFormat with this attribute as pattern is created. 
     *  
     *  The format method of this MessageFormat is called with the 
     *  message summary as the first argument and the label of the 
     *  associated component (if any) as the second argument. 
     *  
     *  Example: "{0}:"
     * 
     * @JSFProperty
     */
    public String getSummaryFormat();
    
    public void setSummaryFormat(String summaryFormat);
    
    /**
     * If present, instead of rendering the message detail, 
     * a MessageFormat with this attribute as pattern is created. 
     * 
     * The format method of this MessageFormat is called with the 
     * message detail as the first argument and the label of the 
     * associated component (if any) as the second argument. 
     * 
     * Example: "The input in field {1} is wrong: {0}"
     * 
     * @JSFProperty
     */
    public String getDetailFormat();
    
    public void setDetailFormat(String detailFormat);
    
    /**
     *  If present, all occurrences of the id of the component for 
     *  which the message is rendered will be replaced by the label. 
     *  
     *  Default: true.
     * 
     * @JSFProperty
     *   defaultValue="true"
     */
    public boolean isReplaceIdWithLabel();
    
    public void setReplaceIdWithLabel(boolean replaceIdWithLabel);
    
    /**
     * If set to true, an empty span element is rendered. 
     * Useful if there is an inputAjax field and the corresponding 
     * error message is displayed there.
     * 
     * @JSFProperty
     *   defaultValue="false"
     */    
    public boolean isForceSpan();
    
    public void setForceSpan(boolean forceSpan);
}
