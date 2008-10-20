/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
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
package org.sakaiproject.scorm.ui.player.decorators;

import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator;


public final class SjaxCallDecorator extends AjaxPostprocessingCallDecorator {
	private static final long serialVersionUID = 1L;
	private String js;

	// FIXME: Think about removing this class -- it used to do something more than the default Decorator, and we may need it to handle clever loading behavior, but otherwise, unnecessary.
	public SjaxCallDecorator(String js) {
		this((IAjaxCallDecorator)null);
		this.js = js;
	}
	
	public SjaxCallDecorator(IAjaxCallDecorator delegate) {
		super(delegate);
	}

	public CharSequence postDecorateScript(CharSequence script)
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(script).append("\n");
		
		// TODO: Maybe turn this back on -- will need to include 
		//if (js != null)
		//	buffer.append(js);
		
		buffer.append("return wcall;\n");
		
		return buffer.toString();
	}
}
