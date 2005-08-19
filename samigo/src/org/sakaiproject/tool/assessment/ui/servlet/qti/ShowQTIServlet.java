/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/ui/servlet/delivery/ShowMediaServlet.java $
 * $Id: ShowMediaServlet.java 244 2005-06-24 04:06:14Z daisyf@stanford.edu $
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

package org.sakaiproject.tool.assessment.ui.servlet.qti;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.ui.bean.qti.XMLDisplay;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager Export to QTI</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: ShowMediaServlet.java 244 2005-06-24 04:06:14Z daisyf@stanford.edu $
 */

public class ShowQTIServlet extends HttpServlet
{
  private static Log log = LogFactory.getLog(ShowQTIServlet.class);
  private String xmlData;

  /**
   * passthu to post
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   */
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws
    ServletException, IOException
  {
    doPost(req, res);
  }

  /**
   * Get the faces context and display the contents of the XMLDisplay bean
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   */
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws
    ServletException, IOException
  {
    XMLDisplay xmlDisp = (XMLDisplay)
      ContextUtil.lookupBeanFromExternalServlet("xml", req, res);
    String xml = xmlDisp.getXml();
    String fileName = xmlDisp.getName() + "." + xml;

    res.setHeader("Content-Disposition",
                  "inline" + ";filename=\"" + fileName + "\";");
    PrintWriter out = res.getWriter();
    log.info("debug show qti:");
    log.info("xml");
    out.print(xml);
  }

}
