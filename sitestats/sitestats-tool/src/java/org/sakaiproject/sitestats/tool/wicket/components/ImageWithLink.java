package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;


/**
 * @author Nuno Fernandes
 */
public class ImageWithLink extends Panel {
	private static final long		serialVersionUID	= 1L;

	public ImageWithLink(String id) {
		this(id, null, null, null, null);
	}
	
	public ImageWithLink(String id, String imgUrl, String lnkUrl, String lnkLabel, String lnkTarget) {
		super(id);
		setRenderBodyOnly(false);
		boolean exists = (lnkTarget != null && lnkLabel != null && lnkUrl != null);
		ExternalLink lnk = null;
		if(exists) {
			add( new ExternalImage("image", imgUrl).setVisible(imgUrl != null) );
			lnk = new ExternalLink("link", lnkUrl, lnkLabel);
			lnk.add(new AttributeModifier("target", true, new Model(lnkTarget)));
		}else{
			StringBuilder b = new StringBuilder();
			b.append(lnkLabel);
			b.append(' ');
			b.append(((String) new ResourceModel("resource_unknown").getObject()));
			add( new ExternalImage("image", "/sakai-sitestats-tool/images/silk/icons/cross.png").setVisible(true) );
			lnk = new ExternalLink("link", lnkUrl, b.toString());
			lnk.setEnabled(false);
		}
		add(lnk);
	}
}
