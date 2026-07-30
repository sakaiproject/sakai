package org.sakaiproject.tool.section.jsf.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import org.sakaiproject.tool.section.jsf.JsfUtil;

public class SectionTitleValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent toValidate, Object value) throws ValidatorException {
        String str = (String) value;
        if (str.trim().length() < 1) {
            ((UIInput) toValidate).setValid(false);

            FacesMessage message = new FacesMessage();
            String messageText = JsfUtil.getLocalizedMessage("sectionTitle.validator.stringWithSpaceOnly");
            message.setDetail(messageText);
            message.setSummary(messageText);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }
}
