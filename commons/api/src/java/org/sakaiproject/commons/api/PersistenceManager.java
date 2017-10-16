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

import java.util.List;

import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Commons;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.commons.api.QueryBean;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
public interface PersistenceManager {

    public boolean postExists(String postId);
    public List<Post> getAllPost(QueryBean queryBean) throws Exception;
    public List<Post> getAllPost(QueryBean queryBean, boolean populate) throws Exception;
    public Comment getComment(String commentId);
    public Comment saveComment(Comment comment);
    public boolean deleteComment(String commentId);
    public Post savePost(Post post);
    public boolean deletePost(Post post);
    public Post getPost(String postId, boolean loadComments);
    public Commons getCommons(String commonsId);
}
