package org.sakaiproject.signup.tool.jsf.validator;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.io.Serializable;

/**
 * <P>
 * This class is a converter to make sure that user has to input something else
 * other than space(s) character only.
 * </P>
 */
public class StringTrimConverter implements Serializable, Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent cmp, String value) {

        if (value != null && cmp instanceof HtmlInputText) {
            // trim the entered value in a HtmlInputText before doing validation/updating the model
            return value.trim();
        }

        return value;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent cmp, Object value) {

        if (value != null) {
            // return the value as is for presentation
            return value.toString();
        }
        return null;
    }

}