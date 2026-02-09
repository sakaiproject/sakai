package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;

@JacksonXmlRootElement(localName = "imsx_POXEnvelopeResponse", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
@Data
@ToString
public class POXEnvelopeResponse {
    
    @JacksonXmlProperty(localName = "imsx_POXHeader", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private POXResponseHeader poxHeader;
    
    @JacksonXmlProperty(localName = "imsx_POXBody", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private POXResponseBody poxBody;
}
