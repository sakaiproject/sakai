/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * ServletResponse instance used to buffer content. This buffering allows for
 * the portlets title to be captured prior to rendering and other similar
 * features. <p/> NOTE: Access the output stream for this response had
 * undertermined results. It is expected that in most situations an
 * IllegalArgumentException will be thrown.
 * 
 * @since Sakai 2.2.4
 * @version $Rev$
 */
@Slf4j
public class ByteArrayServletResponse extends HttpServletResponseWrapper
{
	/**
	 * The printWriter which will be exposed to the requesting resources.
	 */
	private PrintWriter writer = null;

	private String contentType = null;

	private boolean isCommitted = false;

	private long contentLength = -1L;

	private String redirect = null;

	private ServletByteOutputStream outStream = null;

	/**
	 * Sole Constructor. Initializes the response wrapper and the buffered
	 * writer.
	 * 
	 * @param response
	 *        the original servlet response.
	 */
	public ByteArrayServletResponse(HttpServletResponse response)
	{
		super(response);
		log.debug("ByteArrayServletResponse {}", response);
		reset();
	}

	@Override
	public boolean isCommitted()
	{
		boolean retval = isCommitted || super.isCommitted();
		log.debug("isCommitted = {} retval = {}", isCommitted, retval);
		return retval;
	}

	@Override
	public String getContentType()
	{
		log.debug("contentType = {}", contentType);
		return contentType;
	}

	@Override
	public void setContentType(String newType)
	{
		log.debug("setContentType = {}", contentType);
		super.setContentType(newType);
		contentType = newType;
	}

	public String getRedirect()
	{
		return redirect;
	}

	@Override
	public void sendRedirect(String redirectUrl)
        throws java.io.IOException
	{
		log.debug("sendRedirect = {}", redirectUrl);
		isCommitted = true;
		redirect = redirectUrl;
	}

	@Override
	public PrintWriter getWriter()
	{
		log.debug("getWriter()");
		isCommitted = true;
		return writer;
	}

	@Override
	public ServletOutputStream getOutputStream() throws java.io.IOException
	{
		log.debug("getOutputStream()");
		isCommitted = true;
		return outStream;
	}

	@Override
	public void setContentLength(int i)
	{
		contentLength = (long)i;
	}

	public void setContentLengthLong(long i)
	{
		contentLength = i;
	}

	@Override
	public void flushBuffer()
	{
		// Do nothing
	}

	@Override
	public void reset()
	{
		log.debug("reset()");
		outStream = new ServletByteOutputStream();
		writer = new PrintWriter(outStream);
	}

	/**
	 * Forward the request up the chain.
	 */

	public void forwardResponse()
		throws IOException
	{
		log.debug("Forwarding request CT={} CL={}", contentType, contentLength);
		if ( contentType != null ) super.setContentType(contentType);
		// need to add header. Using setContentLength fails for lengths > 32 bits
		if ( contentLength > 0L ) super.setHeader("Content-Length", Long.toString(contentLength));
		ServletOutputStream output = super.getOutputStream();
		if ( redirect != null ) super.sendRedirect(redirect);
		outStream.getContent().writeTo(output);
	}

	/**
	 * Retrieve the buffer.
	 * 
	 * @return
	 */
	public String getInternalBuffer()
	{
		if (log.isDebugEnabled()) {
			log.debug("---- baStream -----");
			log.debug(outStream.getContent().toString());
			log.debug("---- baStream -----");
		}

		// TODO: Should we fall back to regular encoding or freak out?
		try
		{
			return outStream.getContent().toString("UTF-8");
		}
		catch (Exception e)
		{
			return outStream.getContent().toString();
		}
	}
}

@Slf4j
class ServletByteOutputStream extends ServletOutputStream
{
	private ByteArrayOutputStream baStream;

	public ServletByteOutputStream()
	{
		log.debug("Making a ServletByteOutputStream");
		baStream = new ByteArrayOutputStream();
	}

	public ByteArrayOutputStream getContent()
	{
		return baStream;
	}

	public void write(int i) throws java.io.IOException
	{
		log.debug("Writing an int");
		baStream.write(i);
	}

	public void write(byte[] data) throws java.io.IOException
	{
		write(data, 0, data.length);
	}

	public void write(byte[] data, int start, int end) throws java.io.IOException
	{
		log.debug("Writing an array");
		baStream.write(data, start, end);
	}

	public void close() throws java.io.IOException
	{
		log.debug("Close");
	}

	public void flush() throws java.io.IOException
	{
		log.debug("Flush");
	}
}
