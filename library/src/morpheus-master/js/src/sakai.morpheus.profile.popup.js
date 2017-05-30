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

profile.requestFriend = function (requestorId, friendId) {

    $PBJQ.ajax( {
        url: "/direct/profile/" + requestorId + "/requestFriend?friendId=" + friendId,
        dataType: "text",
        cache: false } )
            .done(function (data, textStatus, jqXHR) {

                $PBJQ('#profile-popup-request-button-' + friendId).hide();
                $PBJQ('#profile-popup-cancel-button-' + friendId).show();
            });
    return false;
};

profile.confirmFriendRequest = function (requestorId, friendId) {

    $PBJQ.ajax( {
        url : "/direct/profile/" + requestorId + "/confirmFriendRequest?friendId=" + friendId,
        dataType : "text",
        cache: false })
            .done(function (data, textStatus, jqXHR) {

                $PBJQ('#profile-popup-incoming-block-' + friendId).hide();
                $PBJQ('#profile-popup-remove-button-' + friendId).show();
            });

    return false;
};

profile.removeFriend = function (removerId, friendId) {

    $PBJQ.ajax( {
        url : "/direct/profile/" + removerId + "/removeFriend?friendId=" + friendId,
        dataType : "text",
        cache: false })
            .done(function (data, textStatus, jqXHR) {

                $PBJQ('#profile-popup-remove-button-' + friendId).hide();
                $PBJQ('#profile-popup-request-button-' + friendId).show();
            });

    return false;
};

profile.ignoreFriendRequest = function (removerId, friendId, cancel) {

    $PBJQ.ajax( {
        url : "/direct/profile/" + removerId + "/ignoreFriendRequest?friendId=" + friendId,
        dataType : "text",
        cache: false })
            .done(function (data, textStatus, jqXHR) {

                if (cancel !== undefined && cancel == true) {
                    $PBJQ('#profile-popup-cancel-button-' + removerId).hide();
                    $PBJQ('#profile-popup-request-button-' + removerId).show();
                } else {
                    $PBJQ('#profile-popup-incoming-block-' + friendId).hide();
                    $PBJQ('#profile-popup-request-button-' + friendId).show();
                }
            });

    return false;
};

/**
 * Takes a jQuery array of the elements you want to attach a profile popup to. Each element must
 * have a data attribute with the user's user UUID.
 * @param jqArray An array of jQuery objects.
 */
profile.attachPopups = function (jqArray) {

    if (!(jqArray instanceof $PBJQ)) {
        console.log('profile.attachPopups takes a jQuery object array, from a selector');
        return;
    }

    jqArray.each(function () {

        var userId = this.dataset.userId;

        $PBJQ(this).qtip({
            position: { my: 'top left', at: 'bottom center', viewport: $(window), adjust: { method: 'flipinvert none'} },
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
            }
        });
    });
};
