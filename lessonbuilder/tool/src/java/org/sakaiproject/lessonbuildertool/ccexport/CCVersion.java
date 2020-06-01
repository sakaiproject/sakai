/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.ccexport;

public enum CCVersion {
    V10(0),
    V11(1),
    V12(2),
    V13(3);

    private final int version;

    CCVersion(int version) {
        this.version = version;
    }

    public CCVersion getVersion() {
        return CCVersion.values()[version];
    }

    public boolean equals(CCVersion ccVersion) {
        return this.version == ccVersion.version;
    }

    public boolean greaterThan(CCVersion ccVersion) {
        return this.version > ccVersion.version;
    }

    public boolean lessThan(CCVersion ccVersion) {
        return this.version < ccVersion.version;
    }

    public boolean greaterThanOrEqualTo(CCVersion ccVersion) {
        return (equals(ccVersion) || greaterThan(ccVersion));
    }

    public boolean lessThanOrEqualTo(CCVersion ccVersion) {
        return (equals(ccVersion) || lessThan(ccVersion));
    }
}
