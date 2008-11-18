package uk.ac.lancs.e_science.profile2.tool.components;


import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class CloseButton extends Panel{

	private static final String CLOSE_IMAGE = "/library/image/silk/cross.png";
	
	public CloseButton(String id) {
		super(id);
	
		WebMarkupContainer closeButton = new WebMarkupContainer("closeButton");
		closeButton.setOutputMarkupId(true);
		
		ContextImage image = new ContextImage("img",new Model(CLOSE_IMAGE));
		closeButton.add(image);
		
		add(closeButton);
	
		
		//extend this to allow a behaviour to be set so that when its clicked, something happens
	}
	
	
}
