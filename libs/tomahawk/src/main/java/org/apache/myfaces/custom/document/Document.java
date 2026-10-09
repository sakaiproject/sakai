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
 *
 * Document to enclose the whole document. If not otherwise possible you can use
 * state="start|end" to demarkate the document boundaries
 *
 * @JSFComponent
 *   name = "t:document"
 *   tagClass = "org.apache.myfaces.custom.document.DocumentTag"
 *
 * @author Mario Ivankovits (latest modification by $Author: hoersch#his.de $)
 * @version $Revision: 1.1 $ $Date: 2012-06-23 17:38:52 $
 */
public class Document extends AbstractDocument
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.Document";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.Document";

    public Document()
    {
        super(DEFAULT_RENDERER_TYPE);
    }

    public boolean isIncludeBrowserSelectors()
    {
        return ((Boolean) getStateHelper().eval(PropertyKeys.includeBrowserSelectors, Boolean.FALSE)).booleanValue();
    }

    public void setIncludeBrowserSelectors(boolean renderConditionalCommentsAndStyleClasses)
    {
        getStateHelper().put(PropertyKeys.includeBrowserSelectors, Boolean.valueOf(renderConditionalCommentsAndStyleClasses));
    }

    protected enum PropertyKeys
    {
        includeBrowserSelectors;
    }
}