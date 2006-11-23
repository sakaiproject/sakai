package org.sakaiproject.portal.render.api;

import java.io.IOException;

public class ToolRenderException extends IOException {

    private Throwable t;

    public ToolRenderException(String string, Throwable throwable) {
        super(string);
        this.t = throwable;
    }

	public ToolRenderException(String string)
	{
		super(string);
	}
}
