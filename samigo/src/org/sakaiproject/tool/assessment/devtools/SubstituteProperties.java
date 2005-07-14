/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.devtools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class SubstituteProperties
{
  private static Properties properties;
  private static Map map;
  private static String propFileName; // resource bundle
  private static String fileName; // JSF source
  private static String FLAG = "\\[\\[";

  public static void main(String[] args)
  {
    for (int i = 0; i < args.length; i++) {
      //System.out.println("using argument " + i + ":" + args[i]);
    }

    propFileName = args[0];
    fileName = args[1];
    makeProp();
    makeMap();
    //System.out.println("\n\n" + getReplaced());


  }

  /**
   * read in properties file which is your resource bundle
   */
  private static void makeProp()
  {
    properties = new Properties();
    try {
      properties.load(new FileInputStream(propFileName));
    }
    catch (Exception ex) {
      //System.out.println("oops " + propFileName);
    }
  }

  /**
   * reverse lookup, all text to be replaced is tagged with FLAG="[["
   */
  private static void makeMap()
  {
    map = new HashMap();
    Enumeration enumer = properties.keys();

    while (enumer.hasMoreElements())
    {
      Object key = enumer.nextElement();
      map.put(FLAG + properties.get(key), key);
    }
  }
  /**
   * return a String containing the modified file
   * @return String
   */
  private static String getReplaced()
  {
    String contents = "";
    String line = "";
    try {
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      while (line != null)
      {
        line = br.readLine() ;
        contents += line + "\n";
      }
    }
    catch (Exception ex) {
      //System.out.println("oops " + fileName);
    }

    Iterator iter = map.keySet().iterator();

    while (iter.hasNext())
    {
      Object key = iter.next();
      Object value = map.get(key);
      String toReplace = (String) key;
      String replaceWith = makeTag((String) value);
      try
      {
        contents = contents.replaceAll(toReplace, replaceWith);
      }
      catch (Exception ex) {
        //System.out.println("**** UNABLE TO REPLACE ****: '" +toReplace+"'");
      }
    }


    return contents;

  }

  /**
   * assumes that your resource file has a loadBundle with a var="msg"
   * @param s String
   * @return String
   */
  private static String makeTag(String s)
  {
    return "#{msg." + s + "}";
  }

}
