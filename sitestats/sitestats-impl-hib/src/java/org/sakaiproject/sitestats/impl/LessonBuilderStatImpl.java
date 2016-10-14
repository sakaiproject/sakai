/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
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
package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.ObjectUtils;

import org.sakaiproject.sitestats.api.LessonBuilderStat;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
@Getter @Setter
public class LessonBuilderStatImpl implements LessonBuilderStat, Serializable {

    private static final long serialVersionUID    = 1L;

    private long id;
    private String userId;
    private String siteId;
    private String pageRef;
    private String pageAction;
    private String pageTitle;
    private long pageId;
    private long count;
    private Date date;

    public boolean equals(Object o) {

        if(o == null) return false;
        if(!(o instanceof LessonBuilderStatImpl)) return false;
        LessonBuilderStatImpl other = (LessonBuilderStatImpl) o;
        return id == other.getId()
                && ObjectUtils.equals(siteId, other.getSiteId())
                && ObjectUtils.equals(userId, other.getUserId())
                && ObjectUtils.equals(pageRef, other.getPageRef())
                && ObjectUtils.equals(pageAction, other.getPageAction())
                && ObjectUtils.equals(pageTitle, other.getPageTitle())
                && pageId == other.getPageId()
                && count == other.getCount()
                && ObjectUtils.equals(date, other.getDate());
    }

    @Override
    public int compareTo(LessonBuilderStat other) {

        int val = ObjectUtils.compare(siteId, other.getSiteId());
        if (val != 0) return val;
        val = ObjectUtils.compare(userId, other.getUserId());
        if (val != 0) return val;
        val = ObjectUtils.compare(pageRef, other.getPageRef());
        if (val != 0) return val;
        val = ObjectUtils.compare(pageAction, other.getPageAction());
        if (val != 0) return val;
        val = ObjectUtils.compare(pageTitle, other.getPageTitle());
        if (val != 0) return val;
        val = Long.signum(pageId - other.getPageId());
        if (val != 0) return val;
        val = ObjectUtils.compare(date, other.getDate());
        if (val != 0) return val;
        val = Long.signum(count - other.getCount());
        if (val != 0) return val;
        val = Long.signum(id - other.getId());
        return val;
    }

    public int hashCode() {

        if (siteId == null) return Integer.MIN_VALUE;
        String hashStr = this.getClass().getName() + ":"
                + id
                + ObjectUtils.hashCode(userId)
                + ObjectUtils.hashCode(siteId)
                + ObjectUtils.hashCode(pageRef)
                + ObjectUtils.hashCode(pageAction)
                + ObjectUtils.hashCode(pageTitle)
                + pageId
                + count
                + ObjectUtils.hashCode(date);
        return hashStr.hashCode();
    }

    public String toString() {

        return siteId + " : " + userId + " : " + pageRef + " : "
                    + pageAction + ":" + pageTitle + " : " + pageId + ":" + count + " : " + date;
    }
}
