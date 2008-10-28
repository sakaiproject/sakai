package uk.ac.lancs.e_science.profile2.tool.components;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;

public class AjaxIndicator extends WebMarkupContainer {

	public AjaxIndicator(String id) {
		super(id);
		setOutputMarkupId(true);
	}

	public void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("src", urlFor(AbstractDefaultAjaxBehavior.INDICATOR));
	}
	
	
}
