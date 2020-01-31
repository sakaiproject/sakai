/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.commons.impl;

import java.io.Reader;
import java.util.*;

import org.sakaiproject.commons.api.CommonsConstants;
import org.sakaiproject.commons.api.CommonsEvents;
import org.sakaiproject.commons.api.CommonsFunctions;
import org.sakaiproject.commons.api.CommonsManager;
import org.sakaiproject.commons.api.CommonsReferenceReckoner;
import org.sakaiproject.commons.api.QueryBean;
import org.sakaiproject.commons.api.SakaiProxy;
import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class CommonsContentProducer implements EntityContentProducer {

    private CommonsManager commonsManager;
    private SakaiProxy sakaiProxy;
    private SearchIndexBuilder searchIndexBuilder;
    private ServerConfigurationService serverConfigurationService;
    private SearchService searchService;
    private SiteService siteService;
    private TransactionTemplate transactionTemplate;

    private List<String> addingEvents = new ArrayList<>();
    private List<String> removingEvents = new ArrayList<>();

    private ResourceLoader rl = new ResourceLoader("commons");

    public void init() {

        if ("true".equals(serverConfigurationService.getString("search.enable", "false"))) {
            addingEvents.add(CommonsEvents.POST_CREATED);
            addingEvents.add(CommonsEvents.POST_UPDATED);
            addingEvents.add(CommonsEvents.COMMENT_CREATED);
            addingEvents.add(CommonsEvents.COMMENT_UPDATED);
            removingEvents.add(CommonsEvents.POST_DELETED);
            removingEvents.add(CommonsEvents.COMMENT_DELETED);
            addingEvents.forEach(e -> searchService.registerFunction(e));
            removingEvents.forEach(e -> searchService.registerFunction(e));
            searchIndexBuilder.registerEntityContentProducer(this);
        }
    }

    public boolean isContentFromReader(String reference) {
        return false;
    }

    public Reader getContentReader(String reference) {
        return null;
    }

    public String getContent(String ref) {

        CommonsReferenceReckoner.CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();

        if (CommonsConstants.PostType.COMMENT == r.getType()) {
            Optional<Comment> opComment = commonsManager.getComment(r.getCommentId());
            if (opComment.isPresent()) {
                StringBuilder sb = new StringBuilder();
                SearchUtils.appendCleanString(opComment.get().getContent(), sb);
                return sb.toString();
            }
        }

        if (CommonsConstants.PostType.POST == r.getType()) {
            Optional<Post> opPost = commonsManager.getPost(r.getPostId(), false);
            if (opPost.isPresent()) {
                StringBuilder sb = new StringBuilder();
                SearchUtils.appendCleanString(opPost.get().getContent(), sb);
                return sb.toString();
            }
        }

        return "";
    }

    public String getTitle(String ref) {

        CommonsReferenceReckoner.CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();
        if (r.getType() == CommonsConstants.PostType.POST) {
            return rl.getString("search_title_post");
        } else {
            return rl.getString("search_title_comment");
        }
    }

    public String getUrl(String ref) {

        return transactionTemplate.execute(new TransactionCallback<String>() {

            @Override
            public String doInTransaction(TransactionStatus status) {

                CommonsReferenceReckoner.CommonsReference r
                    = CommonsReferenceReckoner.reckoner().reference(ref).reckon();

                try {
                    Site site = siteService.getSite(r.getContext());
                    ToolConfiguration fromTool = site.getToolForCommonId("sakai.commons");
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + fromTool.getId()
                            + "?ref=" + ref;
                } catch (Exception e) {
                    log.error("Failed to get deep link for context {} and post {}. Returning empty string.", r.getContext(), r.getPostId(), e);
                }
                return "";
            }
        });
    }

    public boolean matches(String ref) {
        return ref.startsWith(CommonsManager.REFERENCE_ROOT);
    }

    public Integer getAction(Event event) {

        String evt = event.getEvent();

        if (addingEvents.contains(evt)) return SearchBuilderItem.ACTION_ADD;
        if (removingEvents.contains(evt)) return SearchBuilderItem.ACTION_DELETE;

        return SearchBuilderItem.ACTION_UNKNOWN;
    }

    public boolean matches(Event event) {

        String evt = event.getEvent();
        return addingEvents.contains(evt) || removingEvents.contains(evt);
    }

    public String getTool() {
        return "commons";
    }

    public String getSiteId(String ref) {
        return CommonsReferenceReckoner.reckoner().reference(ref).reckon().getContext();
    }

    public Iterator<String> getSiteContentIterator(String siteId) {

        List<String> refs = new ArrayList<>();

        try {
            commonsManager.getPosts(QueryBean.builder().siteId(siteId).commonsId(siteId).embedder(CommonsConstants.SEARCH).build()).forEach(p -> {

                refs.add(p.getReference());
                p.getComments().forEach(c -> {
                    refs.add(c.getReference());
                });
            });
        } catch (Exception e) {
            log.error("Failed to get posts during site indexing", e);
        }

        return refs.iterator();
    }

    public boolean isForIndex(String ref) {
        return ref.startsWith(CommonsManager.REFERENCE_ROOT);
    }

    public boolean canRead(String ref) {

        CommonsReferenceReckoner.CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();
        String siteId = r.getContext();
        if (r.getType() == CommonsConstants.PostType.POST) {
            return sakaiProxy.isAllowedFunction(CommonsFunctions.POST_READ_ANY, siteId);
        } else {
            return sakaiProxy.isAllowedFunction(CommonsFunctions.COMMENT_READ_ANY, siteId);
        }
    }

    public Map<String, ?> getCustomProperties(String ref) {
        return null;
    }

    public String getCustomRDF(String ref) {
        return null;
    }

    public String getId(String ref) {

        CommonsReferenceReckoner.CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();
        return (r.getType() == CommonsConstants.PostType.POST) ? r.getPostId() : r.getCommentId();
    }

    public String getType(String ref) {
        return "commons";
    }

    public String getSubType(String ref) {

        CommonsReferenceReckoner.CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();
        return (r.getType() == CommonsConstants.PostType.POST) ? "posts" : "comments";
    }

    public String getContainer(String ref) {
        return CommonsReferenceReckoner.reckoner().reference(ref).reckon().getContext();
    }
}

