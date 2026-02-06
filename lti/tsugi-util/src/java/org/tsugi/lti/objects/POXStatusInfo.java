package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class POXStatusInfo {
    
    @JacksonXmlProperty(localName = "imsx_codeMajor", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private String codeMajor;
    
    @JacksonXmlProperty(localName = "imsx_severity", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private String severity;
    
    @JacksonXmlProperty(localName = "imsx_description", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private String description;
    
    @JacksonXmlProperty(localName = "imsx_messageRefIdentifier", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private String messageRefIdentifier;
    
    @JacksonXmlProperty(localName = "imsx_operationRefIdentifier", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private String operationRefIdentifier;
    
    @JacksonXmlProperty(localName = "imsx_codeMinor", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private POXCodeMinor codeMinor;
}
