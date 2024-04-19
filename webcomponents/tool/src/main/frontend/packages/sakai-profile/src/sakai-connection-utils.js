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

import { getUserId } from "@sakai-ui/sakai-portal-utils";

const fetchIt = (url, errorMessage) => {

  return fetch(url, { cache: "no-cache" })
    .then(r => {

      if (r.ok) {
        return r.text();
      }

      throw new Error(errorMessage + url);
    });
};

export const requestConnection = friendId => {

  const url = `/direct/profile/${getUserId()}/requestFriend?friendId=${friendId}`;
  return fetchIt(url, "Network error while requesting connection at ");
};

export const confirmConnection = friendId => {

  const url = `/direct/profile/${getUserId()}/confirmFriendRequest?friendId=${friendId}`;
  return fetchIt(url, "Network error while confirming connection at ");
};

export const removeConnection = friendId => {

  const url = `/direct/profile/${getUserId()}/removeFriend?friendId=${friendId}`;
  return fetchIt(url, "Network error while removing connection at ");
};

export const ignoreConnection = friendId => {

  const url = `/direct/profile/${getUserId()}/ignoreFriendRequest?friendId=${friendId}`;
  return fetchIt(url, "Network error while ignoring connection at ");
};
