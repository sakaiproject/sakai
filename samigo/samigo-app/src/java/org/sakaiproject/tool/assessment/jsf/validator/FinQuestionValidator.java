/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.jsf.validator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexFormat;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class FinQuestionValidator implements Validator {
	
	public FinQuestionValidator() {
		// TODO Auto-generated constructor stub
	}
	
	public void validate(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {
	
		String text = (String) value;
		text = text.replaceAll("\\s+", "").replace(',','.');  // in Spain, comma is used as a decimal point 	 
		
		int i = text.indexOf("{", 0);
		int j = text.indexOf("}", 0);
		
		while (i != -1 && j > i) {
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

			// Only checks for complex numbers, not real numbers 
			if (complex.getImaginary() == 0) {
				isComplex = false;
			}
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
