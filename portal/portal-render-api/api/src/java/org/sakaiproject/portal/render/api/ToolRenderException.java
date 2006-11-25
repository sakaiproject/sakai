package org.sakaiproject.portal.render.api;

import java.io.IOException;

/**
 * Exception thrown when an error occurs while
 * rendering a portlet.
 *
 * @since Sakai 2.2.3
 * @version $Rev$
 * 
 */
public class ToolRenderException extends IOException {

    /**
     * Root cause;
     */
    private Throwable t;


    /**
     * Default constructor
     * @param string
     * @param throwable
     */
    public ToolRenderException(String string, Throwable throwable) {
        super(string);
        this.t = throwable;
    }

	public ToolRenderException(String string)
	{
		super(string);
	}
}
