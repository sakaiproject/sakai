package org.sakaiproject.poll.tool.mvc;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PollsLocaleService {

    private final LocaleService localeService;

    public Locale getLocaleForCurrentSiteAndUser() {
        return localeService.getLocaleForCurrentSiteAndUser();
    }
}
