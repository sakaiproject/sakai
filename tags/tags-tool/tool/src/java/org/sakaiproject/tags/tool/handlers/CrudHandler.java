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

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A base handler for record types that support CRUD-style updates.
 */
public abstract class CrudHandler extends BaseHandler {


    protected String extractId(HttpServletRequest request) {
        String[] bits = request.getPathInfo().split("/");

        if (bits.length < 2) {
            addError("uuid", "uuid_missing");
            return "";
        } else {
            return bits[bits.length - 2];
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        if (request.getPathInfo().contains("/edit")) {
            if (isGet(request)) {
                handleEdit(request, context);
            } else if (isPost(request)) {
                handleCreateOrUpdate(request, context, CrudMode.UPDATE);
            }
        } else if (request.getPathInfo().contains("/new")) {
            if (isGet(request)) {
                showNewForm(context);
            } else if (isPost(request)) {
                handleCreateOrUpdate(request, context, CrudMode.CREATE);
            }
        } else if (request.getPathInfo().contains("/delete")) {
            if (isGet(request)) {
                sendRedirect("");
            } else if (isPost(request)) {
                handleDelete(request, context);
            }
        } else {
            sendRedirect("");
        }
    }

    protected abstract void handleDelete(HttpServletRequest request,Map<String, Object> context);

    protected abstract void showNewForm(Map<String, Object> context);

    protected abstract void handleCreateOrUpdate(HttpServletRequest request, Map<String, Object> context, CrudMode mode);

    protected abstract void handleEdit(HttpServletRequest request, Map<String, Object> context);

    public enum CrudMode {
        CREATE,
        UPDATE
    }
}

