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
package org.sakaiproject.portal.beans.bullhornhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.sakaiproject.commons.api.CommonsEvents;
import org.sakaiproject.commons.api.CommonsManager;
import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.portal.api.BullhornData;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommonsCommentBullhornHandler extends AbstractBullhornHandler {

    @Inject
    private CommonsManager commonsManager;

    @Inject
    private ServerConfigurationService serverConfigurationService;

    @Inject
    private SiteService siteService;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(CommonsEvents.COMMENT_CREATED);
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e, Cache<String, Long> countCache) {

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
                //ToolConfiguration toolConfig = site.getToolForCommonId("sakai.commons");
                String toolId = site.getToolForCommonId("sakai.commons").getId();
                url = serverConfigurationService.getPortalUrl() + "/directtool/"
                             + toolId + "/posts/" + postId;
            } catch (IdUnusedException ex) {
                log.error("Couldn't find site " + siteId, ex);
                return Optional.empty();
            }

            List<BullhornData> bhEvents = new ArrayList<>();

            // First, send an alert to the post author
            if (!commentCreator.equals(postCreator)) {
                bhEvents.add(new BullhornData(commentCreator, postCreator, siteId, siteTitle, url));
                countCache.remove(postCreator);
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
                    bhEvents.add(new BullhornData(commentCreator, to, siteId, siteTitle, url));
                    countCache.remove(to);
                    sentAlready.add(to);
                }
            }
            return Optional.of(bhEvents);
        }

        return Optional.empty();
    }
}
