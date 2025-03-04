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

const commonsHelpers = {};

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
        $('#commons-toolbar > li > span').removeClass('current');
        $('#commons-main-link > span').addClass('current');

        const templateData = {
                currentUserId: commons.userId,
                isUserSite: commons.isUserSite,
                maxUploadSize: commons.maxUploadSize
            };

        // renderPageOfPosts uses this. Set it to the start page
        commons.page = 0;
        commons.postsRendered = 0;

        commons.currentPosts = [];

        commons.utils.renderTemplate(commons.states.POSTS, templateData, 'commons-content');

        $(document).ready(function () {
            $('.commons-post-editor').toggle(commons.currentUserPermissions.postCreate);

            const editor = $('#commons-post-creator-editor');
            const wrapAndInsert = function (link, loadThumbnail, text) {

                    const clean = DOMPurify.sanitize(link, {USE_PROFILES: {html: true}});
                    if (DOMPurify.removed) {
                        console.warn("DOMPurify removed some dangerous stuff:");
                        console.warn(DOMPurify.removed);
                    }

                    let url;
                    let wrapped;
                    if (commons.urlRegex.test(clean)) {
                        const matched_url = clean.match(commons.urlRegex)[0];
                        const a = document.createElement('a');
                        a.href = matched_url;
                        // We need to add the protocol for the server side code. It needs a valid URL.
                        url = matched_url;
                        if (url.slice(0, a.protocol.length) !== a.protocol) {
                            url = a.protocol + '//' + url;
                        }
                        text = text || matched_url;

                        wrapped = clean.replace(matched_url, `<a href="${url}" target="_blank">${text}</a>`);
					} else {
						text = text || clean;
					    loadThumbnail = false;
						wrapped = text;
					}

                    if (!editor.is(":focus")) editor.focus();

                    if (!document.execCommand('insertHtml', false, wrapped)) {
                        const sel = commons.getSelection();
                        const range = sel.getRangeAt(0);
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

            const editorPostButton = $('#commons-editor-post-button');
            const editorCancelButton = $('#commons-editor-cancel-button');
            const editorLinkButton = $('#commons-editor-link-button');
            const editorImageButton = $('#commons-editor-image-button');

            if (commons.isUserSite) {
                editorImageButton.hide();
            }


            // Clear out HTML and enable buttons
            const enablePostEditor = function (element) {
                if (element.innerHTML == commons.i18n['post_editor_initial_text']) {
                    element.innerHTML = '';
                    editorPostButton.prop('disabled', false);
                    editorCancelButton.prop('disabled', false);
                }
                element.focus();
            };

            editor.click(function (e) {
                enablePostEditor(this);
            }).focus(function (e) {
                enablePostEditor(this);
            }).on('drop', function (e) {
                enablePostEditor(this);
                // get data
                const dt = e.originalEvent.dataTransfer || window.dataTransfer;
                const dropped = dt.getData('text');
                wrapAndInsert(dropped, true);
                e.preventDefault();
            }).on('paste', function (e) {

                const cd = e.originalEvent.clipboardData || window.clipboardData;
                const pasted = cd.getData('text');
                wrapAndInsert(pasted, true);
                e.preventDefault();
            }).blur(function (e) {

                const sel = commons.getSelection();
                commons.selectedText = (document.selection) ? sel.createRange().htmlText : sel.toString();
                commons.currentRange = sel.getRangeAt(0);
            });
            if(commons.currentUserPermissions.postDeleteAny){   //if the user can delete any post, we will give them access to Hi-Priority posting too.
                document.getElementById('commons-editor-priority-container').removeAttribute('style');
                bootstrap.Popover.getOrCreateInstance(document.body); // Initializes all popovers
            }
            editorPostButton.click(function (e) {

                commons.utils.savePost('', editor.html(), function (post) {

                        editor.html(commons.i18n['post_editor_initial_text']);
                        editorPostButton.prop('disabled', true);
                        editorCancelButton.prop('disabled', true);
                        fileField.val('');

                        const newPlaceholderId = 'commons-post-' + post.id;

                        $('#commons-posts').prepend(
                            `<div id="${newPlaceholderId}" class="commons-post"></div>`);
                        commons.utils.addFormattedDateToPost(post);
                        commons.utils.renderPost(post, newPlaceholderId);
                    });
            });

            editorCancelButton.click(function (e) {

                editor.html(commons.i18n['post_editor_initial_text']);
                editorPostButton.prop('disabled', true);
                editorCancelButton.prop('disabled', true);
            });

            const textField = $('#commons-link-dialog-text');

            $('.commons-editor-special-button').click(function (e) {
                if (!editor.is(":focus")) {
                    editor.click();
                }
            });

            // Replace qtip with Bootstrap modal for link dialog
            const linkModal = new bootstrap.Modal(document.getElementById('commons-link-dialog'));
            
            editorLinkButton.click(function(e) {
                textField.val(commons.selectedText);
                linkModal.show();
            });

            const urlField = $('#commons-link-dialog-url');
            const linkTextField = $('#commons-link-dialog-text');
            const thumbnailCheckbox = $('#commons-link-dialog-load-thumbnail');
            const linkInsertButton = $('#commons-link-dialog-insert-button');

            linkInsertButton.click(function (e) {
                if (commons.currentRange) {
                    commons.getSelection().addRange(commons.currentRange);
                }
                const loadThumbnail = thumbnailCheckbox.prop('checked');
                wrapAndInsert(urlField.val(), loadThumbnail, linkTextField.val());
                urlField.val('');
                linkTextField.val('');
                thumbnailCheckbox.prop('checked', false);
                linkModal.hide();
            });

            $('#commons-link-dialog-cancel-button').click(function (e) {
                urlField.val('');
                linkTextField.val('');
                thumbnailCheckbox.prop('checked', false);
                linkModal.hide();
            });

            urlField.keydown(function (e) {
                linkInsertButton.prop('disabled', false);
            });

            if (!commons.isUserSite) {
                // Replace qtip with Bootstrap modal for image dialog
                const imageModal = new bootstrap.Modal(document.getElementById('commons-image-dialog'));
                
                editorImageButton.click(function(e) {
                    imageModal.show();
                });
            }

            const fileInsertButton = $('#commons-image-dialog-insert-button');
            const fileField = $('#commons-image-dialog-file');
            const fileMessage = $('#commons-image-dialog-message');

            fileInsertButton.click(function (e) {
                const file = fileField[0].files[0];
                const extension = file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase();

                if (commons.imageFileExtensions.indexOf(extension) != -1) {
                    const formData = new FormData();
                    formData.append('siteId', commons.siteId);
                    formData.append('imageFile', file);
                    const xhr = new XMLHttpRequest();
                    xhr.open('POST', '/direct/commons/uploadImage', true);
                    xhr.onload = function (e) {
                        editor.append(`<div><img src="${xhr.responseText}" class="commons-image" /></div>`);
                    };
                    xhr.send(formData);
                    if (!commons.isUserSite) {
                        imageModal.hide();
                    }
                }
            });

            fileField.change(function (e) {
                const file = fileField[0].files[0];
                if ((file.size/1000000) > parseInt(commons.maxUploadSize)) {
                    fileMessage.html('File too big');
                } else {
                    fileInsertButton.prop('disabled', false);
                }
            });

            $('#commons-image-dialog-cancel-button').click(function (e) {
                fileInsertButton.prop('disabled', true);
                fileMessage.html('');
                fileField.val('');
                if (!commons.isUserSite) {
                    imageModal.hide();
                }
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
        $('#commons-toolbar > li > span').removeClass('current');
        $('#commons-main-link > span').addClass('current');
        const url = `/direct/commons/post.json?postId=${arg.postId}`;
        $.ajax( { url, dataType: "json", cache: false, timeout: commons.AJAX_TIMEOUT })
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
        commons.utils.renderTemplate('permissions', {}, 'commons-content');
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

    const languagesLoaded = function () {

        if (commons.embedder === 'SITE') {
            commons.utils.renderTemplate('toolbar', {} ,'commons-toolbar');

            $('#commons-main-link>span>a').click(function (e) {
                commons.switchState(commons.states.POSTS);
            });

            $('#commons-permissions-link>span>a').click(function (e) {
                commons.switchState(commons.states.PERMISSIONS);
            });
        }

        const permissionsCallback = function (permissions) {

                commons.currentUserPermissions = new CommonsPermissions(permissions);

                if (commons.currentUserPermissions == null) {
                    return;
                }

                if (!commons.isUserSite) {
                    $('#commons-toolbar').removeClass('hidden').toggle(commons.currentUserPermissions.updateSite);
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

      loadProperties({bundle: 'commons'}).then(i18n => {

        commons.i18n = i18n;
        commonsHelpers["tr"] =  (key, options) => new Handlebars.SafeString(tr("commons", key, options.hash));
        languagesLoaded();
      });
    });

    if (CKEDITOR) {
        CKEDITOR.disableAutoInline = true;
    }

}) (jQuery);

