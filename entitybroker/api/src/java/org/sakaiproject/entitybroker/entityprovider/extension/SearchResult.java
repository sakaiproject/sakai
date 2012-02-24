/**
 * $Id: SearchProvider.java 59674 2009-04-03 23:05:58Z arwhyte@umich.edu $
 * $URL:  $
 * SearchContent - entity-broker - Apr 5, 2008 7:19:14 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;


/**
 * This is a single search result
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class SearchResult extends SearchContent {

    final Date DEFAULT_DATE = new Date(1246665600l);
    /**
     * The date when the index item was last updated
     */
    Date timestamp; // timestamp field
    /**
     * This is the set of search highlights for this result,
     * this will include HTML and should not be escaped
     */
    String highlights;

    protected SearchResult() {
    }

    public SearchResult(String id, String prefix, String reference, String title, String url,
            Date timestamp, Map<String, Object> fields) {
        super(id, prefix, reference, title, url);
        // set up the dates correctly
        if (timestamp == null) {
            timestamp = DEFAULT_DATE;
        }
        if (fields != null) {
            for (Entry<String, Object> entry : fields.entrySet()) {
                if (entry.getValue() != null) {
                    setProperty(entry.getKey(), entry.getValue().toString());
                }
            }
        }
    }

    public SearchResult(SearchContent content) {
        super(content.id, content.prefix, content.reference, content.title, content.url);
        timestamp = DEFAULT_DATE;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setHighlights(String highlights) {
        this.highlights = highlights;
    }

    public String getHighlights() {
        return highlights;
    }

}

