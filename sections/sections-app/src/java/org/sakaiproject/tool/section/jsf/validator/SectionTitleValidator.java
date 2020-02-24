package org.sakaiproject.tool.section.jsf.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

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
