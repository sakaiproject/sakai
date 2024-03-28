package org.adl.datamodels.ieee;

import java.io.Serializable;

import org.adl.datamodels.datatypes.DateTimeValidator;
import org.adl.datamodels.datatypes.InteractionValidator;

public interface IValidatorFactory extends Serializable {

	public DateTimeValidator newDateTimeValidator(boolean isTrue);

	public InteractionValidator newInteractionValidator(int iType, boolean iAllowEmpty, String iElement);

	public InteractionValidator newInteractionValidator(int iType, String iElement);

}
