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
package org.apache.myfaces.custom.document;


/**
 * Document to enclose the document head. If not otherwise possible you can use
 * state="start|end" to demarkate the document boundaries
 * 
 * @JSFComponent
 *   name = "t:documentHead"
 *   tagClass = "org.apache.myfaces.custom.document.DocumentHeadTag" 
 *   
 * @author Mario Ivankovits (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (Thu, 03 Jul 2008) $
 */
public class DocumentHead extends AbstractDocument
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.DocumentHead";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.DocumentHead";

    public DocumentHead()
    {
        super(DEFAULT_RENDERER_TYPE);
    }
}