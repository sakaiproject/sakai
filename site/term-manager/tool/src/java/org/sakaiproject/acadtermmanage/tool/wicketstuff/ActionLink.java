package org.sakaiproject.acadtermmanage.tool.wicketstuff;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

/** An ordinary Wicket Link with a special id so it can be used in ActionPanel */
public abstract class ActionLink<T> extends Link<T> {

	private static final long serialVersionUID = 1L;
	
	

	public ActionLink(){
		super(ActionPanel.LINK_ID);
	}
	
	public ActionLink(IModel<T> model){
		super(ActionPanel.LINK_ID,model);
	}

}
