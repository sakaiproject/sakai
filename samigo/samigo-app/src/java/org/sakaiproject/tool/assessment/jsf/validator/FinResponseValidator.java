package org.sakaiproject.tool.assessment.jsf.validator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.complex.ComplexFormat;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class FinResponseValidator implements Validator {

	public FinResponseValidator() {
		// TODO Auto-generated constructor stub
	}

	public void validate(FacesContext context, UIComponent component, Object value)
	throws ValidatorException {

		// The number can be in a decimal format or complex format
		if (!FinQuestionValidator.isRealNumber((String)value) && !FinQuestionValidator.isComplexNumber((String)value)) {
			String error = (String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "fin_invalid_characters_error");
			throw new ValidatorException(new FacesMessage(error));
		}
	}
}
