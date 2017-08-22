/**
 * Copyright (c) 2006-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.entityproviders;

import org.sakaiproject.sitestats.api.Stat;

import lombok.Getter;

/**
 * Wraps a Stat for the purpose of providing a less verbose JSON feed.
 *
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
@Getter
public class StrippedStat {

    private String userId;
    private String siteId;
    private long count = 0L;
    private long date = 0L;

    public StrippedStat(Stat stat) {

        this.userId = stat.getUserId();
        this.siteId = stat.getSiteId();
        this.count = stat.getCount();
        this.date = stat.getDate().getTime();
    }
}
