/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.servlet.delivery;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager.
 * Upload audio media to delivery bean.
 * This gets a posted input stream (from AudioRecorder.java in the client JVM)
 * and writes out to a file.</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class UploadAudioMediaServlet extends HttpServlet
{
  private static Log log = LogFactory.getLog(UploadAudioMediaServlet.class);

  public UploadAudioMediaServlet()
  {
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {
    doPost(req,res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {
    // writer for status message
    PrintWriter pw = res.getWriter();
    // default status message, if things go wrong
    String status = "Upload failure: empty media location.";

    // we get media location in assessmentXXX/questionXXX/agentId/audio.ext form
    String mediaLocation = null;
//    mediaLocation = req.getHeader("From");
    mediaLocation = "/tmp/test";

    // test for nonemptiness first
    if (mediaLocation != null && !(mediaLocation.trim()).equals(""))
    {
      ServletInputStream inputStream = null;
      FileOutputStream fileOutputStream = null;
      BufferedInputStream bufInputStream = null;
      BufferedOutputStream bufOutputStream = null; int count = 0;

      try
      {
        inputStream = req.getInputStream();
        fileOutputStream = getFileOutputStream(mediaLocation);

        // buffered input for servlet
        bufInputStream = new BufferedInputStream(inputStream);
        // buffered output to file
        bufOutputStream = new BufferedOutputStream(fileOutputStream);

        // write the binary data
        int i = 0;
        count = 0;
        if (bufInputStream != null)
        {
          while ( (i = bufInputStream.read()) != -1)
          {
            bufOutputStream.write(i);
            count++;
          }
        }

        // clean up
        bufOutputStream.close();
        bufInputStream.close();
        if (inputStream != null)
        {
          inputStream.close();
        }
        fileOutputStream.close();
      }
      catch (Exception ex)
      {
        status = "Upload failure: "+ mediaLocation +".  " + ex + ".";
      }

      status = "Acknowleged: "  + count + " bytes.";
      log.info("**** mediaLocation="+mediaLocation);
      log.info("**** count="+count);
    }

    pw.println(status);
    res.flushBuffer();
  }

  private FileOutputStream getFileOutputStream(String mediaLocation){
    FileOutputStream outputStream=null;
    try{
      File media = new File(mediaLocation);
      outputStream = new FileOutputStream(media);
    }
    catch (FileNotFoundException ex) {
      log.warn("file not found="+ex.getMessage());
    }
    return outputStream;
  }

}
