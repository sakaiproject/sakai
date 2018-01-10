/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.messageforums.ui;

import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.Rank;
import org.sakaiproject.component.app.messageforums.dao.hibernate.RankImpl;

/**
 * @author lydial@stanford.edu
 */
@Slf4j
public class ForumRankBean {

    private boolean assignErr;
    private Set<String> assignToIds;
    private String assignToDisplay;
    private String contextId;
    private boolean imageSizeErr;
    private boolean minPostErr;
    private long minPosts = -1;
    private Rank rank;
    private boolean selected = false;
    private String title;
    private boolean titleErr;
    private String type;
    private boolean typeErr;

    public ForumRankBean() {
        this.setRank(new RankImpl());
        this.type = null;
    }

    public ForumRankBean(Rank rank) {
        this.rank = rank;
        this.type = rank.getType();
        this.title = rank.getTitle();
        this.assignToIds = rank.getAssignToIds();
        this.assignToDisplay = rank.getAssignToDisplay();
        this.contextId = rank.getContextId();
        this.minPosts = rank.getMinPosts();
    }

    public Set<String> getAssignToIds() {
        return assignToIds;
    }

    public String getAssignToDisplay() {
        return assignToDisplay;
    }

    public String getContextId() {
        return contextId;
    }

    public long getMinPosts() {
        return minPosts;
    }

    public Rank getRank() {
        return rank;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public boolean isAssignErr() {
        return assignErr;
    }

    public boolean isImageSizeErr() {
        return imageSizeErr;
    }

    public boolean isMinPostErr() {
        return minPostErr;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isTitleErr() {
        return titleErr;
    }

    public boolean isTypeErr() {
        return typeErr;
    }

    public void setAssignErr(boolean assignErr) {
        this.assignErr = assignErr;
    }

    public void setAssignToIds(Set<String> assignToIds) {
        this.assignToIds = assignToIds;
    }

    public void setAssignToDisplay(String assignToDisplay) {
        this.assignToDisplay = assignToDisplay;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public void setImageSizeErr(boolean err) {
        this.imageSizeErr = err;
    }

    public void setMinPostErr(boolean minPostErr) {
        this.minPostErr = minPostErr;
    }

    public void setMinPosts(long minPosts) {
        this.minPosts = minPosts;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleErr(boolean titleErr) {
        this.titleErr = titleErr;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTypeErr(boolean typeErr) {
        this.typeErr = typeErr;
    }
}
