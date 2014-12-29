/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.messageforums;

public interface RankImage extends MutableEntity {
    public Long getRankImageId();

    public Rank getRank();

    public void setRank(Rank rank);

    public void setRankImageId(Long imageattachid);

    public String getAttachmentId();

    public void setAttachmentId(String attachmentId);

    public String getAttachmentName();

    public void setAttachmentName(String attachmentName);

    public String getAttachmentSize();

    public long getAttachmentSizeInKB();

    public void setAttachmentSize(String attachmentSize);

    public String getAttachmentType();

    public void setAttachmentType(String attachmentType);

    public String getAttachmentUrl();

    public void setAttachmentUrl(String attachmentUrl);
}