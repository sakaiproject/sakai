/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2010 Sakai Foundation
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

/* Derived from Apache Tomcat 5.5.31 ServerCookie.java. Modified for Sakai by 
 * changing package name and removing methods not required. Original license below. */

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 *  Server-side cookie representation.
 *  Allows recycling and uses MessageBytes as low-level
 *  representation ( and thus the byte-> char conversion can be delayed
 *  until we know the charset ).
 *
 *  Tomcat.core uses this recyclable object to represent cookies,
 *  and the facade will convert it to the external representation.
 */
public class ServerCookie {

    private static final String OLD_COOKIE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";
    private static final ThreadLocal<DateFormat> OLD_COOKIE_FORMAT =
            ThreadLocal.withInitial(() -> {
                DateFormat df = new SimpleDateFormat(OLD_COOKIE_PATTERN, Locale.US);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df;
            });

    private static final String ancientDate = OLD_COOKIE_FORMAT.get().format(new Date(10000));

    /**
     * If set to true, we parse cookies according to the servlet spec,
     */
    private static final boolean STRICT_SERVLET_COMPLIANCE =
            Boolean.parseBoolean(System.getProperty("org.apache.catalina.STRICT_SERVLET_COMPLIANCE", "false"));

    /**
     * If set to false, we don't use the IE6/7 Max-Age/Expires work around
     */
    private static final boolean ALWAYS_ADD_EXPIRES =
            Boolean.parseBoolean(System.getProperty("org.apache.tomcat.util.http.ServerCookie.ALWAYS_ADD_EXPIRES", "true"));

    private static final String tspecials = ",; ";
    private static final String tspecials2 = "()<>@,;:\\\"/[]?={} \t";
    private static final String tspecials2NoSlash = "()<>@,;:\\\"[]?={} \t";

    private static final String IOS_VERSION_REGEX = "\\(iP.+; CPU .*OS (\\d+)[_\\d]*.*\\) AppleWebKit/";
    private static final String MACOS_VERSION_REGEX = "\\(Macintosh;.*Mac OS X (\\d+)_(\\d+)[_\\d]*.*\\) AppleWebKit/";
    private static final String SAFARI_REGEX = "Version/.* Safari/";
    private static final String MAC_EMBEDDED_REGEX = "^Mozilla/[.\\d]+ \\(Macintosh;.*Mac OS X [_\\d]+\\) AppleWebKit/[.\\d]+ \\(KHTML, like Gecko\\)$";
    private static final String CHROMIUM_REGEX = "Chrom(e|ium)";
    private static final String CHROMIUM_VERSION_REGEX = "Chrom[^ /]+/(\\d+)[.\\d]* ";
    private static final String UC_REGEX = "UCBrowser/";
    private static final String UC_VERSION_REGEX = "UCBrowser/(\\d+)\\.(\\d+)\\.(\\d+)[.\\d]* ";

    private static final Pattern iosVersion = Pattern.compile(IOS_VERSION_REGEX);
    private static final Pattern macosVersion = Pattern.compile(MACOS_VERSION_REGEX);
    private static final Pattern safari = Pattern.compile(SAFARI_REGEX);
    private static final Pattern macEmbedded = Pattern.compile(MAC_EMBEDDED_REGEX);
    private static final Pattern chromium = Pattern.compile(CHROMIUM_REGEX);
    private static final Pattern chromiumVersion = Pattern.compile(CHROMIUM_VERSION_REGEX);
    private static final Pattern ucBrowser = Pattern.compile(UC_REGEX);
    private static final Pattern ucBrowserVersion = Pattern.compile(UC_VERSION_REGEX);

    /**
     * Tests a string and returns true if the string counts as a
     * reserved token in the Java language.
     *
     * @param value     the <code>String</code> to be tested
     *
     * @return          <code>true</code> if the <code>String</code> is
     *                  a reserved token; <code>false</code>
     *                  if it is not
     */
    private static boolean isToken(String value, String literals) {
        String tspecials = (literals == null ? ServerCookie.tspecials : literals);
        if (value == null) return true;
        int len = value.length();

        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);

