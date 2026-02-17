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
package org.sakaiproject.commons.impl;

import java.util.*;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.commons.api.*;
import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.commons.api.datamodel.PostLike;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.util.api.FormattedText;


/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
@Setter @Slf4j
public class CommonsManagerImpl implements CommonsManager {

    private CommonsSecurityManager commonsSecurityManager;
    private PersistenceManager persistenceManager;
    private SakaiProxy sakaiProxy;
    private FormattedText formattedText;

    public void init() {

        log.info("Registering Commons functions ...");

        sakaiProxy.registerFunction(CommonsFunctions.POST_CREATE);
        sakaiProxy.registerFunction(CommonsFunctions.POST_READ_ANY);
        sakaiProxy.registerFunction(CommonsFunctions.POST_UPDATE_ANY);
        sakaiProxy.registerFunction(CommonsFunctions.POST_UPDATE_OWN);
        sakaiProxy.registerFunction(CommonsFunctions.POST_DELETE_ANY);
        sakaiProxy.registerFunction(CommonsFunctions.POST_DELETE_OWN);
        sakaiProxy.registerFunction(CommonsFunctions.COMMENT_CREATE);
        sakaiProxy.registerFunction(CommonsFunctions.COMMENT_READ_ANY);
        sakaiProxy.registerFunction(CommonsFunctions.COMMENT_UPDATE_ANY);
        sakaiProxy.registerFunction(CommonsFunctions.COMMENT_UPDATE_OWN);
        sakaiProxy.registerFunction(CommonsFunctions.COMMENT_DELETE_ANY);
        sakaiProxy.registerFunction(CommonsFunctions.COMMENT_DELETE_OWN);

        log.info("Registered Commons functions.");

        sakaiProxy.registerEntityProducer(this);
    }

    private List<Post> getPosts(String siteId) throws Exception {

        QueryBean query = QueryBean.builder().siteId(siteId).build();
        return commonsSecurityManager.filter(persistenceManager.getAllPost(query), siteId, CommonsConstants.SITE);
    }

    public Optional<Post> getPost(String postId, boolean loadComments) {
        return Optional.ofNullable(persistenceManager.getPost(postId, loadComments));
    }

    public List<Post> getPosts(QueryBean query) throws Exception {

        Cache cache = sakaiProxy.getCache(POST_CACHE);

        // Social commons caches are keyed on the owner's user id
        String key = (query.isUserSite()) ? query.getCallerId() : query.getCommonsId();

        List<Post> posts = (List<Post>) cache.get(key);
        if (posts == null) {
            log.debug("Cache miss or expired on id: {}", key);
            if (query.isUserSite()) {
                log.debug("Getting posts for a user site ...");
                query.getFromIds().add(query.getCallerId());
            }
            List<Post> unfilteredPosts = persistenceManager.getAllPost(query, true);
            cache.put(key, unfilteredPosts);
            return commonsSecurityManager.filter(unfilteredPosts, query.getSiteId(), query.getEmbedder());
        } else {
            log.debug("Cache hit on id: {}", key);
            return commonsSecurityManager.filter(posts, query.getSiteId(), query.getEmbedder());
        }
    }

    public Post savePost(Post post) {

        if (commonsSecurityManager.canCurrentUserEditPost(post)) {
            try {
                post.setContent(formattedText.processFormattedText(post.getContent(), null, true, false));
                Post newOrUpdatedPost = persistenceManager.savePost(post);
                if (newOrUpdatedPost != null) {
                    String commonsId = post.getCommonsId();
                    List<String> contextIds = new ArrayList();
                    contextIds.add(post.getCommonsId());
                    removeContextIdsFromCache(contextIds);
                    return newOrUpdatedPost;
                } else {
                    log.error("Failed to save post");
                }
            } catch (Exception e) {
                log.error("Caught exception whilst saving post", e);
            }
        } else {
            log.warn("Current user cannot save post with id '{}'. Null will be returned.", post.getId());
        }

        return null;
    }

