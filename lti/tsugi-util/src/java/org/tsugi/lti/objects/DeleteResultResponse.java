package org.tsugi.lti.objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.ToString;

/**
 * Jackson-based Delete Result Response object
 * 
 * This class represents a delete result response operation in POX.
 */
@Data
@ToString
@JacksonXmlRootElement(localName = "deleteResultResponse")
public class DeleteResultResponse {
    // Empty response body for delete result
}
