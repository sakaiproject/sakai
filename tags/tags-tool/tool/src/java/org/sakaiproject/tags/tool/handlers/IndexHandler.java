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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tags.api.TagService;

/**
 * A handler for the index page in the PA System administration tool.
 */
@Slf4j
public class IndexHandler extends BaseHandler {

    private final TagService tagService;
    private final int defaultPaginationSize = 10;
    private final int countPerPageGroup = 10;

    public IndexHandler(TagService tagservice) {
        this.tagService = tagservice;
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
        context.put("tagcollections", tagService.getTagCollections().getTagCollectionsPaginated(pageNum,pageSize));
        context.put("tagserviceactive", tagService.getServiceActive());
        context.put("actualtagcollection", "");


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
