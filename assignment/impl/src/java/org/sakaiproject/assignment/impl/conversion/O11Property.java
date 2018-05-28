package org.sakaiproject.assignment.impl.conversion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

@Data
@EqualsAndHashCode(of = "name")
@Slf4j
public class O11Property {
    // List of name=value where value is enc is the encoding used in value
    // <property enc="BASE64" name="XXX" value="YYY"/>
    private String enc;
    private String name;
    private String value;

    public String getDecodedValue() {
        if (StringUtils.isBlank(value)) return null;
        if ("BASE64".equals(enc)) {
            return AssignmentConversionServiceImpl.decodeBase64(value);
        }
        return value;
    }
}
