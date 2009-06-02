package uk.ac.lancs.e_science.profile2.tool.components;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/*
 * Must already have cluetip js libraries setup
 * 
 * Customise the behaviour by configuring like so, on your page:
 * 
 $(document).ready(function() {
	$('.sakai-wicket-iconwithtooltip').cluetip({
		local: true,
		showTitle: false,
		attribute: 'rel',
		cursor: 'pointer',
		cluetipClass: 'jtip',
		arrows: true,
		hoverIntent: true
	});	
});
 * 
 * Eventually this will be done via setters on this object
 * 
 * 
 */

public class IconWithClueTip extends Panel{
	
	public IconWithClueTip(String id, String iconUrl, IModel textModel) {
		super(id);
			
		//tooltip text
		Label text = new Label("text", textModel);
		text.setOutputMarkupId(true);
		add(text);
		
		//we need to id of the text span so that we can map it to the link.
		//the cluetip functions automatically hide it for us.
		StringBuilder textId = new StringBuilder();
		textId.append("#");
		textId.append(text.getMarkupId());
		
		//link
		AjaxFallbackLink link = new AjaxFallbackLink("link") {
			public void onClick(AjaxRequestTarget target) {
				//nothing
			}
		};
		link.add(new AttributeModifier("rel", true, new Model(textId)));
		link.add(new AttributeModifier("href", true, new Model(textId)));
		
		//image
		ContextImage image = new ContextImage("icon",new Model(iconUrl));
		link.add(image);
		
		add(link);
	
	}
	
	
}
