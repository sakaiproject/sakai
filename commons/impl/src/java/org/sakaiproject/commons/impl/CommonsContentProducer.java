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
import org.sakaiproject.commons.api.CommonsReferenceReckoner.CommonsReference;
import org.sakaiproject.commons.api.QueryBean;
import org.sakaiproject.commons.api.SakaiProxy;
import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.util.HTMLParser;
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

    private static final ResourceLoader rl = new ResourceLoader("commons");

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

    @Override
    public boolean isContentFromReader(String reference) {
        return false;
    }

    @Override
    public Reader getContentReader(String reference) {
        return null;
    }

    @Override
    public String getContent(String ref) {

        CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();

        if (CommonsConstants.PostType.COMMENT == r.getType()) {
            Optional<Comment> opComment = commonsManager.getComment(r.getCommentId());
            if (opComment.isPresent()) {
                String content = opComment.get().getContent();
                return content != null ? HTMLParser.stripHtml(content) : "";
            }
        }

        if (CommonsConstants.PostType.POST == r.getType()) {
            Optional<Post> opPost = commonsManager.getPost(r.getPostId(), false);
            if (opPost.isPresent()) {
                String content = opPost.get().getContent();
                return content != null ? HTMLParser.stripHtml(content) : "";
            }
        }

        return "";
    }

    @Override
    public String getTitle(String ref) {

        CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();
        if (r.getType() == CommonsConstants.PostType.POST) {
            return rl.getString("search_title_post");
        } else {
            return rl.getString("search_title_comment");
        }
    }

    @Override
    public String getUrl(String ref) {

        return transactionTemplate.execute(new TransactionCallback<String>() {

            @Override
            public String doInTransaction(TransactionStatus status) {

                CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();

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

    @Override
    public boolean matches(String ref) {
        return ref.startsWith(CommonsManager.REFERENCE_ROOT);
    }

    @Override
    public Integer getAction(Event event) {

        String evt = event.getEvent();

        if (addingEvents.contains(evt)) return SearchBuilderItem.ACTION_ADD;
        if (removingEvents.contains(evt)) return SearchBuilderItem.ACTION_DELETE;

        return SearchBuilderItem.ACTION_UNKNOWN;
    }

    @Override
    public boolean matches(Event event) {

        String evt = event.getEvent();
        return addingEvents.contains(evt) || removingEvents.contains(evt);
    }

    @Override
    public String getTool() {
        return "commons";
    }

    @Override
    public String getSiteId(String ref) {
        return CommonsReferenceReckoner.reckoner().reference(ref).reckon().getContext();
    }

    @Override
    public String getCreatorDisplayName(String ref) {

        CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();

        switch (r.getType()) {

            case POST:
                Optional<Post> optPost = commonsManager.getPost(r.getPostId(), false);
                if (optPost.isPresent()) {
                    return optPost.get().getCreatorDisplayName();
                }
                log.warn("Invalid commons post ref {}. Returning empty creator display name ...", ref);
                break;
            case COMMENT:
                Optional<Comment> optComment = commonsManager.getComment(r.getCommentId());
                if (optComment.isPresent()) {
                    return optComment.get().getCreatorDisplayName();
                }
                log.warn("Invalid commons comment ref {}. Returning empty creator display name ...", ref);
                break;
            default:
        }

        return "";
    }

    @Override
    public String getCreatorId(String ref) {

        CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();

        switch (r.getType()) {

            case POST:
                Optional<Post> optPost = commonsManager.getPost(r.getPostId(), false);
                if (optPost.isPresent()) {
                    return optPost.get().getCreatorId();
                }
                log.warn("Invalid commons post ref {}. Returning empty creator id ...", ref);
                break;
            case COMMENT:
                Optional<Comment> optComment = commonsManager.getComment(r.getCommentId());
                if (optComment.isPresent()) {
                    return optComment.get().getCreatorId();
                }
                log.warn("Invalid commons comment ref {}. Returning empty creator id ...", ref);
                break;
            default:
        }

        return "";
    }

    @Override
    public String getCreatorUserName(String ref) {

        CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();

        switch (r.getType()) {

            case POST:
                Optional<Post> optPost = commonsManager.getPost(r.getPostId(), false);
                if (optPost.isPresent()) {
                    return optPost.get().getCreatorUserName();
                }
                log.warn("Invalid commons post ref {}. Returning empty creator user name ...", ref);
                break;
            case COMMENT:
                Optional<Comment> optComment = commonsManager.getComment(r.getCommentId());
                if (optComment.isPresent()) {
                    return optComment.get().getCreatorUserName();
                }
                log.warn("Invalid commons comment ref {}. Returning empty creator user name ...", ref);
                break;
            default:
        }

        return "";
    }

    @Override
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

    @Override
    public boolean isForIndex(String ref) {
        return ref.startsWith(CommonsManager.REFERENCE_ROOT);
    }

    @Override
    public boolean canRead(String ref) {

        CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();
        String siteId = r.getContext();
        if (r.getType() == CommonsConstants.PostType.POST) {
            return sakaiProxy.isAllowedFunction(CommonsFunctions.POST_READ_ANY, siteId);
        } else {
            return sakaiProxy.isAllowedFunction(CommonsFunctions.COMMENT_READ_ANY, siteId);
        }
    }

    @Override
    public Map<String, ?> getCustomProperties(String ref) {
        return null;
    }

    @Override
    public String getCustomRDF(String ref) {
        return null;
    }

    @Override
    public String getId(String ref) {

        CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();
        return (r.getType() == CommonsConstants.PostType.POST) ? r.getPostId() : r.getCommentId();
    }

    @Override
    public String getType(String ref) {
        return "commons";
    }

    @Override
    public String getSubType(String ref) {

        CommonsReference r = CommonsReferenceReckoner.reckoner().reference(ref).reckon();
        return (r.getType() == CommonsConstants.PostType.POST) ? "posts" : "comments";
    }

    @Override
    public String getContainer(String ref) {
        return CommonsReferenceReckoner.reckoner().reference(ref).reckon().getContext();
    }
}

