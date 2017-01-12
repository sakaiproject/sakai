/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 *
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tags.tool.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.sakaiproject.tags.api.Errors;

/**
 * The base class for all handler implementations.
 */
abstract class BaseHandler implements Handler {

    private Errors errors;
    private Map<String, List<String>> flashMessages;
    private String redirectURI;

    protected boolean isGet(HttpServletRequest request) {
        return "GET".equals(request.getMethod());
    }

    protected boolean isPost(HttpServletRequest request) {
        return "POST".equals(request.getMethod());
    }

    protected void sendRedirect(String uri) {
        redirectURI = uri;
    }

    @Override
    public boolean hasRedirect() {
        return redirectURI != null;
    }

    @Override
    public String getRedirect() {
        return redirectURI;
    }

    public void addErrors(Errors other) {
        getErrors().merge(other);
    }

    public void addError(String field, String errorCode) {
        getErrors().addError(field, errorCode);
    }

    protected boolean hasErrors() {
        return getErrors().hasErrors();
    }

    @Override
    public Errors getErrors() {
        if (errors == null) {
            errors = new Errors();
        }

        return errors;
    }

    /**
     * Record a message that will be added to the user's session and displayed
     * on their next request.
     */
    public void flash(String level, String message) {
        if (flashMessages == null) {
            flashMessages = new HashMap<String, List<String>>();
        }

        if (flashMessages.get(level) == null) {
            flashMessages.put(level, new ArrayList<String>());
        }

        flashMessages.get(level).add(message);
    }

    @Override
    public Map<String, List<String>> getFlashMessages() {
        if (flashMessages == null) {
            flashMessages = new HashMap<String, List<String>>();
        }

        return flashMessages;
    }

    public String toString() {
        return this.getClass().toString();
    }
}

