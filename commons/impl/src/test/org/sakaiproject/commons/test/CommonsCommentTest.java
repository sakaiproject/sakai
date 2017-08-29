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
package org.sakaiproject.commons.test;

import java.util.Date;
import java.util.UUID;

import junit.framework.TestCase;

import org.sakaiproject.commons.api.datamodel.Comment;

public class CommonsCommentTest extends TestCase {

    public void testConstructors() {

        Comment comment = new Comment();
        assertEquals("There should be no content for the no args constructor.",comment.getContent(),"");

        String content = "Nice post.";
        long createdDate = new Date().getTime();

        comment = new Comment(content);
        assertEquals("The text returned did not match the text set",comment.getContent(),content);

        comment = new Comment(content,createdDate);
        assertTrue("The text returned did not match the text set",
                comment.getContent().equals(content) && comment.getCreatedDate() == createdDate);
    }

    public void testUpdateContent() {

        Comment comment = new Comment("blah");

        long firstModDate = comment.getModifiedDate();

        comment.setContent("blah");
        //Mod date should be the same as the content has not changed.
        assertEquals("The modified date has changed while the content has not",comment.getModifiedDate(),firstModDate);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }

        comment.setContent("blah1");
        //Mod date should have changed as the content has changed.
        assertTrue("The modified date should have gone up but it hasn't",comment.getModifiedDate() > firstModDate);
    }

    public void testSetId() {

        Comment comment = new Comment();
        String id = UUID.randomUUID().toString();
        comment.setId(id);
        assertEquals("The id returned did not match the one set",comment.getId(),id);
    }

    public void testSetPostId() {

        Comment comment = new Comment();
        String postId = UUID.randomUUID().toString();
        comment.setPostId(postId);
        assertEquals("The post id returned did not match the one set",comment.getPostId(),postId);
    }

    public void testSetCreatorDisplayName() {

        Comment comment = new Comment();
        String displayName = "John Smith";
        comment.setCreatorDisplayName(displayName);
        assertEquals("The creator display name returned did not match the one set",comment.getCreatorDisplayName(),displayName);
    }

    public void testSetCreatorId() {

        Comment comment = new Comment();
        String creatorId = UUID.randomUUID().toString();
        comment.setCreatorId(creatorId);
        assertEquals("The creator id returned did not match the one set.",creatorId,comment.getCreatorId());
    }

    public void testDates() {

        Comment comment = new Comment();

        Date createdDate = new Date();
        comment.setCreatedDate(createdDate.getTime());
        assertEquals("Created date not returned correctly.",comment.getCreatedDate(),createdDate.getTime());

        Date modifiedDate = new Date();
        comment.setModifiedDate(modifiedDate.getTime());
        assertEquals("Modified date not returned correctly.",comment.getModifiedDate(),modifiedDate.getTime());
    }
}
