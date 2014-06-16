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
