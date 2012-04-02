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
import java.util.ArrayList;
import java.util.StringTokenizer;
public class StringParseUtils
{
  public static void main(String[] args)
  {
  }

  /**
   * Create an all lowercase version substituting "_" for non-alphanumeric with
   * trailing "_".  Limits the length.
   *
   * @param s the String
   * @param max the max number of characters to be output
   * @param min the min number of characters to be outpu
   *
   * @return
   */
  public static String simplifyString(String s, int max, int min)
  {
    s = "" + s;
    if (min > max)
    {
      min = max;
    }

    // normalize to lowercase
    s = s.toLowerCase();

    // hold in a StringBuilder
    StringBuilder sb = new StringBuilder();

    // replace any non alphanumeric chars with underscore
    for (int i = 0; (i < s.length()) && (i < max); i++)
    {
      char c = s.charAt(i);
      if (
          ( (c > ('a' - 1)) && (c < ('z' + 1))) ||
          ( (c > ('0' - 1)) && (c < ('9' + 1))))
      {
        sb.append(c);
      }
      else
      {
        sb.append('_');
      }
    }

    sb.append('_');

    // fill out to minimum length with underscores
    while (sb.length() < min)
    {
      sb.append('_');
    }

    // return the StringBuilder as a String.
    return sb.toString();
  }

  /**
   * utility.
   * @param name full name
   * @return (hopefully) first name
   */
  public static String getFirstNameFromName(String name)
  {
    String[] names = getNameArray(name);
    if (names.length == 0)
    {
      return "";
    }
    else if (names.length == 1)
    {
      return names[0];
    }

    String[] tempNames = new String[names.length - 1];
    System.arraycopy(names, 0, tempNames, 0, tempNames.length);
    names = tempNames;

    StringBuilder buf = new StringBuilder(); 

    for (int i = 0; i < names.length; i++)
    {
      if (names[i].length() > 0)
      {
        if (Character.isLowerCase(names[i].charAt(0)))
        {
          break;
        }
        buf.append(names[i] + " ");
      } // ifnames
    } //for

    String s = buf.toString();
    s = s.trim();

    return s;
  }

/**
 * utility.
 * @param name full name
 * @return (hopefully) last name
 */
  public static String getLastNameFromName(String name)
  {
    String[] names = getNameArray(name);
    if (names.length == 0)
    {
      return "";
    }
    else if (names.length == 1)
    {
      return names[0];
    }

    //String s = "";
    String lastWord = names[names.length - 1];
    boolean smallFound = false;

    
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < names.length; i++)
    {
      if (names[i].length() > 0)
      {
        if (Character.isLowerCase(names[i].charAt(0)))
        {
          smallFound = true;
        }
        if (smallFound)
        {
          buf.append(names[i] + " ");
        } //if
      } // ifnames
    } //for

    String s = buf.toString();
    s = s.trim();

    if (s.length() == 0)
    {
      s = lastWord;
    }

    return s;
  }

/**
 * util
 * @param name
 * @return each word in name
 */
  private static String[] getNameArray(String name)
  {
    ArrayList list = new ArrayList();
    StringTokenizer st = new StringTokenizer(name, " ");
    while (st.hasMoreElements())
    {
      list.add(st.nextToken());
    }

    int size = list.size();
    String sa[] = new String[size];

    for (int i = 0; i < size; i++)
    {
      sa[i] = (String) list.get(i);
    }

    return sa;
  }


}
