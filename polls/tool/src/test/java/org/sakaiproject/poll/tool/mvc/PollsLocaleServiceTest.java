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
