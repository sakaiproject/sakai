/**
 * Copyright (c) 2006-2017 The Apereo Foundation
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
package org.sakaiproject.sitestats.test;

import java.sql.Types;

import org.hibernate.dialect.HSQLDialect;

/**
 * For HSQLDB to work around @Lob with a default precision of 255
 * @see <a href="https://hibernate.atlassian.net/browse/HHH-7541">HHH-7541</a>
 */
public class CustomHsqlDialect extends HSQLDialect {

    public CustomHsqlDialect() {
        super();
        registerColumnType(Types.BLOB, "blob");
        registerColumnType(Types.CLOB, "clob");
    }
}
