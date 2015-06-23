/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
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

package org.sakaiproject.pasystem.tool.handlers;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sakaiproject.pasystem.api.Errors;

/**
 * The interface implemented by all handlers.
 */
public interface Handler {
    /**
     * Handle a request and either redirect the user or set the request context
     * to display an appropriate view.
     *
     * If a string entry called "subpage" is added to the context, this will be
     * resolved to a handlebars template and rendered.
     */
    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context);

    /**
     * True if the handler has returned a redirect.
     */
    public boolean hasRedirect();

    public String getRedirect();

    /**
     * Return any validation errors produced by this request.
     */
    public Errors getErrors();

    /**
     * Return any flash messages (from the previous request) that should be displayed.
     */
    public Map<String, List<String>> getFlashMessages();
}
