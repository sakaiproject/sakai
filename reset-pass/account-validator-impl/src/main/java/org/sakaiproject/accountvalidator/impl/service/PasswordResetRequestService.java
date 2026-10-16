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
package org.sakaiproject.accountvalidator.impl.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Setter;
import org.sakaiproject.accountvalidator.api.model.PasswordReset;
import org.sakaiproject.accountvalidator.api.repository.ValidationAccountRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Reserves durable rolling quotas before any email or password-reset work is attempted. */
@Setter
public class PasswordResetRequestService {
    private ValidationAccountRepository repository;
    private Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean reserve(String key, int maximum, int cooldownSeconds) {
        Instant now = clock.instant();
        PasswordReset quota = repository.findPasswordResetByUserId(key).orElseGet(() -> {
            PasswordReset created = new PasswordReset();
            created.setUserId(key);
            return created;
        });
        quota.getRequests().removeIf(time -> !time.isAfter(now.minus(1, ChronoUnit.HOURS)));
        if (quota.getRequests().size() >= maximum
                || quota.getRequests().stream().anyMatch(time -> time.isAfter(now.minusSeconds(cooldownSeconds)))) {
            return false;
        }
        quota.getRequests().add(now);
        // @Version and the primary key make concurrent reservations conflict, including first requests.
        repository.savePasswordReset(quota);
        return true;
    }
}
