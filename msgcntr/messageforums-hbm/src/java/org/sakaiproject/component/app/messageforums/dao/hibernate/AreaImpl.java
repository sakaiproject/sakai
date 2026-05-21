/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/AreaImpl.java $
 * $Id: AreaImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.HiddenGroup;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.PrivateForum;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AreaImpl extends MutableEntityImpl implements Area {

    @Getter @Setter private Boolean availabilityRestricted = false;
    @Getter @Setter private Set<HiddenGroup> hiddenGroups;
    @Setter @Getter private Boolean autoMarkThreadsRead;
    @Setter @Getter private Boolean availability = true;
    @Setter @Getter private Boolean enabled;
    @Setter @Getter private Boolean hidden;
    @Setter @Getter private Boolean locked;
    @Setter @Getter private Boolean moderated;
    @Setter @Getter private Boolean postFirst;
    @Setter @Getter private Boolean sendEmailOut = false;
    @Setter @Getter private Date closeDate;
    @Setter @Getter private Date openDate;
    @Setter @Getter private Set<DiscussionForum> discussionForumsSet;
    @Setter @Getter private Set<OpenForum> openForumsSet;
    @Setter @Getter private Set<PrivateForum> privateForumsSet;
    @Setter @Getter private Set<DBMembershipItem> membershipItemSet;
    @Setter @Getter private String contextId;
    @Setter @Getter private String name;
    @Setter @Getter private String typeUuid;
    @Setter @Getter private int sendToEmail;

    public List<OpenForum> getOpenForums() {
        return openForumsSet == null ? new ArrayList<>() : new ArrayList<>(openForumsSet);
    }

    public void setOpenForums(List<OpenForum> openForums) {
        this.openForumsSet = openForums == null ? new HashSet<>() : new HashSet<>(openForums);
    }

    public List<PrivateForum> getPrivateForums() {
        return privateForumsSet == null ? new ArrayList<>() : new ArrayList<>(privateForumsSet);
    }

    public void setPrivateForums(List<PrivateForum> privateForums) {
        this.privateForumsSet = privateForums == null ? new HashSet<>() : new HashSet<>(privateForums);
    }

    public List<DiscussionForum> getDiscussionForums() {
        return discussionForumsSet == null ? new ArrayList<>() : new ArrayList<>(discussionForumsSet);
    }

    public void setDiscussionForums(List<DiscussionForum> discussionForums) {
        this.discussionForumsSet = discussionForums == null ? new HashSet<>() : new HashSet<>(discussionForums);
    }

    public String toString() {
        return "Area/" + id;
    }

    public void addPrivateForum(PrivateForum forum) {
        log.debug("addPrivateForum(forum {})", forum);
        if (forum == null) {
            throw new IllegalArgumentException("forum == null");
        }
        if (privateForumsSet == null) {
            privateForumsSet = new HashSet<>();
        }
        forum.setArea(this);
        privateForumsSet.add(forum);
    }

    public void removePrivateForum(PrivateForum forum) {
        log.debug("removePrivateForum(forum {})", forum);
        if (forum == null) {
            throw new IllegalArgumentException("Illegal topic argument passed!");
        }
        forum.setArea(null);
        if (privateForumsSet != null) {
            privateForumsSet.remove(forum);
        }
    }

    public void addDiscussionForum(DiscussionForum forum) {
        log.debug("addForum(forum {})", forum);
        if (forum == null) {
            throw new IllegalArgumentException("forum == null");
        }
        if (discussionForumsSet == null) {
            discussionForumsSet = new HashSet<>();
        }
        forum.setArea(this);
        discussionForumsSet.add(forum);
    }

    public void removeDiscussionForum(DiscussionForum forum) {
        log.debug("removeDiscussionForum(forum {})", forum);
        if (forum == null) {
            throw new IllegalArgumentException("Illegal topic argument passed!");
        }
        forum.setArea(null);
        if (discussionForumsSet != null) {
            discussionForumsSet.remove(forum);
        }
        if (openForumsSet != null) {
            openForumsSet.remove(forum);
        }
    }

    public void addOpenForum(OpenForum forum) {
        log.debug("addOpenForum(forum {})", forum);
        if (forum == null) {
            throw new IllegalArgumentException("forum == null");
        }
        if (openForumsSet == null) {
            openForumsSet = new HashSet<>();
        }
        forum.setArea(this);
        openForumsSet.add(forum);
    }

    public void removeOpenForum(OpenForum forum) {
        log.debug("removeOpenForum(forum {})", forum);
        if (forum == null) {
            throw new IllegalArgumentException("Illegal topic argument passed!");
        }
        forum.setArea(null);
        if (openForumsSet != null) {
            openForumsSet.remove(forum);
        }
    }

    public void addMembershipItem(DBMembershipItem item) {
        log.debug("addMembershipItem(item {})", item);
        if (item == null) {
            throw new IllegalArgumentException("item == null");
        }
        if (membershipItemSet == null) {
            membershipItemSet = new HashSet<>();
        }
        membershipItemSet.add(item);
    }

    public void removeMembershipItem(DBMembershipItem item) {
        log.debug("removeMembershipItem(item {})", item);
        if (item == null) {
            throw new IllegalArgumentException("Illegal level argument passed!");
        }
        if (membershipItemSet != null) {
            membershipItemSet.remove(item);
        }
    }

}
