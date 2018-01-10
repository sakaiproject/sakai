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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.tool.forms.TagForm;

/**
 * A handler for creating and updating tags in the Tags Service administration tool.
 */
@Slf4j
public class TagsHandler extends CrudHandler {

    private static final int TAGSERVICE_URL_TAGSINTAGCOLLECTION_PREFIX_LENGTH = 20;
    private final TagService tagService;

    public TagsHandler(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        String referer =  request.getHeader("referer");
        context.put("returnpath", referer);
        try {
            context.put("actualtagcollection", referer.substring(referer.indexOf("tagsintagcollection/") + TAGSERVICE_URL_TAGSINTAGCOLLECTION_PREFIX_LENGTH, referer.indexOf("/manage")));
            context.put("tagcollectionidreadonly", "readonly hidden");
            context.put("actualtagcollectionname", tagService.getTagCollections().getForId(context.get("actualtagcollection").toString()).get().getName());
        }catch (Exception e){
            context.put("actualtagcollection", "");
            context.put("tagcollectionidreadonly", "");
            context.put("actualtagcollectionname", "");
        }
        if (request.getPathInfo().contains("/preview") && isGet(request)) {
            handlePreview(request, response, context);
        } else {
            super.handle(request, response, context);
        }
    }

    @Override
    protected void handleDelete(HttpServletRequest request, Map<String, Object> context) {
        String uuid = extractId(request);
        tagService.getTags().deleteTag(uuid);

        flash("info", "tag_deleted");
        sendRedirect("tagsintagcollection/" + context.get("actualtagcollection") + "/manage");
    }


    private void handlePreview(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        String uuid = extractId(request);

        context.put("layout", false);
        try {
            Optional<Tag> tag = tagService.getTags().getForId(uuid);

            if (tag.isPresent()) {
                // Don't let the portal buffering hijack our response.
                // Include enough content to count as having returned a
                // body.
                response.getWriter().write(tag.get().getTagLabel());
            }else{

                response.getWriter().write("     ");
            }
        } catch (IOException e) {
            log.warn("Write failed while previewing tag", e);
        }
    }

    @Override
    protected void handleEdit(HttpServletRequest request, Map<String, Object> context) {
        String uuid = extractId(request);
        context.put("subpage", "tag_form");
        Optional<Tag> tag = tagService.getTags().getForId(uuid);
        if (tag.isPresent()) {
            Optional<TagCollection> tagCollection = tagService.getTagCollections().getForId(tag.get().getTagCollectionId());
            if (tagCollection.get().getExternalCreation()){
                context.put("externalcreation", " readonly ");
                context.put("isExternallyUpdated","style=display:none");
            }
            showEditForm(TagForm.fromTag(tag.get()), context, CrudMode.UPDATE);
        } else {
            flash("danger", "No tag found for UUID: " + uuid);
            sendRedirect("");
        }
    }


    private void showEditForm(TagForm tagForm, Map<String, Object> context, CrudMode mode) {
        context.put("subpage", "tag_form");

        if (CrudMode.UPDATE.equals(mode)) {
            context.put("mode", "edit");
        } else {
            context.put("mode", "new");
        }

        context.put("tag", tagForm);
    }

    @Override
    protected void handleCreateOrUpdate(HttpServletRequest request, Map<String, Object> context, CrudMode mode) {
        String uuid = extractId(request);
        TagForm tagForm = TagForm.fromRequest(uuid, request);

        this.addErrors(tagForm.validate(mode));

        if (hasErrors()) {
            showEditForm(tagForm, context, mode);
            return;
        }

        if (CrudMode.CREATE.equals(mode)) {
            tagService.getTags().createTag(tagForm.toTag());
            flash("info", "tag_created");
        } else {
            tagService.getTags().updateTag(tagForm.toTag());
            flash("info", "tag_updated");
        }
        sendRedirect("tagsintagcollection/" + tagForm.toTag().getTagCollectionId() + "/manage");
    }

    @Override
    protected void showNewForm(Map<String, Object> context) {

        context.put("subpage", "tag_form");
        context.put("mode", "new");
        String actualCollection = context.getOrDefault("actualtagcollection","none").toString();
        if (!actualCollection.equals("none")){

            Optional<TagCollection> tagCollection = tagService.getTagCollections().getForId(actualCollection);
            if (tagCollection.isPresent()){
                if (tagCollection.get().getExternalCreation()) {
                    context.put("externalcreation", " readonly ");
                }
            }

        }else{
            context.put("externalcreation", "");
        }

    }

}
