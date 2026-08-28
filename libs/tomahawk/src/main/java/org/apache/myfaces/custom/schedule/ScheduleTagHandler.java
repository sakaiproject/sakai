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
package org.apache.myfaces.custom.schedule;

import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.MetaRuleset;

import org.apache.myfaces.custom.facelets.tag.MethodRule;

/**
 * 
 * @since 1.1.7
 */
public class ScheduleTagHandler extends ComponentHandler {
 
    private static final String MOUSE_LISTENER = "mouseListener";
    private static final String MOUSE_LISTENER_EXPRESSION = "mouseListenerExpression";
    
    private static final Class [] mouseListenerParamList = new Class[]{ScheduleMouseEvent.class}; 

    public ScheduleTagHandler(ComponentConfig tagConfig) {
        super(tagConfig);
    }

    protected MetaRuleset createMetaRuleset(Class type)
    {       
        return super.createMetaRuleset(type).alias("class", "styleClass")
            .addRule(
                new MethodRule(MOUSE_LISTENER, MOUSE_LISTENER_EXPRESSION, 
                        String.class, mouseListenerParamList));
    }

 }