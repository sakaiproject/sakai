package org.sakaiproject.tool.gradebook.jsf;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Validates that drop scores values entered on the Gradebook setup page (dropLowest, dropHighest, keepHighest) are positive.
 * Cannot perform cross validation here, because JSF does not update the GradebookSetupBean.categories with values
 * entered on the screen before the validation phase
 *
 */
public class DropScoresValidator implements Validator, Serializable {

	/**
     * default
     */
    private static final long serialVersionUID = 1L;

    /**
	 * @see javax.faces.validator.Validator#validate(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.Object)
	 */
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
	    
        if (value != null) {
            if (!(value instanceof Integer)) {
                throw new IllegalArgumentException("The drop score value must be a Integer");
            }
        }

        try {
            Integer toValidate = (Integer)value;
            if(toValidate < 0) {
                throw new ValidatorException(new FacesMessage(FacesUtil.getLocalizedString(context, "cat_itemvalue_require_positive")));
            }
        } catch(NumberFormatException e) {
            throw new ValidatorException(new FacesMessage(FacesUtil.getLocalizedString(context, "cat_itemvalue_invalid")));
        }
	}
}


