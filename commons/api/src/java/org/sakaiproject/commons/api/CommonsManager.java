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

import java.util.*;

import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
public interface CommonsManager extends EntityProducer {

    public static final String ENTITY_PREFIX = "commons";
    public static final String REFERENCE_ROOT = Entity.SEPARATOR + ENTITY_PREFIX;

    public static final String POST_CACHE = "org.sakaiproject.commons.sortedPostCache";

    public Post getPost(String postId, boolean loadComments);

    public List<Post> getPosts(QueryBean query) throws Exception;

    public Post savePost(Post post);

    public boolean deletePost(String postId);

    public Comment saveComment(String commonsId, Comment comment);

    public boolean deleteComment(String siteId, String commonsId, String embedder, String commentId, String commentCreatorId, String postCreatorId);
}
