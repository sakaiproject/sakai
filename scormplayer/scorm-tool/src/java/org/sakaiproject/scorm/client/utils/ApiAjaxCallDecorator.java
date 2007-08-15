package org.sakaiproject.scorm.client.utils;

import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator;

public final class ApiAjaxCallDecorator extends AjaxPostprocessingCallDecorator {
	private static final long serialVersionUID = 1L;
	private String resultId;
	
	public ApiAjaxCallDecorator(String resultId) {
		this((IAjaxCallDecorator)null);
		this.resultId = resultId;
	}
	
	public ApiAjaxCallDecorator(IAjaxCallDecorator delegate) {
		super(delegate);
	}

	public CharSequence postDecorateScript(CharSequence script)
	{
		StringBuffer buffer = new StringBuffer();
		
		/*for (int i=0;i<numArgs;i++) {
			buffer.append(argIds[i])
				.append(" = document.getElementById('")
				.append(argIds[i]).append("').value;\n");
		}*/
		
		buffer.append(script)
			.append("\n return document.getElementById('").append(resultId).append("').value;\n");
		return buffer.toString();
	}
}
