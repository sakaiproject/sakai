package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;


public class AjaxIndicator extends WebMarkupContainer implements IAjaxIndicatorAware {

	public AjaxIndicator(String id) {
		super(id);
	}

	public void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("src", urlFor(AbstractDefaultAjaxBehavior.INDICATOR));
	}

	public String getAjaxIndicatorMarkupId() {
		return this.getMarkupId();
	}
	
	
}
