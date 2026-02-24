/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2025 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link IframeUrlUtil}.
 *
 * @see <a href="https://jira.sakaiproject.org/browse/SAK-52376">SAK-52376</a>
 */
public class IframeUrlUtilTest {

    private static final String BASE = "http://localhost:8080";

    @Test
    public void testNullOrEmptySource() {
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai(null, BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("   ", BASE));
    }

    @Test
    public void testNullOrEmptyBaseUrl() {
        // Relative URLs are local regardless of base (resolved against current origin)
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("/portal/site/abc", null));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("/portal/site/abc", ""));
        // Absolute URLs cannot be validated without a valid base
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("http://localhost:8080/portal", null));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("http://localhost:8080/portal", ""));
    }

    @Test
    public void testDangerousSchemes() {
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("javascript:alert(1)", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("data:text/html,<script>", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("file:///etc/passwd", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("mailto:admin@example.com", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("about:blank", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("JAVASCRIPT:evil()", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("DATA:text/html,foo", BASE));
    }

    @Test
    public void testProtocolRelativeUrls() {
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("//example.com/path", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("//localhost:8080/portal", BASE));
    }

    @Test
    public void testRelativeUrlsAreLocal() {
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("/portal/site/abc", BASE));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("/", BASE));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("./path", BASE));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("../other/path", BASE));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("path", BASE));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("path/with/slashes", BASE));
    }

    @Test
    public void testSameOriginAbsoluteUrls() {
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("http://localhost:8080/portal/site/x", BASE));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("http://localhost:8080/", BASE));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("https://localhost:8443/tool", "https://localhost:8443"));
    }

    @Test
    public void testDifferentScheme() {
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("https://localhost:8080/portal", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("http://localhost:8443/portal", "https://localhost:8443"));
    }

    @Test
    public void testDifferentHost() {
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("http://example.com/portal", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("http://127.0.0.1:8080/portal", BASE));
    }

    @Test
    public void testDifferentPort() {
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("http://localhost:9090/portal", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("http://localhost/portal", BASE));
    }

    @Test
    public void testDefaultPortsMatch() {
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("http://localhost:80/portal", "http://localhost"));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("http://localhost/portal", "http://localhost:80"));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("https://localhost:443/portal", "https://localhost"));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("https://localhost/portal", "https://localhost:443"));
    }

    @Test
    public void testHostAndSchemeCaseNormalization() {
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("HTTP://LOCALHOST:8080/portal", BASE));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("http://LocalHost:8080/portal", BASE));
    }

    @Test
    public void testWhitespaceTrimming() {
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("  /portal/site/abc  ", BASE));
        Assert.assertTrue(IframeUrlUtil.isLocalToSakai("  http://localhost:8080/portal  ", BASE + "  "));
    }

    @Test
    public void testMalformedAbsoluteUrl() {
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("http://", BASE));
        Assert.assertFalse(IframeUrlUtil.isLocalToSakai("not-a-valid-uri:://foo", BASE));
    }
}
