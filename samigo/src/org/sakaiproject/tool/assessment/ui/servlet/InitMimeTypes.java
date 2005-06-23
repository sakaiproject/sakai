/**********************************************************************************
*
* $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/ui/servlet/StoreApplicationContext.java,v 1.5 2005/06/03 20:37:11 janderse.umich.edu Exp $
*
***********************************************************************************/
/*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.FileInputStream;
import java.io.IOException;
import javax.activation.MimetypesFileTypeMap;
import org.sakaiproject.tool.assessment.util.MimeTypesLocator;

public class InitMimeTypes extends HttpServlet{

  private static Log log = LogFactory.getLog(InitMimeTypes.class);
  protected static ServletContext context;

  public void init (ServletConfig config) throws ServletException {
    super.init(config);
    context = config.getServletContext();
    String path = context.getRealPath("WEB-INF/mime.types");
    log.debug("**** mimetypes path="+path);
    MimetypesFileTypeMap mimeTypeMap = null;
    FileInputStream input = null;
    try{
	input = new FileInputStream(path);
	log.debug("**** input="+input);
	mimeTypeMap = new MimetypesFileTypeMap(input);
	log.debug("**** mimeTypeMap="+mimeTypeMap);
        MimeTypesLocator.getInstance().setMimetypesFileTypeMap(mimeTypeMap);
    }
    catch(Exception ex){
	log.warn(ex.getMessage());
    }
    finally{
        try {
	    if (input != null) input.close();
        }
        catch (IOException ex1) {
        }
    }
  }

}

/**********************************************************************************
*
* $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/ui/servlet/StoreApplicationContext.java,v 1.5 2005/06/03 20:37:11 janderse.umich.edu Exp $
*
***********************************************************************************/

