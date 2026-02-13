package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class POXResponseHeaderInfo {
    
    @JacksonXmlProperty(localName = "imsx_version", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private String version;
    
    @JacksonXmlProperty(localName = "imsx_messageIdentifier", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private String messageIdentifier;
    
    @JacksonXmlProperty(localName = "imsx_statusInfo", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private POXStatusInfo statusInfo;
}
