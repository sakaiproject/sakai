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
import java.util.Date;

import org.junit.Test;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;

public class SiteStatsReportFormTest {

    @Test
    public void loadsReportsWithNullableDatesInTheEffectiveUserTimeZone() {
        ZoneId zoneId = ZoneId.of("Pacific/Kiritimati");
        Date savedDate = Date.from(Instant.parse("2026-07-16T12:00:00Z"));
        LocalDate expectedDate = LocalDate.of(2026, 7, 17);

        for (Date from : new Date[] {null, savedDate}) {
            for (Date to : new Date[] {null, savedDate}) {
                ReportParams params = new ReportParams("site-id");
                params.setWhen(ReportManager.WHEN_ALL);
                params.setWhenFrom(from);
                params.setWhenTo(to);
                ReportDef report = new ReportDef();
                report.setReportParams(params);

                SiteStatsReportForm form = SiteStatsReportForm.from(report, zoneId);

                assertEquals(ReportManager.WHEN_ALL, form.getWhen());
                assertEquals(from == null ? null : expectedDate, form.getWhenFrom());
                assertEquals(to == null ? null : expectedDate, form.getWhenTo());
            }
        }
    }

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
}
