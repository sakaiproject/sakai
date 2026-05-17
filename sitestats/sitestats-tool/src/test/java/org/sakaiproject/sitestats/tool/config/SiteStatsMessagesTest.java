/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.sitestats.tool.config;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

public class SiteStatsMessagesTest {

    private MessageSource messageSource;

    @Before
    public void setUp() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("Messages");
        messageSource = source;
    }

    @Test
    public void formatsReportTitleInSaveSuccessMessages() {
        assertMessage(new Locale("ro", "RO"), "Raportul 'Quarterly' a fost salvat cu succes");
        assertMessage(new Locale("sr"), "Izveštaj 'Quarterly' je uspešno sačuvan");
        assertMessage(new Locale("es"), "El informe 'Quarterly' ha sido guardado con éxito");
    }

    private void assertMessage(Locale locale, String expected) {
        assertEquals(expected, messageSource.getMessage(
                "report_save_success", new Object[] { "Quarterly" }, "report_save_success", locale));
    }
}
