package org.sakaiproject.tool.gradebook.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.NumberConverter;
import javax.faces.validator.ValidatorException;

import org.sakaiproject.util.ResourceLoader;

/*
 * converts drop scores values to Integer (from the default Long) to avoid a ClassCastException
 * as JSF tries to assign the Long values to Category Integer attributes (dropLowest, dropHighest, keepHighest).
 */
public class DropScoresConverter extends NumberConverter {

    public DropScoresConverter() {
        setType("number");
        setLocale(new ResourceLoader().getLocale());         
    }
    
    public Object getAsObject(FacesContext context, UIComponent component, String newValue) throws ConverterException {
        if(newValue.indexOf('.') != -1) {
            FacesMessage message = new FacesMessage(FacesUtil.getLocalizedString("cat_drop_score_too_precise"));
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(message);
        }

        try {
            Integer converted = new Integer(newValue);
            return converted;
        } catch(NumberFormatException e) {
            FacesMessage message = new FacesMessage(FacesUtil.getLocalizedString("cat_invalid_drop_score"));
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(message);
        }
    }
}
