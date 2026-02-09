package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;

/**
 * Jackson-based POX Response Header object
 * 
 * This class represents the header section of a POX response envelope.
 */
@JacksonXmlRootElement(localName = "imsx_POXHeader", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
@Data
@ToString
public class POXResponseHeader {
    
    @JacksonXmlProperty(localName = "imsx_POXResponseHeaderInfo", namespace = "http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0")
    private POXResponseHeaderInfo responseHeaderInfo;
}
