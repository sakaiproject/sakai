/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.messageforums;

import java.util.List;

public interface RankManager {
    /**
     * Get all ranks for the site
     */
    public List getRankList(String contextid);

    /**
     * Get all ranks for the site sorted by min Post descending
     */
    public List findRanksByContextIdOrderByMinPostDesc(final String contextId);

    public List findRanksByContextIdBasedOnNumPost(final String contextId);

    public List findRanksByContextIdUserId(final String contextId, final String userid);

    public void saveRank(Rank rank);

    public RankImage createRankImage();

    public void removeRank(Rank rank);

    public Rank getRankById(Long rankId);

    public void removeImageAttachmentObject(RankImage o);

    public RankImage createRankImageAttachmentObject(String id, String fileName);

    public void addImageAttachToRank(final Rank rank, final RankImage imageAttach);

    public void removeImageAttachToRank(final Rank rank, final RankImage imageAttach);
}