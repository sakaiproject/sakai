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
        url : "/direct/profile/" + requestorId + "/requestFriend?friendId=" + friendId,
        dataType : "text",
        cache: false } )
            .done(function (data, textStatus, jqXHR) {

                $PBJQ('#profile-popup-unconnected-block-' + friendId).hide();
                $PBJQ('#profile-popup-requested-block-' + friendId).show();
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
                $PBJQ('#profile-popup-connected-block-' + friendId).show();
            });

    return false;
};

profile.removeFriend = function (removerId, friendId) {

    $PBJQ.ajax( {
        url : "/direct/profile/" + removerId + "/removeFriend?friendId=" + friendId,
        dataType : "text",
        cache: false })
            .done(function (data, textStatus, jqXHR) {

                $PBJQ('#profile-popup-connected-block-' + friendId).hide();
                $PBJQ('#profile-popup-unconnected-block-' + friendId).show();
            });

    return false;
};

profile.ignoreFriendRequest = function (removerId, friendId) {

    $PBJQ.ajax( {
        url : "/direct/profile/" + removerId + "/ignoreFriendRequest?friendId=" + friendId,
        dataType : "text",
        cache: false })
            .done(function (data, textStatus, jqXHR) {

                $PBJQ('#profile-popup-requested-block-' + friendId).hide();
                $PBJQ('#profile-popup-incoming-block-' + friendId).hide();
                $PBJQ('#profile-popup-unconnected-block-' + friendId).show();
            });

    return false;
};
