/*
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
*/

package org.sakaiproject.tool.assessment.devtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import org.sakaiproject.tool.assessment.util.StringParseUtils;

/**
 *
 * <p>Description: Get text delimited by "<" and ">" as in XML and HTML</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Ed Smiley
 * @version $Id: GetTextFromXML.java,v 1.3 2005/05/31 19:14:27 janderse.umich.edu Exp $
 */

public class GetTextFromXML
{
  private static String dirName; // JSF source directory
  private static File file;
  private static String SEP = "<>";
  private static String ends = "jsp";

  public static void main(String[] args)
  {
    if (args.length == 0)
    {
      //System.out.println("no argument!");
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
              //System.out.println(getVarName(tok) + "=" + tok);
            }
        }

      }

    }
    catch (Exception ex) {
      //System.out.println(ex);
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
  try {
    BufferedReader br = new BufferedReader(new FileReader(file));
    while (line != null)
    {
      line = br.readLine() ;
      contents += line + "\n";
    }
  }
  catch (Exception ex) {
    //System.out.println("oops " + file.getName());
  }


  return contents;

}


}
