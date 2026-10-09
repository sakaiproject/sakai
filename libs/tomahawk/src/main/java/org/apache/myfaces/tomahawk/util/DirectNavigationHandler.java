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

package org.apache.myfaces.tomahawk.util;

import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * An alternative implementation of the standard JSF NavigationHandler API which
 * directly using the outcome of an action as the name of the page to navigate to.
 * <p>
 * This bypasses the "logical navigation" approach of standard JSF, so that pages
 * can be wired together by using actual urls in command actions and action-method
 * return values.
 * <p>
 * When this navigation-handler is configured, all navigation-rule entries in
 * faces-config.xml files are ignored completely.
 * <p>
 * This navigation handler does not work well in combination with other custom
 * navigation handlers, as it never passes any calls down to underlying
 * implementations. Having another NavigationHandler decorate this one will
 * work; having this navigation handler decorate another will not as the
 * decorated handler will never be invoked. Therefore if multiple navigation
 * handlers are to be used (eg the Tomahawk RedirectTrackerNavigationHandler
 * in combination with this one), then this should be listed first in the
 * faces-config.xml file so that the other handlers decorate this one. 
 */
public class DirectNavigationHandler extends NavigationHandler
{
    /**
     * Gives the handleNavigation() method an alternative behaviour. Linking
     * is now processed directly to the given url (e.g. action="/pages/site.jsp").
     *
     * There is no check if the outcome value really points to a valid page.
     */
    public void handleNavigation(FacesContext context, String fromAction, String outcome)
    {
        if(outcome == null || outcome.length()==0)
        {
            return;
        }

        ViewHandler viewHandler=context.getApplication().getViewHandler();
        UIViewRoot viewRoot=viewHandler.createView(context,outcome);
        context.setViewRoot(viewRoot);
        context.renderResponse();
    }
}
