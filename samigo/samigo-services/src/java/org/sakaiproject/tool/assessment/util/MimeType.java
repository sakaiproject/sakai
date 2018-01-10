/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.util;

import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * In memory mime type lookup utility class.
 * We could also load from config file/table...
 *
 * @author esmiley@stanford.edu
 * @version $Id$
 */
@Slf4j
public class MimeType {
  private static HashMap extensionMime = new HashMap();
  private static HashMap mimeExtension = new HashMap();
  private static boolean loaded = false;
  private static final String UNKNOWN_EXTENSION = ".unknown_binary";

  /**
   *  command line unit test
   * @param args command line args
   */
  public static void main(String[] args)
  {
    log.info("mime for BMP= " + get("BMP"));
    log.info("mime for .zip= " + get(".zip"));
    log.info("mime for .HTM= " + get (".HTM"));
    log.info("extension for application/x-tar= " +
                       getExtension("application/x-tar"));
    log.info("extension for application/x-gzip= "+
                       getExtension("application/x-gzip"));
  }

  /**
   * get a mime type from an extension
   * @param extension the extension
   * @return the mime type
   */
  public static String get(String extension){
    if (!loaded)
    {
      init();
    }
    if (extension == null) return "";

    String ext = extension.toLowerCase();
    if (! ext.startsWith(".")){
      ext = "." + ext;
    }

    return "" + extensionMime.get(ext);

  }

  /**
   * get an extension form a mime type
   * @param mimeType the mime type
   * @return the extension
   */
  public static String getExtension(String mimeType){
    if (!loaded)
    {
      init();
    }
    if (mimeType == null) return UNKNOWN_EXTENSION;

    String mime = mimeType.toLowerCase();

    String extension = "" + mimeExtension.get(mime);

    if ("null".equals(extension)) extension = UNKNOWN_EXTENSION;

    return extension;

  }

