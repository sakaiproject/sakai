/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.ui.reporting.util;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class ScormDurationFormatterTest
{
    @Test
    public void formatsIsoDurationWithFractions()
    {
        String formatted = ScormDurationFormatter.format("PT1M4.79S", Locale.US);
        Assert.assertEquals("1 min, 4.79 s", formatted);
    }

    @Test
    public void formatOrNullReturnsNullForBlank()
    {
        Assert.assertNull(ScormDurationFormatter.formatOrNull("   ", Locale.US));
    }

    @Test
    public void formatOrNullFallsBackToOriginalWhenParsingFails()
    {
        String bogus = "n/a";
        Assert.assertEquals(bogus, ScormDurationFormatter.formatOrNull(bogus, Locale.US));
    }
}
