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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Commons;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.commons.api.CommonsConstants;
import org.sakaiproject.commons.api.PersistenceManager;
import org.sakaiproject.commons.api.QueryBean;
import org.sakaiproject.commons.api.SakaiProxy;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
@Getter @Setter @Slf4j
public class PersistenceManagerImpl implements PersistenceManager {

    private static final String POST_SELECT
        = "SELECT cp.*,cw.ID as COMMONS_ID,cw.SITE_ID,cw.EMBEDDER FROM COMMONS_POST as cp, COMMONS_COMMONS as cw, COMMONS_COMMONS_POST as cwp "
            + "WHERE cp.ID = ? AND cp.ID = cwp.POST_ID and cwp.COMMONS_ID = cw.ID";
    private static final String COMMONS_SELECT = "SELECT * FROM COMMONS_COMMONS WHERE ID = ?";
    private static final String COMMONS_POSTS_SELECT
        = "SELECT cw.ID as COMMONS_ID,cw.SITE_ID,cw.EMBEDDER,cp.* FROM COMMONS_COMMONS as cw,COMMONS_COMMONS_POST as cwp,COMMONS_POST as cp "
            + "WHERE cw.ID = ? AND cwp.COMMONS_ID = cw.ID AND cp.ID = cwp.POST_ID ORDER BY CREATED_DATE DESC";
    private static final String SOCIAL_COMMONS_POSTS_SELECT
        = "SELECT cw.ID as COMMONS_ID,cw.SITE_ID,cw.EMBEDDER,cp.* FROM COMMONS_COMMONS as cw,COMMONS_COMMONS_POST as cwp,COMMONS_POST as cp "
            + "WHERE cw.ID = ? AND cwp.COMMONS_ID = cw.ID AND cp.ID = cwp.POST_ID AND CREATOR_ID IN (";
    private static final String COMMONS_POST_INSERT = "INSERT INTO COMMONS_COMMONS_POST VALUES(?,?)";
    private static final String COMMONS_INSERT = "INSERT INTO COMMONS_COMMONS VALUES(?,?,?)";
    private static final String COMMENT_SELECT = "SELECT * FROM COMMONS_COMMENT WHERE ID = ?";
    private static final String COMMENTS_SELECT = "SELECT * FROM COMMONS_COMMENT WHERE POST_ID = ? ORDER BY CREATED_DATE ASC";
    private static final String COMMENT_INSERT = "INSERT INTO COMMONS_COMMENT VALUES(?,?,?,?,?,?)";
    private static final String COMMENT_UPDATE = "UPDATE COMMONS_COMMENT SET CONTENT = ?, MODIFIED_DATE = ? WHERE ID = ?";
    private static final String COMMENT_DELETE = "DELETE FROM COMMONS_COMMENT WHERE ID = ?";
    private static final String POST_UPDATE = "UPDATE COMMONS_POST SET CONTENT = ?, MODIFIED_DATE = ?, RELEASE_DATE = ? WHERE ID = ?";
    private static final String POST_INSERT = "INSERT INTO COMMONS_POST VALUES (?,?,?,?,?,?)";
    private static final String POST_DELETE = "DELETE FROM COMMONS_POST WHERE ID = ?";
    private static final String COMMONS_POST_DELETE = "DELETE FROM COMMONS_COMMONS_POST WHERE POST_ID = ?";
    private static final String COMMENTS_DELETE = "DELETE FROM COMMONS_COMMENT WHERE POST_ID = ?";

    private SakaiProxy sakaiProxy;
    private ServerConfigurationService serverConfigurationService;
    private SqlService sqlService;

    public void init() {

        if (serverConfigurationService.getBoolean("auto.ddl", true)) {
            sqlService.ddl(this.getClass().getClassLoader(), "commons_tables");
        }
    }

    public boolean postExists(String postId) {

        log.debug("postExists({})", postId);

        List<Post> posts = sqlService.dbRead(POST_SELECT
                , new Object[] {postId}
                , new SqlReader<Post>() {
                    public Post readSqlResultRecord(ResultSet result) {
                        return new Post();
                    }
                });

        return posts.size() > 0;
    }

    public List<Post> getAllPost(final QueryBean query) throws Exception {
        return getAllPost(query, false);
    }

    public List<Post> getAllPost(final QueryBean query, boolean populate) throws Exception {

        log.debug("getAllPost({})", query);

        if (query.embedder.equals(CommonsConstants.SOCIAL)) {
            int numFromIds = query.fromIds.size();
            if (numFromIds > 0) {
                String sql = SOCIAL_COMMONS_POSTS_SELECT;
                for (int i = 0;i < numFromIds;i++) sql += "?,";
                // Trim off the trailing comma
                sql = sql.substring(0,sql.length() - 1);
                sql += ") ORDER BY CREATED_DATE DESC"; 
                List<String> params = new ArrayList<String>();
                params.add(query.commonsId);
                params.addAll(query.fromIds);
                return sqlService.dbRead(sql
                        , params.toArray()
                        , new SqlReader<Post>() {
                            public Post readSqlResultRecord(ResultSet result) {
                                return loadPostFromResult(result, populate);
                            }
                        });
            } else {
                log.warn("SOCIAL posts requested, but no connection ids supplies. Returning an empty list ...");
                return new ArrayList<Post>();
            }
        } else {
            return sqlService.dbRead(COMMONS_POSTS_SELECT
                    , new Object[] {query.commonsId}
                    , new SqlReader<Post>() {
                        public Post readSqlResultRecord(ResultSet result) {
                            return loadPostFromResult(result, populate);
                        }
                    });
        }
    }

