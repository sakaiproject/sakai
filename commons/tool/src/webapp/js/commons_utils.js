commons.utils = {

    POST_WITHOUT_COMMENTS_STYLE: 'commons-post-without-comments',
    POST_WITH_COMMENTS_STYLE: 'commons-post-with-comments',
    OGP_IMAGE_REGEX: /og:image" content=["']([^"]*)["']\s*\//,
    TWITTER_IMAGE_REGEX: /twitter:image" content="([^"]*)"/,
    OGP_TITLE_REGEX: /og:title" content="([^"]*)"/,
    OGP_DESCRIPTION_REGEX: /og:description" content="([^"&#]*)"/,
    OGP_SITE_NAME_REGEX: /og:site_name" content="([^"]*)"/,
    AUTHOR_REGEX: /author" content="([^"]*)"/,

    fromHtml: function (html) {
        return html.replace(/<br>/g, '\n');
    },
    toHtml: function (text) {
        return text.replace(/\r\n|\n|\r/g, '<br>');
    },
    getOGPMarkup: function (url, callback) {

        const self = this;

        $.get(`/direct/commons/getUrlMarkup?url=${url}`, function (markup) {

            const div = document.createElement('div');

            let title = '';
            let matches = markup.match(self.OGP_TITLE_REGEX);
            if (matches && matches.length == 2) {
                title = matches[1];
                div.innerHTML = title;
                title = $(div).html();
            }

            let image = '';
            matches = markup.match(self.TWITTER_IMAGE_REGEX);
            if (matches && matches.length == 2) {
                image = matches[1];
            } else {
                matches = markup.match(self.OGP_IMAGE_REGEX);
                if (matches && matches.length == 2) {
                    image = matches[1];
                }
            }

            let description = '';
            matches = markup.match(self.OGP_DESCRIPTION_REGEX);
            if (matches && matches.length == 2) {
                description = matches[1];
                div.innerHTML = description;
                description = $(div).html();
            }

            const a = document.createElement('a');
            a.href = url;
            let siteName = a.hostname.toUpperCase();
            matches = markup.match(self.AUTHOR_REGEX);
            if (matches && matches.length == 2) {
                 siteName += ` | ${commons.i18n['by']} ${matches[1].toUpperCase()}`;
            }

            if (!title && !image) {
                callback(null);
            } else {
                callback(Handlebars.templates['og_fragment']({title: title,
                                                                image: image,
                                                                url: url,
                                                                description: description,
                                                                siteName: siteName}, {helpers: commonsHelpers}));
            }
        });
    },
    addHandlersToComment: function (comment) {
        $(`#commons-comment-edit-link-${comment.id}`).click(commons.utils.editCommentHandler);
        $(`#commons-comment-delete-link-${comment.id}`).click(commons.utils.deleteCommentHandler);
        $(`#commons-like-link-${comment.id}`).click(commons.utils.likePostHandler);
    },
    editPostHandler: function (e) {
        const postId = this.dataset.postId;
        const contentDiv = $(`#commons-post-content-${postId}`);
        commons.utils.oldContent = contentDiv.html();
        contentDiv.prop('contenteditable', true).focus();
        const postEditButtons = $(`#commons-post-edit-buttons-${postId}`);
        postEditButtons.show();

        $(document).ready(function () {
            $(`#commons-inplace-post-editor-post-button-${postId}`).off('click').click(function (e) {
                commons.utils.savePost(postId, contentDiv.html(), function () {
                    contentDiv.prop('contenteditable', false);
                    $(`#commons-post-options-${postId}`).show();
                    postEditButtons.hide();
                });
            });
        });
        $(`#commons-post-options-${postId}`).hide();
    },
    deletePostHandler: function (e) {
        const postId = this.dataset.postId;
        commons.utils.deletePost(postId, function () {
            $(`#commons-post-${postId}`).remove();
        });
    },
    cancelPostEdit: function (postId) {
        const contentDiv = $(`#commons-post-content-${postId}`);
        contentDiv.html(this.oldContent);
        contentDiv.prop('contenteditable', false);
        $(`#commons-post-options-${postId}`).show();
        $(`#commons-post-edit-buttons-${postId}`).hide();
    },
    editCommentHandler: function (e) {
        // Get the comment id from the link
        const commentId = this.dataset.commentId;

        const container = $(`#commons-comment-${commentId}`);
        const contentSpan = $(`#commons-comment-content-${commentId}`);
        const postId = container.data('post-id');

        // Comment metadata is attached to the container
        const comment = {
            id: commentId,
            postId: postId,
            creatorId: container.data('creator-id'),
            creatorDisplayName: container.data('creator-display-name'),
            createdDate: container.data('created-date'),
            modifiedDate: container.data('modified-date'),
            content: contentSpan.html(),
        };

        commons.utils.addPermissionsToComment(comment);

        commons.utils.commentBeingEdited = comment;
        commons.utils.renderTemplate('inplace_comment_editor', comment, `commons-comment-${commentId}`);

        $(document).ready(function () {
            const textarea = $(`#commons-comment-textarea-${comment.id}`);
            textarea.val(commons.utils.fromHtml(comment.content));
            textarea.each(function () { autosize(this); }).focus();

            $(`#commons-inplace-comment-editor-cancel-button-${comment.id}`).click(function (e) {
                commons.utils.renderTemplate(
                    'comment', commons.utils.commentBeingEdited, `commons-comment-${commentId}`);
                $(`#commons-comment-edit-link-${commentId}`).click(commons.utils.editCommentHandler);
            });

            $(`#commons-inplace-comment-editor-post-button-${commentId}`).click(function (e) {
                commons.utils.saveComment(commentId, postId, textarea.val(), function (savedComment) {
                    textarea.val('');
                    $(`#commons-comments-${postId}`).show();
                    commons.utils.addPermissionsToComment(savedComment);
                    commons.utils.renderTemplate('comment', savedComment, `commons-comment-${savedComment.id}`);
                    commons.utils.addHandlersToComment(savedComment);
                    commons.utils.addLikeCount(document.getElementById(`commons-likes-count-${commentId}`));   //re-render likesNumber after edit
                    commons.utils.getUserLikes();   //re-render Like styling for the page
                });
            });
        });
    },
    deleteCommentHandler: function (e) {

        if (!confirm(commons.i18n['delete_comment_message'])) {
            return false;
        }

        var commentId = this.dataset.commentId;
        var postId = this.dataset.postId;
        var numComments = $('#commons-comments-' + postId + ' .commons-comment').length;
        if (numComments <= 4) {
            $('#commons-hide-comments-link-' + postId).hide();
            $('#commons-show-comments-link-' + postId).hide();
        }
        var commentToDelete = $('#commons-comment-' + commentId);

        commons.utils.deleteComment(postId, commentId, function () {

                commentToDelete.remove();
                var comments = $('#commons-comments-' + postId + ' .commons-comment');
                numComments = comments.length;
                if (numComments > 3) {
                    comments.slice(numComments - 4, numComments - 1)
                        .removeClass('comments-comment-not-recent')
                        .addClass('commons-comment-recent');
                }
            });
    },
    likePostHandler: function(){
        var postId = this.dataset.postId;
        
        // Call the likePost function to update the server
        commons.utils.likePost(postId);
        
        // Use jQuery for more consistent DOM manipulation
        var $likeButton = $('#commons-like-link-' + postId);
        var $likeCount = $('#commons-likes-count-number-' + postId);
        var $likeIcon = $('#commons-like-icon-' + postId);
        var likeNumber = parseInt($likeCount.attr('data-count'));
        
        // Update the UI
        $likeIcon.removeClass('si-like si-liked');
        
        // Check if already liked and toggle state
        if ($likeButton.hasClass('likedAlready')) {
            // Unliking
            likeNumber = likeNumber - 1;
            $likeButton.removeClass('likedAlready');
            $likeIcon.addClass('si-like');
            
            // Update the cache
            if (commons.userLikes) {
                commons.userLikes = commons.userLikes.filter(function(like) {
                    return like.postId !== postId;
                });
            }
        } else {
            // Liking
            likeNumber = likeNumber + 1;
            $likeButton.addClass('likedAlready');
            $likeIcon.addClass('si-liked');
            
            // Update the cache
            if (commons.userLikes) {
                commons.userLikes.push({ postId: postId });
            }
        }
        
        // Update the like count
        $likeCount.text(likeNumber.toString());
        $likeCount.attr('data-count', likeNumber.toString());
        
        // Show/hide singular/plural text based on count
        if (likeNumber === 1) {
            $('#commons-likes-person-' + postId).show();
            $('#commons-likes-people-' + postId).hide();
        } else if (likeNumber === 0) {
            $('#commons-likes-person-' + postId).hide();
            $('#commons-likes-people-' + postId).hide();
        } else {
            $('#commons-likes-person-' + postId).hide();
            $('#commons-likes-people-' + postId).show();
        }
    },
    cancelCommentEdit: function (commentId) {

        commons.utils.renderTemplate('comment', commons.utils.commentBeingEdited, `commons-comment-${commentId}`);
        $(`#commons-comment-edit-link-${commentId}`).click(commons.utils.editCommentHandler);
    },
    getCurrentUserPermissions: function (callback) {

        $.ajax( {
            url: '/direct/commons/userPerms.json?siteId=' + commons.siteId + '&embedder=' + commons.embedder,
            dataType: 'json',
            cache: false,
            timeout: commons.AJAX_TIMEOUT
        }).done(function (json) {
            callback(json.data);
        }).fail(function (xmlHttpRequest, textStatus, error) {
            alert('Failed to get the current user permissions. Status: ' + textStatus + '. Error: ' + error);
        });
    },
    formatDate: function (millis) {

        if (millis <= 0) {
            return commons.i18n['none'];
        } else {
            var m = moment(millis);
            return m.format('L LT');
        }
    },
    addFormattedDatesToPosts: function (posts) {

        posts.forEach(function (p) {
            commons.utils.addFormattedDateToPost(p);
        });
    },
    addFormattedDateToPost: function (post) {

        post.formattedCreatedDate = this.formatDate(post.createdDate);
        post.formattedReleaseDate = this.formatDate(post.releaseDate);
        post.formattedModifiedDate = this.formatDate(post.modifiedDate);

        post.comments.forEach(function (c, index) {

            if (index < (post.comments.length - 3)) {
                c.orderClass = 'commons-comment-not-recent';
            } else {
                c.orderClass = 'commons-comment-recent';
                c.isRecent = true;
            }
            c.formattedCreatedDate = commons.utils.formatDate(c.createdDate);
            c.formattedModifiedDate = commons.utils.formatDate(c.modifiedDate);
        });
    },
    addFormattedDatesToCurrentPost: function () {
        this.addFormattedDateToPost(commons.currentPost);
    },
    savePost: function (postId, content, callback) {

        var success = false;
        var priority = document.getElementById('commons-editor-priority').checked;
        document.getElementById('commons-editor-priority').checked = false; //uncheck the box by default, we have what we need from it already.
        if (!postId) postId = '';

        if ('' == content) {
            alert(commons.i18n['no_content_warning']);
            return 0;
        }

        var post = {
                'id': postId,
                'content': content,
                'commonsId': commons.commonsId,
                'siteId': commons.siteId,
                'embedder': commons.embedder,
                'priority': priority
            };

        $.ajax({
            url: '/direct/commons/savePost.json',
            type: 'POST',
            data: post,
            timeout: commons.AJAX_TIMEOUT
        }).done(function (data) {
            callback(data);
        }).fail(function(xmlHttpRequest, textStatus, error) {
                alert("Failed to store post. Status: " + textStatus + '. Error: ' + error);
        });
    },
    saveComment: function (commentId, postId, content, callback) {

        content = commons.utils.toHtml(content);

        var comment = {
                'id': commentId,
                'postId': postId,
                'content': content,
                'commonsId': commons.commonsId,
                'siteId': commons.siteId,
                'embedder': commons.embedder
            };

        $.ajax( {
            url: "/direct/commons/saveComment.json",
            type: 'POST',
            data: comment,
            timeout: commons.AJAX_TIMEOUT
        }).done(function (comment) {
            callback(comment);
        }).fail(function (xmlHttpRequest, textStatus, error) {
            alert("Failed to save comment. Status: " + textStatus + '. Error: ' + error);
        });

        return false;
    },
    likePost: function(postId){
        var url = "/direct/commons/likePost?&postId=" + postId;
        $.ajax({
            url: url,
            type: 'POST',
            data: postId,
            timeout: commons.AJAX_TIMEOUT
        }).done(function () {
            // Update the names of likers for this post
            commons.utils.getPostLikerNames(postId);
        });
    },
    deleteComment: function (postId, commentId, callback) {

        var commentCreatorId = document.getElementById('commons-comment-' + commentId).dataset.creatorId;
        var postCreatorId = document.getElementById('commons-post-' + postId).dataset.creatorId;

        var url = '/direct/commons/deleteComment?siteId=' + commons.siteId + '&commonsId=' + commons.commonsId
                        + '&postId=' + postId + '&embedder=' + commons.embedder + '&commentId=' + commentId 
                        + '&commentCreatorId=' + commentCreatorId + '&postCreatorId=' + postCreatorId;

        $.ajax( { url: url, timeout: commons.AJAX_TIMEOUT })
        .done(function (text, status) {
            callback();
        }).fail(function (xmlHttpRequest, textStatus, error) {
            alert("Failed to delete comment. Status: " + textStatus + ". Error: " + error);
        });

        return false;
    },
    deletePost: function (postId, callback) {
                        
        if (!confirm(commons.i18n['delete_post_message'])) {
            return false;
        }

        $.ajax({
            url: '/direct/commons/deletePost?postId=' + postId + '&siteId=' + commons.siteId + '&commonsId=' + commons.commonsId,
            timeout: commons.AJAX_TIMEOUT
        }).done(function (text, status) {
            callback();
        }).fail(function (xmlHttpRequest, textStatus, error) {
            alert("Failed to delete post. Status: " + textStatus + ". Error: " + error);
        });

        return false;
    },
    addPermissionsToPost: function (p) {

        p.currentUserId = commons.userId;

        p.canComment = commons.currentUserPermissions.commentCreate || commons.currentUserPermissions.commentUpdateAny;
        p.canDelete = commons.currentUserPermissions.postDeleteAny
                        || (commons.currentUserPermissions.postDeleteOwn && p.creatorId === commons.userId);
        p.canEdit = commons.currentUserPermissions.postUpdateAny
                        || (commons.currentUserPermissions.postUpdateOwn && p.creatorId === commons.userId);
        p.isModified = p.modifiedDate > p.releaseDate;

        p.comments.forEach(function (c) { commons.utils.addPermissionsToComment(c); });
    },
    addPermissionsToComment: function (c) {

        var postCreatorId = document.getElementById('commons-post-' + c.postId).dataset.creatorId;

        c.canComment = commons.currentUserPermissions.commentCreate || commons.currentUserPermissions.commentUpdateAny;
        c.modified = c.modifiedDate > c.createdDate;
        c.canDelete = commons.currentUserPermissions.commentDeleteAny
                        || (commons.currentUserPermissions.commentDeleteOwn && c.creatorId === commons.userId)
                        || (commons.embedder === 'SOCIAL' && postCreatorId === commons.userId);
        c.canEdit = commons.currentUserPermissions.commentUpdateAny
                        || (commons.currentUserPermissions.commentUpdateOwn && c.creatorId === commons.userId);
    },
    renderTemplate: function (name, data, output) {

        var template = Handlebars.templates[name];
        document.getElementById(output).innerHTML = template(data, {helpers: commonsHelpers});
    },
    renderPost: function (post, output) {

        this.addPermissionsToPost(post);
        post.currentUserId = commons.userId;

        this.renderTemplate('post', post, output);

        const self = this;

        // Set up event listeners
        $('#commons-post-edit-link-' + post.id).click(self.editPostHandler);
        $('#commons-post-delete-link-' + post.id).click(self.deletePostHandler);
        $('#commons-like-link-' + post.id).click(self.likePostHandler);
        
        // Use the numberOfLikes property from the post object
        const likesCountElement = $('#commons-likes-count-number-' + post.id);
        if (likesCountElement.length > 0) {
            const count = post.numberOfLikes || 0;
            likesCountElement.text(count.toString());
            likesCountElement.attr('data-count', count.toString());
            
            // Show singular or plural text based on count
            if (count === 1) {
                $('#commons-likes-person-' + post.id).show();
                $('#commons-likes-people-' + post.id).hide();
            } else {
                $('#commons-likes-person-' + post.id).hide();
                $('#commons-likes-people-' + post.id).show();
            }
        }
        
        bootstrap.Popover.getOrCreateInstance(document.body); // Initializes all popovers
        
        const textarea = $('#commons-comment-textarea-' + post.id);
        textarea.each(function() { autosize(this); });
        
        const creator = $('#commons-comment-creator-' + post.id);
        const commentLink = $('#commons-create-comment-link-' + post.id);
        commentLink.click(function(e) {
            e.preventDefault();
            creator.show();
            textarea.focus();
        });

        const cancelButton = $('#commons-inplace-comment-editor-cancel-button-' + post.id);
        cancelButton.click(function(e) {
            e.preventDefault();
            creator.hide();
        });

        const showCommentsLink = $('#commons-show-comments-link-' + post.id);
        const hideCommentsLink = $('#commons-hide-comments-link-' + post.id);
        
        showCommentsLink.click(function(e) {
            e.preventDefault();
            $('#commons-comments-' + post.id + ' .commons-comment-not-recent').show();
            showCommentsLink.hide();
            hideCommentsLink.show();
        });

        hideCommentsLink.click(function(e) {
            e.preventDefault();
            $('#commons-comments-' + post.id + ' .commons-comment-not-recent').hide();
            hideCommentsLink.hide();
            showCommentsLink.show();
        });

        const postButton = $('#commons-inplace-comment-editor-post-button-' + post.id);
        if (postButton) {
            postButton.click(function(e) {
                e.preventDefault();
                
                const text = textarea.val().trim();
                
                if (text.length === 0) {
                    alert(commons.i18n['no_content_warning']);
                    return;
                }
                
                commons.utils.saveComment('', post.id, text, function(savedComment) {
                    textarea.val('');
                    creator.hide();
                    
                    const commentsContainer = $('#commons-comments-' + post.id);
                    const numComments = commentsContainer.find('.commons-comment').length;
                    
                    if (numComments >= 3) {
                        const recentComments = commentsContainer.find('.commons-comment-recent');
                        recentComments.removeClass('commons-comment-recent');
                        
                        if (recentComments.length > 0) {
                            const earliestRecentComment = $(recentComments[0]);
                            earliestRecentComment.addClass('commons-comment-not-recent');
                            
                            if (numComments === 3 || showCommentsLink.is(':visible')) {
                                earliestRecentComment.hide();
                            }
                            
                            if (recentComments.length > 1) $(recentComments[1]).addClass('commons-comment-recent');
                            if (recentComments.length > 2) $(recentComments[2]).addClass('commons-comment-recent');
                            
                            if (numComments === 3 || showCommentsLink.is(':visible')) {
                                showCommentsLink.show();
                            }
                        }
                    }
                    
                    commons.utils.addPermissionsToComment(savedComment);
                    savedComment.formattedCreatedDate = commons.utils.formatDate(savedComment.createdDate);
                    savedComment.orderClass = 'commons-comment-recent';
                    savedComment.isRecent = true;
                    
                    const wrappedComment = Handlebars.templates['wrapped_comment'](savedComment, {helpers: commonsHelpers});
                    $('#commons-comments-container-' + post.id).append(wrappedComment);
                    
                    self.addHandlersToComment(savedComment);
                });
            });
        }

        if (post.comments.length <= 3) {
            showCommentsLink.hide();
        }
        
        if (post.priority === true) {
            $('#commons-post-inner-container-' + post.id).addClass('alert-info');
            $('#commons-high-priority-' + post.id).show();
        }
        
        post.comments.forEach(function(c) { self.addHandlersToComment(c); });
    },
    renderPageOfPosts: function (all) {
        const self = this;
        
        console.debug('renderPageOfPosts called with all =', all, 'page =', commons.page);
        
        // If we're already loading posts, don't make another request
        if (commons.postsLoading) {
            console.debug('Already loading posts, ignoring request');
            return;
        }
        
        // Set loading state
        commons.postsLoading = true;
        console.debug('Setting postsLoading to true');
        
        // Remove any existing load more button
        $('#commons-load-more').remove();
        
        // Remove any existing "no more posts" message
        $('.commons-no-more-posts').remove();
        
        const url = `/direct/commons/posts/${commons.commonsId}.json?siteId=${commons.siteId}&embedder=${commons.embedder}&page=${(all) ? '-1' : commons.page}`;
        console.debug('Fetching posts from URL:', url);

        $.ajax({
            url,
            dataType: 'json',
            cache: false,
            timeout: commons.AJAX_TIMEOUT
        })
        .done(function(data) {
            console.debug('AJAX request successful, data:', data);
            
            // Reset loading state
            commons.postsLoading = false;
            console.debug('Setting postsLoading to false');
            
            commons.postsTotal = data.postsTotal;
            const posts = data.posts || [];
            
            // If no posts were found and this is the first page, show a message
            if ((posts.length === 0 && commons.page === 0) || (data.status === 'END' && commons.postsTotal === 0)) {
                // Remove any existing "no posts" message to avoid duplicates
                $('.commons-no-posts').remove();
                $('#commons-posts').append(`<div class="commons-no-posts">${commons.i18n['no_posts_yet']}</div>`);
                return;
            }

            if (data.status === 'END') {
                console.debug('Received END status, no more posts to load');
            }
            
            commons.currentPosts = commons.currentPosts.concat(posts);

            if (commons.page == 0 && data.postsTotal > 0) {
                $('#commons-body-toggle').show();
            }

            commons.postsRendered += posts.length;

            commons.utils.addFormattedDatesToPosts(posts);

            // Add the next batch of placeholders to the post list
            const template = Handlebars.templates['posts_placeholders'];
            $('#commons-posts').append(template({ posts }, {helpers: commonsHelpers}));

            // Now render them into their placeholders
            posts.forEach(p => commons.utils.renderPost(p, `commons-post-${p.id}`));
            
            // Apply user likes once for all posts
            commons.utils.getUserLikes();

            try {
                if (window.frameElement) {
                    setMainFrameHeight(window.frameElement.id);
                }
            } catch (err) {
                // This is likely under an LTI provision scenario.
                // XSS protection will block this call.
            }
            
            commons.page += 1;
            console.debug('Incremented page counter to', commons.page);

            // Add a "Load More" button if this is not the first page and we have more posts to load
            if (commons.page > 0 && data.status !== 'END') {
                // Remove any existing load more button
                $('#commons-load-more').remove();
                
                // Add a new load more button
                $('#commons-posts').append(
                    '<div id="commons-load-more" class="commons-load-more">' +
                    '<button class="btn btn-primary">' + commons.i18n['load_more'] + '</button>' +
                    '</div>'
                );
                
                // Add click handler for the load more button
                $('#commons-load-more button').click(function() {
                    console.debug('Load more button clicked');
                    $(this).prop('disabled', true).text(commons.i18n['loading_posts']);
                    commons.utils.renderPageOfPosts();
                });
            } else {
                console.debug('No more posts to load, not adding "Load More" button');
            }
        }).fail(function(xhr, textStatus, errorThrown) {
            // Reset loading state
            commons.postsLoading = false;
            
            console.error("Failed to get posts:", xhr, textStatus, errorThrown);
            $('#commons-posts').append('<div class="commons-error">' + commons.i18n['error_loading_posts'] + '</div>');
            
            // Don't increment page counter on failure
            // Add a "Load More" button to allow retry
            console.debug('Adding "Load More" button for retry');
            
            // Add a new load more button
            $('#commons-posts').append(
                '<div id="commons-load-more" class="commons-load-more">' +
                '<button class="btn btn-primary">' + commons.i18n['load_more'] + '</button>' +
                '</div>'
            );
            
            // Add click handler for the load more button
            $('#commons-load-more button').click(function() {
                console.debug('Retry button clicked');
                $(this).prop('disabled', true).text(commons.i18n['loading_posts']);
                commons.utils.renderPageOfPosts();
            });
        });
    },
    placeCaretAtEnd: function (el) {

        el.focus();
        if (typeof window.getSelection != "undefined"
                && typeof document.createRange != "undefined") {
            var range = document.createRange();
            range.selectNodeContents(el);
            range.collapse(false);
            var sel = window.getSelection();
            sel.removeAllRanges();
            sel.addRange(range);
        } else if (typeof document.body.createTextRange != "undefined") {
            var textRange = document.body.createTextRange();
            textRange.moveToElementText(el);
            textRange.collapse(false);
            textRange.select();
        }
    },
    addLikeCount: function(likesLink){
        var postId = likesLink.getAttribute('id').substring(20);
        var url = "/direct/commons/countLikes.json?&postId=" + postId;
        $.ajax({
            url: url,
            type: 'GET',
            data: postId,
            cache: false,
            timeout: commons.AJAX_TIMEOUT,
            success: function(result){
                var count = parseInt(result.data);
                if(count === 1){
                    document.getElementById('commons-likes-person-' + postId).removeAttribute('style');
                    document.getElementById('commons-likes-people-' + postId).setAttribute('style','display:none;');
                }
                document.getElementById('commons-likes-count-number-' + postId).textContent = count.toString();
                document.getElementById('commons-likes-count-number-' + postId).setAttribute('data-count', count.toString());
                commons.utils.getPostLikerNames(postId);
            }
        });
    },
    getUserLikes: function(callback){
        // If we already have the likes data cached and it's not a forced refresh, use the cache
        if (commons.userLikes && !callback) {
            this.applyUserLikes(commons.userLikes);
            return;
        }
        
        var url = "/direct/commons/userLikes.json";
        $.ajax({
            url: url,
            type: 'GET',
            cache: false,
            timeout: commons.AJAX_TIMEOUT,
            success: function(result){
                // Cache the likes data
                commons.userLikes = result.commons_collection || [];
                
                // Apply the likes to the UI
                commons.utils.applyUserLikes(commons.userLikes);
                
                // Call the callback if provided
                if (typeof callback === 'function') {
                    callback();
                }
            }
        });
    },
    
    // New function to apply user likes to the UI
    applyUserLikes: function(userLikes) {
        if (!userLikes || !userLikes.length) return;
        
        // First reset all like buttons to the default state
        $('.commons-like-link').removeClass('likedAlready');
        $('.commons-like-icon').removeClass('si-liked').addClass('si-like');
        
        // Then apply the user's likes
        for (var i = 0; i < userLikes.length; i++) {
            var postId = userLikes[i].postId;
            var likeLink = $('#commons-like-link-' + postId);
            var likeIcon = $('#commons-like-icon-' + postId);
            
            if (likeLink.length) {
                likeIcon.removeClass('si-like').addClass('si-liked');
                likeLink.addClass('likedAlready');
            }
        }
    },
    getPostLikerNames: function(postId){
        var url = "/direct/commons/postLikes.json?&postId=" + postId;
        $.ajax({
            url: url,
            type: 'GET',
            data: postId,
            cache: false,
            timeout: commons.AJAX_TIMEOUT,
            success: function(result){
                var likeNow = document.getElementById('commons-likes-count-' + postId);
                var likeNames = ``;
                for(var count=0; count<result.commons_collection.length; count++){
                    likeNames = likeNames + result.commons_collection[count].data + `<br>`;
                }
                likeNow.setAttribute('data-content', likeNames);
            }
        });
    }
};
