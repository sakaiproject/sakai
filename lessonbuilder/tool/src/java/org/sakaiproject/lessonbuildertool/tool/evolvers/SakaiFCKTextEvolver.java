/*
 * Created on 22 Sep 2006 Temp fix for http://www.caret.cam.ac.uk/jira/browse/RSF-44 from
 * http://ponder.org.uk/rsf/posts/list/184.page
 * 
 * This class replaces the text area with the FCKEditor.
 */
package org.sakaiproject.lessonbuildertool.tool.evolvers;

import java.util.Iterator;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.component.cover.ServerConfigurationService;

import uk.org.ponder.htmlutil.HTMLUtil;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
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

	public void setContext(String context) {
		this.context = context;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	public UIJointContainer evolveTextInput(UIInput toevolve) {
		return evolveTextInput(toevolve, "1");
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
						e.printStackTrace();
					}
				} else if (((UIFreeAttributeDecorator) decorator).attributes.get("width") != null) {
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

		String editor = ServerConfigurationService.getString("wysiwyg.editor");
		
		UIContainer parent = toevolve.parent;
		toevolve.parent.remove(toevolve);
		UIJointContainer joint = new UIJointContainer(parent, toevolve.ID, COMPONENT_ID);

		joint.decorators = toevolve.decorators;

		String id = toevolve.ID;
		toevolve.ID = SEED_ID; // must change ID while unattached
		joint.addComponent(toevolve);
		String js = null;

		if ("ckeditor".equals(editor)) {
		    js = HTMLUtil.emitJavascriptCall("sakai.editor.launch", new String[] { toevolve.getFullID(), null, "800px", "200px"});
		} else {
		    String collectionID = context.equals("") ? "" : contentHostingService.getSiteCollection(context);
		    js = HTMLUtil.emitJavascriptCall("SakaiProject.fckeditor.initializeEditor", new String[] { toevolve.getFullID(), collectionID, height, width });
		}
		
		UIVerbatim.make(joint, "textarea-js", js);

		return joint;
	}

}
