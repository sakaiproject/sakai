/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.assessment.integration.helper.ifc;

import org.sakaiproject.event.api.NotificationService;

import java.util.Optional;

/**
 * Abstracts access to Sakai's {@code TagService}.
 */
public interface TagServiceHelper {

    /**
     * Provides a means for TagServiceHelper to mediate all interactions with
     * Sakai events, e.g. can act as a choke point which effectively disables
     * all event delivery, as opposed to having to specially configure each
     * event handler. This is consistent with the overall purpose of this
     * class, which is to encapsulate Samigo's access to the Sakai
     * {@code TagService}.
     *
     * <p>When "enabled", this function is expected to call back to
     * {@link TagEventHandler#registerEventCallbacks(NotificationService)} to
     * complete the registration process.</p>
     *
     * @param tagEventHandler
     */
    void registerTagEventHandler(TagEventHandler tagEventHandler);

    Optional<TagView> findTagById(String id);

    Optional<TagCollectionView> findTagCollectionById(String id);


    class TagView {
        public TagView() {}

        public TagView(String tagId, String tagLabel, String tagCollectionId, String tagCollectionName) {
            this.tagId = tagId;
            this.tagLabel = tagLabel;
            this.tagCollectionId = tagCollectionId;
            this.tagCollectionName = tagCollectionName;
        }

        public String tagId;
        public String tagCollectionId;
        public String tagLabel;
        public String tagCollectionName;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TagView)) return false;

            TagView tagView = (TagView) o;

            if (tagId != null ? !tagId.equals(tagView.tagId) : tagView.tagId != null) return false;
            if (tagCollectionId != null ? !tagCollectionId.equals(tagView.tagCollectionId) : tagView.tagCollectionId != null)
                return false;
            if (tagLabel != null ? !tagLabel.equals(tagView.tagLabel) : tagView.tagLabel != null) return false;
            return tagCollectionName != null ? tagCollectionName.equals(tagView.tagCollectionName) : tagView.tagCollectionName == null;

        }

        @Override
        public int hashCode() {
            int result = tagId != null ? tagId.hashCode() : 0;
            result = 31 * result + (tagCollectionId != null ? tagCollectionId.hashCode() : 0);
            result = 31 * result + (tagLabel != null ? tagLabel.hashCode() : 0);
            result = 31 * result + (tagCollectionName != null ? tagCollectionName.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "TagView{" +
                    "tagId='" + tagId + '\'' +
                    ", tagCollectionId='" + tagCollectionId + '\'' +
                    ", tagLabel='" + tagLabel + '\'' +
                    ", tagCollectionName='" + tagCollectionName + '\'' +
                    '}';
        }
    }

    class TagCollectionView {
        public TagCollectionView() {}

        public TagCollectionView(String tagCollectionId, String tagCollectionName) {
            this.tagCollectionId = tagCollectionId;
            this.tagCollectionName = tagCollectionName;
        }

        public String tagCollectionId;
        public String tagCollectionName;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TagCollectionView)) return false;

            TagCollectionView that = (TagCollectionView) o;

            if (tagCollectionId != null ? !tagCollectionId.equals(that.tagCollectionId) : that.tagCollectionId != null)
                return false;
            return tagCollectionName != null ? tagCollectionName.equals(that.tagCollectionName) : that.tagCollectionName == null;

        }

        @Override
        public int hashCode() {
            int result = tagCollectionId != null ? tagCollectionId.hashCode() : 0;
            result = 31 * result + (tagCollectionName != null ? tagCollectionName.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "TagCollectionView{" +
                    "tagCollectionId='" + tagCollectionId + '\'' +
                    ", tagCollectionName='" + tagCollectionName + '\'' +
                    '}';
        }
    }

}
