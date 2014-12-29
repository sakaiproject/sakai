/**
 * $Id$
 * $URL$
 * Formatter.java - entity-broker - Apr 12, 2008 11:20:37 AM - azeckoski
 **************************************************************************
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
 */

package org.sakaiproject.entitybroker.entityprovider.extension;


/**
 * Defines a list of possible format types (extensions) which can be handled 
 * and indicates which are handled internally
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Formats {

    /**
     * HTML formatted text (text/html or application/xhtml+xml) <br/>
     * http://en.wikipedia.org/wiki/HTML <br/>
     * INPUT: POST or GET form data <br/>
     * OUTPUT: (X)HTML text <br/>
     */
    public static String HTML = "html";
    public static String HTML_MIME_TYPE = "text/html"; // also application/xhtml+xml
    public static String[] HTML_EXTENSIONS = new String[] {"html","htm","HTML","HTM"};

    /**
     * Special output format which indicates this entity will use internally generated
     * forms for input data, the output simply produces very simple html forms,
     * this is handled internally <br/>
     * INPUT: none <br/>
     * OUTPUT: XHTML text (forms for submitting data) <br/>
     */
    public static String FORM = "form";
    public static String FORM_MIME_TYPE = HTML_MIME_TYPE;
    public static String[] FORM_EXTENSIONS = new String[] {"form"};

    /**
     * XML formatted text (application/xml or text/xml) <br/>
     * http://en.wikipedia.org/wiki/XML <br/>
     * INPUT: XML text <br/>
     * OUTPUT: XML text <br/>
     */
    public static String XML = "xml";
    public static String XML_MIME_TYPE = "application/xml";
    public static String[] XML_EXTENSIONS = new String[] {"xml","XML"};

    /**
     * JSON formatted text (application/json or text/javascript) <br/>
     * http://en.wikipedia.org/wiki/JSON <br/>
     * INPUT: JSON text <br/>
     * OUTPUT: JSON text <br/>
     */
    public static String JSON = "json";
    public static String JSON_MIME_TYPE = "application/json"; // can be switched to plain so its easier to work with
    public static String[] JSON_EXTENSIONS = new String[] {"json","jsn","JSON","JSN"};

    /**
     * JSON formatted text with JSONP callback (application/javascript) <br/>
     * http://en.wikipedia.org/wiki/JSON <br/>
     * INPUT: -not supported, see JSON-
     * OUTPUT: JSON text with callback (jsonEntityFeed({...}) by default) <br/>
     */
    public static String JSONP = "jsonp";
    public static String JSONP_MIME_TYPE = "application/javascript";
    public static String[] JSONP_EXTENSIONS = new String[] {"jsonp","JSONP"};

    /**
     * Plain text (text/plain) <br/>
     * http://en.wikipedia.org/wiki/Plain_text <br/>
     * INPUT: -not supported- <br/>
     * OUTPUT: text <br/>
     */
    public static String TXT = "txt";
    public static String TXT_MIME_TYPE = "text/plain";
    public static String[] TXT_EXTENSIONS = new String[] {"txt","text","TXT","TEXT"};

    /**
     * RSS 2 XML feed (application/rss+xml) <br/>
     * http://en.wikipedia.org/wiki/RSS <br/>
     * INPUT: -not supported- <br/>
     * OUTPUT: -not supported- <br/>
     */
    public static String RSS = "rss";
    public static String RSS_MIME_TYPE = "application/rss+xml";
    public static String[] RSS_EXTENSIONS = new String[] {"rss","RSS"};

    /**
     * ATOM XML feed (application/atom+xml) <br/>
     * http://en.wikipedia.org/wiki/ATOM <br/>
     * INPUT: -not supported- <br/>
     * OUTPUT: -not supported- <br/>
     */
    public static String ATOM = "atom";
    public static String ATOM_MIME_TYPE = "application/atom+xml";
    public static String[] ATOM_EXTENSIONS = new String[] {"atom","ATOM"};

    /**
     * All character data should be encoded and decoded as UTF-8,
     * this constant is the proper encoding string to use
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * the array of all the known formats in this file
     */
    public static String[] ALL_KNOWN_FORMATS = new String[] {
        HTML, XML, JSON, JSONP, TXT, RSS, ATOM
    };

}
