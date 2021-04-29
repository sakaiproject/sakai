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
package org.sakaiproject.lessonbuildertool.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.messaging.api.BullhornData;
import org.sakaiproject.messaging.api.bullhornhandlers.AbstractBullhornHandler;
import org.sakaiproject.user.api.User;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AddLessonsCommentBullhornHandler extends AbstractBullhornHandler {

    @Resource(name = "org.sakaiproject.lessonbuildertool.model.SimplePageToolDao")
    private SimplePageToolDao simplePageToolDao;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(LessonBuilderEvents.COMMENT_CREATE);
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e) {

        List<BullhornData> bhEvents = new ArrayList<>();

        String ref = e.getResource();
        String context = e.getContext();
        String[] pathParts = ref.split("/");
        String from = e.getUserId();

        try {
            long commentId = Long.parseLong(pathParts[pathParts.length - 1]);
            SimplePageComment comment = simplePageToolDao.findCommentById(commentId);

            String url = simplePageToolDao.getPageUrl(comment.getPageId());

            if (url != null) {
                List<String> done = new ArrayList<>();
                // Alert tutor types.
                List<User> receivers = securityService.unlockUsers(
                    SimplePage.PERMISSION_LESSONBUILDER_UPDATE, "/site/" + context);
                for (User receiver : receivers) {
                    String to = receiver.getId();
                    if (!to.equals(from)) {
                        //doInsert(from, to, event, ref, "title", context, e.getEventTime(), url);
                        bhEvents.add(new BullhornData(from, to, context, "title", url));
                        done.add(to);
                    }
                }

                // Get all the comments in the same item
                List<SimplePageComment> comments
                    = simplePageToolDao.findCommentsOnItems(
                        Arrays.asList(new Long[] {comment.getItemId()}));

                if (comments.size() > 1) {
                    // Not the first, alert all the other commenters unless they already have been
                    for (SimplePageComment c : comments) {
                        String to = c.getAuthor();
                        if (!to.equals(from) && !done.contains(to)) {
                            //doInsert(from, to, event, ref, "title", context, e.getEventTime(), url);
                            bhEvents.add(new BullhornData(from, to, context, "title", url));
                            done.add(to);
                        }
                    }
                }
            } else {
                log.error("null url for page {}", comment.getPageId());
            }

            return Optional.of(bhEvents);
        } catch (NumberFormatException nfe) {
            log.error("Caught number format exception whilst handling events", nfe);
        }

        return Optional.empty();
    }
}
