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
package org.apache.myfaces.custom.inputTextHelp;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.component.html.ext.HtmlInputText;

/**
 * Extends standard inputText by helptext support. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:inputTextHelp"
 *   class = "org.apache.myfaces.custom.inputTextHelp.HtmlInputTextHelp"
 *   tagClass = "org.apache.myfaces.custom.inputTextHelp.HtmlInputTextHelpTag"
 * @since 1.1.7
 * @author Thomas Obereder
 * @version $Date: 2005-07-02 15:32:34 +01:00 (Thu, 09 Jun 2005)
 */
public abstract class AbstractHtmlInputTextHelp extends HtmlInputText
{
    public static final String JS_FUNCTION_SELECT_TEXT = "selectText";
    public static final String JS_FUNCTION_RESET_HELP = "resetHelpValue";
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlInputTextHelp";
    public static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.TextHelp";

    /**
     * @JSFProperty
     */
    public abstract String getHelpText();

    /**
     * @JSFProperty
     *   defaultValue="false"
     */
    public abstract boolean isSelectText();
    
    /**
     * Overrides the name field used by this input.
     */
    @JSFProperty(tagExcluded=true)
    public abstract String getName();
    
    /**
     * Overrides the targetClientId used for rendered client behaviors by this component.
     */
    @JSFProperty(tagExcluded=true)
    public abstract String getTargetClientId();
}
