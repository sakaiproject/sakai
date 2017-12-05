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
 * A handler for creating and updating Tag collections in the Tags Service administration tool.
 */
@Slf4j
public class TagsInTagCollectionsHandler extends BaseHandler {

    private final TagService tagService;
    private final int defaultPaginationSize = 10;
    private final int countPerPageGroup = 10;

    public TagsInTagCollectionsHandler(TagService tagservice) {
        this.tagService = tagservice;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {

        String uuid = extractId(request);

        int pageNum = extractPageNum(request);
        int pageSize = extractPageSize(request);

        context.put("pageSize",pageSize);
        context.put("pageNum",pageNum);
        context.put("totalPages", (int) Math.ceil((double) tagService.getTags().getTotalTagsInCollection(uuid)/(double)pageSize));
        context.put("countPerPageGroup",countPerPageGroup);

        context.put("subpage", "tagsintagcollection");
        context.put("tagsintagcollection", tagService.getTags().getTagsPaginatedInCollection(pageNum,pageSize,uuid));
        context.put("tagserviceactive", tagService.getServiceActive());
        String actualcollectionname="";
        Boolean isExternallyCreated=false;
        try {
            if  (tagService.getTagCollections().getForId(uuid).isPresent()) {
                actualcollectionname = tagService.getTagCollections().getForId(uuid).get().getName();
                isExternallyCreated = tagService.getTagCollections().getForId(uuid).get().getExternalCreation();
            }
        }catch(Exception e){
        }
        if (isExternallyCreated){
            context.put("isExternallyCreated","style=display:none");
            context.put("editLabel","Preview");
        }else{
            context.put("isExternallyCreated","");
            context.put("editLabel","Edit");
        }
        context.put("actualtagcollectionname",actualcollectionname);
        context.put("uuid",uuid);
    }


    private String extractId(HttpServletRequest request) {
        String[] bits = request.getPathInfo().split("/");

        if (bits.length < 2) {
            addError("uuid", "uuid_missing");
            return "";
        } else {

            if (bits[bits.length - 3].equals("manage")){
                return bits[bits.length - 4];
            }else {
                return bits[bits.length - 2];
            }
        }
    }

    private int extractPageNum(HttpServletRequest request) {
        String[] bits = request.getPathInfo().split("/");

        if (bits.length < 4) {
            return 1;
        } else {
            try{
                return Integer.parseInt(bits[bits.length - 2]);
            }catch (Exception e) {
                return 1;
            }
        }
    }

    private int extractPageSize(HttpServletRequest request) {
        String[] bits = request.getPathInfo().split("/");

        if (bits.length < 4) {
            return defaultPaginationSize;
        } else {
            try{
                return Integer.parseInt(bits[bits.length - 1]);
            }catch (Exception e) {
                return defaultPaginationSize;
            }
        }
    }

}
