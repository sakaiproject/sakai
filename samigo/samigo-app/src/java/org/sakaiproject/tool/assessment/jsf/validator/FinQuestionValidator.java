package org.sakaiproject.tool.assessment.jsf.validator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.complex.ComplexFormat;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class FinQuestionValidator implements Validator {
	
	public FinQuestionValidator() {
		// TODO Auto-generated constructor stub
	}
	
	public void validate(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {
	
		String text = (String)value;
		
		int i = text.indexOf("{", 0);
		int j = text.indexOf("}", 0);
		
		while (i != -1) {
			String number = text.substring(i+1, j);
			
			StringTokenizer st = new StringTokenizer(number, "|");
		      
			if (st.countTokens() > 1) {
				String number1 = st.nextToken().trim();
		        String number2 = st.nextToken().trim();
		        
		        // The first value in range must have a valid format
		        if (!isRealNumber(number1)) {
					String error=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "fin_invalid_characters_error");
					throw new ValidatorException(new FacesMessage(error));
				}
		        
		        // The second value in range must have a valid format
		        if (!isRealNumber(number2)) {
					String error=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "fin_invalid_characters_error");
					throw new ValidatorException(new FacesMessage(error));
				}
		        
		        // The range must be in increasing order
		        BigDecimal rango1 = new BigDecimal(number1);
		        BigDecimal rango2 = new BigDecimal(number2);
		        if (rango1.compareTo(rango2) != -1) {
		        	String error=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "fin_invalid_characters_error");
		        	throw new ValidatorException(new FacesMessage(error));
		        }
		    }
			else {
		    	// The number can be in a decimal format or complex format
				if (!isRealNumber(number) && !isComplexNumber(number)) {
					String error=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "fin_invalid_characters_error");
					throw new ValidatorException(new FacesMessage(error));
				}
			}
	
			i = text.indexOf("{", i+1);
			if (j+1 < text.length()) j = text.indexOf("}", j+1);
			else j = -1;
		}
		
	}
	
	static boolean isComplexNumber(String value) {
		
		boolean isComplex = true;
		Complex complex=null;
		try {
			DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance(Locale.US);
			df.setGroupingUsed(false);
			
			// Numerical format ###.## (decimal symbol is the point)
			ComplexFormat complexFormat = new ComplexFormat(df);
			complex = complexFormat.parse(value);

		// This is because there is a bug parsing complex number. 9i is parsed as 9
			if (complex.getImaginary() == 0 && value.contains("i")) isComplex = false;
		} catch (Exception e) {
			isComplex = false;
		}

	return isComplex;
	}
	
	static boolean isRealNumber(String value) {
		
		boolean isReal = true;
		try {
			// Number has decimal format? If no, Exception is throw
			new BigDecimal(value);
			
		} catch (Exception e) {
			isReal = false;
		}
	
		return isReal;
	}
	
}