    public Comment getComment(String commentId) {

        List<Comment> comments = sqlService.dbRead(COMMENT_SELECT, new Object[] { commentId }, new SqlReader<Comment>() {
                public Comment readSqlResultRecord(ResultSet result) {
                    try {
                        return new Comment(result);
                    } catch (SQLException sqle) {
                        log.error("Failed to get comment", sqle);
                        return null;
                    }
                }
            });

        Comment comment = comments.get(0);
        comment.setCreatorDisplayName(sakaiProxy.getDisplayNameForTheUser(comment.getCreatorId()));
        return comment;
    }

    public Comment saveComment(Comment comment) {

        if ("".equals(comment.getId())) {
            comment.setId(UUID.randomUUID().toString());
            sqlService.dbWrite(COMMENT_INSERT
                , new Object[] { comment.getId()
                                    , comment.getPostId()
                                    , comment.getContent()
                                    , comment.getCreatorId()
                                    , new Timestamp(comment.getCreatedDate())
                                    , new Timestamp(comment.getModifiedDate()) });
        } else {
            sqlService.dbWrite(COMMENT_UPDATE
                , new Object[] { comment.getContent()
                                    , new Timestamp(comment.getModifiedDate())
                                    , comment.getId() });
        }

        return getComment(comment.getId());
    }

    public boolean deleteComment(String commentId) {

        sqlService.dbWrite(COMMENT_DELETE, new Object[] { commentId });
        return true;
    }

    public Post savePost(Post post) {

        log.debug("savePost()");

        if (postExists(post.getId())) {
            sqlService.dbWrite(POST_UPDATE
                , new Object[] { post.getContent()
                                    , new Timestamp(new Date().getTime())
                                    , new Timestamp(post.getReleaseDate())
                                    , post.getId() });

        } else {
            Runnable transaction = new Runnable() {

                public void run() {

                    // Test if the commons exists.
                    if (getCommons(post.getCommonsId()) == null) {
                        // Commons doesn't exist yet. Create it.
                        String embedder = post.getEmbedder();
                        String siteId = (embedder.equals(CommonsConstants.SOCIAL)) ? CommonsConstants.SOCIAL : post.getSiteId();
                        sqlService.dbWrite(COMMONS_INSERT
                            , new Object [] { post.getCommonsId(), siteId, embedder });
                    }

                    post.setId(UUID.randomUUID().toString());
                    sqlService.dbWrite(POST_INSERT
                        , new Object [] { post.getId()
                                            , post.getContent()
                                            , post.getCreatorId()
                                            , new Timestamp(post.getCreatedDate())
                                            , new Timestamp(post.getModifiedDate())
                                            , new Timestamp(post.getReleaseDate())});
                    sqlService.dbWrite(COMMONS_POST_INSERT
                        , new Object [] { post.getCommonsId(), post.getId() });
                }
            };
            sqlService.transact(transaction, "COMMONS_POST_CREATION_TRANSACTION");
        }

        return getPost(post.getId(), false);
    }

    public boolean deletePost(Post post) {

        log.debug("deletePost({})", post.getId());

        Runnable transaction = new Runnable() {

            public void run() {

                Object[] params = new Object [] { post.getId() };
                sqlService.dbWrite(COMMENTS_DELETE, params);
                sqlService.dbWrite(COMMONS_POST_DELETE, params);
                sqlService.dbWrite(POST_DELETE, params);
            }
        };

        return sqlService.transact(transaction, "COMMONS_POST_DELETION_TRANSACTION");
    }

    public Post getPost(String postId, boolean loadComments) {

        List<Post> posts = sqlService.dbRead(POST_SELECT, new Object[] { postId }, new SqlReader<Post>() {
                public Post readSqlResultRecord(ResultSet result) {
                    return loadPostFromResult(result, loadComments);
                }
            });

        if (posts.size() < 1) {
            return null;
        } else {
            return posts.get(0);
        }
    }

    public Commons getCommons(String commonsId) {

        List<Commons> commons = sqlService.dbRead(COMMONS_SELECT
            , new Object[] {commonsId}
            , new SqlReader<Commons>() {
                public Commons readSqlResultRecord(ResultSet result) {
                    try {
                        return new Commons(result);
                    } catch (SQLException sqle) {
                        return null;
                    }
                }
            });

        if (commons.size() > 0) {
            return commons.get(0);
        } else {
            log.warn("No commons for id '" + commonsId + "'. Returning null ...");
            return null;
        }
    }

    private Post loadPostFromResult(ResultSet result, boolean loadComments) {

        try {
            Post post = new Post(result);
            post.setCreatorDisplayName(
                    sakaiProxy.getDisplayNameForTheUser(post.getCreatorId()));
            if (loadComments) {
                List<Comment> comments = sqlService.dbRead(COMMENTS_SELECT
                        , new Object[] {post.getId()}
                        , new SqlReader<Comment>() {
                            public Comment readSqlResultRecord(ResultSet commentResult) {

                                try {
                                    Comment comment = new Comment(commentResult);
                                    comment.setCreatorDisplayName(
                                            sakaiProxy.getDisplayNameForTheUser(comment.getCreatorId()));
                                    String toolId = sakaiProxy.getCommonsToolId(post.getSiteId());
                                    String url = sakaiProxy.getPortalUrl() + "/directtool/"
                                                            + toolId + "?state=post&postId=" + post.getId();
                                    comment.setUrl(url);
                                    return comment;
                                } catch (SQLException sqle) {
                                    log.error("Failed to read comment from DB.", sqle);
                                    return null;
                                }
                            }
                        });
                post.setComments(comments);
            }
            return post;
        } catch (SQLException sqle) {
            log.error("Failed to read post from DB.", sqle);
            return null;
        }
    }
}
