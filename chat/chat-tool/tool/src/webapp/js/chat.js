/**********************************************************************************
 * URL:
 * Id:
 ***********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

import { addClass, ajax, append, attr, domReady, fadeIn, fadeOut, hasClass, html, on, remove, removeAttr, removeClass, text, trigger, val }
  from '/library/js/vanilla.js';

// Define constants for window objects
const portal = window.portal || { user: { id: '' }, siteId: '' };
const bootstrap = window.bootstrap || { Modal: { getInstance: () => {} } };

/**
 * Chat class - Handles chat functionality
 */
class Chat {
    constructor() {
      this.messageCountTemplate = '';
      this.messagesUnreadedTemplate = '';
      this.shownMessages = 0;
      this.totalMessages = 0;
      this.currentChatChannelId = null;
      this.keycode_enter = 13;
      this.lastMessageTime = Date.now(); // Time of last message activity
      this.maxPollInterval = 30000; // Maximum polling interval when inactive (30 seconds)
      this.minPollInterval = 5000;  // Minimum polling interval when active (5 seconds)
      this.pageVisibility = 'visible';
      this.pollInterval = 10000; // Current polling interval, will adjust dynamically
      this.timeoutVar = null;
      this.unreadedMessages = 0;
      this.url_submit = "/direct/chat-message/";

      // Bind the visibility change event
      document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'hidden' || document.visibilityState === 'visible') {
          this.changePageVisibility(document.visibilityState);
        }
      });
    }

    /**
     * Helper function to handle fadeout and removal of divisor messages
     */
    handleDivisorFadeOut() {
      const elements = Array.from(document.querySelectorAll(".divisorNewMessages")).filter(element => element.id !== "divisorNewMessages");
      elements.forEach(element => {
        fadeOut(element, 300, () => {
          remove(element);
        });
      });
    }

    /**
     * Helper function to handle fadeout of scroll bottom button and reset unread messages
     */
    handleScrollBottomFadeOut() {
      const scrollBottomElements = document.querySelectorAll(".scrollBottom");
      scrollBottomElements.forEach(element => {
        fadeOut(element, 300, () => {
          this.unreadedMessages = 0;
          this.updateUnreadedMessages();
        });
      });
    }

    init() {
      this.updateShownText();
      this.scrollChat();
      this.updateChatData();

      const textarea = document.querySelector("#topForm\\:controlPanel\\:message");
      const submitButton = document.querySelector("#topForm\\:controlPanel\\:submit");
      const resetButton = document.querySelector("#topForm\\:controlPanel\\:reset");

      // Apply Bootstrap classes to form elements
      addClass(textarea, "form-control");
      addClass(submitButton, "btn btn-primary");
      addClass(resetButton, "btn btn-secondary");

      // Add character counter
      const formGroup = textarea.parentNode;
      if (formGroup) {
        const charCounter = document.createElement("small");
        addClass(charCounter, "form-text text-muted mt-1 char-counter");
        text(charCounter, "0 characters");
        append(formGroup, charCounter);

        // Update character counter on input
        on(textarea, 'input', () => {
          const length = val(textarea).length;
          text(charCounter, `${length} character${length !== 1 ? 's' : ''}`);
        });
      }

      on(submitButton, 'click', () => {
        const messageBody = val(textarea);
        attr(submitButton, "disabled", true);
        const params = {
          "chatChannelId": this.currentChatChannelId,
          "body": messageBody
        };
        // If message body or currentChatChannelId are empty
        if (!this.currentChatChannelId || !messageBody || !messageBody.replace(/\n/g, "").replace(/ /g, "").length) {
          val(textarea, "");
          textarea.focus();
          removeAttr(submitButton, "disabled");
          return false;
        }
        this.sendMessage(params, textarea, submitButton);
      });

      on(textarea, 'keydown', (e) => {
        const keycode = e.keyCode;
        if (keycode === this.keycode_enter && !attr(submitButton, "disabled")) {
          trigger(submitButton, "click");
          return false;
        }
      });

      on(resetButton, 'click', () => {
        val(textarea, "");
        textarea.focus();
        // Update character counter
        const charCounter = document.querySelector(".char-counter");
        if (charCounter) {
          text(charCounter, "0 characters");
        }
      });

      const chatList = document.querySelector(".chatList");
      addClass(chatList, "list-group");
      on(chatList, 'click', (event) => {
        if (hasClass(event.target, 'chatRemove') || hasClass(event.target.parentNode, 'chatRemove')) {
          const clickedElement = hasClass(event.target, 'chatRemove') ? event.target : event.target.parentNode;
          const messageItem = clickedElement.parentNode.parentNode;
          const messageId = attr(messageItem, "data-message-id");
          const ownerDisplayName = text(messageItem.querySelector(".chatName"));
          const date = text(messageItem.querySelector(".chatDate"));
          const messageBody = html(messageItem.querySelector(".chatText"));
          this.showRemoveModal(messageId, ownerDisplayName, date, messageBody);
        }
      });

      const deleteButton = document.querySelector("#deleteButton");
      on(deleteButton, 'click', (event) => {
        const messageId = attr(event.currentTarget, "data-message-id");
        this.deleteMessage(messageId);
      });

      const chatListWrapper = document.querySelector("#chatListWrapper");
      on(chatListWrapper, 'scroll', () => {
        const wrapper = document.querySelector(".chatListWrapper");
        if ((wrapper.scrollHeight - wrapper.scrollTop) <= wrapper.clientHeight) {
          setTimeout(() => this.handleDivisorFadeOut(), 3000);
          this.handleScrollBottomFadeOut();
        }
      });

      const scrollBottomButton = document.querySelector(".scrollBottom");
      addClass(scrollBottomButton, "btn btn-primary position-absolute bottom-0 end-0 m-3");
      on(scrollBottomButton, 'click', () => {
        this.scrollChat();
        setTimeout(() => this.handleDivisorFadeOut(), 3000);
        this.handleScrollBottomFadeOut();
      });
    }

    changePageVisibility(newVisibility) {
      this.pageVisibility = newVisibility;
      if (newVisibility === 'visible') {
        // Reset polling interval to default
        this.pollInterval = 10000;
        // Update last activity time to indicate user is active
        this.lastMessageTime = Date.now();
        // Trigger an immediate update
        this.updateChatData();
      } else if (newVisibility === 'hidden') {
        // Set polling interval to maximum when page is hidden to reduce resource usage
        // while still maintaining the connection
        this.pollInterval = this.maxPollInterval;
      }
    }

    onAjaxError(xhr) {
      const textarea = document.querySelector("#topForm\\:controlPanel\\:message");
      const submitButton = document.querySelector("#topForm\\:controlPanel\\:submit");
      if (xhr.status === 404) {
        const missingChannel = document.querySelector("#missingChannel");
        removeClass(missingChannel, "d-none");
        addClass(missingChannel, "alert alert-danger");
        val(textarea, "");
        attr(textarea, "disabled", true);
        attr(submitButton, "disabled", true);
        attr(document.querySelector("#topForm\\:controlPanel\\:reset"), "disabled", true);
      } else {
        const errorSubmit = document.querySelector("#errorSubmit");
        removeClass(errorSubmit, "d-none");
        addClass(errorSubmit, "alert alert-danger");
        removeAttr(submitButton, "disabled");
      }
      textarea.focus();
    }

    sendMessage(params, textarea, submitButton) {
      const errorSubmit = document.querySelector("#errorSubmit");
      ajax({
        url: this.url_submit + 'new',
        data: params,
        type: "POST",
        contentType: 'application/x-www-form-urlencoded',
        beforeSend: () => {
          addClass(errorSubmit, "d-none");
          const spinner = document.createElement("span");
          addClass(spinner, "spinner-border spinner-border-sm me-2");
          attr(spinner, "role", "status");
          attr(spinner, "aria-hidden", "true");
          attr(submitButton, "disabled");
          submitButton.prepend(spinner);
        },
        error: (xhr) => {
          // Remove spinner
          const spinner = submitButton.querySelector(".spinner-border");
          if (spinner) {
            remove(spinner);
          }
          this.onAjaxError(xhr);
        },
        success: (data) => {
          // Update last activity time when sending a message
          this.lastMessageTime = Date.now();
          // Reset polling interval to minimum for immediate feedback
          this.pollInterval = this.minPollInterval;

          // Remove spinner
          const spinner = submitButton.querySelector(".spinner-border");
          if (spinner) {
            remove(spinner);
          }

          this.scrollChat();
          this.updateChatData();
          val(textarea, "");
          textarea.focus();

          // Ensure button is re-enabled after all other operations
          setTimeout(() => {
            removeAttr(submitButton, "disabled");
          }, 500);
        }
      });
    }

    deleteMessage(messageId) {
      const removeModal = document.querySelector("#removemodal");
      const modal = bootstrap.Modal.getInstance(removeModal);
      modal.hide();

      ajax({
        url: this.url_submit + messageId,
        type: "DELETE",
        success: (data) => {
          modal.hide();
          this.updateChatData();
        }
      });
    }

    updateChatData() {
      this.doUpdateChatData();
      if(this.timeoutVar !== null) {
        clearTimeout(this.timeoutVar);
      }

      // Adjust polling interval based on activity
      const timeSinceLastActivity = Date.now() - this.lastMessageTime;

      // If there's been no recent activity, gradually increase the polling interval
      if (timeSinceLastActivity < 60000) { // Less than 1 minute
        this.pollInterval = Math.max(this.minPollInterval, this.pollInterval - 5000); // Decrease by 5 seconds
      } else {
        this.pollInterval = Math.min(this.maxPollInterval, this.pollInterval + 5000); // Increase by 5 seconds
      }

      // Always schedule the next poll, but use a longer interval when the page is hidden
      // console.log("[DEBUG_LOG] updateChatData set next poll in", this.pollInterval, "ms");
      this.timeoutVar = setTimeout(() => {
        this.updateChatData();
      }, this.pollInterval);
    }

    doUpdateChatData() {
      const url = this.url_submit + portal.user.id + "/chatData.json";
      if(!this.currentChatChannelId) {
        return;
      }
      const params = {
        "siteId": portal.siteId,
        "channelId": this.currentChatChannelId
      };
      // console.log("[DEBUG_LOG] doUpdateChatData making request to:", url, "with params:", params);
      ajax({
        url: url,
        data: params,
        type: "GET",
        contentType: 'application/x-www-form-urlencoded',
        cache: false,
        success: (data) => {
          // console.log("[DEBUG_LOG] doUpdateChatData received response:", data);
          this.addMessages(data.data.messages);
          this.updatePresentUsers(data.data.presentUsers);
          this.deleteMessages(data.data.deletedMessages);
        },
        error: (xhr) => {
          this.onAjaxError(xhr);
        }
      });
    }

    addMessages(messages) {
      const chatListWrapper = document.querySelector(".chatListWrapper");
      let scrolledToBottom = true;
      if ((chatListWrapper.scrollHeight - chatListWrapper.scrollTop) > chatListWrapper.clientHeight) {
        scrolledToBottom = false;
      }

      // Update last activity time if there are new messages
      if (messages.length > 0) {
        this.lastMessageTime = Date.now();

        if (!scrolledToBottom) {
          const divisorElements = document.querySelectorAll(".divisorNewMessages");
          if (divisorElements.length < 2) {
            const divisorNewMessages = document.querySelector("#divisorNewMessages").cloneNode(true);
            removeAttr(divisorNewMessages, "id");
            removeClass(divisorNewMessages, "d-none");
            addClass(divisorNewMessages, "list-group-item list-group-item-info");
            const chatList = document.querySelector("#topForm\\:chatList");
            append(chatList, divisorNewMessages);
          }
        }
      }

      for (let i=0; i<messages.length; i++) {
        const chatListItems = document.querySelectorAll("#topForm\\:chatList li:not(.divisorNewMessages)");
        const lastItem = chatListItems.length > 0 ? chatListItems[chatListItems.length - 1] : null;
        const lastMessageOwnerId = lastItem ? attr(lastItem, "data-owner-id") : null;
        const lastMessageMillis = lastItem ? attr(lastItem, "data-millis") : null;
        const messageId = messages[i].id;
        const ownerId = messages[i].owner;
        const ownerDisplayName = messages[i].ownerDisplayName;

        const messageDate = messages[i].messageDate;
        const messageMillisDiff = messages[i].messageDate - (lastMessageMillis ? lastMessageMillis : 0);
        const localizedDate = messages[i].messageDateString.localizedDate;
        const localizedTime = messages[i].messageDateString.localizedTime;
        const dateID = messages[i].messageDateString.dateID;
        const dateStr = this.renderDate(localizedDate, localizedTime, dateID);

        const messageBody = messages[i].body;
        const removeable = messages[i].removeable;
        const existingMessage = document.querySelector(`#topForm\\:chatList li[data-message-id="${messageId}"]`);
        if (!existingMessage) {
          const messageItem = document.querySelector("#chatListItem").cloneNode(true);
          removeClass(messageItem, "d-none");
          addClass(messageItem, "list-group-item");
          removeAttr(messageItem, "id");
          attr(messageItem, "data-message-id", messageId);
          attr(messageItem, "data-owner-id", ownerId);
          attr(messageItem, "data-millis", messageDate);
          const htmlContent = html(messageItem).replace(/USER_ID_PLACEHOLDER/g, ownerId);
          html(messageItem, htmlContent);

          const chatMessage = messageItem.querySelector(".chatMessage");
          attr(chatMessage, "data-message-id", messageId);
          addClass(chatMessage, "ms-2");

          const chatName = messageItem.querySelector(".chatName");
          attr(chatName, "id", ownerId);
          text(chatName, ownerDisplayName);
          addClass(chatName, "fw-bold");

          const chatDate = messageItem.querySelector(".chatDate");
          text(chatDate, dateStr);
          addClass(chatDate, "text-muted small ms-2");

          const chatText = messageItem.querySelector(".chatText");
          html(chatText, messageBody);

          if (removeable) {
            const chatRemove = messageItem.querySelector(".chatRemove");
            removeClass(chatRemove, "d-none");
            addClass(chatRemove, "btn btn-sm btn-link text-danger float-end");
          }

          if (lastMessageOwnerId === ownerId && messageMillisDiff < (5*60*1000)) {
            addClass(messageItem, "nestedMessage border-start-0 border-end-0 py-2");
            const chatMessageDate = messageItem.querySelector(".chatMessageDate");
            text(chatMessageDate, dateStr);
            addClass(chatMessageDate, "text-muted small");
          }

          const chatList = document.querySelector("#topForm\\:chatList");
          append(chatList, messageItem);
          this.totalMessages++;
          this.shownMessages++;
          this.updateShownText();

          if (scrolledToBottom) {
            this.scrollChat();
            setTimeout(() => this.handleDivisorFadeOut(), 3000);
            this.handleScrollBottomFadeOut();
          } else {
            this.unreadedMessages++;
            this.updateUnreadedMessages();
            const divisorElements = document.querySelectorAll(".divisorNewMessages:not(#divisorNewMessages)");
            // Update all newMessages elements with the current unreadedMessages count
            const currentUnreadedCount = this.unreadedMessages; // Create a local copy to avoid closure issues
            for (let j = 0; j < divisorElements.length; j++) {
              const newMessagesElement = divisorElements[j].querySelector(".newMessages");
              text(newMessagesElement, currentUnreadedCount);
              addClass(newMessagesElement, "badge bg-danger rounded-pill me-2");
            }

            const scrollBottomElement = document.querySelector(".scrollBottom");
            fadeIn(scrollBottomElement, 300);
            removeClass(scrollBottomElement, "d-none");
            addClass(scrollBottomElement, "btn btn-primary position-absolute bottom-0 end-0 m-3");
          }
        }
      }
    }

    deleteMessages(messages) {
      for (let i=0; i<messages.length; i++) {
        const messageId = messages[i].id;
        if(messageId !== '*') {
          const message = document.querySelector(`#topForm\\:chatList li[data-message-id="${messageId}"]`);
          if (message) {
            const nextElement = message.nextElementSibling;
            if (!hasClass(message, "nestedMessage") && nextElement && hasClass(nextElement, "nestedMessage")) {
              removeClass(nextElement, "nestedMessage");
            }
            remove(message);
            this.totalMessages--;
            this.shownMessages--;
            this.updateShownText();
          }
        } else {
          const messagesToRemove = document.querySelectorAll("#topForm\\:chatList li:not(#chatListItem):not(#divisorNewMessages)");
          for (let j = 0; j < messagesToRemove.length; j++) {
            remove(messagesToRemove[j]);
          }
          this.totalMessages = 0;
          this.shownMessages = 0;
          this.updateShownText();
        }
      }
    }

    updatePresentUsers(users) {
      // console.log("[DEBUG_LOG] updatePresentUsers called with users:", users);
      const presence = document.getElementById("presence");

      // No need to switch tabs, just update the presence list directly
      // This avoids the flickering issue while still ensuring the list is updated

      // Check if users array is empty or undefined
      if (!users || users.length === 0) {
        // console.log("[DEBUG_LOG] No users to display in presence list");
        if (presence) {
          // Clear the presence list and add a message
          presence.textContent = "";
          addClass(presence, "list-group");
          const noUsersElement = document.createElement("li");
          addClass(noUsersElement, "list-group-item text-center");
          noUsersElement.textContent = "No users currently online";
          presence.appendChild(noUsersElement);
        }
        return;
      }

      if (presence) {
        presence.textContent = "";
        addClass(presence, "list-group");
      } else {
        // console.log("[DEBUG_LOG] Presence element not found");
        return;
      }

      document.querySelectorAll("sakai-user-photo").forEach(function(el) {
        el.online = false;
      });

      users.forEach(user => {
        const ownerId = user.id;
        let userId = ownerId;
        if(ownerId.indexOf(':') > -1) {
          userId = ownerId.substring(ownerId.indexOf(":") + 1);
        }
        const userElement = document.createElement("li");

        userElement.setAttribute("data-user-id", ownerId);
        addClass(userElement, "list-group-item d-flex align-items-center");

        userElement.innerHTML = `
          <div class="chat-presence-row d-flex align-items-center w-100">
            <div class="me-3">
              <sakai-user-photo user-id="${ownerId}" profile-popup="on"></sakai-user-photo>
            </div>
            <div class="flex-grow-1">${user.name}</div>
            <div class="ms-auto">
              <span class="badge bg-success rounded-pill">online</span>
            </div>
          </div>`;
        if (presence) {
          presence.appendChild(userElement);
        }
        document.querySelectorAll(`.chatList sakai-user-photo[user-id='${userId}']`).forEach(function(el) {
          el.online = true;
        });
      });
    }

    showRemoveModal(messageId, ownerDisplayName, date, messageBody) {
      const removeModal = document.querySelector("#removemodal");
      const ownerElement = removeModal.querySelector("#owner");
      text(ownerElement, ownerDisplayName);

      const dateElement = removeModal.querySelector("#date");
      text(dateElement, date);

      const messageElement = removeModal.querySelector("#message");
      html(messageElement, messageBody);

      const deleteButton = removeModal.querySelector("#deleteButton");
      attr(deleteButton, "data-message-id", messageId);
      addClass(deleteButton, "btn-danger");

      // Enhance modal with Bootstrap classes
      const modalDialog = removeModal.querySelector(".modal-dialog");
      addClass(modalDialog, "modal-dialog-centered");

      const modalBody = removeModal.querySelector(".modal-body");
      addClass(modalBody, "p-4");

      // Create a table with Bootstrap classes
      const table = modalBody.querySelector("table");
      if (table) {
        addClass(table, "table table-bordered");
      }

      const modal = new bootstrap.Modal(removeModal);
      modal.toggle();
    }

    scrollChat() {
      const scrollableChat = document.querySelector("#chatListWrapper");
      if (scrollableChat) {
        scrollableChat.scrollTop = scrollableChat.scrollHeight;
      }
    }

    updateShownText() {
      let countText = this.messageCountTemplate + '';
      countText = countText.replace('*SHOWN*', this.shownMessages);
      countText = countText.replace('*TOTAL*', this.totalMessages);
      const messagesShownTotal = document.querySelector("#chat2_messages_shown_total");
      if (messagesShownTotal) {
        html(messagesShownTotal, countText);
      }
    }

    updateUnreadedMessages() {
      const unreadedText = this.messagesUnreadedTemplate.replace("*UNREADED*", this.unreadedMessages);
      const scrollBottomElement = document.querySelector(".scrollBottom");
      if (scrollBottomElement) {
        attr(scrollBottomElement, "title", unreadedText);
        const newMessagesElement = scrollBottomElement.querySelector(".newMessages");
        if (newMessagesElement) {
          text(newMessagesElement, this.unreadedMessages);
        }
      }
    }

    renderDate(localizedDate, localizedTime, dateID) {
      let msgTime = "";
      if(window.display_date && window.display_time) {
        msgTime = " " + localizedDate + " " + localizedTime + " " ;
      } else if (window.display_date) {
        msgTime = " " + localizedDate + " " ;
      } else if(window.display_time) {
        msgTime = " " + localizedTime + " " ;
      } else if(window.display_id) {
        msgTime = " (" + dateID + ") " ;
      }
      return msgTime;
    }
  }

// Create a singleton instance of the Chat class
window.chat = new Chat();

// Initialize when the document is ready
domReady(() => {
  // Check if there's configuration stored by the JSP
  if (window.chatConfig) {
    // Apply stored configuration
    if (window.chatConfig.currentChatChannelId) {
      window.chat.currentChatChannelId = window.chatConfig.currentChatChannelId;
    }
    if (window.chatConfig.pollInterval) {
      window.chat.pollInterval = window.chatConfig.pollInterval;
    }
    if (window.chatConfig.totalMessages) {
      window.chat.totalMessages = window.chatConfig.totalMessages;
      window.chat.shownMessages = window.chatConfig.totalMessages;
    }
    if (window.chatConfig.messageCountTemplate) {
      window.chat.messageCountTemplate = window.chatConfig.messageCountTemplate;
    }
    if (window.chatConfig.messagesUnreadedTemplate) {
      window.chat.messagesUnreadedTemplate = window.chatConfig.messagesUnreadedTemplate;
    }
  }
  window.chat.init();
});
