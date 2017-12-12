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
import org.sakaiproject.tags.tool.forms.TagCollectionForm;

/**
 * A handler for creating and updating Tag collections in the Tags Service administration tool.
 */
@Slf4j
public class TagCollectionsHandler extends CrudHandler {

    private final TagService tagService;

    public TagCollectionsHandler(TagService tagservice) {
        this.tagService = tagservice;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        if (request.getPathInfo().contains("/preview") && isGet(request)) {
            handlePreview(request, response, context);
        } else {
            super.handle(request, response, context);
        }
    }

    @Override
    protected void handleDelete(HttpServletRequest request, Map<String, Object> context) {
        String uuid = extractId(request);
        tagService.getTagCollections().deleteTagCollection(uuid);

        flash("info", "tagcollection_deleted");
        sendRedirect("");
    }

    private void handlePreview(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        String uuid = extractId(request);

        context.put("layout", false);
        try {
            Optional<TagCollection> tagCollection = tagService.getTagCollections().getForId(uuid);

            if (tagCollection.isPresent()) {
                // Don't let the portal buffering hijack our response.
                // Include enough content to count as having returned a
                // body.
                response.getWriter().write(tagCollection.get().getName());
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
        context.put("subpage", "tagcollection_form");
        Optional<TagCollection> tagCollection = tagService.getTagCollections().getForId(uuid);

        if (tagCollection.isPresent()) {
            showEditForm(TagCollectionForm.fromTagCollection(tagCollection.get()), context, CrudMode.UPDATE);
        } else {
            log.warn("No tag collection found for UUID: " + uuid);
            sendRedirect("");
        }
    }

    @Override
    protected void showNewForm(Map<String, Object> context) {
        context.put("subpage", "tagcollection_form");
        context.put("mode", "new");
    }

    @Override
    protected void handleCreateOrUpdate(HttpServletRequest request, Map<String, Object> context, CrudMode mode) {
        String uuid = extractId(request);
        TagCollectionForm tagCollectionForm = TagCollectionForm.fromRequest(uuid, request);

        this.addErrors(tagCollectionForm.validate());

        if (CrudMode.CREATE.equals(mode)) {
            if (tagService.getTagCollections().getForExternalSourceName(tagCollectionForm.toTagCollection().getExternalSourceName()).isPresent()){
                this.addError("externalsourcename","error_unique_externalsource");
            }
            if (tagService.getTagCollections().getForName(tagCollectionForm.toTagCollection().getName()).isPresent()){
                this.addError("name","error_unique_name");
            }
        }else{
            String actualExternalSourceName = tagService.getTagCollections().getForId(uuid).get().getExternalSourceName();
            String futureExternalSourceName = tagCollectionForm.toTagCollection().getExternalSourceName();
            String actualName = tagService.getTagCollections().getForId(uuid).get().getName();
            String futureName = tagCollectionForm.toTagCollection().getName();
            if (!(actualExternalSourceName.equals(futureExternalSourceName))) {
                if (tagService.getTagCollections().getForExternalSourceName(futureExternalSourceName).isPresent()) {
                    this.addError("externalsourcename", "error_unique_externalsource");
                }
            }
            if (!(actualName.equals(futureName))) {
                if (tagService.getTagCollections().getForName(futureName).isPresent()) {
                    this.addError("name", "error_unique_name");
                }
            }
        }

        if (hasErrors()) {
            showEditForm(tagCollectionForm, context, mode);
            return;
        }

        if (CrudMode.CREATE.equals(mode)) {
            tagService.getTagCollections().createTagCollection(tagCollectionForm.toTagCollection());
            flash("info", "tagcollection_created");
        } else {
            tagService.getTagCollections().updateTagCollection(tagCollectionForm.toTagCollection());
            flash("info", "tagcollection_updated");
        }

        sendRedirect("");
    }

    private void showEditForm(TagCollectionForm tagCollectionForm, Map<String, Object> context, CrudMode mode) {
        context.put("subpage", "tagcollection_form");

        if (CrudMode.UPDATE.equals(mode)) {
            context.put("mode", "edit");
        } else {
            context.put("mode", "new");
        }

        context.put("tagcollection", tagCollectionForm);
    }
}