            if (tspecials.indexOf(c) != -1)
                return false;
        }
        return true;
    }

    private static boolean containsCTL(String value) {
        if (value == null) return false;
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c < 0x20 || c >= 0x7f) {
                if (c == 0x09)
                    continue; //allow horizontal tabs
                return true;
            }
        }
        return false;
    }

    private static boolean isToken2(String value, String literals) {
        String tspecials2 = (literals == null ? ServerCookie.tspecials2 : literals);
        if (value == null) return true;
        int len = value.length();

        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);

            if (tspecials2.indexOf(c) != -1)
                return false;
        }
        return true;
    }

    public static void appendCookieValue(StringBuffer headerBuf,
                                         int version,
                                         String name,
                                         String value,
                                         String path,
                                         String domain,
                                         String comment,
                                         int maxAge,
                                         boolean isSecure,
                                         boolean isHttpOnly,
                                         String sameSite,
                                         String userAgent) {
        StringBuffer buf = new StringBuffer();
        // Servlet implementation checks name
        buf.append(name);
        buf.append("=");
        // Servlet implementation does not check anything else

        version = maybeQuote2(version, buf, value, true);

        // Add version 1 specific information
        if (version == 1) {
            // Version=1 ... required
            buf.append("; Version=1");

            // Comment=comment
            if (comment != null) {
                buf.append("; Comment=");
                maybeQuote2(version, buf, comment);
            }
        }

        // Add domain information, if present
        if (domain != null) {
            buf.append("; Domain=");
            maybeQuote2(version, buf, domain);
        }

        // Max-Age=secs ... or use old "Expires" format
        // TODO RFC2965 Discard
        if (maxAge >= 0) {
            if (version > 0) {
                buf.append("; Max-Age=");
                buf.append(maxAge);
            }
            // IE6, IE7 and possibly other browsers don't understand Max-Age.
            // They do understand Expires, even with V1 cookies!
            if (version == 0 || ALWAYS_ADD_EXPIRES) {
                // Wdy, DD-Mon-YY HH:MM:SS GMT ( Expires Netscape format )
                buf.append("; Expires=");
                // To expire immediately we need to set the time in past
                if (maxAge == 0)
                    buf.append(ancientDate);
                else
                    ((DateFormat) OLD_COOKIE_FORMAT.get()).format(
                            new Date(System.currentTimeMillis() +
                                    maxAge * 1000L),
                            buf, new FieldPosition(0));
            }
        }

        // Path=path
        if (path != null) {
            buf.append("; Path=");
            if (version == 0) {
                maybeQuote2(version, buf, path);
            } else {
                maybeQuote2(version, buf, path, ServerCookie.tspecials2NoSlash, false);
            }
        }

        // Secure
        if (isSecure) {
            buf.append("; Secure");
        }

        // HttpOnly
        if (isHttpOnly) {
            buf.append("; HttpOnly");
        }

        // SameSite
        if (StringUtils.equalsIgnoreCase("none", sameSite)) {
            // SameSite=None signals that the cookie data can be shared with third parties/external sites
            if (shouldSendSameSiteNone(userAgent)) {
                buf.append("; SameSite=None");
            }
            // some browsers are not compatible with this option as it was not among the original options
            // so we forgo setting the header completely
        } else if (StringUtils.equalsIgnoreCase("lax", sameSite)) {
            // SameSite=Lax enables only first-party cookies to be sent/accessed
            if (shouldNotSendSameSiteLaxOrStrict(userAgent)) {
                // some browsers just don't play nice
                if (shouldSendSameSiteNone(userAgent)) {
                    buf.append("; SameSite=None");
                }
            } else {
                buf.append("; SameSite=Lax");
            }
        } else {
            // SameSite=Strict is a subset of 'lax' and won’t fire if the incoming link is from an external site
            // this is the default for most browsers nowadays
            if (shouldNotSendSameSiteLaxOrStrict(userAgent)) {
                if (shouldSendSameSiteNone(userAgent)) {
                    buf.append("; SameSite=None");
                }
            } else {
                buf.append("; SameSite=Strict");
            }
        }

        headerBuf.append(buf);
    }

    private static boolean alreadyQuoted(String value) {
        if (value == null || value.length() == 0) return false;
        return (value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"');
    }

    /**
     * Quotes values using rules that vary depending on Cookie version.
     *
     * @param version
     * @param buf
     * @param value
     */
    private static int maybeQuote2(int version, StringBuffer buf, String value) {
        return maybeQuote2(version, buf, value, false);
    }

    private static int maybeQuote2(int version, StringBuffer buf, String value, boolean allowVersionSwitch) {
        return maybeQuote2(version, buf, value, null, allowVersionSwitch);
    }

    private static int maybeQuote2(int version, StringBuffer buf, String value, String literals, boolean allowVersionSwitch) {
        if (value == null || value.length() == 0) {
            buf.append("\"\"");
        } else if (containsCTL(value))
            throw new IllegalArgumentException("Control character in cookie value, consider BASE64 encoding your value");
        else if (alreadyQuoted(value)) {
            buf.append('"');
            buf.append(escapeDoubleQuotes(value, 1, value.length() - 1));
            buf.append('"');
        } else if (allowVersionSwitch && (!STRICT_SERVLET_COMPLIANCE) && version == 0 && !isToken2(value, literals)) {
            buf.append('"');
            buf.append(escapeDoubleQuotes(value, 0, value.length()));
            buf.append('"');
            version = 1;
        } else if (version == 0 && !isToken(value, literals)) {
            buf.append('"');
            buf.append(escapeDoubleQuotes(value, 0, value.length()));
            buf.append('"');
        } else if (version == 1 && !isToken2(value, literals)) {
            buf.append('"');
            buf.append(escapeDoubleQuotes(value, 0, value.length()));
            buf.append('"');
        } else {
            buf.append(value);
        }
        return version;
    }

    /**
     * Escapes any double quotes in the given string.
     *
     * @param s          the input string
     * @param beginIndex start index inclusive
     * @param endIndex   exclusive
     * @return The (possibly) escaped string
     */
    private static String escapeDoubleQuotes(String s, int beginIndex, int endIndex) {

        if (s == null || s.length() == 0 || s.indexOf('"') == -1) {
            return s;
        }

        StringBuffer b = new StringBuffer();
        for (int i = beginIndex; i < endIndex; i++) {
            char c = s.charAt(i);
            if (c == '\\' ) {
                b.append(c);
                //ignore the character after an escape, just append it
                if (++i>=endIndex) throw new IllegalArgumentException("Invalid escape character in cookie value.");
                b.append(s.charAt(i));
            } else if (c == '"')
                b.append('\\').append('"');
            else
                b.append(c);
        }

        return b.toString();
    }

    private static boolean shouldSendSameSiteNone(String userAgent) {
        // modeled from psuedo code at
        // https://www.chromium.org/updates/same-site/incompatible-clients
        return  !isSameSiteNoneIncompatible(StringUtils.trimToEmpty(userAgent));
    }

    private static boolean shouldNotSendSameSiteLaxOrStrict(String userAgent) {
        // Safari download manager will not send cookies to Lax or Strict
        return hasWebKitDownloadManagerBug(StringUtils.trimToEmpty(userAgent));
    }

    private static boolean isSameSiteNoneIncompatible(String userAgent) {
        return hasWebKitSameSiteBug(userAgent) || dropsUnrecognizedSameSiteCookies(userAgent);
    }

    private static boolean hasWebKitSameSiteBug(String userAgent) {
        return isIosVersion(12, userAgent)
                || (isMacosxVersion(10, 14, userAgent) && (isSafari(userAgent)
                || isMacEmbeddedBrowser(userAgent)));
    }

    private static boolean hasWebKitDownloadManagerBug(String userAgent) {
        return isMacosxVersion(10, 15, userAgent) && (isSafari(userAgent));
    }

    private static boolean dropsUnrecognizedSameSiteCookies(String userAgent) {
        if (isUcBrowser(userAgent)) {
            return !isUcBrowserVersionAtLeast(12, 13, 2, userAgent);
        }
        return isChromiumBased(userAgent)
                && isChromiumVersionAtLeast(51, userAgent)
                && !isChromiumVersionAtLeast(67, userAgent);
    }


    private static boolean isIosVersion(int major, String userAgent) {
        Matcher matcher = iosVersion.matcher(userAgent);
        if (matcher.find()) {
            return matcher.group(1).equals(Integer.toString(major));
        }
        return false;
    }

    private static boolean isMacosxVersion(int major, int minor, String userAgent) {
        Matcher matcher = macosVersion.matcher(userAgent);
        if (matcher.find() && matcher.groupCount() == 2) {
            return (matcher.group(1).equals(Integer.toString(major))) && (matcher.group(2).equals(Integer.toString(minor)));
        }
        return false;
    }

    private static boolean isSafari(String userAgent) {
        Matcher matcher = safari.matcher(userAgent);
        return matcher.find() && !isChromiumBased(userAgent);
    }

    private static boolean isMacEmbeddedBrowser(String userAgent) {
        return macEmbedded.matcher(userAgent).find();
    }

    private static boolean isChromiumBased(String userAgent) {
        return chromium.matcher(userAgent).find();
    }

    private static boolean isChromiumVersionAtLeast(int major, String userAgent) {
        Matcher matcher = chromiumVersion.matcher(userAgent);
        if (matcher.find()) {
            String version = matcher.group(1);
            if (StringUtils.isNumeric(version)) {
                int v = Integer.parseInt(version);
                return v >= major;
            }
        }
        return false;
    }

    private static boolean isUcBrowser(String userAgent) {
        return ucBrowser.matcher(userAgent).find();
    }

    private static boolean isUcBrowserVersionAtLeast(int major, int minor, int build, String userAgent) {
        Matcher matcher = ucBrowserVersion.matcher(userAgent);
        if (matcher.find() && matcher.groupCount() == 3) {
            String majorVersion = matcher.group(1);
            String minorVersion = matcher.group(2);
            String buildVersion = matcher.group(3);
            if (StringUtils.isNumeric(majorVersion)) {
                int majorVer = Integer.parseInt(majorVersion);
                if (majorVer != major) return majorVer > major;
            }
            if (StringUtils.isNumeric(minorVersion)) {
                int minorVer = Integer.parseInt(minorVersion);
                if (minorVer != minor) return minorVer > minor;
            }
            if (StringUtils.isNumeric(buildVersion)) {
                int buildVer = Integer.parseInt(buildVersion);
                return buildVer >= build;
            }
        }
        return false;
    }
}

