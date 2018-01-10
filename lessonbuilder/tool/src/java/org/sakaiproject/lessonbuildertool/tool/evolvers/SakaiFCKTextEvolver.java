/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
/*
 * Created on 22 Sep 2006 Temp fix for http://www.caret.cam.ac.uk/jira/browse/RSF-44 from
 * http://ponder.org.uk/rsf/posts/list/184.page
 * 
 * This class replaces the text area with the FCKEditor.
 */
package org.sakaiproject.lessonbuildertool.tool.evolvers;

import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.htmlutil.HTMLUtil;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.component.cover.ServerConfigurationService;

@Slf4j
public class SakaiFCKTextEvolver implements TextInputEvolver {
	public static final String COMPONENT_ID = "sakai-FCKEditor:";
	private String context;
	private ContentHostingService contentHostingService;
        private static boolean newEditor = isNewEditor();

	/*
	 * Default height and width, can be set at the tool level in the requestContext.xml bean def or
	 * by setting decorators at the producer level.
	 */
	public String height = "600";
	public String width = "400";

	public void setContext(String context) {
		this.context = context;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	public UIJointContainer evolveTextInput(UIInput toevolve) {
		return evolveTextInput(toevolve, "1");
	}

        private static boolean isNewEditor() {
	    // 2.8 and later are new editor calling 

	    String sakaiVersion = ServerConfigurationService.getString("version.sakai", "2.6");

	    boolean isNew = false;
	    int version = 2;
	    int major = 6;
	    int minor = 0;
	    if (sakaiVersion != null) {
		String []parts = sakaiVersion.split("\\.");
		if (parts.length >= 1) {
		    try {
			version = Integer.parseInt(parts[0]);
		    } catch (Exception e) {
		    };
		}
		if (parts.length >= 2) {
		    try {
			String[] s = parts[1].split("\\D");
			major = Integer.parseInt(s[0]);
		    } catch (Exception e) {
		    };
		}
		// may be something like 2.8.1-foo, so must terminate on non-digit
		if (parts.length >= 3) {
		    try {
			String[] s = parts[2].split("\\D");
			minor = Integer.parseInt(s[0]);
		    } catch (Exception e) {
		    };
		}

		// samigo starting with 2.8.0 has the new editor calling protocol
		if (version > 2 || (version == 2 && major >= 8))
		    isNew = true;
		log.info("EditPage thinks Sakai version is " + version + " major " + major + " isNew=" + isNew);
	    }
	    //log.info("isnew " + isNew);
	    return isNew;
	}

	public UIJointContainer evolveTextInput(UIInput toevolve, String index) {

		// dig out the size decorators and adjust the editor size.
		// TODO: If you know a cleaner way, please replace this block!
		for (Iterator<UIDecorator> decorators = toevolve.decorators.iterator(); decorators.hasNext();) {
			UIDecorator decorator = decorators.next();
			if (decorator instanceof uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator) {
				if (((UIFreeAttributeDecorator) decorator).attributes.get("height") != null) {
					try {
						height = (String) ((UIFreeAttributeDecorator) decorator).attributes.get("height");
					} catch (Exception e) {
						// height isn't set to a string, show the developer a stack trace since
						// he/she is lost...
						log.error(e.getMessage(), e);
					}
				} else if (((UIFreeAttributeDecorator) decorator).attributes.get("width") != null) {
					try {
						width = (String) ((UIFreeAttributeDecorator) decorator).attributes.get("width");
					} catch (Exception e) {
						// height isn't set to a string, show the developer a stack trace since
						// he/she is lost...
						log.error(e.getMessage(), e);
					}
				}
			}
		}

		String editor = ServerConfigurationService.getString("wysiwyg.editor");
		
		UIContainer parent = toevolve.parent;
		toevolve.parent.remove(toevolve);
		UIJointContainer joint = new UIJointContainer(parent, toevolve.ID, COMPONENT_ID);

		joint.decorators = toevolve.decorators;

		String id = toevolve.ID;
		toevolve.ID = SEED_ID; // must change ID while unattached
		joint.addComponent(toevolve);
		String js = null;

		if (newEditor || "ckeditor".equals(editor)) {
		    js = HTMLUtil.emitJavascriptCall("sakai.editor.launch", new String[] { "\"" + toevolve.getFullID() + "\"", "{baseFloatZIndex: 100010}", "\"800px\"", "\"200px\""}, false);
		} else {
		    String collectionID = context.equals("") ? "" : contentHostingService.getSiteCollection(context);
		    js = HTMLUtil.emitJavascriptCall("SakaiProject.fckeditor.initializeEditor", new String[] { toevolve.getFullID(), collectionID, height, width });
		}
		
		UIVerbatim.make(joint, "textarea-js", js);

		return joint;
	}

}
