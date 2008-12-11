package org.sakaiproject.sitestats.tool.wicket.components;

import java.util.List;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.extensions.ajax.markup.html.WicketAjaxIndicatorAppender;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;


public class IndicatingAjaxDropDownChoice extends DropDownChoice implements IAjaxIndicatorAware {
	private static final long					serialVersionUID	= 1L;
	private final WicketAjaxIndicatorAppender	indicatorAppender	= new WicketAjaxIndicatorAppender();
	
	public IndicatingAjaxDropDownChoice(String id, List data, IChoiceRenderer renderer) {
		super(id, data, renderer);
		add(indicatorAppender);
	}	

	public String getAjaxIndicatorMarkupId() {
		return indicatorAppender.getMarkupId();
	}
}
