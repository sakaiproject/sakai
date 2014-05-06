package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Class to add a confirm javascript window as needed.
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class JavascriptEventConfirmation extends AttributeModifier {
	
	private static final long serialVersionUID = 1L;

	public JavascriptEventConfirmation(String event, IModel model) {
		super(event, model);
	}

	protected String newValue(final String currentValue, final String replacementValue) {
		String prefix = "var conf = confirm('" + replacementValue + "'); " + "if (!conf) return false; ";
		String result = prefix;
		if (currentValue != null) {
			result = prefix + currentValue;
		}
		return result;
	}
}