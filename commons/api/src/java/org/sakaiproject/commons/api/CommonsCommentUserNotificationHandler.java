/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.commons.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommonsCommentUserNotificationHandler extends AbstractUserNotificationHandler {

    @Resource
    private CommonsManager commonsManager;

    @Resource
    private ServerConfigurationService serverConfigurationService;

    @Resource
    private SiteService siteService;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(CommonsEvents.COMMENT_CREATED);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        String commentCreator = e.getUserId();

        String ref = e.getResource();
        final String siteId = e.getContext();
        String[] pathParts = ref.split("/");

        String postId = pathParts[4];

        // To is always going to be the author of the original post
        Optional<Post> post = commonsManager.getPost(postId, true);
        if (post.isPresent()) {
            if ("SOCIAL".equals(siteId)) {
                return Optional.empty();
            }

            String postCreator = post.get().getCreatorId();

            String url = null;
            String siteTitle = null;
            try {
                Site site = siteService.getSite(siteId);
                siteTitle = site.getTitle();
                String toolId = site.getToolForCommonId(CommonsConstants.TOOL_ID).getId();
                url = serverConfigurationService.getPortalUrl() + "/directtool/"
                             + toolId + "/posts/" + postId;
            } catch (IdUnusedException ex) {
                log.error("Couldn't find site " + siteId, ex);
                return Optional.empty();
            }

            List<UserNotificationData> bhEvents = new ArrayList<>();

            // First, send an alert to the post author
            if (!commentCreator.equals(postCreator)) {
                bhEvents.add(new UserNotificationData(commentCreator, postCreator, siteId, siteTitle, url, CommonsConstants.TOOL_ID, false, null));
            }

            List<String> sentAlready = new ArrayList<>();

            // Now, send an alert to anybody else who has commented on this post.
            for (Comment comment : post.get().getComments()) {

                String to = comment.getCreatorId();

                // If we're commenting on our own post, no alert needed
                if (to.equals(postCreator) || to.equals(commentCreator)) {
                    continue;
                }

                if (!sentAlready.contains(to)) {
                    bhEvents.add(new UserNotificationData(commentCreator, to, siteId, siteTitle, url, CommonsConstants.TOOL_ID, false, null));
                    sentAlready.add(to);
                }
            }
            return Optional.of(bhEvents);
        }

        return Optional.empty();
    }
}
