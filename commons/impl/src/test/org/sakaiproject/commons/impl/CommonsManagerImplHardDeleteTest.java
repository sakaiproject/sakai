package org.sakaiproject.commons.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.commons.api.PersistenceManager;
import org.sakaiproject.commons.api.QueryBean;
import org.sakaiproject.commons.api.SakaiProxy;
import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Commons;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.commons.api.datamodel.PostLike;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheEventListener;
import org.sakaiproject.memory.api.CacheLoader;
import org.sakaiproject.memory.api.CacheStatistics;
import org.sakaiproject.memory.api.Configuration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;

public class CommonsManagerImplHardDeleteTest {

    private CommonsManagerImpl commonsManager;
    private TestPersistenceManager persistenceManager;
    private TestSakaiProxy sakaiProxy;

    @Before
    public void setUp() {
        persistenceManager = new TestPersistenceManager();
        sakaiProxy = new TestSakaiProxy();

        commonsManager = new CommonsManagerImpl();
        commonsManager.setPersistenceManager(persistenceManager);
        commonsManager.setSakaiProxy(sakaiProxy);
    }

    @Test
    public void hardDeleteRemovesSitePostsAndComments() throws Exception {

        String siteId = "site-1";
        String commonsId = "commons-1";
        String otherSiteId = "site-2";
        String otherCommonsId = "commons-2";

        Post sitePostOne = createPost("post-1", siteId, commonsId);
        Comment siteCommentOne = createComment("comment-1", sitePostOne.getId());
        sitePostOne.setComments(Collections.singletonList(siteCommentOne));
        persistenceManager.addPost(sitePostOne);

        Post sitePostTwo = createPost("post-2", siteId, commonsId);
        Comment siteCommentTwo = createComment("comment-2", sitePostTwo.getId());
        sitePostTwo.setComments(Collections.singletonList(siteCommentTwo));
        persistenceManager.addPost(sitePostTwo);

        Post otherSitePost = createPost("post-3", otherSiteId, otherCommonsId);
        persistenceManager.addPost(otherSitePost);

        Cache<String, Object> cache = sakaiProxy.getCache(org.sakaiproject.commons.api.CommonsManager.POST_CACHE);
        cache.put(siteId, new Object());
        cache.put(commonsId, new Object());
        cache.put(otherCommonsId, new Object());

        commonsManager.hardDelete(siteId);

        assertFalse("Posts for the site should be deleted", persistenceManager.hasAnyPostsForSite(siteId));
        assertEquals(1, persistenceManager.getDeletedCommentCount(sitePostOne.getId()));
        assertEquals(1, persistenceManager.getDeletedCommentCount(sitePostTwo.getId()));
        assertTrue("Posts for other sites should remain", persistenceManager.hasPost(otherSitePost.getId()));
        assertFalse("Cache entry for the site should be removed", cache.containsKey(siteId));
        assertFalse("Cache entry for commons should be removed", cache.containsKey(commonsId));
        assertTrue("Cache entries for other sites should remain", cache.containsKey(otherCommonsId));
    }

    private Post createPost(String id, String siteId, String commonsId) {
        Post post = new Post();
        post.setId(id);
        post.setSiteId(siteId);
        post.setCommonsId(commonsId);
        post.setCreatorId("creator");
        post.setContent("content");
        return post;
    }

