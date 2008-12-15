package uk.ac.lancs.e_science.profile2.tool.components;

import java.util.Map;
import org.apache.wicket.markup.html.form.IChoiceRenderer;


/* HashMapChoiceRenderer.java
 * Steve Swinsburg
 * s.swinsburg@lancaster.ac.uk
 * December 2008
 * 
 * Wraps up the IChoiceRenderer actions into a simple constructor that takes a Map of choices that are to be rendered
 * So as to separate key/value in a dropdownchoice component and can be reused.
 * 
 */



public class HashMapChoiceRenderer implements IChoiceRenderer {
	
	private Map m_choices;
	
	public HashMapChoiceRenderer(Map choices) {
		m_choices = choices;
	}

	public String getDisplayValue(Object object) {
		return (String) m_choices.get(object);
	}

	public String getIdValue(Object object, int index) {
		return object.toString();
	}
}
