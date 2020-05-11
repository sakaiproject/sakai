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
package org.sakaiproject.lessonbuildertool.ccexport;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CCUtils {

    @Setter private SimplePageToolDao simplePageToolDao;

    public void outputIndent(ZipPrintStream out, int indent) {
        StringBuffer buffer = new StringBuffer(indent);
        IntStream.range(0, indent).forEach(i -> buffer.append(" "));
        out.print(buffer.toString());
    }

    public String fixup(CCConfig ccConfig, String text, CCResourceItem CCResourceItem) {
        // http://lessonbuilder.sakaiproject.org/53605/
        StringBuilder ret = new StringBuilder();
        String sakaiIdBase = "/group/" + ccConfig.getSiteId();
        // I'm matching against /access/content/group not /access/content/group/SITEID, because SITEID can in some installations
        // be user chosen. In that case there could be escaped characters, and the escaping in HTML URL's isn't unique.
        Pattern target = Pattern.compile("(?:https?:)?(?://[-a-zA-Z0-9.]+(?::[0-9]+)?)?/access/content(/group/)|http://lessonbuilder.sakaiproject.org/", Pattern.CASE_INSENSITIVE);
        Matcher matcher = target.matcher(text);
        // technically / isn't allowed in an unquoted attribute, but sometimes people
        // use sloppy HTML
        Pattern wordend = Pattern.compile("[^-a-zA-Z0-9._:/]");
        int index = 0;
        while (true) {
            if (!matcher.find()) {
                ret.append(text.substring(index));
                break;
            }
            String sakaiId = null;
            int start = matcher.start();
            if (matcher.start(1) >= 0) { // matched /access/content...
                // make sure it's the right siteid. This approach will get it no matter
                // how the siteid is url encoded
                int startsite = matcher.end(1);
                int last = text.indexOf("/", startsite);
                if (last < 0) continue;
                String sitepart = null;
                try {
                    sitepart = URLDecoder.decode(text.substring(startsite, last), "UTF-8");
                } catch (Exception e) {
                    log.info("Decode failed in export, {}", e.toString());
                }

                if (!ccConfig.getSiteId().equals(sitepart)) continue;

                // it matches, now map it
                // unfortunately the hostname and port are a bit unpredictable. Don't use them for match. I think siteids are
                // unique enough that if /access/content/group/SITEID matches that should be enough
                int sakaistart = matcher.start(1); //start of sakaiid, can't find end until we figure out quoting

                // need to find sakaiend. To do that we need to find the close quote
                int sakaiend = 0;
                char quote = text.charAt(start - 1);
                if (quote == '\'' || quote == '"') {
                    // quoted, this is easy
                    sakaiend = text.indexOf(quote, sakaistart);
                } else { // not quoted. find first char not legal in unquoted attribute
                    Matcher wordendMatch = wordend.matcher(text);
                    if (wordendMatch.find(sakaistart)) {
                        sakaiend = wordendMatch.start();
                    } else
                        sakaiend = text.length();
                }
                try {
                    sakaiId = removeDotDot(URLDecoder.decode(text.substring(sakaistart, sakaiend), "UTF-8"));
                } catch (Exception e) {
                    log.info("Decoding url, {}", e.toString());
                }
                ret.append(text, index, start);
                ret.append("$IMS-CC-FILEBASE$..");
                ret.append(removeDotDot(text.substring(last, sakaiend)));
                index = sakaiend;  // start here next time
            } else { // matched http://lessonbuilder.sakaiproject.org/
                int last = matcher.end(); // should be start of an integer
                int endnum = text.length();  // end of the integer
                for (int i = last; i < text.length(); i++) {
                    if ("0123456789".indexOf(text.charAt(i)) < 0) {
                        endnum = i;
                        break;
                    }
                }
                String numString = text.substring(last, endnum);
                if (numString.length() >= 1) {
                    Long itemId = new Long(numString);
                    SimplePageItem item = simplePageToolDao.findItem(itemId);
                    sakaiId = item.getSakaiId();
                    int itemType = item.getType();
                    if ((itemType == SimplePageItem.RESOURCE || itemType == SimplePageItem.MULTIMEDIA) &&
                            sakaiId.startsWith(sakaiIdBase)) {
                        ret.append(text, index, start);
                        ret.append("$IMS-CC-FILEBASE$.." + sakaiId.substring(sakaiIdBase.length()));
                        if (text.charAt(endnum) == '/')
                            endnum++;
                        index = endnum;
                    }
                }
            }
            if (sakaiId != null) {
                CCResourceItem r = ccConfig.getFileMap().get(sakaiId);
                if (r != null) {
                    CCResourceItem.getDependencies().add(r.getResourceId());
                }
            }
        }
        return StringEscapeUtils.escapeXml11(ret.toString());
    }

    // turns the links into relative links
    // fixups will get a list of offsets where fixups were done, for loader to reconstitute HTML
    public String relFixup(CCConfig ccConfig, String text, CCResourceItem CCResourceItem, StringBuilder fixups) {
        // http://lessonbuilder.sakaiproject.org/53605/
        StringBuilder ret = new StringBuilder();
        String sakaiIdBase = "/group/" + ccConfig.getSiteId();
        // I'm matching against /access/content/group not /access/content/group/SITEID, because SITEID can in some installations
        // be user chosen. In that case there could be escaped characters, and the escaping in HTML URL's isn't unique.
        Pattern target = Pattern.compile("(?:https?:)?(?://[-a-zA-Z0-9.]+(?::[0-9]+)?)?/access/content(/group/)|http://lessonbuilder.sakaiproject.org/", Pattern.CASE_INSENSITIVE);
        Matcher matcher = target.matcher(text);
        // technically / isn't allowed in an unquoted attribute, but sometimes people
        // use sloppy HTML
        Pattern wordend = Pattern.compile("[^-a-zA-Z0-9._:/]");
        int index = 0;
        while (true) {
            if (!matcher.find()) {
                ret.append(text.substring(index));
                break;
            }
            String sakaiId = null;
            int start = matcher.start();
            if (matcher.start(1) >= 0) { // matched /access/content...
                // make sure it's the right siteid. This approach will get it no matter
                // how the siteid is url encoded
                int startsite = matcher.end(1);
                int last = text.indexOf("/", startsite);
                if (last < 0) continue;
                String sitepart = null;
                try {
                    sitepart = URLDecoder.decode(text.substring(startsite, last), "UTF-8");
                } catch (Exception e) {
                    log.info("Decode failed in export, {}", e.toString());
                }

                if (!ccConfig.getSiteId().equals(sitepart)) continue;

                int sakaistart = matcher.start(1); //start of sakaiid, can't find end until we figure out quoting

                // need to find sakaiend. To do that we need to find the close quote
                int sakaiend = 0;
                char quote = text.charAt(start - 1);
                if (quote == '\'' || quote == '"') {
                    // quoted, this is easy
                    sakaiend = text.indexOf(quote, sakaistart);
                } else { // not quoted. find first char not legal in unquoted attribute
                    Matcher wordendMatch = wordend.matcher(text);
                    if (wordendMatch.find(sakaistart)) {
                        sakaiend = wordendMatch.start();
                    } else
                        sakaiend = text.length();
                }
                try {
                    sakaiId = removeDotDot(URLDecoder.decode(text.substring(sakaistart, sakaiend), "UTF-8"));
                } catch (Exception e) {
                    log.info("Exception in CCExport URLDecoder " + e);
                }
                // do the mapping. resource.location is a relative URL of the page we're looking at
                // sakaiid is the URL of the object, starting /group/
                String base = getParent(CCResourceItem.getLocation());
                String thisref = sakaiId.substring(sakaiIdBase.length() + 1);
                String relative = relativize(thisref, base);
                ret.append(text, index, start);
                // we're now at start of URL. save it for fixup list
                if (fixups != null) {
                    if (fixups.length() > 0) fixups.append(",");
                    fixups.append("" + ret.length());
                }
                // and now add the new relative URL
                ret.append(relative);
                index = sakaiend;  // start here next time
            } else { // matched http://lessonbuilder.sakaiproject.org/
                int last = matcher.end(); // should be start of an integer
                int endnum = text.length();  // end of the integer
                for (int i = last; i < text.length(); i++) {
                    if ("0123456789".indexOf(text.charAt(i)) < 0) {
                        endnum = i;
                        break;
                    }
                }
                String numString = text.substring(last, endnum);
                if (numString.length() >= 1) {
                    Long itemId = new Long(numString);
                    SimplePageItem item = simplePageToolDao.findItem(itemId);
                    sakaiId = item.getSakaiId();
                    int itemType = item.getType();
                    if ((itemType == SimplePageItem.RESOURCE || itemType == SimplePageItem.MULTIMEDIA) && sakaiId.startsWith(sakaiIdBase)) {
                        ret.append(text, index, start);
                        String base = getParent(CCResourceItem.getLocation());
                        String thisref = sakaiId.substring(sakaiIdBase.length() + 1);
                        String relative = relativize(thisref, base);
                        // we're now at start of URL. save it for fixup list
                        if (fixups != null) {
                            if (fixups.length() > 0)
                                fixups.append(",");
                            fixups.append("" + ret.length());
                        }
                        // and now add the new relative URL
                        ret.append(relative);
                        if (text.charAt(endnum) == '/')
                            endnum++;
                        index = endnum;
                    }
                }
            }
            if (sakaiId != null) {
                CCResourceItem r = ccConfig.getFileMap().get(sakaiId);
                if (r != null) {
                    CCResourceItem.getDependencies().add(r.getResourceId());
                }
            }
        }
        if (fixups != null && fixups.length() > 0) {
            return ("<!--fixups:" + fixups.toString() + "-->" + ret.toString());
        }
        return ret.toString();
    }

    public String relFixup(CCConfig ccConfig, String text, CCResourceItem CCResourceItem) {
        return relFixup(ccConfig, text, CCResourceItem, null);
    }

    // return base directory of file, including trailing /
    // "" if it is in home directory
    public String getParent(String s) {
        int i = s.lastIndexOf("/");
        if (i < 0) {
            return "";
        }
        return s.substring(0, i + 1);
    }

    // return relative path to target from base
    // base is assumed to be "" or ends in /
    public String relativize(String target, String base) {
        if (StringUtils.isBlank(base)) return target;
        if (target.startsWith(base)) {
            return target.substring(base.length());
        } else {
            // get parent directory of base directory.
            // base directory ends in /
            int i = base.lastIndexOf("/", base.length() - 2);
            String path = "";
            if (i >= 0) {
                path = base.substring(0, i + 1); // include /
            }
            return "../" + relativize(target, path);
        }
    }

    /**
     * Path dot manipulation
     * <p>
     * xxx/abc/../ccc
     * xxx/ccc
     * xxx/../ccc
     * ccc
     *
     * @param path
     * @return
     */
    public String removeDotDot(String path) {
        while (true) {
            int i = path.indexOf("/../");
            if (i < 1) return path;

            int j = path.lastIndexOf("/", i - 1);
            if (j < 0) j = 0;
            else j = j + 1;
            path = path.substring(0, j) + path.substring(i + 4);
        }
    }

    public boolean isLink(ContentResource r) {
        return r.getResourceType().equals("org.sakaiproject.content.types.urlResource") ||
                r.getContentType().equals("text/url");
    }
}
