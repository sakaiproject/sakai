/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.util;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * Simple class which adds HTML5 placeholder behavior to <input>s
 *
 * Initially created by Tom
 * from http://wickedsource.org/2011/12/19/wicket-html5-required-and-placeholder-attributes/
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class PlaceholderBehavior extends Behavior {
    private final String placeholder;

    public PlaceholderBehavior(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        tag.put("placeholder", this.placeholder);
    }
}
