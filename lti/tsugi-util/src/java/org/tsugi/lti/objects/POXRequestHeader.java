package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;

@JacksonXmlRootElement(localName = "imsx_POXHeader", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
@Data
@ToString
public class POXRequestHeader {
    
    @JacksonXmlProperty(localName = "imsx_POXRequestHeaderInfo", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private POXRequestHeaderInfo requestHeaderInfo;
}
