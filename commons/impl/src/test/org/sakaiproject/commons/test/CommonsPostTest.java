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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Post;

public class CommonsPostTest extends TestCase {

    public void testSetId() {

        Post post = new Post();
        String id = UUID.randomUUID().toString();
        post.setId(id);
        assertEquals("The id returned did not match the one set",id,post.getId());
    }

    public void testSetCreatorId() {

        Post post = new Post();
        String creatorId = UUID.randomUUID().toString();
        post.setCreatorId(creatorId);
        assertEquals("The creator id returned did not match the one set.",creatorId,post.getCreatorId());
    }

    public void testComments() {

        Comment comment1 = new Comment("Nice one!");
        Comment comment2 = new Comment("Could do better.");
        Comment comment3 = new Comment("See me (and wear something nice).");

        List<Comment> commentsTest = Arrays.asList(comment1,comment2,comment3);

        // First test that list comment setting is returned ok
        Post post = new Post();
        post.setComments(commentsTest);
        assertEquals("Comments not returned correctly.",post.getComments(),commentsTest);
        
        // Now test that individual comment setting is returned ok
        post = new Post();
        post.addComment(comment1);
        post.addComment(comment2);
        post.addComment(comment3);
        assertEquals("Comments not returned correctly.",post.getComments(),commentsTest);
    }

    public void testDates() {

        Post post = new Post();

        Date createdDate = new Date();
        post.setCreatedDate(createdDate.getTime());
        assertEquals("Created date not returned correctly.",post.getCreatedDate(),createdDate.getTime());

        Date modifiedDate = new Date();
        post.setModifiedDate(modifiedDate.getTime());
        assertEquals("Modified date not returned correctly.",post.getModifiedDate(),modifiedDate.getTime());
    }
}
