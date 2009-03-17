package uk.ac.lancs.e_science.profile2.tool.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.Model;

/** 
 * ExternalImage is a component that renders an image given a url
 * 
 * @author	Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 * @since	March, 2009
 *
 */
public class ExternalImage extends WebComponent {

	private static final long serialVersionUID = 1L;

	/**
	 * Render an image into an 'img' tag with the wicket:id and src url
	 * @param id
	 * @param imageUrl
	 */
	public ExternalImage(String id, String imageUrl) {
	    super(id);
	    add(new AttributeModifier("src", true, new Model(imageUrl)));
	    setVisible(!(imageUrl==null || imageUrl.equals("")));
	}
	
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		checkComponentTag(tag, "img");
	}

}