    private Comment createComment(String id, String postId) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setPostId(postId);
        comment.setCreatorId("creator");
        comment.setContent("comment");
        return comment;
    }

    private static class TestPersistenceManager implements PersistenceManager {

        private final Map<String, Post> posts = new ConcurrentHashMap<>();
        private final Map<String, List<Comment>> commentsByPost = new ConcurrentHashMap<>();
        private final Map<String, Integer> deletedCommentCounts = new ConcurrentHashMap<>();

        void addPost(Post post) {
            posts.put(post.getId(), copyPost(post));
            List<Comment> comments = new ArrayList<>();
            if (post.getComments() != null) {
                comments.addAll(post.getComments().stream().map(this::copyComment).collect(Collectors.toList()));
            }
            commentsByPost.put(post.getId(), comments);
        }

        boolean hasPost(String postId) {
            return posts.containsKey(postId);
        }

        boolean hasAnyPostsForSite(String siteId) {
            return posts.values().stream().anyMatch(p -> siteId.equals(p.getSiteId()));
        }

        int getDeletedCommentCount(String postId) {
            return deletedCommentCounts.getOrDefault(postId, 0);
        }

        @Override
        public boolean postExists(String postId) {
            return posts.containsKey(postId);
        }

        @Override
        public List<Post> getAllPost(QueryBean queryBean) {
            return posts.values().stream()
                    .filter(post -> queryBean.getSiteId().equals(post.getSiteId()))
                    .map(this::copyPost)
                    .collect(Collectors.toList());
        }

        @Override
        public List<Post> getAllPost(QueryBean queryBean, boolean populate) {
            return getAllPost(queryBean);
        }

        @Override
        public Optional<Comment> getComment(String commentId) {
            return commentsByPost.values().stream()
                    .flatMap(List::stream)
                    .filter(comment -> comment.getId().equals(commentId))
                    .findFirst();
        }

        @Override
        public Comment saveComment(Comment comment) {
            throw new UnsupportedOperationException("Not supported in test harness");
        }

        @Override
        public boolean deleteComment(String commentId) {
            return commentsByPost.values().stream()
                    .anyMatch(comments -> comments.removeIf(comment -> comment.getId().equals(commentId)));
        }

        @Override
        public Post savePost(Post post) {
            posts.put(post.getId(), copyPost(post));
            return copyPost(post);
        }

        @Override
        public boolean deletePost(Post post) {
            List<Comment> comments = commentsByPost.remove(post.getId());
            if (comments != null) {
                deletedCommentCounts.put(post.getId(), comments.size());
            }
            return posts.remove(post.getId()) != null;
        }

        @Override
        public Post getPost(String postId, boolean loadComments) {
            Post post = posts.get(postId);
            return (post == null) ? null : copyPost(post);
        }

        @Override
        public Commons getCommons(String commonsId) {
            return null;
        }

        @Override
        public boolean likePost(String postId, String userId) {
            return false;
        }

        @Override
        public int countPostLikes(String postId) {
            return 0;
        }

        @Override
        public int doesUserLike(String postId, String userId) {
            return 0;
        }

        @Override
        public List<PostLike> getAllUserLikes(String userId) {
            return Collections.emptyList();
        }

        @Override
        public List<PostLike> getAllPostLikes(String postId) {
            return Collections.emptyList();
        }

        private Post copyPost(Post original) {
            Post copy = new Post();
            copy.setId(original.getId());
            copy.setSiteId(original.getSiteId());
            copy.setCommonsId(original.getCommonsId());
            copy.setCreatorId(original.getCreatorId());
            copy.setContent(original.getContent());
            copy.setCreatedDate(original.getCreatedDate());
            copy.setModifiedDate(original.getModifiedDate());
            copy.setReleaseDate(original.getReleaseDate());
            copy.setPriority(original.isPriority());
            copy.setEmbedder(original.getEmbedder());
            return copy;
        }

        private Comment copyComment(Comment original) {
            Comment copy = new Comment();
            copy.setId(original.getId());
            copy.setPostId(original.getPostId());
            copy.setCreatorId(original.getCreatorId());
            copy.setContent(original.getContent());
            copy.setCreatedDate(original.getCreatedDate());
            copy.setModifiedDate(original.getModifiedDate());
            return copy;
        }
    }

    private static class TestSakaiProxy implements SakaiProxy {

        private final TestCache cache = new TestCache("commons-post-cache");

        @Override
        public String getCurrentSiteId() {
            return null;
        }

        @Override
        public Site getSiteOrNull(String siteId) {
            return null;
        }

        @Override
        public String getCurrentSiteLocale() {
            return null;
        }

        @Override
        public Tool getCurrentTool() {
            return null;
        }

        @Override
        public String getCurrentToolId() {
            return null;
        }

        @Override
        public Session getCurrentSession() {
            return null;
        }

        @Override
        public String getCurrentUserId() {
            return null;
        }

        @Override
        public ToolSession getCurrentToolSession() {
            return null;
        }

        @Override
        public void setCurrentToolSession(ToolSession toolSession) {
        }

        @Override
        public String getDisplayNameForTheUser(String userId) {
            return null;
        }

        @Override
        public User getUser(String userId) {
            return null;
        }

        @Override
        public boolean isCurrentUserAdmin() {
            return false;
        }

        @Override
        public String getPortalUrl() {
            return null;
        }

        @Override
        public void registerEntityProducer(EntityProducer entityProducer) {
        }

        @Override
        public void registerFunction(String function) {
        }

        @Override
        public boolean isAllowedFunction(String function, String siteId) {
            return false;
        }

        @Override
        public boolean isAllowedFunction(String function, org.sakaiproject.authz.api.Role role) {
            return false;
        }

        @Override
        public void postEvent(String event, String entityId, String siteId) {
        }

        @Override
        public Set<String> getSiteUsers(String siteId) {
            return Collections.emptySet();
        }

        @Override
        public String getCommonsToolId(String siteId) {
            return null;
        }

        @Override
        public Set<String> getSitePermissionsForCurrentUser(String siteId, String embedder) {
            return Collections.emptySet();
        }

        @Override
        public Map<String, Set<String>> getSitePermissions(String siteId) {
            return Collections.emptyMap();
        }

        @Override
        public boolean setPermissionsForSite(String siteId, Map<String, Object> params) {
            return false;
        }

        @Override
        public Cache<String, Object> getCache(String cache) {
            return this.cache;
        }

        @Override
        public boolean isUserSite(String siteId) {
            return false;
        }

        @Override
        public String storeFile(org.apache.commons.fileupload.FileItem fileItem, String siteId) {
            return null;
        }
    }

    private static class TestCache implements Cache<String, Object> {

        private final String name;
        private final Map<String, Object> entries = new ConcurrentHashMap<>();
        private CacheLoader cacheLoader;
        private CacheEventListener cacheEventListener;
        private boolean distributed;

        TestCache(String name) {
            this.name = name;
        }

        @Override
        public Object get(String key) {
            return entries.get(key);
        }

        @Override
        public Map<String, Object> getAll(Set<? extends String> keys) {
            return keys.stream().filter(entries::containsKey).collect(Collectors.toMap(key -> key, entries::get));
        }

        @Override
        public boolean containsKey(String key) {
            return entries.containsKey(key);
        }

        @Override
        public void put(String key, Object payload) {
            entries.put(key, payload);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> map) {
            entries.putAll(map);
        }

        @Override
        public boolean remove(String key) {
            return entries.remove(key) != null;
        }

        @Override
        public void removeAll(Set<? extends String> keys) {
            keys.forEach(entries::remove);
        }

        @Override
        public void removeAll() {
            entries.clear();
        }

        @Override
        public void clear() {
            entries.clear();
        }

        @Override
        public Configuration getConfiguration() {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void close() {
            clear();
        }

        @Override
        public <T> T unwrap(Class<T> clazz) {
            if (clazz.isInstance(this)) {
                return clazz.cast(this);
            }
            if (clazz.isInstance(entries)) {
                return clazz.cast(entries);
            }
            throw new IllegalArgumentException("Unsupported unwrap type: " + clazz.getName());
        }

        @Override
        public void registerCacheEventListener(CacheEventListener cacheEventListener) {
            this.cacheEventListener = cacheEventListener;
        }

        @Override
        public CacheStatistics getCacheStatistics() {
            return new CacheStatistics() {
                @Override
                public long getCacheHits() {
                    return 0;
                }

                @Override
                public long getCacheMisses() {
                    return 0;
                }
            };
        }

        @Override
        public Properties getProperties(boolean includeExpensiveDetails) {
            Properties properties = new Properties();
            properties.put("name", name);
            return properties;
        }

        @Override
        public String getDescription() {
            return "TestCache:" + name;
        }

        @Override
        public void attachLoader(CacheLoader cacheLoader) {
            this.cacheLoader = cacheLoader;
        }

        @Override
        public boolean isDistributed() {
            return distributed;
        }
    }
}
