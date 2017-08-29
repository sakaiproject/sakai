/**
 * Copyright (c) 2007-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.mailsender.tool.evolvers;

import java.util.Iterator;

import uk.org.ponder.htmlutil.HTMLUtil;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;

public class SakaiRichTextEvolver implements TextInputEvolver {
	public static final String COMPONENT_ID = "sakai-FCKEditor:";

	public String height = null;
	public String width = null;

	public UIJointContainer evolveTextInput(UIInput toevolve) {
		String height = this.height;
		String width = this.width;

		// dig out the size decorators and adjust the editor size.
		// TODO: If you know a cleaner way, please replace this block!

	    if (toevolve.decorators != null)
		for (Iterator<UIDecorator> decorators = toevolve.decorators.iterator(); decorators.hasNext();) {
			UIDecorator decorator = decorators.next();
			if (decorator instanceof UIFreeAttributeDecorator) {
				if (((UIFreeAttributeDecorator) decorator).attributes.get("height") != null) {
					height = String.valueOf(((UIFreeAttributeDecorator) decorator).attributes.get("height"));
				} else if (((UIFreeAttributeDecorator) decorator).attributes.get("width") != null) {
					width = String.valueOf(((UIFreeAttributeDecorator) decorator).attributes.get("width"));
				}
			}
		}

		UIContainer parent = toevolve.parent;
		toevolve.parent.remove(toevolve);
		UIJointContainer joint = new UIJointContainer(parent, toevolve.ID, COMPONENT_ID);

		joint.decorators = toevolve.decorators;

		toevolve.ID = SEED_ID; // must change ID while unattached
		joint.addComponent(toevolve);
		//(03:15:52 PM) Noah Botimer: call it like this: sakai.editor.launch('myawesometextarea', {height: 400, width: 600});
		//(03:16:04 PM) Noah Botimer: don't specify the width unless you have to
		String options = "{noop:0";
		if (height != null) {
			options += ",height:" + height;
		}
		if (width != null) {
			options += ",width:" + width;
		}
		options += "}";
		String js = HTMLUtil.emitJavascriptCall("sakai.editor.launch", new String[] { toevolve.getFullID(), options });
		UIVerbatim.make(joint, "textarea-js", js);

		return joint;
	}

}