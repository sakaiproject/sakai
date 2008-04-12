/**
 * $Id$
 * $URL$
 * Formatter.java - entity-broker - Apr 12, 2008 11:20:37 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
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
   public static String HTML_MIME_TYPE = "text/html";
   /**
    * XML formatted text (application/xml or text/xml) <br/>
    * http://en.wikipedia.org/wiki/XML <br/>
    * INPUT: XML text <br/>
    * OUTPUT: XML text <br/>
    */
   public static String XML = "xml";
   public static String XML_MIME_TYPE = "application/xml";
   /**
    * JSON formatted text (application/json or text/javascript) <br/>
    * http://en.wikipedia.org/wiki/JSON <br/>
    * INPUT: JSON text <br/>
    * OUTPUT: JSON text <br/>
    */
   public static String JSON = "json";
   public static String JSON_MIME_TYPE = "application/json";
   /**
    * Plain text (text/plain) <br/>
    * http://en.wikipedia.org/wiki/Plain_text <br/>
    * INPUT: -not supported- <br/>
    * OUTPUT: text <br/>
    */
   public static String TXT = "txt";
   public static String TXT_MIME_TYPE = "text/plain";

   /**
    * RSS 2 XML feed (application/rss+xml) <br/>
    * http://en.wikipedia.org/wiki/RSS <br/>
    * INPUT: -not supported- <br/>
    * OUTPUT: -not supported- <br/>
    */
   public static String RSS = "rss";
   public static String RSS_MIME_TYPE = "application/rss+xml";

   /**
    * ATOM XML feed (application/atom+xml) <br/>
    * http://en.wikipedia.org/wiki/ATOM <br/>
    * INPUT: -not supported- <br/>
    * OUTPUT: -not supported- <br/>
    */
   public static String ATOM = "atom";
   public static String ATOM_MIME_TYPE = "application/atom+xml";

   /**
    * All character data should be encoded and decoded as UTF-8,
    * this constant is the proper encoding string to use
    */
   public static final String UTF_8 = "UTF-8";

}
