/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



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




