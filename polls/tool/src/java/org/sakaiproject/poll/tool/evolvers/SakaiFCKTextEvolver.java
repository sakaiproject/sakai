/*
 * Created on 22 Sep 2006 Temp fix for http://www.caret.cam.ac.uk/jira/browse/RSF-44 from
 * http://ponder.org.uk/rsf/posts/list/184.page
 * 
 * This class replaces the text area with the FCKEditor.
 */
package org.sakaiproject.poll.tool.evolvers;

import java.util.Iterator;

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

public class SakaiFCKTextEvolver implements TextInputEvolver {
	public static final String COMPONENT_ID = "sakai-FCKEditor:";
	private String context;
	private ContentHostingService contentHostingService;

	/*
	 * Default height and width, can be set at the tool level in the requestContext.xml bean def or
	 * by setting decorators at the producer level.
	 */
	public String height = "600";
	public String width = "400";
	// there are two versions of this code, one that sets size and one that doesn't. Try to do both
	boolean sizeset = false;

	public void setContext(String context) {
		this.context = context;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}


	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
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
							// height isn't set to a string, show the developer a stack trace since
							// he/she is lost...
							e.printStackTrace();
						}
					} else if (((UIFreeAttributeDecorator) decorator).attributes.get("width") != null) {
						sizeset = true;
						try {
							width = (String) ((UIFreeAttributeDecorator) decorator).attributes.get("width");
						} catch (Exception e) {
							// height isn't set to a string, show the developer a stack trace since
							// he/she is lost...
							e.printStackTrace();
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
				js = HTMLUtil.emitJavascriptCall("SakaiProject.fckeditor.initializeEditor", new String[] { toevolve.getFullID(), collectionID, height, width });
			else
				js = HTMLUtil.emitJavascriptCall("SakaiProject.fckeditor.initializeEditor", new String[] { toevolve.getFullID(), collectionID });

		}
		UIVerbatim.make(joint, "textarea-js", js);

		return joint;
	}

}
