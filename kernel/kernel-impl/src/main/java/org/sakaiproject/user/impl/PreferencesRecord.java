/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.user.impl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "SAKAI_PREFERENCES")
@Getter
@Setter
@NoArgsConstructor
public class PreferencesRecord {

    @Id
    @Column(name = "PREFERENCES_ID", length = 99, nullable = false)
    private String id;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "XML", length = Integer.MAX_VALUE)
    private String xml;

    @Version
    @Column(name = "VERSION", nullable = false)
    @ColumnDefault("0")
    private Long version;

    public PreferencesRecord(String id, String xml, Long version) {
        this.id = id;
        this.xml = xml;
        this.version = version;
    }
}
