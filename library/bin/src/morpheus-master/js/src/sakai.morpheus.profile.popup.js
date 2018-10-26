/*
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var profile = profile || {};

profile.requestFriend = function (requestorId, friendId, callback) {

    $PBJQ.ajax( {
        url: "/direct/profile/" + requestorId + "/requestFriend?friendId=" + friendId,
        dataType: "text",
        cache: false } )
            .done(function (data, textStatus, jqXHR) {

                $PBJQ('#profile-popup-request-button-' + friendId).hide();
                $PBJQ('#profile-popup-cancel-button-' + friendId).show();
                if (callback) callback(friendId);
            });
    return false;
};

profile.confirmFriendRequest = function (requestorId, friendId, callback) {

    $PBJQ.ajax( {
        url : "/direct/profile/" + requestorId + "/confirmFriendRequest?friendId=" + friendId,
        dataType : "text",
        cache: false })
            .done(function (data, textStatus, jqXHR) {

                $PBJQ('#profile-popup-incoming-block-' + friendId).hide();
                $PBJQ('#profile-popup-remove-button-' + friendId).show();
                if (callback) callback(friendId);
            });

    return false;
};

profile.removeFriend = function (removerId, friendId, callback, displayName) {

    $PBJQ.ajax( {
        url : "/direct/profile/" + removerId + "/removeFriend?friendId=" + friendId,
        dataType : "text",
        cache: false })
            .done(function (data, textStatus, jqXHR) {

                $PBJQ('#profile-popup-remove-button-' + friendId).hide();
                $PBJQ('#profile-popup-request-button-' + friendId).show();
                if (callback && displayName) callback(friendId, displayName);
            });

    return false;
};

profile.ignoreFriendRequest = function (removerId, friendId, cancel, callback) {

    $PBJQ.ajax( { url : '/direct/profile/' + removerId + '/ignoreFriendRequest?friendId=' + friendId, cache: false })
        .done(function (data, textStatus, jqXHR) {

            if (cancel !== undefined && cancel == true) {
                $PBJQ('#profile-popup-cancel-button-' + removerId).hide();
                $PBJQ('#profile-popup-request-button-' + removerId).show();
                if (callback) callback(removerId);
            } else {
                $PBJQ('#profile-popup-incoming-block-' + friendId).hide();
                $PBJQ('#profile-popup-request-button-' + friendId).show();
                if (callback) callback(friendId);
            }

        });

    return false;
};

/**
 * Takes a jQuery array of the elements you want to attach a profile popup to. Each element must
 * have data attributes with the user's user UUID. You can also supply an object of callback
 * functions. Currently only connect is supported. You can also control where the qtip is anchored
 * by marking a descendant element with the class 'profile-popup-target'. The first descendant of this
 * type will be used as the anchor.
 *
 * eg: profile.attachPopups($PBJQ('.profile-popup'), {connect: myConnectCallback});
 *
 * @param jqArray An array of jQuery objects.
 */
profile.attachPopups = function (jqArray, callbacks) {

    if (!(jqArray instanceof $PBJQ)) {
        console.log('profile.attachPopups takes a jQuery object array, from a selector');
        return;
    }

    jqArray.each(function () {

        var userId = this.dataset.userId;
        var callbackDisplayName = this.dataset.displayName;

        var targets = $PBJQ(this).find('.profile-popup-target');
        var target = (targets.length > 0) ? targets.eq(0) : $PBJQ(this);

        $PBJQ(this).qtip({
            position: { target: target, my: 'top left', at: 'bottom center', viewport: $PBJQ(window), adjust: { method: 'flipinvert none'} },
            show: { event: 'click', delay: 0 },
            style: { classes: 'profile-popup-qtip qtip-shadow' },
            hide: { event: 'click unfocus' },
            content: {
                text: function (event, api) {

                    return $PBJQ.ajax( { url: "/direct/portal/" + userId + "/formatted", cache: false })
                        .then(function (html) {
                                return html;
                            }, function (xhr, status, error) {
                                api.set('content.text', status + ': ' + error);
                            });
                }
            },
            events: {
                visible: function(event, api) {

                    var requestButton = $PBJQ('#profile-popup-request-button-' + userId);
                    requestButton.on('click', function (e) {
                        profile.requestFriend(portal.user.id, userId);
                    });

                    var cancelButton = $PBJQ('#profile-popup-cancel-button-' + userId);
                    cancelButton.on('click', function (e) {
                        profile.ignoreFriendRequest(userId, portal.user.id, true);
                    });

                    var acceptButton = $PBJQ('#profile-popup-accept-button-' + userId);
                    acceptButton.on('click', function (e) {
                        profile.confirmFriendRequest(portal.user.id, userId);
                    });
                    var ignoreButton = $PBJQ('#profile-popup-ignore-button-' + userId);
                    ignoreButton.on('click', function (e) {
                        profile.ignoreFriendRequest(portal.user.id, userId, false);
                    });
                    var removeButton = $PBJQ('#profile-popup-remove-button-' + userId);
                    removeButton.on('click', function (e) {
                        profile.removeFriend(portal.user.id, userId);
                    });

                    if (callbacks) {
                        if (callbacks.connect) {
                            requestButton.off('click').on('click', function (e) {

                                var callback = function (friendId) {

                                        callbacks.connect(friendId);
                                        api.destroy();
                                    };
                                profile.requestFriend(portal.user.id, userId, callback);
                            });
                        }
                        if (callbacks.cancel) {
                            cancelButton.off('click').on('click', function (e) {

                                var callback = function (friendId) {

                                        callbacks.cancel(friendId);
                                        api.destroy();
                                    };

                                profile.ignoreFriendRequest(userId, portal.user.id, true, callback);
                            });
                        }
                        if (callbacks.accept) {
                            acceptButton.off('click').on('click', function (e) {

                                var callback = function (friendId) {

                                        callbacks.accept(friendId);
                                        api.destroy();
                                    };

                                profile.confirmFriendRequest(portal.user.id, userId, callback);
                            });
                        }
                        if (callbacks.ignore) {
                            ignoreButton.off('click').on('click', function (e) {

                                var callback = function (friendId) {

                                        callbacks.ignore(friendId);
                                        api.destroy();
                                    };

                                profile.ignoreFriendRequest(portal.user.id, userId, false, callback);
                            });
                        }
                        if (callbacks.remove) {
                            removeButton.off('click').on('click', function (e) {

                                var callback = function (friendId, displayName) {

                                        callbacks.remove(friendId, displayName);
                                        api.destroy();
                                    };

                                profile.removeFriend(portal.user.id, userId, callback, callbackDisplayName);
                            });
                        }
                    }
                }
            }
        });
    });
};
