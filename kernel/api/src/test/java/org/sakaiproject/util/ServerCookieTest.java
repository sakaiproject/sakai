/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerCookieTest {

    private static final String MAC_CHROME_81 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.92 Safari/537.36";
    private static final String WIN_CHROME_51 = "Mozilla/5.0 doogiePIM/1.0.4.2 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36";
    private static final String WIN_CHROME_66 = "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.6 Safari/537.36";
    private static final String MAC_FIREFOX_74 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:74.0) Gecko/20100101 Firefox/74.0";
    private static final String MAC_SAFARI_13_1 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Safari/605.1.15";
    private static final String MAC_SAFARI_12 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Safari/605.1.15";
    private static final String UC_BROWSER_11_5 = "Mozilla/5.0 (Linux; U; Android 6.0.1; zh-CN; F5121 Build/34.0.A.1.247) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/40.0.2214.89 UCBrowser/11.5.1.944 Mobile Safari/537.36";
    private static final String UC_BROWSER_12_13_5 = "Mozilla/5.0 (Linux; U; Android 10; en-US; Redmi K20 Pro Build/QKQ1.190825.002) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/12.13.5.1209 Mobile Safari/537.36";

    @Test
    public void sameSite() {

        // Chrome
        StringBuffer result = new StringBuffer();
        ServerCookie.appendCookieValue(result, 0, "JSESSIONID", "0f91340e-60ba-4560-9e42-fed0d05bd95d.localhost", "/", null, null, -1, true, false, "lax", MAC_CHROME_81);
        Assert.assertTrue(StringUtils.contains(result.toString(), "SameSite=Lax"));

        result = new StringBuffer();
        ServerCookie.appendCookieValue(result, 0, "JSESSIONID", "0f91340e-60ba-4560-9e42-fed0d05bd95d.localhost", "/", null, null, -1, true, false, "none", WIN_CHROME_51);
        Assert.assertFalse(StringUtils.contains(result.toString(), "SameSite="));

        result = new StringBuffer();
        ServerCookie.appendCookieValue(result, 0, "JSESSIONID", "0f91340e-60ba-4560-9e42-fed0d05bd95d.localhost", "/", null, null, -1, true, false, "none", WIN_CHROME_66);
        Assert.assertFalse(StringUtils.contains(result.toString(), "SameSite="));

        // Safari
        result = new StringBuffer();
        ServerCookie.appendCookieValue(result, 0, "JSESSIONID", "0f91340e-60ba-4560-9e42-fed0d05bd95d.localhost", "/", null, null, -1, true, false, "lax", MAC_SAFARI_13_1);
        Assert.assertTrue(StringUtils.contains(result.toString(), "SameSite=None"));

        result = new StringBuffer();
        ServerCookie.appendCookieValue(result, 0, "JSESSIONID", "0f91340e-60ba-4560-9e42-fed0d05bd95d.localhost", "/", null, null, -1, true, false, "none", MAC_SAFARI_12);
        Assert.assertFalse(StringUtils.contains(result.toString(), "SameSite="));

        // Firefox
        result = new StringBuffer();
        ServerCookie.appendCookieValue(result, 0, "JSESSIONID", "0f91340e-60ba-4560-9e42-fed0d05bd95d.localhost", "/", null, null, -1, true, false, "lax", MAC_FIREFOX_74);
        Assert.assertTrue(StringUtils.contains(result.toString(), "SameSite=Lax"));

        // UC Browser
        result = new StringBuffer();
        ServerCookie.appendCookieValue(result, 0, "JSESSIONID", "0f91340e-60ba-4560-9e42-fed0d05bd95d.localhost", "/", null, null, -1, true, false, "none", UC_BROWSER_11_5);
        Assert.assertFalse(StringUtils.contains(result.toString(), "SameSite="));

        result = new StringBuffer();
        ServerCookie.appendCookieValue(result, 0, "JSESSIONID", "0f91340e-60ba-4560-9e42-fed0d05bd95d.localhost", "/", null, null, -1, true, false, "none", UC_BROWSER_12_13_5);
        Assert.assertTrue(StringUtils.contains(result.toString(), "SameSite=None"));
    }
}
