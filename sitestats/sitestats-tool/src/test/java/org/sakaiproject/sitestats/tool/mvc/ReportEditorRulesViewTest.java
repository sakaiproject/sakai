/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.mvc.ReportEditorRulesView.TotalOption;

public class ReportEditorRulesViewTest {

    @Test
    public void exposesOrderedImmutableTotalOptionsForTheTemplate() {
        List<TotalOption> options = new ReportEditorRulesView().getTotalOptions();

        assertEquals(StatsManager.T_USER, options.get(0).getValue());
        assertEquals("report_option_user", options.get(0).getLabelKey());
        assertTrue(options.get(0).getAllowedReportTypes().contains("what-events"));
        assertEquals("report_option_resourceaction", options.get(4).getLabelKey());
        assertEquals(StatsManager.T_LASTDATE, options.get(options.size() - 1).getValue());
        assertEquals("report_option_lastdate", options.get(options.size() - 1).getLabelKey());
        assertThrows(UnsupportedOperationException.class,
                () -> options.add(options.get(0)));
    }
}
