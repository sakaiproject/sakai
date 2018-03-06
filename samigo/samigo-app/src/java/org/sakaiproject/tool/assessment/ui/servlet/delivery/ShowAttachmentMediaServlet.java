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

package org.sakaiproject.tool.assessment.ui.servlet.delivery;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAttachmentData;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.util.TextFormat;

/**
 * <p>
 * Title: Samigo
 * </p>
 * <p>
 * Description: Sakai Assessment Manager
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004 Sakai Project
 * </p>
 * <p>
 * Organization: Sakai Project
 * </p>
 * 
 * @author Ed Smiley
 * @version $Id: .java 17070 2006-10-12 00:07:52Z
 *          ktsao@stanford.edu $
 */
@Slf4j
public class ShowAttachmentMediaServlet extends HttpServlet
{
  /**
	 * 
	 */
  private static final long serialVersionUID = 2203681863823855810L;

  public ShowAttachmentMediaServlet()
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
	String resourceId = req.getParameter("resourceId");
	String mimeType = req.getParameter("mimeType");
	String filename = req.getParameter("filename");

	String cleanedFilename = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(filename);
	
    res.setHeader("Content-Disposition", "inline;filename=\"" + cleanedFilename +"\";");
    log.debug("resourceId = " + resourceId);
    log.debug("mimeType = " + mimeType);
    log.debug("cleanedFilename = " + cleanedFilename);
    
    // ** note that res.setContentType() must be called before
	// res.getOutputStream(). see javadoc on this
    res.setContentType(mimeType);
    
	ContentResource cr = null;
	byte[] media = null;
	try {
		// create a copy of the resource
		cr = AssessmentService.getContentHostingService().getResource(resourceId);
		if (cr == null) {
			return;
		}
		media = cr.getContent();
		if (media == null) {
			return;
		}
		res.setContentLength((int) cr.getContentLength());
	    log.debug("**** media.length = " + media.length);
		
	} catch (PermissionException e) {
		log.warn("PermissionException from doPost(): " +  e.getMessage());
	} catch (IdUnusedException e) {
		log.warn("IdUnusedException from doPost(): " + e.getMessage());
	} catch (TypeException e) {
		log.warn("TypeException from doPost(): " + e.getMessage());
	} catch (ServerOverloadException e) {
		log.warn("ServerOverloadException from doPost(): " + e.getMessage());
	}
	finally {
		// in case of any exceptions above being caught, media would still be null 
		if (media == null) {
			return;
		}
	}

	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(media);
	BufferedInputStream buf_inputStream = new BufferedInputStream(byteArrayInputStream);
	ServletOutputStream outputStream = res.getOutputStream();
    BufferedOutputStream buf_outputStream = null;
    int count=0;
    try{
    	
    	buf_outputStream = new BufferedOutputStream(outputStream);

    	int i=0;
    	while ((i=buf_inputStream.read()) != -1){
    		buf_outputStream.write(i);
    		count++;
    	}

    	//res.setContentLength(count);
    	res.flushBuffer();
    }
    catch(Exception e){
    	log.warn(e.getMessage());
    }
    finally {
    	if (buf_outputStream != null) {
    		try {
    			buf_outputStream.close();
    		}
    		catch(IOException e) {
    			log.error(e.getMessage());
    		}
    	}
    	if (buf_inputStream != null) {
    		try {
    			buf_inputStream.close();
    		}
    		catch(IOException e) {
    			log.error(e.getMessage());
    		}
    	}
    	if (outputStream != null) {
    		try {
    			outputStream.close();
    		}
    		catch(IOException e) {
    			log.error(e.getMessage());
    		}
    	}
    	if (byteArrayInputStream != null) {
    		try {
    			byteArrayInputStream.close();
    		}
    		catch(IOException e) {
    			log.error(e.getMessage());
    		}
    	}
    }
  }
}
