package org.sakaiproject.assignment.impl.conversion;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidationException;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidator;

public class Assignment12ConversionJobValidator implements ConfigurableJobPropertyValidator {

    @Override
    public void assertValid(String propertyLabel, String value) throws ConfigurableJobPropertyValidationException {
        if (!StringUtils.isNumeric(value)) {
            throw new ConfigurableJobPropertyValidationException(propertyLabel + ".invalid");
        }
    }
}
