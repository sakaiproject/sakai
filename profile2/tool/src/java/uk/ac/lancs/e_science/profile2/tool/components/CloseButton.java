package uk.ac.lancs.e_science.profile2.tool.components;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;

public class CloseButton extends WebMarkupContainer{

	public CloseButton(String id) {
		super(id);
		setOutputMarkupId(true);
	}

	/*
	 * this is meant to be a drop in close button component, will need to accept some args
	 * 
	 */

}
