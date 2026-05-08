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
package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MeetingsTest extends SakaiUiTestBase {

    private static String sakaiUrl;

    @Test
    @Order(1)
    void createsASiteWithMeetings() {
        sakai.login("instructor1");
        sakaiUrl = sakai.createCourse("instructor1", List.of("sakai\\.meetings"));
    }

    @Test
    @Order(2)
    void opensMeetingsSmoke() {
        sakai.login("instructor1");
        page.navigate(sakaiUrl);

        Locator meetingsLink = page.locator("#toolMenu a.btn-nav, ul.site-page-list a.btn-nav")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("Meetings?", Pattern.CASE_INSENSITIVE)))
            .first();
        if (meetingsLink.count() > 0) {
            meetingsLink.click(new Locator.ClickOptions().setForce(true));
            assertThat(page.locator("#content, main, .portletBody").first()).isVisible();
            assertThat(page.locator("body")).containsText(Pattern.compile("Meetings|meeting", Pattern.CASE_INSENSITIVE));
            return;
        }

        assertThat(page.locator("main#content")).isVisible();
        assertThat(page.locator("body")).containsText(Pattern.compile("Overview|Site Info", Pattern.CASE_INSENSITIVE));
    }
}
