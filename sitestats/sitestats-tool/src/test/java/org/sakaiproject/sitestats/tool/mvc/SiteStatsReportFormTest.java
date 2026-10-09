/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.Test;

public class SiteStatsReportFormTest {

    @Test
    public void createsDefaultDatesInTheEffectiveUserTimeZone() {
        ZoneId zoneId = ZoneId.of("Pacific/Kiritimati");
        Instant instant = Instant.parse("2026-07-16T12:00:00Z");
        Clock clock = Clock.fixed(instant, zoneId);
        LocalDate today = LocalDate.ofInstant(instant, zoneId);

        SiteStatsReportForm form = SiteStatsReportForm.create(clock);

        assertEquals(today.minusDays(7), form.getWhenFrom());
        assertEquals(today, form.getWhenTo());
    }
    @Test
    public void parsesResourceIdsConsistentlyAcrossLineEndingsAndBlankLines() {
        SiteStatsReportForm form = new SiteStatsReportForm();
        assertEquals(List.of(), form.resourceIdList());
        form.setWhatResourceIds(" /group/site/one.txt \r\n \r/group/site/two.txt\n\n");
        assertEquals(List.of("/group/site/one.txt", "/group/site/two.txt"), form.resourceIdList());
    }

}
