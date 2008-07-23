/**********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package edu.indiana.lib.twinpeaks.search.sru.ss360search;

import edu.indiana.lib.twinpeaks.util.*;
import java.util.*;


/**
 * Data normalization for 360 Search results
 */
public class ResultUtils
{

  private static org.apache.commons.logging.Log	_log = LogUtils.getLog(ResultUtils.class);
  /*
   * Database names
   */
  public static final String ANTHRO_PLUS  = "FVX";
  public static final String JSTOR        = "JST";

  /**
   * Normalize one 360 Search result data item.  Essentially, just dispatch
   * the proper normalization method for this result, based on the data
   * element name and origin database
   *
   * @param resultdata Result data
   * @param partDataName Data element name (spage, creator, etc)
   * @param databse The origin database (JST, EAP, etc)
   * @return The normalized result (at a minimum, we always trim() the text)
   */
  public static String normalize(String resultData,
                                 String partDataName,
                                 String database)
  {
    String result = resultData.trim();

    _log.debug("normalize() called with " + result
            +  " -- " + partDataName
            +  " -- " + database);
    /*
     * Stop now if the element name ot database wasn't provided
     */
    if ((partDataName == null) || (database == null))
    {
      return result;
    }
    /*
     * Anthropology Plus
     */
    if (ANTHRO_PLUS.equals(database))
    {
      if ("issue".equals(partDataName))
      {
        return normalizeAnthroPlusIssue(result);
      }

      if ("title".equals(partDataName))
      {
        return normalizeAnthroPlusTitle(result);
      }
      return result;
    }
    /*
     * JSTOR
     */
    if (JSTOR.equals(database))
    {
      if ("pages".equals(partDataName))
      {
        return normalizeJstorPageRange(result);
      }

      if ("spage".equals(partDataName))
      {
        return normalizeJstorStartPage(result);
      }
      return result;
    }
    /*
     * No additional normalization required
     */
     return result;
  }

  /*
   * Database specific normalization routines
   */

  /*
   * Anthropology Plus
   */

  /**
   * Normalize the issue number (remove the "o." prefix)
   * @param issue The issue number
   * @return A normalized issue
   */
  public static String normalizeAnthroPlusIssue(String issue)
  {
    return removePrefix(issue, "o.");
  }

  /**
   * Normalize the title (remove trailing " /")
   * @param pages The page range text
   * @return The normalized title
   */
  public static String normalizeAnthroPlusTitle(String title)
  {
    if (title.endsWith(" /"))
    {
      return title.substring(0, title.length() - 2);
    }
    return title;
  }

  /*
   * JSTOR
   */

  /**
   * Normalize the page range ("pp. ", "p. " are removed)
   * @param pages The page range text
   * @return The normalized range
   */
  public static String normalizeJstorPageRange(String range)
  {
    return removeJstorPagePrefix(range);
  }

  /**
   * Normalize the start page ("pp. ", "p. " are removed)
   * @param pages The start page text
   * @return The normalized starting page
   */
  public static String normalizeJstorStartPage(String page)
  {
    return removeJstorPagePrefix(page);
  }

  /**
   * Remove leading "pages" prefix ("p. ", "pp. ")
   * @param page Page, page range, etc.
   * @return normalized page numbers
   */
  public static String removeJstorPagePrefix(String page)
  {
    for (int i = 0; i < page.length(); i++)
    {
      char c = page.charAt(i);

      switch (c)
      {
        case 'p': // Skip prefix characters
        case '.':
          break;

        default:  // Our result is the trimmed text following the prefix
          return page.substring(i).trim();
      }
    }
    return page;
  }

  /*
   * String handling
   */

  /**
   * Remove prefix text
   * @param text Text item to edit
   * @param prefix The text we want to remove
   * @return Prefix-free, trimmed text
   */
  public static String removePrefix(String text, String prefix)
  {
    if ((text.length() > prefix.length()) && (text.startsWith(prefix)))
    {
      return text.substring(prefix.length()).trim();
    }
    return text;
  }
}