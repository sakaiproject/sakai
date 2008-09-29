package uk.ac.lancs.e_science.profile2.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.panel.Panel;

public class MyInterestsPanel extends Panel{
	
	private transient Logger log = Logger.getLogger(MyInterestsPanel.class);

	public MyInterestsPanel(String id) {
		
		super(id);
		
		//edit button
		add(new AjaxFallbackLink("editButton") {
			public void onClick(AjaxRequestTarget target) {
				// add the components that need to be updated to 
				// the target
			}
		});
		
	}
}
