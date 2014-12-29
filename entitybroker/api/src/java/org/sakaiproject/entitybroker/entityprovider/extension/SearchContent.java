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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents a set of data to put into the search index,
 * this should represent the data of a single entity which should be indexed into the search engine
 * 
 * NOTE: summary is the major piece of content which is being indexed,
 * this may be an entire HTML page or just a bit of text, it is the part that will be searched
 * when normal searches are executed
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class SearchContent {

    protected String id;
    protected String prefix;
    protected String reference;
    protected String title;
    protected String url;
    transient protected String summary;
    transient protected Reader summaryReader;
    protected Map<String, String> properties = new HashMap<String, String>(0);

    protected SearchContent() {
    }

    /**
     * Create a set of content for indexing,
     * NOTE: make sure you also set the summary using {@link #setSummary(String)} or {@link #setSummaryReader(Reader)}
     * 
     * @param id the entity id (e.g. "1234")
     * @param prefix the entity prefix (e.g. "user")
     * @param reference the entity reference (e.g. /prefix/id)
     * @param title the display title for this content
     * @param url the URL to this content
     */
    public SearchContent(String id, String prefix, String reference, String title, String url) {
        super();
        this.id = id;
        this.prefix = prefix;
        this.reference = reference;
        this.title = title;
        this.url = url;
    }

    /**
     * Set any property on this search content
     * 
     * @param name the key for the property
     * @param value the value
     */
    public void setProperty(String name, String value) {
        if (name != null) {
            if ("id".equals(name)) {
                id = value;
            } else if ("prefix".equals(name)) {
                prefix = value;
            } else if ("reference".equals(name)) {
                reference = value;
            } else if ("title".equals(name)) {
                title = value;
            } else if ("url".equals(name)) {
                url = value;
            } else if ("summary".equals(name)) {
                summary = value;
            } else {
                properties.put(name, value);
            }
        }
    }

    /**
     * Get any property from this search content
     * 
     * @param name the name of the property
     * @return the value OR null if it is not set
     */
    public String getProperty(String name) {
        String value = null;
        if (name != null) {
            if ("id".equals(name)) {
                value = id;
            } else if ("prefix".equals(name)) {
                value = prefix;
            } else if ("reference".equals(name)) {
                value = reference;
            } else if ("title".equals(name)) {
                value = title;
            } else if ("url".equals(name)) {
                value = url;
            } else if ("summary".equals(name)) {
                value = summary;
            } else {
                value = properties.get(name);
            }
        }
        return value;
    }

    /**
     * @return the main summary data for this content
     */
    String getSummary() {
        if (summaryReader != null) {
            // convert the reader into a string
            BufferedReader reader = new BufferedReader( summaryReader );
            String line  = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");
            try {
                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                    stringBuilder.append( ls );
                }
            } catch (IOException e) {
                throw new RuntimeException("Failure while reading from summaryReader: " + e, e);
            }
            summary = stringBuilder.toString();
        }
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
        if (summary != null) {
            this.summaryReader = null;
        }
    }
    
    public Reader getSummaryReader() {
        return summaryReader;
    }
    
    public void setSummaryReader(Reader summaryReader) {
        this.summaryReader = summaryReader;
        if (summaryReader != null) {
            this.summary = null;
        }
    }


    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }


}
