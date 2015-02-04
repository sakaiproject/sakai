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

import java.util.Set;

public interface Rank extends MutableEntity {
    public final static String RANK_TYPE_INDIVIDUAL = "1";
    public final static String RANK_TYPE_POST_COUNT = "2";

    // defined in hbm.xml
    public String getTitle();

    public void setTitle(String title);

    public String getType();

    public void setType(String ranktype);

    public Set<String> getAssignToIds();

    public void setAssignToIds(Set<String> assignToIds);

    public long getMinPosts();

    public void setMinPosts(long number_of_post);

    public String getContextId();

    public void setContextId(String contextId);

    public RankImage getRankImage();

    public void setRankImage(RankImage imageattach);

    public void setAssignToDisplay(String assigned_to_display);

    public String getAssignToDisplay();

}
