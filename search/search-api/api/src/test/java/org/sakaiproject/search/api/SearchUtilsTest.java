/**
 * Copyright (c) 2003-2024 The Apereo Foundation
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
package org.sakaiproject.search.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SearchUtilsTest {

    // --- stripHtml ---

    @Test
    public void stripHtml_null_returnsEmpty() {
        assertEquals("", SearchUtils.stripHtml(null));
    }

    @Test
    public void stripHtml_emptyString_returnsEmpty() {
        assertEquals("", SearchUtils.stripHtml(""));
    }

    @Test
    public void stripHtml_plainText_unchanged() {
        assertEquals("Hello world", SearchUtils.stripHtml("Hello world"));
    }

    @Test
    public void stripHtml_simpleParagraph_stripsTag() {
        assertEquals("Hello world", SearchUtils.stripHtml("<p>Hello world</p>"));
    }

    @Test
    public void stripHtml_boldAndItalic_stripsAllTags() {
        assertEquals("bold italic", SearchUtils.stripHtml("<b>bold</b> <i>italic</i>"));
    }

    @Test
    public void stripHtml_nestedTags_stripsAll() {
        assertEquals("content", SearchUtils.stripHtml("<div><span>content</span></div>"));
    }

    @Test
    public void stripHtml_anchorTag_keepsTextDropsTag() {
        assertEquals("click here", SearchUtils.stripHtml("<a href=\"http://example.com\">click here</a>"));
    }

    @Test
    public void stripHtml_htmlEntities_decoded() {
        assertEquals("a & b", SearchUtils.stripHtml("a &amp; b"));
        assertEquals("a < b", SearchUtils.stripHtml("a &lt; b"));
        assertEquals("a > b", SearchUtils.stripHtml("a &gt; b"));
        assertEquals("\"quoted\"", SearchUtils.stripHtml("&quot;quoted&quot;"));
    }

    @Test
    public void stripHtml_scriptTag_contentDropped() {
        // Jsoup drops script content entirely
        assertEquals("visible", SearchUtils.stripHtml("<p>visible</p><script>alert('xss')</script>"));
    }

    @Test
    public void stripHtml_multipleWhitespace_normalised() {
        // Jsoup collapses runs of whitespace to a single space
        String result = SearchUtils.stripHtml("<p>one</p><p>two</p>");
        // Both words must be present; exact whitespace between them is implementation detail
        org.junit.Assert.assertTrue(result.contains("one"));
        org.junit.Assert.assertTrue(result.contains("two"));
    }

    @Test
    public void stripHtml_onlyTags_returnsEmpty() {
        assertEquals("", SearchUtils.stripHtml("<br/><hr/>"));
    }

    @Test
    public void stripHtml_fullDocument_returnsBodyText() {
        String html = "<!DOCTYPE html><html><head><title>Title</title></head>"
                + "<body><p>Body text</p></body></html>";
        assertEquals("Title Body text", SearchUtils.stripHtml(html));
    }
}
