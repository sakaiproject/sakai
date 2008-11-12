package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.WicketAjaxIndicatorAppender;


public abstract class IndicatingAjaxCheckBox extends AjaxCheckBox implements IAjaxIndicatorAware {
	private static final long					serialVersionUID	= 1L;
	private final WicketAjaxIndicatorAppender	indicatorAppender	= new WicketAjaxIndicatorAppender();

	
	public IndicatingAjaxCheckBox(String id) {
		super(id);
		add(indicatorAppender);
	}

	@Override
	protected abstract void onUpdate(AjaxRequestTarget target);

	public String getAjaxIndicatorMarkupId() {
		return indicatorAppender.getMarkupId();
	}

}
