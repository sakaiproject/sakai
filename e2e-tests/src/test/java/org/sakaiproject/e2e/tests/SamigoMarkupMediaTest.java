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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

class SamigoMarkupMediaTest extends SakaiUiTestBase {

    @Test
    void markupAssessmentPreservesEmbeddedMedia() {
        String title = "SAK-52273 " + UUID.randomUUID();
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai\\.samigo"));
        page.navigate(siteUrl);
        sakai.toolClick("Tests");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add").setExact(true)).click();
        page.locator("#authorIndexForm\\:title").fill(title);
        page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName("Create using markup text")).check();
        page.locator("#authorIndexForm\\:createnew").click();
        // HTML produced by the rich text editor, including recorded audio and YouTube embeds.
        page.locator("#samLiteEntryForm\\:data").fill("""
            1. Watch and listen. Is 2 < 3?
            <div class="ckeditor-html5-video"><video controls="controls" src="/media/video.mp4" onclick="alert(1)"></video></div>
            <audio controls="controls" src="/media/recording.webm"></audio>
            <audio controls="controls"><source src="/media/audio.mp3" type="audio/mpeg" /></audio>
            <video controls="controls"><source src="/media/video.mp4" type="video/mp4" /><track src="/media/captions.vtt" kind="captions" srclang="en" label="English" /></video>
            <iframe src="https://www.youtube.com/embed/test" title="YouTube video"></iframe>
            *a. Yes
            b. No
            """);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next >").setExact(true)).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create Assessment").setExact(true)).click();
        Locator row = page.locator("#authorIndexForm\\:coreAssessments tbody tr")
            .filter(new Locator.FilterOptions().setHas(page.getByText(title, new Page.GetByTextOptions().setExact(true))));
        row.getByRole(AriaRole.BUTTON).click();
        row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit").setExact(true)).click();

        Locator question = page.locator(".samigo-question-callout");
        assertThat(question).containsText("Watch and listen. Is 2 < 3?");
        assertThat(question.locator("video[src='/media/video.mp4'][controls]")).hasCount(1);
        assertThat(question.locator("audio[src='/media/recording.webm'][controls]")).hasCount(1);
        assertThat(question.locator("audio source[src='/media/audio.mp3']")).hasCount(1);
        assertThat(question.locator("video source[src='/media/video.mp4']")).hasCount(1);
        assertThat(question.locator("video track[src='/media/captions.vtt'][kind='captions']")).hasCount(1);
        assertThat(question.locator("iframe[src='https://www.youtube.com/embed/test']")).hasCount(1);
        assertThat(question.locator("video[onclick]")).hasCount(0);
        assertThat(question).not().containsText("<video");
        assertThat(question).not().containsText("<audio");
        assertThat(question).not().containsText("<iframe");
    }
}
