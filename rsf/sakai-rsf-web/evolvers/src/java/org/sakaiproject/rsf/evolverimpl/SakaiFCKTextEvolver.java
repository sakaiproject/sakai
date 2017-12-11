/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
/*
 * Created on 22 Sep 2006 Temp fix for http://www.caret.cam.ac.uk/jira/browse/RSF-44 from
 * http://ponder.org.uk/rsf/posts/list/184.page
 * 
 * This class replaces the text area with the FCKEditor.
 */
package org.sakaiproject.rsf.evolverimpl;

import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;

import uk.org.ponder.htmlutil.HTMLUtil;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;

@Slf4j
public class SakaiFCKTextEvolver implements TextInputEvolver {
	public static final String COMPONENT_ID = "sakai-FCKEditor:";
	private String context;
	private ContentHostingService contentHostingService;

	/*
	 * Default height and width, can be set at the tool level in the
	 * requestContext.xml bean def or by setting decorators at the producer
	 * level.
	 */
	public String height = "600";
	public String width = "400";
	// there are two versions of this code, one that sets size and one that
	// doesn't. Try to do both
	boolean sizeset = false;

	public void setContext(String context) {
		this.context = context;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	private ServerConfigurationService serverConfigurationService;

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public UIJointContainer evolveTextInput(UIInput toevolve) {

		// dig out the size decorators and adjust the editor size.
		// TODO: If you know a cleaner way, please replace this block!

		if (toevolve.decorators != null)
			for (Iterator<UIDecorator> decorators = toevolve.decorators.iterator(); decorators.hasNext();) {
				UIDecorator decorator = decorators.next();
				if (decorator instanceof uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator) {
					if (((UIFreeAttributeDecorator) decorator).attributes.get("height") != null) {
						sizeset = true;
						try {
							height = (String) ((UIFreeAttributeDecorator) decorator).attributes.get("height");
						} catch (Exception e) {
							// height isn't set to a string, show the developer
							// a stack trace since
							// he/she is lost...
							log.error(e.getMessage(), e);
						}
					} else if (((UIFreeAttributeDecorator) decorator).attributes.get("width") != null) {
						sizeset = true;
						try {
							width = (String) ((UIFreeAttributeDecorator) decorator).attributes.get("width");
						} catch (Exception e) {
							// height isn't set to a string, show the developer
							// a stack trace since
							// he/she is lost...
							log.error(e.getMessage(), e);
						}
					}
				}
			}

		String editor = serverConfigurationService.getString("wysiwyg.editor");

		UIContainer parent = toevolve.parent;
		toevolve.parent.remove(toevolve);
		UIJointContainer joint = new UIJointContainer(parent, toevolve.ID, COMPONENT_ID);

		joint.decorators = toevolve.decorators;

		toevolve.ID = SEED_ID; // must change ID while unattached
		joint.addComponent(toevolve);
		String js = null;
		if ("ckeditor".equals(editor)) {
			js = HTMLUtil.emitJavascriptCall("sakai.editor.launch", new String[] { toevolve.getFullID() });
		} else {
			String collectionID = context.equals("") ? "" : contentHostingService.getSiteCollection(context);
			if (sizeset)
				js = HTMLUtil.emitJavascriptCall("SakaiProject.fckeditor.initializeEditor",
						new String[] { toevolve.getFullID(), collectionID, height, width });
			else
				js = HTMLUtil.emitJavascriptCall("SakaiProject.fckeditor.initializeEditor",
						new String[] { toevolve.getFullID(), collectionID });

		}
		UIVerbatim.make(joint, "textarea-js", js);

		return joint;
	}

}
