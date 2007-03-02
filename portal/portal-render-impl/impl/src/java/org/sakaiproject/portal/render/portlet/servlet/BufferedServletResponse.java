package org.sakaiproject.portal.render.portlet.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * ServletResponse instance used to buffer content. This buffering allows for the
 * portlets title to be captured prior to rendering and other similar features.
 * <p/> NOTE: Access the output stream for this response had undertermined
 * results. It is expected that in most situations an IllegalArgumentException
 * will be thrown.
 * 
 * @since Sakai 2.2.4
 * @version $Rev$
 */
public class BufferedServletResponse extends HttpServletResponseWrapper
{

	/**
	 * The writer to which content will be buffered
	 */
	private StringWriter buffer = null;

	/**
	 * The printWriter which will be exposed to the requesting resources.
	 */
	private PrintWriter writer = null;

	/**
	 * The default content type for all portlets.
	 */
	private String contentType = "text/html";

	/**
	 * Sole Constructor. Initializes the response wrapper and the buffered
	 * writer.
	 * 
	 * @param response
	 *        the original servlet response.
	 */
	public BufferedServletResponse(HttpServletResponse response)
	{
		super(response);
		buffer = new StringWriter();
		writer = new PrintWriter(buffer);
	}

	@Override
	public String getContentType()
	{
		return contentType;
	}

	@Override
	public PrintWriter getWriter()
	{
		return writer;
	}

	/**
	 * Retrieve the buffer.
	 * 
	 * @return
	 */
	public StringWriter getInternalBuffer()
	{
		return buffer;
	}
}
