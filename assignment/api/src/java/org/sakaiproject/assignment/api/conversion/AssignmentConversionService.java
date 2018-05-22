package org.sakaiproject.assignment.api.conversion;

import java.util.List;

public interface AssignmentConversionService {

    /**
     * Starts the assignments conversion
     *
     * @param numberOfAttributes maximum attributes per element in an xml document, 0 means use default
     * @param lengthOfAttribute maximum characters of an attribute, 0 means use default
     */
    void runConversion(int numberOfAttributes, int lengthOfAttribute);

    Object serializeFromXml(String xml, Class clazz);
}
