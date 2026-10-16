/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.accountvalidator.api.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Lob;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** A password-reset token and its rolling email history, independent of account validation. */
@Entity
@Table(name = "PASSWORD_RESET", uniqueConstraints = @UniqueConstraint(name = "UK_PASSWORD_RESET_TOKEN", columnNames = "RESET_TOKEN"))
@Getter
@Setter
public class PasswordReset {
    @Id
    @Column(name = "USER_ID", length = 99)
    private String userId;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @Column(name = "RESET_TOKEN", length = 36)
    private String token;

    @Column(name = "CREATED_AT")
    private Instant createdAt;

    @Column(name = "CONSUMED_AT")
    private Instant consumedAt;

    @Lob
    @Column(name = "REQUEST_TIMES", length = 65535, nullable = false)
    @Convert(converter = PasswordResetTimesConverter.class)
    private List<Instant> requests = new ArrayList<>();
}
