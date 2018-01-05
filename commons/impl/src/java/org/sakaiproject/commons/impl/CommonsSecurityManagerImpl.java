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

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.authz.api.Role;
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

        if (sakaiProxy.isAllowedFunction(CommonsFunctions.COMMENT_CREATE, post.getSiteId())) {
            return true;
        }

        return false;
    }

    public boolean canCurrentUserDeletePost(Post post) throws SecurityException {

        String siteId = post.getSiteId();

        if (sakaiProxy.isAllowedFunction(CommonsFunctions.POST_DELETE_ANY, siteId)) {
            return true;
        }

        String currentUser = sakaiProxy.getCurrentUserId();

        if (currentUser != null && currentUser.equals(post.getCreatorId())
                && (siteId.equals(CommonsConstants.SOCIAL)
                        || sakaiProxy.isAllowedFunction(CommonsFunctions.POST_DELETE_OWN, siteId))) {
            return true;
        }

        return false;
    }

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

    public boolean canCurrentUserDeleteComment(String siteId, String embedder, String commentCreatorId, String postCreatorId) throws SecurityException {

        String currentUserId = sakaiProxy.getCurrentUserId();

        if (sakaiProxy.isAllowedFunction(CommonsFunctions.COMMENT_DELETE_ANY, siteId)) {
            return true;
        }

        if (sakaiProxy.isAllowedFunction(CommonsFunctions.COMMENT_DELETE_OWN, siteId)
                && commentCreatorId.equals(currentUserId)) {
            return true;
        }

        if (embedder.equals(CommonsConstants.SOCIAL) && postCreatorId.equals(currentUserId)) {
            // You can always delete comments on your social posts
            return true;
        }

        return false;
    }

    /**
     * Tests whether the current user can read each Post and if not, filters
     * that post out of the resulting list
     */
    public List<Post> filter(List<Post> posts, String siteId, String embedder) {

        if (posts != null && posts.size() > 0) {
            long now = (new Date()).getTime();
            posts = posts.stream().filter(p -> p.getReleaseDate() <= now).collect(Collectors.toList());
            if (embedder.equals(CommonsConstants.SITE)) {
                boolean readAny = securityService.unlock(CommonsFunctions.POST_READ_ANY, "/site/" + siteId);
                return (readAny) ? posts : new ArrayList();
            } else if (embedder.equals(CommonsConstants.ASSIGNMENT)) {
                boolean readAny = securityService.unlock(AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION, "/site/" + siteId);
                return (readAny) ? posts : new ArrayList();
            } else if (embedder.equals(CommonsConstants.SOCIAL)) {
                return posts;
            } else {
                return new ArrayList();
            }
        } else {
            return posts;
        }
    }

    public boolean canCurrentUserReadPost(Post post) {

        Site site = sakaiProxy.getSiteOrNull(post.getSiteId());

        if (site != null) {
            return securityService.unlock(CommonsFunctions.POST_READ_ANY, "/site/" + post.getSiteId());
        } else {
            return false;
        }
    }

    public Site getSiteIfCurrentUserCanAccessTool(String siteId) {

        Site site;
        try {
            site = siteService.getSiteVisit(siteId);
        } catch (Exception e) {
            return null;
        }

        //check user can access the tool, it might be hidden
        ToolConfiguration toolConfig = site.getToolForCommonId("sakai.commons");
        if(!toolManager.isVisible(site, toolConfig)) {
            return null;
        }

        return site;
    }
}
