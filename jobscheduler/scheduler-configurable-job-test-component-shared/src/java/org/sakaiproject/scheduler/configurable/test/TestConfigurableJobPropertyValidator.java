package org.sakaiproject.scheduler.configurable.test;

import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidationException;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidator;

import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 25, 2010
 * Time: 4:24:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestConfigurableJobPropertyValidator implements ConfigurableJobPropertyValidator
{
    private static final HashSet<String>
        VALID_BOOLEAN_VALUES = new HashSet<String> (4);

    static
    {
        VALID_BOOLEAN_VALUES.add("true");
        VALID_BOOLEAN_VALUES.add("false");
        VALID_BOOLEAN_VALUES.add("yes");
        VALID_BOOLEAN_VALUES.add("no");
    }

    private static final String
        INVALID_STRING = "bad";

    public void assertValid(String propertyLabel, String value)
        throws ConfigurableJobPropertyValidationException
    {
        if (TestConfigurableJob.STRING_PROPERTY.equals(propertyLabel))
        {
            assertStringValid(value);
        }
        else if (TestConfigurableJob.INTEGER_PROPERTY.equals(propertyLabel))
        {
            assertIntegerValid(value);
        }
        else if (TestConfigurableJob.BOOLEAN_PROPERTY.equals(propertyLabel))
        {
            assertBooleanValid(value);
        }
    }

    private void assertBooleanValid(String value)
        throws ConfigurableJobPropertyValidationException
    {
        if (value != null && value.trim().length() > 0 && !VALID_BOOLEAN_VALUES.contains(value))
        {
            throw new ConfigurableJobPropertyValidationException("boolean.value.invalid");
        }
    }

    private void assertIntegerValid(String value)
        throws ConfigurableJobPropertyValidationException
    {
        try
        {
            int
                iValue = Integer.parseInt(value);

            if (iValue == -1)
            {
                throw new ConfigurableJobPropertyValidationException ("integer.value.negativeone");
            }
        }
        catch (NumberFormatException nfe)
        {
            throw new ConfigurableJobPropertyValidationException("integer.value.format");
        }
    }

    private void assertStringValid(String value)
        throws ConfigurableJobPropertyValidationException {
        if (INVALID_STRING.equals(value))
        {
            throw new ConfigurableJobPropertyValidationException("string.value.invalid");
        }
    }
}
