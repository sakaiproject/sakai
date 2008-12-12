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
	
	
	public CloseButton(String id, final String parentId) {
		super(id);
			
		//container
		WebMarkupContainer closeButton = new WebMarkupContainer("closeButton");
		closeButton.setOutputMarkupId(true);
		
		//image
		ContextImage image = new ContextImage("img",new Model(CLOSE_IMAGE));
		
		AjaxFallbackLink link = new AjaxFallbackLink("link") {
			public void onClick(AjaxRequestTarget target) {
				if(target != null) {
					
					target.appendJavascript("$('#" + parentId + "').slideUp();");
					target.appendJavascript("setMainFrameHeight(window.name);");

					//do we also need to remove the component as well?
					
					//resize iframe
					//target.appendJavascript("setMainFrameHeight(window.name);");
				}
			}
						
		};
		
		
		link.add(image);
		
		closeButton.add(link);
		
		add(closeButton);
		
		
	
		
	
		
		//extend this to allow a behaviour to be set so that when its clicked, something happens
	}
	
	
}
