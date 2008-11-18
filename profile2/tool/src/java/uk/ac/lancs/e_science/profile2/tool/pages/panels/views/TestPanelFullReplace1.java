package uk.ac.lancs.e_science.profile2.tool.pages.panels.views;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.views.TestPanelFullReplace2;


public class TestPanelFullReplace1 extends Panel {
	
	
	public TestPanelFullReplace1(String id) {
		super(id);
		
		final Component thisPanel = this;
		
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new Model("click me")) {
			public void onClick(AjaxRequestTarget target) {
				/*			
				Component newPanel = new TestPanelFullReplace2("fullReplace");
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
				}
				*/
			}
						
		};
		editButton.setOutputMarkupId(true);
		add(editButton);
		
		
		
		
	}
	
}
