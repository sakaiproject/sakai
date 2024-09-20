/**
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lti.api;

import java.util.Map;

import org.sakaiproject.user.api.User;

/**
 *  @author Adrian Fish <a.fish@lancaster.ac.uk>
 */
public interface UserPictureSetter {

    /**
     * BLTI-155. If Profile2 is installed, set the profile picture to the user_image url, if supplied.
     * 
     * @param payload The LTI launch parameters in a Map
     * @param user The provisioned user who MUST be already logged in.
     * @param isTrustedConsumer If this is true, do nothing as we assume that a local
     * 							user corresponding to the consumer user already exists
     * @param isEmailTrustedConsumer If this is true, do nothing as we assume that a local
     * 							user corresponding to the consumer user already exists
     */
    public void setupUserPicture(Map payload, User user, boolean isTrustedConsumer, boolean isEmailTrustedConsumer);
}
