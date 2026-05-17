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
