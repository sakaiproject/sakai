package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.string.Strings;

/**
 * An AjaxCheckBox with some convenience methods to see whether or not it is checked
 * 
 * Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public abstract class EnablingCheckBox extends AjaxCheckBox{

	private static final long serialVersionUID = 1L;
	
	public EnablingCheckBox(final String id) {
		this(id, null);
	}
	
	public EnablingCheckBox(final String id, IModel model) {
		super(id, model);
	}
	 
	public boolean isChecked() {
	  
		final String value = getValue();
	  
		if (value != null) {
			try {
				return Strings.isTrue(value);
			} catch (StringValueConversionException e) {
				return false;
			}
		}
		return false;
	}
}