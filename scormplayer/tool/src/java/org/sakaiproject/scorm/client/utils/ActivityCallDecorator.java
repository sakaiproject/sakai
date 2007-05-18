package org.sakaiproject.scorm.client.utils;

import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator;

public final class ActivityCallDecorator extends AjaxPostprocessingCallDecorator {
	private static final long serialVersionUID = 1L;

	public ActivityCallDecorator() {
		this((IAjaxCallDecorator)null);
	}
	
	public ActivityCallDecorator(IAjaxCallDecorator delegate) {
		super(delegate);
	}

	public CharSequence postDecorateScript(CharSequence script)
	{
		StringBuffer buffer = new StringBuffer();
			
		buffer.append(script).append(" return true;");
		
		return buffer.toString();
	}
	
}
