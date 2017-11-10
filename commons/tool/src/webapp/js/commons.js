/* Stuff that we always expect to be setup */
commons.currentUserPermissions = null;
commons.currentPost = null;
commons.currentPosts = [];
commons.currentState = null;
commons.page = 0;
commons.postsTotal = 0;
commons.postsRendered = 0;
commons.urlRegex = /(ftp|http|https):\/\/[^ "]+/;

commons.LOCAL_STORAGE_KEY = 'commons';
commons.AJAX_TIMEOUT = 5000;

Handlebars.registerPartial('comment', Handlebars.partials['comment']);
Handlebars.registerPartial('wrapped_comment', Handlebars.partials['wrapped_comment']);
Handlebars.registerPartial('inplace_comment_editor', Handlebars.partials['inplace_comment_editor']);

commons.states = {
        POSTS: 'posts',
        POST: 'post',
        PERMISSIONS: 'permissions',
        PERMISSIONS_NOT_SET: 'permissions_not_set'
    };

commons.getSelection = function () {
    return (window.getSelection) ? window.getSelection() : document.selection;
};

commons.imageFileExtensions = ['png','jpg','jpeg','gif'];

commons.switchState = function (state, arg) {

    commons.currentState = state;

    $("#commons-post-editor").toggle(commons.currentUserPermissions.postCreate);

    if (commons.states.POSTS === state) {

        var templateData = {
                currentUserId: commons.userId,
                isUserSite: commons.isUserSite
            };

        // renderPageOfPosts uses this. Set it to the start page
        commons.page = 0;
        commons.postsRendered = 0;

        commons.currentPosts = [];

        commons.utils.renderTemplate(commons.states.POSTS, templateData, 'commons-content');

        $(document).ready(function () {
            $('.commons-post-editor').toggle(commons.currentUserPermissions.postCreate);

            var editor = $('#commons-post-creator-editor');
            var wrapAndInsert = function (link, loadThumbnail, text) {
                    var url;
                    var wrapped;
                    if (commons.urlRegex.test(link)) {
                        var matched_url = link.match(commons.urlRegex)[0];
                        var a = document.createElement('a');
                        a.href = matched_url;
                        // We need to add the protocol for the server side code. It needs a valid URL.
                        url = matched_url;
                        if (url.slice(0, a.protocol.length) !== a.protocol) {
                            url = a.protocol + '//' + url;
                        }
                        text = text || matched_url;

                        wrapped = link.replace(matched_url, '<a href=\"' + url + '" target="_blank">' + text + "</a>");
					} else {
						text = text || link;
					    loadThumbnail = false;
						wrapped = text;
					}

                    if (!editor.is(":focus")) editor.focus();

                    if (!document.execCommand('insertHtml', false, wrapped)) {
                        var sel = commons.getSelection();
                        var range = sel.getRangeAt(0);
                        a.innerHTML = text;
                        a.target = '_blank';
                        range.deleteContents();
                        range.insertNode(a);
                    }

                    if (loadThumbnail) {
                       commons.utils.getOGPMarkup(url, function (fragment) {

                            if (fragment) {
                                editor.append(fragment);
                            }
                        });
                    }
                };

            var editorPostButton = $('#commons-editor-post-button');
            var editorCancelButton = $('#commons-editor-cancel-button');
            var editorLinkButton = $('#commons-editor-link-button');
            var editorImageButton = $('#commons-editor-image-button');

            if (commons.isUserSite) {
                editorImageButton.hide();
            }

            editor.click(function (e) {

                if (this.innerHTML == commons.i18n['post_editor_initial_text']) {
                    this.innerHTML = '';
                    $('#commons-editor-post-button').prop('disabled', false);
                    editorPostButton.prop('disabled', false);
                    editorCancelButton.prop('disabled', false);
                }
            }).on('paste', function (e) {

                var cd = e.originalEvent.clipboardData;
                if (!cd) cd = window.clipboardData;
                var pasted = cd.getData('text');
                wrapAndInsert(pasted, true);
                e.preventDefault();
            }).blur(function (e) {

                var sel = commons.getSelection();
                commons.selectedText = (document.selection) ? sel.createRange().htmlText : sel.toString();
                commons.currentRange = sel.getRangeAt(0);
            });

            editorPostButton.click(function (e) {

                commons.utils.savePost('', editor.html(), function (post) {

                        editor.html(commons.i18n['post_editor_initial_text']);
                        editorPostButton.prop('disabled', true);
                        editorCancelButton.prop('disabled', true);
                        fileField.val('');

                        var newPlaceholderId = 'commons-post-' + post.id;

                        $('#commons-posts').prepend(
                            '<div id=\"' + newPlaceholderId + '\" class=\"commons-post\"></div>');
                        commons.utils.addFormattedDateToPost(post);
                        commons.utils.renderPost(post, newPlaceholderId);
                    });
            });

            editorCancelButton.click(function (e) {

                editor.html(commons.i18n['post_editor_initial_text']);
                editorPostButton.prop('disabled', true);
                editorCancelButton.prop('disabled', true);
            });

            var textField = $('#commons-link-dialog-text');

            $('.commons-editor-special-button').click(function (e) {

                if (!editor.is(":focus")) {
                    editor.click();
                }
            });

            editorLinkButton.qtip({
                suppress: false,
                content: { text: $('#commons-link-dialog') },
                style: { classes: 'commons-qtip qtip-shadow' },
                show: {event: 'click', delay: 0},
                hide: {event: 'click', delay: 0},
                events: {
                    show: function (event, api) {
                        textField.val(commons.selectedText);
                    }
                }
            });

            var urlField = $('#commons-link-dialog-url');
            var textField = $('#commons-link-dialog-text');
            var thumbnailCheckbox = $('#commons-link-dialog-load-thumbnail');
            var linkInsertButton = $('#commons-link-dialog-insert-button');

            linkInsertButton.click(function (e) {

                if (commons.currentRange) {
                    commons.getSelection().addRange(commons.currentRange);
                }
                var loadThumbnail = thumbnailCheckbox.prop('checked');
                wrapAndInsert(urlField.val(), loadThumbnail, textField.val());
                urlField.val('');
                textField.val('');
                thumbnailCheckbox.prop('checked', false);
                editorLinkButton.qtip('api').hide();
            });

            $('#commons-link-dialog-cancel-button').click(function (e) {

                urlField.val('');
                textField.val('');
                thumbnailCheckbox.prop('checked', false);
                editorLinkButton.qtip('api').hide();
            });

            urlField.keydown(function (e) {
                linkInsertButton.prop('disabled', false);
            });

            if (!commons.isUserSite) {
                editorImageButton.qtip({
                    suppress: false,
                    content: { text: $('#commons-image-dialog') },
                    style: { classes: 'commons-qtip qtip-shadow' },
                    show: {event: 'click', delay: 0},
                    hide: {event: 'click', delay: 0}
                });
            }

            var fileInsertButton = $('#commons-image-dialog-insert-button');
            var fileField = $('#commons-image-dialog-file');

            fileInsertButton.click(function (e) {

                var file = fileField[0].files[0];
                var extension = file.name.substring(file.name.lastIndexOf('.') + 1);

                if (commons.imageFileExtensions.indexOf(extension) != -1) {
                    var formData = new FormData();
                    formData.append('siteId', commons.siteId);
                    formData.append('imageFile', file);
                    var xhr = new XMLHttpRequest();
                    xhr.open('POST', '/direct/commons/uploadImage', true);
                    xhr.onload = function (e) {
                        editor.append("<div><img src=\"" + xhr.responseText + "\" class=\"commons-image\" /></div>");
                    };
                    xhr.send(formData);
                    editorImageButton.qtip('api').hide();
                }
            });

            fileField.change(function (e) {
                fileInsertButton.prop('disabled', false);
            });

            $('#commons-image-dialog-cancel-button').click(function (e) {

                fileInsertButton.prop('disabled', true);
                fileField.val('');
                editorImageButton.qtip('api').hide();
            });

            if (window.parent === window) {
                commons.utils.renderPageOfPosts();
            } else {
                commons.utils.renderPageOfPosts(true);
                try {
                    if (window.frameElement) {
                        setMainFrameHeight(window.frameElement.id);
                    }
                } catch (err) {
                    // This is likely under an LTI provision scenario.
                    // XSS protection will block this call.
                }
            }
        });
    } else if (commons.states.POST === state) {
        var url = "/direct/commons/post.json?postId=" + arg.postId;
        $.ajax( { url : url, dataType: "json", cache: false, timeout: commons.AJAX_TIMEOUT })
            .done(function (data) {
                commons.utils.addFormattedDateToPost(data);
                commons.utils.renderTemplate('single_post', data, 'commons-content');
                commons.utils.renderPost(data, 'commons-post-' + data.id);
                $(document).ready(function () {

                    $('#commons-view-commons-link').click(function (e) {
                        commons.switchState(commons.states.POSTS, {});
                    });
                });
            }).fail(function (xmlHttpRequest, textStatus, errorThrown) {
            });
    } else if (commons.states.PERMISSIONS === state) {
        $('#commons-toolbar > li > span').removeClass('current');
        $('#commons-permissions-link > span').addClass('current');

        var permissionsCallback = function (perms) {

                commons.utils.renderTemplate('permissions', {'perms': perms}, 'commons-content');

                $(document).ready(function () {
                    $('#commons_permissions_save_button').click(commons.utils.savePermissions);
                });
            };

        commons.utils.getSitePermissionMatrix(permissionsCallback);
    } else if (commons.states.PERMISSIONS_NOT_SET === state) {
        commons.utils.renderTemplate('permissions_not_set', {}, 'commons-content');
    }
};

(function ($) {

    if (!commons.isUserSite && !commons.commonsId) {
        alert('The commonsId MUST be supplied as page parameters');
        return;
    }

    moment.locale(sakai.locale.userLocale);

    var languagesLoaded = function () {

        commons.i18n = portal.i18n.translations['commons'];

        if (commons.embedder === 'SITE') {
            commons.utils.renderTemplate('toolbar', {} ,'commons-toolbar');

            $('#commons-permissions-link>span>a').click(function (e) {
                commons.switchState(commons.states.PERMISSIONS);
            });
        }

        var permissionsCallback = function (permissions) {

                commons.currentUserPermissions = new CommonsPermissions(permissions);

                if (commons.currentUserPermissions == null) {
                    return;
                }

                if (commons.isUserSite) {
                    $("#commons-toolbar").hide();
                } else {
                    $("#commons-toolbar").toggle(commons.currentUserPermissions.updateSite);
                }

                if (commons.currentUserPermissions.postReadAny || commons.currentUserPermissions.postCreate) {
                    if (commons.postId !== '') {
                        commons.switchState(commons.states.POST, {postId: commons.postId});
                    } else {
                        commons.switchState(commons.states.POSTS, {});
                    }
                } else {
                    commons.switchState(commons.states.PERMISSIONS_NOT_SET, {});
                }
            };

        commons.utils.getCurrentUserPermissions(permissionsCallback);
    };

    $(document).ready(function () {

        portal.i18n.loadProperties({
            resourceClass: 'org.sakaiproject.commons.api.CommonsManager',
            resourceBundle: 'org.sakaiproject.commons.impl.ui',
            namespace: 'commons',
            callback: function () { languagesLoaded(); }
        });
    });

    if (CKEDITOR) {
        CKEDITOR.disableAutoInline = true;
    }

    commons.scrollable = $(window.frameElement ? window.frameElement.ownerDocument.defaultView : window);
    commons.doc = $(window.frameElement ? window.frameElement.ownerDocument : document);

}) (jQuery);

