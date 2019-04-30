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

(function ($) {

  profile.requestFriend = function (requestorId, friendId, callback) {

    return new Promise((resolve, reject) => {

      $.ajax( {
        url: "/direct/profile/" + requestorId + "/requestFriend?friendId=" + friendId,
        dataType: "text",
        cache: false } )
          .done(function (data, textStatus, jqXHR) {

            $('#profile-popup-request-button-' + friendId).hide();
            $('#profile-popup-cancel-button-' + friendId).show();
            if (callback) callback(friendId);
            resolve(true);
          })
          .fail((jqXHR, textStatus, errorThrown) => reject());
    });
  };

  profile.confirmFriendRequest = function (requestorId, friendId, callback) {

    return new Promise((resolve, reject) => {

      $.ajax( {
        url : "/direct/profile/" + requestorId + "/confirmFriendRequest?friendId=" + friendId,
        dataType : "text",
        cache: false })
          .done(function (data, textStatus, jqXHR) {

            $('#profile-popup-incoming-block-' + friendId).hide();
            $('#profile-popup-remove-button-' + friendId).show();
            if (callback) callback(friendId);
            resolve(true);
          })
          .fail((jqXHR, textStatus, errorThrown) => reject());
    });
  };

  profile.removeFriend = function (removerId, friendId, callback, displayName) {

    return new Promise((resolve, reject) => {

      $.ajax( {
        url : "/direct/profile/" + removerId + "/removeFriend?friendId=" + friendId,
        dataType : "text",
        cache: false })
          .done(function (data, textStatus, jqXHR) {

            $('#profile-popup-remove-button-' + friendId).hide();
            $('#profile-popup-request-button-' + friendId).show();
            if (callback) callback(friendId);
            resolve(true);
          })
          .fail((jqXHR, textStatus, errorThrown) => reject());
    });
  };

  profile.ignoreFriendRequest = function (removerId, friendId, cancel, callback) {

    return new Promise((resolve, reject) => {

      $.ajax( {
        url : '/direct/profile/' + removerId + '/ignoreFriendRequest?friendId=' + friendId,
        cache: false })
          .done(function (data, textStatus, jqXHR) {

            if (cancel !== undefined && cancel == true) {
              $('#profile-popup-cancel-button-' + removerId).hide();
              $('#profile-popup-request-button-' + removerId).show();
              if (callback) callback(removerId);
            } else {
              $('#profile-popup-incoming-block-' + friendId).hide();
              $('#profile-popup-request-button-' + friendId).show();
              if (callback) callback(friendId);
            }
            resolve(true);
          })
          .fail((jqXHR, textStatus, errorThrown) => reject());
    });
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
  profile.attachPopups = function (jqArray, options) {

    if (!(jqArray instanceof $)) {
        console.log('profile.attachPopups takes a jQuery object array, from a selector');
        return;
    }

    if (!options) options = {};

    var hide = options.hide;

    var callbacks = options.callbacks;

    if (!callbacks) callbacks = {};

    jqArray.each(function () {

      var userId = this.dataset.userId;
      var callbackDisplayName = this.dataset.displayName;

      var targets = $(this).find('.profile-popup-target');
      var target = (targets.length > 0) ? targets.eq(0) : $(this);
      var position = {
        target: target,
        my: 'top left',
        at: 'bottom center',
        viewport: $(window),
        adjust: { method: 'flipinvert none'}
      };

      if (options && options.container) {
        position.container = $(`#${options.container}`);
      }

      $(this).qtip({
        position: position,
        show: { event: 'click', delay: 0 },
        style: { classes: 'profile-popup-qtip qtip-shadow' },
        hide: { event: 'click unfocus' },
        content: {
          text: function (event, api) {

            return $.ajax( { url: "/direct/portal/" + userId + "/formatted", cache: false })
              .then(function (html) {
                  return html;
                }, function (xhr, status, error) {
                    api.set('content.text', status + ': ' + error);
                });
          }
        },
        events: {
          visible: function (event, api) {

            $('#profile-popup-request-button-' + userId).off("click").on("click", (e) => {
              profile.requestFriend(portal.user.id, userId, callbacks.connect)
                .then(() => { if (hide) api.hide() });
            });
            $('#profile-popup-cancel-button-' + userId).off("click").on("click", (e) => {
              profile.ignoreFriendRequest(userId, portal.user.id, true, callbacks.cancel)
                .then(() => { if (hide) api.hide() });
            });
            $('#profile-popup-accept-button-' + userId).off("click").on("click", (e) => {
              profile.confirmFriendRequest(portal.user.id, userId, callbacks.accept)
                .then(() => { if (hide) api.hide() });
            });
            $('#profile-popup-ignore-button-' + userId).off("click").on("click", (e) => {
              profile.ignoreFriendRequest(portal.user.id, userId, false, callbacks.ignore)
                .then(() => { if (hide) api.hide() });
            });
            $('#profile-popup-remove-button-' + userId).off("click").on("click", (e) => {
              profile.removeFriend(portal.user.id, userId, callbacks.remove)
                .then(() => { if (hide) api.hide() });
            });
          }
        }
      });
    });
  };
}) ($PBJQ);
