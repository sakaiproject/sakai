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
package org.apache.myfaces.custom.tabbedpane;

import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.MetaRuleset;

import org.apache.myfaces.custom.facelets.tag.MethodRule;

/**
 * 
 * @since 1.1.7
 *
 */
public class HtmlPanelTabbedPaneTagHandler extends ComponentHandler
{
    static final String METHOD_BINDING_ATTR_NAME = "tabChangeListener";
    static final Class[] METHOD_BINDING_SIGNATURE = { TabChangeEvent.class };

    protected final static MethodRule actionListenerTagRule = new MethodRule(
            METHOD_BINDING_ATTR_NAME, void.class, METHOD_BINDING_SIGNATURE);

    public HtmlPanelTabbedPaneTagHandler(ComponentConfig config)
    {
        super(config);
    }

    protected MetaRuleset createMetaRuleset(Class type)
    {
        return super.createMetaRuleset(type).alias("class", "styleClass")
            .addRule(actionListenerTagRule);
    }

}