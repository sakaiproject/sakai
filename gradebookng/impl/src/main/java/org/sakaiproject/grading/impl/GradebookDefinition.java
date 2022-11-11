/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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
package org.sakaiproject.grading.impl;

import java.io.Externalizable;
import java.util.Collection;
import java.util.Map;
import java.util.List;

import org.sakaiproject.grading.api.Assignment;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GradebookDefinition extends VersionedExternalizable implements Externalizable {

    private static final long serialVersionUID = 1L;
    public static final String EXTERNALIZABLE_VERSION = "1";

    private String selectedGradingScaleUid;
    private Map<String, Double> selectedGradingScaleBottomPercents;
    private Collection<Assignment> assignments;
    private int gradeType;
    private int categoryType;
    private List category;

    public String getExternalizableVersion() {
        return EXTERNALIZABLE_VERSION;
    }
}