    public boolean deletePost(String postId) {

        try {
            Post post = persistenceManager.getPost(postId, false);
            if (commonsSecurityManager.canCurrentUserDeletePost(post)) {
                if (persistenceManager.deletePost(post)) {
                    List<String> contextIds = new ArrayList();
                    String commonsId = post.getCommonsId();
                    contextIds.add(post.getCommonsId());
                    // Invalidate all caches for this site
                    removeContextIdsFromCache(contextIds);
                    return true;
                }
            } else {
                log.warn("Can't delete post '" + postId + "'");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    public boolean likePost(String postId, String userId){
        persistenceManager.likePost(postId, userId);
        return true;
    }

    public int countPostLikes(String postId){
        return persistenceManager.countPostLikes(postId);
    }

    public int doesUserLike(String postId, String userId){
        return persistenceManager.doesUserLike(postId, userId);
    }

    public List<PostLike> getAllUserLikes(String userId){
        return persistenceManager.getAllUserLikes(userId);
    }

    public List<PostLike> getAllPostLikes(String postId){
        return persistenceManager.getAllPostLikes(postId);
    }

    public Optional<Comment> getComment(String commentId) {
        return persistenceManager.getComment(commentId);
    }

    public Comment saveComment(String commonsId, Comment comment) {

        try {
            Post post = persistenceManager.getPost(comment.getPostId(), false);
            if (commonsSecurityManager.canCurrentUserCommentOnPost(post)) {
                Comment savedComment = persistenceManager.saveComment(comment);
                if (savedComment != null) {
                    List<String> contextIds = new ArrayList();
                    contextIds.add(commonsId);
                    removeContextIdsFromCache(contextIds);
                    return savedComment;
                }
            }
        } catch (Exception e) {
            log.error("Caught exception whilst saving comment", e);
        }

        return null;
    }

    public boolean deleteComment(String siteId, String commonsId, String embedder, String commentId, String commentCreatorId, String postCreatorId) {

        try {
            if (commonsSecurityManager.canCurrentUserDeleteComment(siteId, embedder, commentCreatorId, postCreatorId)
                    && persistenceManager.deleteComment(commentId)) {
                List<String> contextIds = new ArrayList();
                contextIds.add(commonsId);
                removeContextIdsFromCache(contextIds);
                return true;
            }
        } catch (Exception e) {
            log.error("Caught exception whilst deleting comment.", e);
        }

        return false;
    }

    private String serviceName() {
        return CommonsManager.class.getName();
    }

    public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {

        log.debug("archive(siteId:{}, archivePath:{})", siteId, archivePath);

        StringBuilder results = new StringBuilder();

        results.append(getLabel() + ": Started.\n");

        int postCount = 0;

        try {
            // start with an element with our very own (service) name
            Element element = doc.createElement(serviceName());
            element.setAttribute("version", "11.x");
            ((Element) stack.peek()).appendChild(element);
            stack.push(element);

            Element commons = doc.createElement("commons");
            List<Post> posts = getPosts(siteId);
            if (posts != null && posts.size() > 0) {
                for (Post post : posts) {
                    Element postElement = post.toXml(doc, stack);
                    commons.appendChild(postElement);
                    postCount++;
                }
            }

            ((Element) stack.peek()).appendChild(commons);
            stack.push(commons);

            stack.pop();

            results.append(getLabel() + ": Finished. " + postCount + " post(s) archived.\n");
        } catch (Exception any) {
            results.append(getLabel() + ": exception caught. Message: " + any.getMessage());
            log.warn(getLabel() + " exception caught. Message: " + any.getMessage());
        }

        stack.pop();

        return results.toString();
    }

    /**
     * From EntityProducer
     */
    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport) {

        log.debug("merge(siteId:{},root tagName:{},archivePath:{},fromSiteId:{})", siteId, root.getTagName(), archivePath, fromSiteId);

        StringBuilder results = new StringBuilder();

        int postCount = 0;

        NodeList postNodes = root.getElementsByTagName(XmlDefs.POST);
        final int numberPosts = postNodes.getLength();

        for (int i = 0; i < numberPosts; i++) {
            Node child = postNodes.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                log.error("Post nodes should be elements. Skipping ...");
                continue;
            }

            Element postElement = (Element) child;

            Post post = new Post();
            post.fromXml(postElement);
            post.setSiteId(siteId);

            savePost(post);

            for (Comment comment : post.getComments()) {
                comment.setPostId(post.getId());
                saveComment(siteId, comment);
            }

            postCount++;
        }

        results.append("Stored " + postCount + " posts.");

        return results.toString();
    }

    /**
     * From EntityProducer
     */
    public Entity getEntity(Reference ref) {

        log.debug("getEntity(Ref ID:{})", ref.getId());

        Entity rv = null;

        try {
            String reference = ref.getReference();

            String[] parts = reference.split(Entity.SEPARATOR);

            if (parts.length == 5) {
                String postId = parts[4];
                rv = persistenceManager.getPost(postId, true);
            }
        } catch (Exception e) {
            log.warn("getEntity(): " + e);
        }

        return rv;
    }

    /**
     * From EntityProducer
     */
    public Collection getEntityAuthzGroups(Reference ref, String userId) {

        log.debug("getEntityAuthzGroups(Ref ID:{},{})", ref.getId(), userId);

        List ids = new ArrayList();
        ids.add("/site/" + ref.getContext());
        return ids;
    }

    public ResourceProperties getEntityResourceProperties(Reference ref) {

        try {
            String reference = ref.getReference();

            int lastIndex = reference.lastIndexOf(Entity.SEPARATOR);
            String postId = reference.substring(lastIndex, reference.length() - lastIndex);
            Entity entity = persistenceManager.getPost(postId, false);
            return entity.getProperties();
        } catch (Exception e) {
            log.warn("getEntity(): " + e);
            return null;
        }
    }

    @Override
    public String getEntityUrl(Reference ref) {
        return getEntity(ref).getUrl();
    }

    @Override
    public String getLabel() {
        return "commons";
    }

    /**
     * From EntityProducer
     */
    public boolean parseEntityReference(String referenceString, Reference reference) {

        String[] parts = referenceString.split(Entity.SEPARATOR);

        if (parts.length < 2 || !parts[1].equals("commons")) // Leading slash adds
                                                          // an empty element
            return false;

        if (parts.length == 2) {
            reference.set("sakai:commons", "", "", null, "");
            return true;
        }

        String siteId = parts[2];
        String subType = parts[3];

        return false;
    }

    public boolean willArchiveMerge() {
        return true;
    }

    public String getEntityPrefix() {
        return CommonsManager.ENTITY_PREFIX;
    }

    public boolean entityExists(String id) {

        String postId = id.substring(id.lastIndexOf(Entity.SEPARATOR));

        try {
            if (persistenceManager.postExists(postId))
                return true;
        } catch (Exception e) {
            log.error("entityExists threw an exception", e);
        }

        return false;
    }

    private void removeContextIdsFromCache(List<String> contextIds) {

        Cache cache = sakaiProxy.getCache(POST_CACHE);
        contextIds.forEach(contextId -> cache.remove(contextId));
    }
}
