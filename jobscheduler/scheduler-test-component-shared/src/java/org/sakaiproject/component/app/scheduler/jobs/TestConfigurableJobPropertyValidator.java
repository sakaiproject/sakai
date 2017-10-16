/**
 * Copyright (c) 2003-2010 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.scheduler.jobs;

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
