/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.Test;

public class SiteStatsReportFormTest {

    @Test
    public void createsDefaultDatesInTheEffectiveUserTimeZone() {
        ZoneId zoneId = ZoneId.of("Pacific/Kiritimati");
        LocalDate today = LocalDate.now(zoneId);

        SiteStatsReportForm form = SiteStatsReportForm.create(zoneId);

        assertEquals(today.minusDays(7), form.getWhenFrom());
        assertEquals(today, form.getWhenTo());
    }
}
