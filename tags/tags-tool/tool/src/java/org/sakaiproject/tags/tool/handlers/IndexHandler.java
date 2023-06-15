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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.api.ToolManager;

/**
 * A handler for the index page in the PA System administration tool.
 */
@Slf4j
public class IndexHandler extends BaseHandler {

    private final TagService tagService;
    private final SecurityService securityService;
    private final SessionManager sessionManager;
    private final ToolManager toolManager;
    private final int defaultPaginationSize = 10;
    private final int countPerPageGroup = 10;

    public IndexHandler(TagService tagservice, SessionManager sessionManager, SecurityService securityService, ToolManager toolManager) {
        this.tagService = tagservice;
        this.securityService = securityService;
        this.sessionManager = sessionManager;
        this.toolManager = toolManager;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {

        int pageNum = extractPageNum(request);
        int pageSize = extractPageSize(request);

        context.put("pageSize",pageSize);
        context.put("pageNum",pageNum);
        context.put("totalPages", (int) Math.ceil((double) tagService.getTagCollections().getTotalTagCollections()/(double)pageSize));
        context.put("countPerPageGroup",countPerPageGroup);
        context.put("subpage", "index");
        if (securityService.isSuperUser()) {
            context.put("tagcollections", tagService.getTagCollections().getTagCollectionsPaginated(pageNum,pageSize));
        } else {
            List<TagCollection> collections = new ArrayList<>();
            String siteId = toolManager.getCurrentPlacement().getContext();
            // add site tag collection
            TagCollection siteCollection = tagService.getTagCollections().getForId(siteId).orElse(null);
            if (siteCollection != null) {
                collections.add(siteCollection);
            }
            // add user tag collection
            TagCollection userCollection = tagService.getTagCollections().getForId(sessionManager.getCurrentSessionUserId()).orElse(null);
            if (userCollection != null) {
                collections.add(userCollection);
            }
            context.put("tagcollections", collections);
        }
        context.put("tagserviceactive", tagService.getServiceActive());
        context.put("actualtagcollection", "");
        context.put("canCreate", securityService.isSuperUser());

    }

    private int extractPageNum(HttpServletRequest request) {
        try {
            String[] bits = request.getPathInfo().split("/");
            if (bits.length < 4) {
                return 1;
            } else {
                try {
                    return Integer.parseInt(bits[bits.length - 2]);
                } catch (Exception e) {
                    return 1;
                }
            }
        }catch (Exception e){
            return 1;
        }
    }

    private int extractPageSize(HttpServletRequest request) {

        try {
            String[] bits = request.getPathInfo().split("/");
            if (bits.length < 4) {
                return defaultPaginationSize;
            } else {
                try {
                    return Integer.parseInt(bits[bits.length - 1]);
                } catch (Exception e) {
                    return defaultPaginationSize;
                }
            }
        }catch (Exception e){
            return defaultPaginationSize;
        }
    }
}
