package org.sakaiproject.tool.assessment.jsf.convert;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.sakaiproject.util.ResourceLoader;

public class AnswerSurveyConverter implements Converter {

	private static ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");
	
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		
		return getAsString(arg0, arg1, arg2);
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		String text = (String)arg2;
		if (text == null) return null;
		
		if (text.equals("st_agree") || text.equals("st_disagree") || text.equals("st_undecided")
			|| text.equals("st_below_average") || text.equals("st_average") || text.equals("st_above_average")
			|| text.equals("st_strongly_disagree") || text.equals("st_strongly_agree") 
			|| text.equals("st_unacceptable") || text.equals("st_excellent")
			|| text.equals("st_yes") || text.equals("st_no"))
		{
			return rb.getString(text);
		}
		return text;
	}

}
