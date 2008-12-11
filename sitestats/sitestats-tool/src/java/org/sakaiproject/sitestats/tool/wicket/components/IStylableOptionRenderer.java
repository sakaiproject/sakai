package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;

public interface IStylableOptionRenderer extends IOptionRenderer {

	public String getStyle(Object value);
}
