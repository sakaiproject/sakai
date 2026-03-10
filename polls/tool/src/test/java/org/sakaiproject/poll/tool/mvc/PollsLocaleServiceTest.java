package org.sakaiproject.poll.tool.mvc;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.util.api.LocaleService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PollsLocaleServiceTest {

    @Test
    public void delegatesToKernelLocaleService() {
        LocaleService localeService = mock(LocaleService.class);
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.FRANCE);

        PollsLocaleService service = new PollsLocaleService(localeService);

        Assert.assertEquals(Locale.FRANCE, service.getLocaleForCurrentSiteAndUser());
    }
}
