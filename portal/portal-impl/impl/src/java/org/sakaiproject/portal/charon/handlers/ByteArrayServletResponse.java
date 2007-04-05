package org.sakaiproject.portal.charon.handlers;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Writer;

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
public class ByteArrayServletResponse extends HttpServletResponseWrapper
{
	/**
	 * The printWriter which will be exposed to the requesting resources.
	 */
	private PrintWriter writer = null;

	/**
	 * The default content type for all portlets.
	 */
	private String contentType = "text/html";

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
		// System.out.println("ByteArrayServletResponse "+response);
      		outStream = new ServletByteOutputStream();
		writer = new PrintWriter(outStream);
	}

	@Override
	public String getContentType()
	{
		// System.out.println("contentType = "+contentType);
		return contentType;
	}

	@Override
	public PrintWriter getWriter()
	{
		// System.out.println("getWriter()");
		return writer;
	}

	@Override
       public ServletOutputStream getOutputStream() throws java.io.IOException {
		// System.out.println("getOutputStream()");
		return outStream;
         }

	/**
	 * Retrieve the buffer.
	 * 
	 * @return
	 */
	public String getInternalBuffer()
	{
		// System.out.println("---- baStream -----");
		// System.out.println(outStream.getContent().toString());
		// System.out.println("---- baStream -----");
		return outStream.getContent().toString();
	}
}

class ServletByteOutputStream extends ServletOutputStream {
	       private ByteArrayOutputStream baStream;

               public ServletByteOutputStream()
		{
                        // System.out.println("Making a ServletByteOutputStream");
                        baStream = new ByteArrayOutputStream();
                }

		public ByteArrayOutputStream getContent()
		{
			return baStream;
		}
                public void write(int i) throws java.io.IOException 
		{
                        // System.out.println("Writing an int");
                        baStream.write(i);
                }

                public void write(byte[] data) throws java.io.IOException
                {
                        write(data,0,data.length);
                }

                public void write(byte[] data, int start, int end)
                        throws java.io.IOException
                {
                        // System.out.println("Writing an array");
                        baStream.write(data, start, end);
                }

                public void close() throws java.io.IOException
                {
                        // System.out.println("Close");
                }

           	public void flush() throws java.io.IOException 
		{
                        // System.out.println("Flush");
                }
}
