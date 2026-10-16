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
import java.util.stream.Collectors;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Keeps the bounded rolling history in the reset row instead of a collection table. */
@Converter
public class PasswordResetTimesConverter implements AttributeConverter<List<Instant>, String> {
    @Override
    public String convertToDatabaseColumn(List<Instant> requests) {
        return requests.stream().map(Instant::toString).collect(Collectors.joining(","));
    }

    @Override
    public List<Instant> convertToEntityAttribute(String stored) {
        List<Instant> requests = new ArrayList<>();
        if (stored == null || stored.isEmpty()) {
            return requests;
        }
        for (String value : stored.split(",", -1)) {
            requests.add(Instant.parse(value));
        }
        return requests;
    }
}
