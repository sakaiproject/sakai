package uk.ac.lancs.e_science.profile2.tool.components;


import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.tool.pages.panels.MyInfoEdit;

public class CloseButton extends Panel{

	private static final String CLOSE_IMAGE = "/library/image/silk/cross.png";
	
	
	public CloseButton(String id) {
		super(id);
		
		final String thisComponent = id;
	
		//container
		WebMarkupContainer closeButton = new WebMarkupContainer("closeButton");
		closeButton.setOutputMarkupId(true);
		
		//image
		ContextImage image = new ContextImage("img",new Model(CLOSE_IMAGE));
		/*
		AjaxFallbackLink editButton = new AjaxFallbackLink("editButton", new ResourceModel("button.edit")) {
			public void onClick(AjaxRequestTarget target) {
				Component newPanel = new MyInfoEdit(id, userProfile);
				newPanel.setOutputMarkupId(true);
				thisPanel.replaceWith(newPanel);
				if(target != null) {
					target.addComponent(newPanel);
					//resize iframe
					target.appendJavascript("setMainFrameHeight(window.name);");
				}
			}
						
		};
		*/
		
		
		Link link = new Link("link") {
			public void onClick() {
				//setResponsePage(new TestData());
			}
		};
		link.add(image);
		
		closeButton.add(link);
		
		add(closeButton);
		
		
	
		
	
		
		//extend this to allow a behaviour to be set so that when its clicked, something happens
	}
	
	
}
