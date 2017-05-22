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
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import org.sakaiproject.sitestats.api.LessonBuilderStat;

import lombok.Data;

/**
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
@Data
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

    @Override
    public int compareTo(LessonBuilderStat other) {

        int val = Objects.compare(siteId, other.getSiteId(), Comparator.nullsFirst(String::compareToIgnoreCase));
        if (val != 0) return val;
        val = Objects.compare(userId, other.getUserId(), Comparator.nullsFirst(String::compareToIgnoreCase));
        if (val != 0) return val;
        val = Objects.compare(pageRef, other.getPageRef(), Comparator.nullsFirst(String::compareToIgnoreCase));
        if (val != 0) return val;
        val = Objects.compare(pageAction, other.getPageAction(), Comparator.nullsFirst(String::compareToIgnoreCase));
        if (val != 0) return val;
        val = Objects.compare(pageTitle, other.getPageTitle(), Comparator.nullsFirst(String::compareToIgnoreCase));
        if (val != 0) return val;
        val = Long.signum(pageId - other.getPageId());
        if (val != 0) return val;
        val = Objects.compare(date, other.getDate(), Comparator.nullsFirst(Date::compareTo));
        if (val != 0) return val;
        val = Long.signum(count - other.getCount());
        if (val != 0) return val;
        val = Long.signum(id - other.getId());
        return val;
    }
}
