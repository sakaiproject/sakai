/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.driver.services.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class RenderConfig {
    private static final Log LOG =
        LogFactory.getLog(RenderConfig.class);

    private Map pages;
    private String defaultPageId;

    // internally used.
    private int orderNumberCounter = 0;
    private Comparator pageComparator;

    public RenderConfig() {
        this.pages = new java.util.HashMap();
        this.pageComparator = new Comparator() {
            public int compare(Object a, Object b) {
                PageConfig pa = (PageConfig)a;
                PageConfig pb = (PageConfig)b;
                if(pa.getOrderNumber() > pb.getOrderNumber()) {
                    return 1;
                }
                else if(pa.getOrderNumber() == pb.getOrderNumber()) {
                    return 0;
                }
                else {
                    return -1;
                }
            }

            public boolean equals(Object a) {
                return false;
            }
        };
    }


    public String getDefaultPageId() {
        return defaultPageId;
    }

    public void setDefaultPageId(String defaultPageId) {
        this.defaultPageId = defaultPageId;
    }

    public List getPages() {
        List col =  new ArrayList(pages.values());
        Collections.sort(col, pageComparator);
        return col;
    }

    public PageConfig getPageConfig(String pageId) {
        if (pageId == null || "".equals(pageId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    "Requested page is null.  Returning default: " +
                    defaultPageId);
            }
            pageId = defaultPageId;
        }
//        return (PageConfig) pages.get(pageId);

         // TODO: Make sure this is needed.
         //This is the PLUTO-251 fix submitted by Charles Severence. Thank you!!!
         // Sometimes pages come with a prefix of a slash - if the page is not
         // found, and the first character of the pageId is a slash we attempt
         // to look up the page without the slash.

         PageConfig retval = (PageConfig) pages.get(pageId);

         if ( retval == null && pageId.startsWith("/") && pageId.length() > 2 ) {
        	 retval = (PageConfig) pages.get(pageId.substring(1));
         }


         if (retval == null)
         {
             LOG.warn("Couldn't find a PageConfig for page ID: [" + pageId + "]");
             if ((retval = (PageConfig)pages.get(defaultPageId)) != null)
             {
                 if (LOG.isDebugEnabled())
                 {
                     LOG.debug("Returning default page ID: [" + defaultPageId + "]");
                 }
             }
             else
             {
                 LOG.error("Could not find default page Id for render config!");
             }
         }
         return retval;
    }

    public void addPage(PageConfig config) {
        config.setOrderNumber(orderNumberCounter++);
        pages.put(config.getName(), config);
    }
    
    public void removePage(PageConfig config){
        pages.remove(config.getName());
    }

}
