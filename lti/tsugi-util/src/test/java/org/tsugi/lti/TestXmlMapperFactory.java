package org.tsugi.lti;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Factory for creating configured XmlMapper instances for tests.
 * 
 * Provides a standardized XmlMapper configuration used across all LTI test classes
 * to ensure consistent XML serialization/deserialization behavior.
 */
public class TestXmlMapperFactory {
    
    /**
     * Creates a new XmlMapper instance with standard test configuration:
     * - WRITE_XML_DECLARATION enabled (adds XML declaration to output)
     * - FAIL_ON_EMPTY_BEANS disabled (allows empty beans to serialize)
     * - Default use wrapper disabled (matches Jackson XML default behavior)
     * 
     * @return A configured XmlMapper instance for use in tests
     */
    public static XmlMapper createXmlMapper() {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setDefaultUseWrapper(false);
        return mapper;
    }
}
