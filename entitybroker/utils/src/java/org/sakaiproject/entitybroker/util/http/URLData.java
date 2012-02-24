/**
 * $Id$
 * $URL$
 * URLData.java - entity-broker - Jan 2, 2009 12:28:51 PM - azeckoski
 ***********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.entitybroker.util.http;

import org.sakaiproject.entitybroker.util.TemplateParseUtil;


/**
 * This is a storage and parsing utility class,
 * supports the ability to get the data out of a URL
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class URLData {
    public String originalURL;
    public String protocol;
    public String server;
    public String port;
    public String path;
    public String extension;
    public String servletName;
    public String contextPath;
    public String pathInfo;
    public String pathInfoNoExtension;
    public String query;

    public URLData(String url) {
        if (url == null || "".equals(url)) {
            throw new IllegalArgumentException("url cannot be null or empty string");
        }
        url = url.trim();
        originalURL = url;
        int length = url.length();
        int curLoc = 0;
        int protLoc = url.indexOf("://");
        if (protLoc > 0) {
            protocol = url.substring(0, protLoc);
            int slashLoc = url.indexOf('/', protLoc+3);
            if (slashLoc == -1) {
                slashLoc = length;
            }
            int colonLoc = url.indexOf(':', protLoc+3);
            if (colonLoc > 0) {
                server = url.substring(protLoc+3, colonLoc);
                port = url.substring(colonLoc+1, slashLoc);
            } else {
                server = url.substring(protLoc+3, slashLoc);
                port = "80";
            }
            curLoc = slashLoc + 1;
        } else {
            protocol = "http";
            server = "localhost";
            port = "80";
        }
        if (curLoc < length) {
            // split into path and query string
            int questLoc = url.indexOf('?');
            if (questLoc > 0) {
                path = url.substring(curLoc, questLoc);
                if (questLoc < length -1) {
                    query = url.substring(questLoc+1);
                } else {
                    query = "";
                }
            } else {
                // no query string
                path = url.substring(curLoc);
                query = "";
            }
        } else {
            // no path
            path = "";
            servletName = "";
            contextPath = "";
            pathInfo = "";
            query = "";
            extension = "";
            pathInfoNoExtension = "";
        }
        // get servlet from path
        if (path != null && path.length() > 2) {
            int start = 0;
            int slashLoc = path.indexOf('/');
            if (slashLoc == 0) {
                start = 1;
                slashLoc = path.indexOf('/', 1);
            }
            if (slashLoc == -1) {
                slashLoc = path.length();
            }
            servletName = path.substring(start, slashLoc);
            if (servletName.length() > 0) {
                contextPath = "/" + servletName;
            }
            pathInfo = path.substring(slashLoc);
            pathInfoNoExtension = pathInfo;
            extension = "";
            // check for extension
            if (pathInfo.indexOf('.') != -1) {
                String[] parsed = TemplateParseUtil.findExtension(pathInfo);
                pathInfoNoExtension = parsed[1];
                extension = parsed[2] == null ? "" : parsed[2];
            }
        }
    }
    // done
}
