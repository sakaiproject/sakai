/*
 *                       Navigo Software License
 *
 * Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.
 *
 * This work, including software, documents, or other related items (the
 * "Software"), is being provided by the copyright holder(s) subject to the
 * terms of the Navigo Software License. By obtaining, using and/or copying this
 * Software, you agree that you have read, understand, and will comply with the
 * following terms and conditions of the Navigo Software License:
 *
 * Permission to use, copy, modify, and distribute this Software and its
 * documentation, with or without modification, for any purpose and without fee
 * or royalty is hereby granted, provided that you include the following on ALL
 * copies of the Software or portions thereof, including modifications or
 * derivatives, that you make:
 *
 *    The full text of the Navigo Software License in a location viewable to
 *    users of the redistributed or derivative work.
 *
 *    Any pre-existing intellectual property disclaimers, notices, or terms and
 *    conditions. If none exist, a short notice similar to the following should
 *    be used within the body of any redistributed or derivative Software:
 *    "Copyright 2003, Trustees of Indiana University, The Regents of the
 *    University of Michigan and Stanford University, all rights reserved."
 *
 *    Notice of any changes or modifications to the Navigo Software, including
 *    the date the changes were made.
 *
 *    Any modified software must be distributed in such as manner as to avoid
 *    any confusion with the original Navigo Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The name and trademarks of copyright holder(s) and/or Indiana University,
 * The University of Michigan, Stanford University, or Navigo may NOT be used
 * in advertising or publicity pertaining to the Software without specific,
 * written prior permission. Title to copyright in the Software and any
 * associated documentation will at all times remain with the copyright holders.
 * The export of software employing encryption technology may require a specific
 * license from the United States Government. It is the responsibility of any
 * person or organization contemplating export to obtain such a license before
 * exporting this Software.
 */
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

    // hold in a StringBuffer
    StringBuffer sb = new StringBuffer();

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

    // return the StringBuffer as a String.
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

    String s = "";

    for (int i = 0; i < names.length; i++)
    {
      if (names[i].length() > 0)
      {
        if (Character.isLowerCase(names[i].charAt(0)))
        {
          break;
        }
        s += names[i] + " ";
      } // ifnames
    } //for

    s.trim();

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

    String s = "";
    String lastWord = names[names.length - 1];
    boolean smallFound = false;

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
          s += names[i] + " ";
        } //if
      } // ifnames
    } //for

    s.trim();

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
