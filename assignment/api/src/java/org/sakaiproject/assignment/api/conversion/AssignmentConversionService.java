package org.sakaiproject.assignment.api.conversion;

import java.util.List;

public interface AssignmentConversionService {

    void runConversion();

    Object serializeFromXml(String xml, Class clazz);
}
