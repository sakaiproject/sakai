/*************************************************************************************
 * Copyright 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.

 *************************************************************************************/

package org.sakaiproject.commons.impl;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.commons.api.CommonsConstants;
import org.sakaiproject.commons.api.CommonsFunctions;
import org.sakaiproject.commons.api.CommonsSecurityManager;
import org.sakaiproject.commons.api.SakaiProxy;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
@Setter @Slf4j
public class CommonsSecurityManagerImpl implements CommonsSecurityManager {

    private SakaiProxy  sakaiProxy;
    private SecurityService securityService;
    private SiteService siteService;
    private ToolManager toolManager;

    @Override
    public boolean canCurrentUserCommentOnPost(Post post) {

        log.debug("canCurrentUserCommentOnPost()");

        // This acts as an override
        if (sakaiProxy.isAllowedFunction(CommonsFunctions.COMMENT_UPDATE_ANY, post.getSiteId())) {
            return true;
        }

        // An author can always comment on their own posts
        if (post.getCreatorId().equals(sakaiProxy.getCurrentUserId())) {
            return true;
        }

        return sakaiProxy.isAllowedFunction(CommonsFunctions.COMMENT_CREATE, post.getSiteId());
    }

    @Override
    public boolean canCurrentUserDeletePost(Post post) throws SecurityException {

        String siteId = post.getSiteId();

        if (sakaiProxy.isAllowedFunction(CommonsFunctions.POST_DELETE_ANY, siteId)) {
            return true;
        }

        String currentUser = sakaiProxy.getCurrentUserId();

        return currentUser != null && currentUser.equals(post.getCreatorId())
                && (siteId.equals(CommonsConstants.SOCIAL)
                || sakaiProxy.isAllowedFunction(CommonsFunctions.POST_DELETE_OWN, siteId));
    }

    @Override
    public boolean canCurrentUserEditPost(Post post) {

        // This acts as an override
        if (sakaiProxy.isAllowedFunction(CommonsFunctions.POST_UPDATE_ANY, post.getSiteId())) {
            return true;
        }

        String currentUser = sakaiProxy.getCurrentUserId();

        // If the current user is authenticated and the post author, yes.
        if (currentUser != null && currentUser.equals(post.getCreatorId())) {
            if (StringUtils.isBlank(post.getId())) {
                return sakaiProxy.isAllowedFunction(CommonsFunctions.POST_CREATE, post.getSiteId());
            } else {
                return sakaiProxy.isAllowedFunction(CommonsFunctions.POST_UPDATE_OWN, post.getSiteId());
            }
        }

        return false;
    }

    @Override
    public boolean canCurrentUserDeleteComment(String siteId, String embedder, String commentCreatorId, String postCreatorId) throws SecurityException {

        String currentUserId = sakaiProxy.getCurrentUserId();

        if (sakaiProxy.isAllowedFunction(CommonsFunctions.COMMENT_DELETE_ANY, siteId)) {
            return true;
        }

        if (sakaiProxy.isAllowedFunction(CommonsFunctions.COMMENT_DELETE_OWN, siteId)
                && commentCreatorId.equals(currentUserId)) {
            return true;
        }

        // You can always delete comments on your social posts
        return embedder.equals(CommonsConstants.SOCIAL) && postCreatorId.equals(currentUserId);
    }

    /**
     * Tests whether the current user can read each Post and if not, filters
     * that post out of the resulting list
     */
    @Override
    public List<Post> filter(List<Post> posts, String siteId, String embedder) {

        if (posts != null && !posts.isEmpty()) {
            long now = Instant.now().toEpochMilli();
            posts = posts.stream().filter(p -> p.getReleaseDate() <= now).collect(Collectors.toList());
            switch (embedder) {
                case CommonsConstants.SITE -> {
                    if (securityService.unlock(CommonsFunctions.POST_READ_ANY, "/site/" + siteId)) {
                        return posts;
                    } else {
                        // Filter to only keep posts authored by current user
                        String currentUserId = sakaiProxy.getCurrentUserId();
                        return posts.stream()
                                .filter(post -> post.getCreatorId().equals(currentUserId))
                                .collect(Collectors.toList());
                    }
                }
                case CommonsConstants.ASSIGNMENT -> {
                    boolean readAny = securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, "/site/" + siteId);
                    return (readAny) ? posts : new ArrayList<>();
                }
                case CommonsConstants.SOCIAL, CommonsConstants.SEARCH -> {
                    return posts;
                }
                default -> {
                    return new ArrayList<>();
                }
            }
        } else {
            return posts;
        }
    }

    @Override
    public boolean canCurrentUserReadPost(Post post) {

        Site site = sakaiProxy.getSiteOrNull(post.getSiteId());

        if (site != null) {
            return securityService.unlock(CommonsFunctions.POST_READ_ANY, "/site/" + post.getSiteId());
        } else {
            return false;
        }
    }

    @Override
    public Site getSiteIfCurrentUserCanAccessTool(String siteId) {

        Site site;
        try {
            site = siteService.getSiteVisit(siteId);
        } catch (Exception e) {
            return null;
        }

        //check user can access the tool, it might be hidden
        ToolConfiguration toolConfig = site.getToolForCommonId(CommonsConstants.TOOL_ID);
        if(!toolManager.isVisible(site, toolConfig)) {
            return null;
        }

        return site;
    }
}
