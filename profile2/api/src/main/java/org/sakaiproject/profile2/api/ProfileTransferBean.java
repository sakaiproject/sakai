/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.profile2.api;

public class ProfileTransferBean {

    public String id;
    public String eid;
    public String firstName;
    public String lastName;
    public String displayName;
    public String imageUrl;
    public String imageThumbUrl;
    public boolean locked;
    public String mobile;
    public String type;
    public String creatorDisplayName;
    public String formattedCreatedDate;
    public String modifierDisplayName;
    public String formattedModifiedDate;
    public String name;
    public String nickname;
    public String pronouns;
    public String email;
    public String phoneticPronunciation;
    public String profileUrl;
    public boolean hasPronunciationRecording;
    public String nameRecordingUrl;
    public String audioBase64;
    public String studentNumber;
    public String facebookUrl;
    public String linkedinUrl;
    public String instagramUrl;

    public boolean disabled;
    public boolean canUpdatePicture;
    public boolean canEdit;
    public boolean canEditNameAndEmail;
}
