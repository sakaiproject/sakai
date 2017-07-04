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

package org.sakaiproject.commons.api;

import java.util.List;

import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.site.api.Site;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
public interface CommonsSecurityManager {

    public boolean canCurrentUserCommentOnPost(Post post);
    public boolean canCurrentUserDeletePost(Post post) throws SecurityException;
    public boolean canCurrentUserEditPost(Post post);
    public List<Post> filter(List<Post> posts, String siteId, String embedder);
    public boolean canCurrentUserReadPost(Post post);
    public Site getSiteIfCurrentUserCanAccessTool(String siteId);
    public boolean canCurrentUserDeleteComment(String siteId, String embedder, String commentCreatorId, String postCreatorId) throws SecurityException;
}
