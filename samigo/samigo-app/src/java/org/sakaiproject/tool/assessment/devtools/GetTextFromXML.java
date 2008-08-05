/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.devtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



import org.sakaiproject.tool.assessment.util.StringParseUtils;

/**
 *
 * <p>Description: Get text delimited by "<" and ">" as in XML and HTML</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Ed Smiley
 * @version $Id$
 */

public class GetTextFromXML
{

  private static Log log = LogFactory.getLog(GetTextFromXML.class);

  private static String dirName; // JSF source directory
  private static File file;
  private static String SEP = "<>";
  private static String ends = "jsp";

  public static void main(String[] args)
  {
    if (args.length == 0)
    {
      //log.info("no argument!");
      System.exit(0);
    }
    dirName = args[0];

    try {
      file = new File(dirName);
      if (!file.isDirectory())
      {
        throw new RuntimeException(args[0] + " not a directory");
      }

      File[] list = file.listFiles();
      boolean displayToken = true;
      for (int i = 0; i < list.length; i++) {
        File src = list[i];
        if (src.isDirectory()) continue;

        StringTokenizer st = new StringTokenizer(getContents(src), SEP, true);
        while (st.hasMoreElements()){
          String tok = st.nextToken();
          if (tok.equals("<"))
          {
            displayToken = false;
          }
          else if (tok.equals(">"))
          {
            displayToken = true;
            continue;
          }

          tok = tok.trim();

          if (displayToken && tok.length()>0)
            {
              //log.info(getVarName(tok) + "=" + tok);
            }
        }

      }

    }
    catch (RuntimeException ex) {
      //log.info(ex);
      System.exit(0);
    }

  }

  /**
   * compute a variable name
   * @param value String
   * @return String
   */
  public static String getVarName(String value)
  {
    return StringParseUtils.simplifyString(value, 20, 20);
  }

  /**
 * return a String containing the file
 * @return String
 */
private static String getContents(File file)
{
  String contents = "";
  String line = "";
  BufferedReader br = null;
  FileReader fr = null;
  try {
	fr = new FileReader(file);
    br = new BufferedReader(fr);
    while (line != null)
    {
      line = br.readLine() ;
      contents += line + "\n";
    }
  }
  catch (FileNotFoundException e) {
	  log.warn(e.getMessage());
  } catch (IOException e) {
	log.warn(e.getMessage());
  }
  finally {
      if (br != null) {
    	  try
    	  {
    		  br.close();
    	  }
    	  catch (IOException ex1)
    	  {
    		  log.warn(ex1.getMessage());
    	  }
      }
      if (fr != null) {
    	  try
    	  {
    		  fr.close();
    	  }
    	  catch (IOException ex1)
    	  {
    		  log.warn(ex1.getMessage());
    	  }
      }
  }
  return contents;

}


}
