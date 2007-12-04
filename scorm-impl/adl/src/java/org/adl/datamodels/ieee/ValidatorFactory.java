package org.adl.datamodels.ieee;

import org.adl.datamodels.datatypes.DateTimeValidator;
import org.adl.datamodels.datatypes.DateTimeValidatorImpl;
import org.adl.datamodels.datatypes.InteractionValidator;
import org.adl.datamodels.datatypes.InteractionValidatorImpl;

public class ValidatorFactory implements IValidatorFactory {

	public ValidatorFactory() {}
	
	public DateTimeValidator newDateTimeValidator(boolean isTrue) {
		return new DateTimeValidatorImpl(isTrue);
	}
	
	public InteractionValidator newInteractionValidator(int iType, String iElement) {
		return new InteractionValidatorImpl(iType, iElement); 
	}

	public InteractionValidator newInteractionValidator(int iType, boolean iAllowEmpty, String iElement) {
		return new InteractionValidatorImpl(iType, iAllowEmpty, iElement);
	}
	
}
