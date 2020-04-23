/**
 * Copyright (c) 2003-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.assignment.impl.conversion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
