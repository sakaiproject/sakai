package org.sakaiproject.acadtermmanage.tool.wicketstuff;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
/**
 * A panel containing a single Link
 */
public class ActionPanel <S>extends Panel {
	private static final long serialVersionUID = 1L;
	
	public static final String LINK_ID="id_action_link";
	
	public ActionPanel(String id, Link<S> theActionLink) {
		super(id);
		add(theActionLink);
	}

	

}
