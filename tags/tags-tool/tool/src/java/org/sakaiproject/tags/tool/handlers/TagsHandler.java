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
import org.sakaiproject.util.api.FormattedText;

/**
 * A handler for creating and updating tags in the Tags Service administration tool.
 */
@Slf4j
public class TagsHandler extends CrudHandler {

    private final TagService tagService;
    private final FormattedText formattedText;

    public TagsHandler(TagService tagService, FormattedText formattedText) {
        this.tagService = tagService;
        this.formattedText = formattedText;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        String collectionId = request.getParameter("tagCollectionId");
        if (!request.getPathInfo().contains("/new")) {
            collectionId = tagService.getTag(extractId(request)).map(Tag::getTagCollectionId).orElse(null);
        }
        context.put("actualtagcollection", collectionId);
        context.put("tagcollectionidreadonly", "readonly hidden");
        if (collectionId != null) {
            tagService.getTagCollection(collectionId).ifPresent(collection ->
                context.put("actualtagcollectionname", collection.getName()));
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
        tagService.deleteTag(uuid);

        flash("info", "tag_deleted");
        sendRedirect("tagsintagcollection/" + context.get("actualtagcollection") + "/manage");
    }


    private void handlePreview(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        String uuid = extractId(request);

        context.put("layout", false);
        try {
            Optional<Tag> tag = tagService.getTag(uuid);

            if (tag.isPresent()) {
                // Don't let the portal buffering hijack our response.
                // Include enough content to count as having returned a
                // body.
                response.getWriter().write(formattedText.escapeHtml(tag.get().getTagLabel()));
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
        Optional<Tag> tag = tagService.getTag(uuid);
        if (tag.isPresent()) {
            Optional<TagCollection> tagCollection = tagService.getTagCollection(tag.get().getTagCollectionId());
            if (Boolean.TRUE.equals(tagCollection.get().getExternalCreation())){
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

        this.addErrors(tagForm.validate(tagService));

        if (hasErrors()) {
            showEditForm(tagForm, context, mode);
            return;
        }

        if (CrudMode.CREATE.equals(mode)) {
            tagService.createTag(tagForm.toTag());
            flash("info", "tag_created");
        } else {
            tagService.updateTag(tagForm.toTag());
            flash("info", "tag_updated");
        }
        sendRedirect("tagsintagcollection/" + tagForm.toTag().getTagCollectionId() + "/manage");
    }

    @Override
    protected void showNewForm(Map<String, Object> context) {

        context.put("subpage", "tag_form");
        context.put("mode", "new");
        String collectionId = (String) context.get("actualtagcollection");
        Optional<TagCollection> collection = collectionId == null
            ? Optional.empty() : tagService.getTagCollection(collectionId);
        if (collection.isEmpty()) {
            flash("danger", "uuid_missing");
            sendRedirect("");
            return;
        }
        if (Boolean.TRUE.equals(collection.get().getExternalCreation())) {
            context.put("externalcreation", " readonly ");
        }
    }

}