  /**
   * loads the cross references
   */
  private static void init()
  {
    extensionMime.put(".aif", "audio/x-aiff"); // Binary
    extensionMime.put(".aifc", "audio/x-aiff"); // Binary
    extensionMime.put(".aiff", "audio/x-aiff"); // Binary
    extensionMime.put(".au", "audio/basic"); // Binary
    extensionMime.put(".avi", "video/x-msvideo"); // Binary
    extensionMime.put(".bmp", "image/x-MS-bmp"); // Binary
    extensionMime.put(".cpio", "application/x-cpio"); // Binary
    extensionMime.put(".csh", "application/x-csh"); // Binary
    extensionMime.put(".dvi", "application/x-dvi"); // Binary
    extensionMime.put(".fif", "application/fractals"); // Binary
    extensionMime.put(".gif", "image/gif"); // Binary
    extensionMime.put(".gtar", "application/x-gtar"); // Binary
    extensionMime.put(".gz", "application/x-gzip"); // Binary
    extensionMime.put(".hqx", "application/mac-binhex40"); // Binary
    extensionMime.put(".htm", "text/html"); // Text
    extensionMime.put(".html", "text/html"); // Text
    extensionMime.put(".ief", "image/ief"); // Binary
    extensionMime.put(".jf", "application/x-javascript"); // Binary
    extensionMime.put(".latex", "application/x-latex"); // Binary
    extensionMime.put(".ls", "application/x-javascript"); // Binary
    extensionMime.put(".mocha", "application/x-javascript"); // Binary
    extensionMime.put(".mov", "video/quicktime"); // Binary
    extensionMime.put(".mpeg", "video/mpeg"); // Binary
    extensionMime.put(".pac", "application/x-ns-proxy-autoconfig"); // Binary
    extensionMime.put(".pbm", "image/x-portable-bitmap"); // Binary
    extensionMime.put(".pnm", "image/x-portable-anymap"); // Binary
    extensionMime.put(".ppm", "image/x-portable-pixmap"); // Binary
    extensionMime.put(".ps", "application/postscript"); // Binary
    extensionMime.put(".ras", "image/x-cmu-raster"); // Binary
    extensionMime.put(".rgb", "image/x-rgb"); // Binary
    extensionMime.put(".rtf", "application/rtf"); // Binary
    extensionMime.put(".shar", "appliation/x-shar"); // Binary
    extensionMime.put(".sit", "application/x-stuffit"); // Binary
    extensionMime.put(".tar", "application/x-tar"); // Binary
    extensionMime.put(".tcl", "application/x-tcl"); // Binary
    extensionMime.put(".tex", "application/x-tex"); // Binary
    extensionMime.put(".tif", "image/tiff"); // Binary
    extensionMime.put(".tiff", "image/tiff"); // Binary
    extensionMime.put(".txt", "text/plain"); // Text
    extensionMime.put(".xhtml", "text/xhtml"); // Text
    extensionMime.put(".xml", "text/xml"); // Text
    extensionMime.put(".xsd", "text/xml"); // Text
    extensionMime.put(".xsl", "text/xml"); // Text
    extensionMime.put(".xslt", "text/xml"); // Text
    extensionMime.put(".wav", "audio/x-wav"); // Binary
    extensionMime.put(".xbm", "image/x-bitmap"); // Binary
    extensionMime.put(".xpm", "image/x-pixmap"); // Binary
    extensionMime.put(".xwd", "image/xwindowdump"); // Binary
    extensionMime.put(".z", "application/x-compress"); // Binary
    extensionMime.put(".zip", "application/x-zip-compressed"); // Binary
    mimeExtension.put("application/x-shar", ".shar");
    mimeExtension.put("application/fractals", ".fif");
    mimeExtension.put("application/mac-binhex40", ".hqx");
    mimeExtension.put("application/postscript", ".ps");
    mimeExtension.put("application/rtf", ".rtf");
    mimeExtension.put("application/x-compress", ".z");
    mimeExtension.put("application/x-cpio", ".cpio");
    mimeExtension.put("application/x-csh", ".csh");
    mimeExtension.put("application/x-dvi", ".dvi");
    mimeExtension.put("application/x-gtar", ".gtar");
    mimeExtension.put("application/x-gzip", ".gz");
    mimeExtension.put("application/x-javascript", ".jf");
    mimeExtension.put("application/x-javascript", ".ls");
    mimeExtension.put("application/x-javascript", ".mocha");
    mimeExtension.put("application/x-latex", ".latex");
    mimeExtension.put("application/x-ns-proxy-autoconfig", ".pac");
    mimeExtension.put("application/x-stuffit", ".sit");
    mimeExtension.put("application/x-tar", ".tar");
    mimeExtension.put("application/x-tcl", ".tcl");
    mimeExtension.put("application/x-tex", ".tex");
    mimeExtension.put("application/x-zip-compressed", ".zip");
    mimeExtension.put("audio/basic", ".au");
    mimeExtension.put("audio/x-aiff", ".aiff");
    mimeExtension.put("audio/x-wav", ".wav");
    mimeExtension.put("image/gif", ".gif");
    mimeExtension.put("image/ief", ".ief");
    mimeExtension.put("image/tiff", ".tif");
    mimeExtension.put("image/x-MS-bmp", ".bmp");
    mimeExtension.put("image/x-bitmap", ".xbm");
    mimeExtension.put("image/x-cmu-raster", ".ras");
    mimeExtension.put("image/x-pixmap", ".xpm");
    mimeExtension.put("image/x-portable-anymap", ".pnm");
    mimeExtension.put("image/x-portable-bitmap", ".pbm");
    mimeExtension.put("image/x-portable-pixmap", ".ppm");
    mimeExtension.put("image/x-rgb", ".rgb");
    mimeExtension.put("image/xwindowdump", ".xwd");
    mimeExtension.put("text/html", ".html");
    mimeExtension.put("text/plain", ".txt");
    mimeExtension.put("text/xhtml", ".xhtml");
    mimeExtension.put("text/xml", ".xml");
    mimeExtension.put("video/mpeg", ".mpeg");
    mimeExtension.put("video/quicktime", ".mov");
    mimeExtension.put("video/x-msvideo", ".avi");
  }
}
