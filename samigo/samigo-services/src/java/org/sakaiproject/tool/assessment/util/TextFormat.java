/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Copyright: Copyright (c) 2003-5</p>
 * <p>Organization: Sakai Project</p>
 * @author jlannan
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */
@Slf4j
public class TextFormat
{
  private static final String HTML;
  private static final String SMART;
  private static final String PLAIN;
  private static final Vector vProtocols;
  //private String upperText;
  private StringBuilder returnText;
  private StringBuilder resource;
  private ArrayList arrLst;

  static
  {
    HTML = "HTML";
    SMART = "SMART";
    PLAIN = "PLAIN";

    vProtocols = new Vector();
    vProtocols.add("http://");
    vProtocols.add("https://");
    vProtocols.add("ftp://");
    vProtocols.add("www.");
    vProtocols.add("telent:");
    vProtocols.add("mailto:");
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param text TEXT TO BE MODIFIED
   * @param texttype TYPE OF TEXT -- PLAIN, HTML, OR SMART
   * @param iconPath PATH TO ICON IMAGES IN APPLICATION
   *
   * @return DOCUMENTATION PENDING
   */
  public String formatText(String text, String texttype, String iconPath)
  {
    if(log.isDebugEnabled())
    {
      log.debug(
        "formatText(String " + text + ", String " + texttype + ", String " +
        iconPath + ")");
    }

    returnText = new StringBuilder();

    if((texttype == null) || (text == null))
    {
      return text;
    }
    else if(texttype.equals(TextFormat.PLAIN))
    {
      return text;
    }
    else if(texttype.equals(TextFormat.HTML))
    {
      return text;
    }
    else if(texttype.equals(TextFormat.SMART))
    {
      int start = 0;
      int end = 0;

      while(true)
      {
        arrLst = new ArrayList();

        // traverse vector of protocol strings
        Iterator i = vProtocols.iterator();
        Integer retVal;
        while(i.hasNext())
        {
          String str = (String) i.next();
          arrLst.add(retVal = indexOfIgnoreCase(text, str));
          if(retVal.intValue() == -1)
          {
            i.remove();
          }
        }

        start = minimum(arrLst);
        log.debug("start: " + String.valueOf(start));
        if((start == -1) || vProtocols.isEmpty())
        {
          break;
        }

        // find either the next space or the end of string whichever comes first
        if((end = text.indexOf(" ", start)) == -1)
        {
          end = text.length();
        }

        // extract text and resource text from StringBuilder
        if(start != 0)
        {
          returnText.append(text.substring(0, start));
          log.debug(
            "adding pre-resource text: " + text.substring(0, start));
        }

        log.debug("end: " + String.valueOf(end));

        resource = new StringBuilder();
        String upper = text.substring(start, end).toUpperCase();
        try
        {
          if(upper.startsWith("HTTPS://"))
          {
            resource.append("https://");
            resource.append(
              URLEncoder.encode(text.substring(start + 8, end), "UTF-8"));
            log.debug("hi" + resource);
          }
          else if(
            upper.startsWith("HTTP://") || upper.startsWith("MAILTO:") ||
              upper.startsWith("TELNET:"))
          {
            resource.append("http://");
            resource.append(
              URLEncoder.encode(text.substring(start + 7, end), "UTF-8"));
          }
          else if(upper.startsWith("FTP://"))
          {
            resource.append("ftp://");
            resource.append(
              URLEncoder.encode(text.substring(start + 6, end), "UTF-8"));
          }
          else if(upper.startsWith("WWW."))
          {
            resource.append("www.");
            resource.append(
              URLEncoder.encode(text.substring(start + 4, end), "UTF-8"));
          }
          else
          {
            ;
          }
        }
        catch(UnsupportedEncodingException e)
        {
          log.error(e.getMessage(), e);
        }

        String temp = resource.toString();
        resource.insert(resource.length(), "', target=_new>" + temp + "</a>");
        resource.insert(0, "<a href='");

        //stringParts.add(resource.toString());
        returnText.append(resource);
        log.debug("add ing resource: " + resource.toString());

        // delete resource string
        text = text.substring(end);
      }

      // add remaining characters to buffer
      if(text.length() != 0)
      {
        returnText.append(text);
      }

      int temp = 0;

      // replace emoticons with images
      while((temp = returnText.indexOf(":-)")) != -1)
      {
        returnText.replace(
          temp, temp + 3, "<img src='" + iconPath + "smile.gif'/>");
      }

      while((temp = returnText.indexOf(":-(")) != -1)
      {
        returnText.replace(
          temp, temp + 3, "<img src='" + iconPath + "frown.gif'/>");
      }

      while((temp = returnText.indexOf(":-o")) != -1)
      {
        returnText.replace(
          temp, temp + 3, "<img src='" + iconPath + "suprise.gif'/>");
      }

      while((temp = returnText.indexOf(";-)")) != -1)
      {
        returnText.replace(
          temp, temp + 3, "<img src='" + iconPath + "wink.gif'/>");
      }

      while((temp = returnText.indexOf(":)")) != -1)
      {
        returnText.replace(
          temp, temp + 2, "<img src='" + iconPath + "smile.gif'/>");
      }

      while((temp = returnText.indexOf(":(")) != -1)
      {
        returnText.replace(
          temp, temp + 2, "<img src='" + iconPath + "frown.gif'/>");
      }

      while((temp = returnText.indexOf(":o")) != -1)
      {
        returnText.replace(
          temp, temp + 2, "<img src='" + iconPath + "suprise.gif'/>");
      }

      while((temp = returnText.indexOf(";)")) != -1)
      {
        returnText.replace(
          temp, temp + 2, "<img src='" + iconPath + "wink.gif'/>");
      }
        return returnText.toString();

    }
    else
    {
      return "";
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param str STRING TO BE SEARCHED
   * @param searchString STRING TO SEARCH FOR WITHIN str
   *
   * @return INDEX LOCATION OF searchString within str
   */
  public Integer indexOfIgnoreCase(String str, String searchString)
  {
    if(log.isDebugEnabled())
    {
      log.debug(
        "indexOfIgnoreCase(String " + str + ", String " + searchString + ")");
    }

    return Integer.valueOf(str.toUpperCase().indexOf(searchString.toUpperCase()));
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param a LIST OF INDICIES OF OCCURANCES OF HYPERLINKS WITHIN TEXT
   *
   * @return MINIMUM OF ALL OCCURANCES OR -1 IF NO OCCURANCES
   */
  public int minimum(ArrayList a)
  {
    if(log.isDebugEnabled())
    {
      log.debug("minimum(ArrayList " + a + ")");
    }

    boolean firstNumber = true;
    int tmp = 0;
    int min = -1;

    Iterator i = a.iterator();
    while(i.hasNext())
    {
      tmp = ((Integer) i.next()).intValue();

      if(firstNumber && (tmp != -1))
      {
        firstNumber = false;
        min = tmp;
      }

      if((tmp != -1) && (tmp < min))
      {
        min = tmp;
      }
    }

    return min;
  }

  public static String convertPlaintextToFormattedTextNoHighUnicode(String value) {
	  if (value == null) return "";

	  try
	  {
		  StringBuilder buf = new StringBuilder();
		  final int len = value.length();
		  for (int i = 0; i < len; i++)
		  {
			  char c = value.charAt(i);
			  switch (c)
			  {
			  case '<':
			  {
				  if (buf == null) buf = new StringBuilder(value.substring(0, i));
				  buf.append("&lt;");
			  }
			  break;

			  case '>':
			  {
				  if (buf == null) buf = new StringBuilder(value.substring(0, i));
				  buf.append("&gt;");
			  }
			  break;

			  case '&':
			  {
				  if (buf == null) buf = new StringBuilder(value.substring(0, i));
				  buf.append("&amp;");
			  }
			  break;

			  case '"':
			  {
				  if (buf == null) buf = new StringBuilder(value.substring(0, i));
				  buf.append("&quot;");
			  }
			  break;
			  case '\n':
			  {
				  if (buf == null) buf = new StringBuilder(value.substring(0, i));
				  buf.append("<br />\n");
			  }
			  break;
			  default:
			  {
				  if (buf != null) buf.append(c);
			  }
			  break;
			  }
		  } // for

		  return (buf == null) ? value : buf.toString();
	  }
	  catch (Exception e)
	  {
		  log.warn("convertPlaintextToFormattedTextNoHighUnicode: {}", e.getMessage());
		  return "";
	  }

  } // convertPlaintextToFormattedTextNoHighUnicode

}
