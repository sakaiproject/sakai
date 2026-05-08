/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.grading.impl.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.sakaiproject.plus.api.PlusService;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;

import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.Getter;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class GradingTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.grading")
    @Getter
    protected AdditionalHibernateMappings additionalHibernateMappings;

    @Bean(name = "org.sakaiproject.util.api.LocaleService")
    public LocaleService localeService() {
        LocaleService localeService = mock(LocaleService.class);
        when(localeService.parseDouble(any(), any(Locale.class))).thenAnswer(invocation -> {
            String origin = invocation.getArgument(0);
            Locale locale = invocation.getArgument(1);
            if (origin == null) return null;
            String trimmed = origin.trim();
            if (trimmed.isEmpty()) return null;
            try {
                return Double.valueOf(trimmed);
            } catch (NumberFormatException nfe) {
                try {
                    NumberFormat format = NumberFormat.getInstance(locale);
                    format.setGroupingUsed(true);
                    return format.parse(trimmed).doubleValue();
                } catch (ParseException pe) {
                    return null;
                }
            }
        });
        when(localeService.parseDouble(any(String.class))).thenAnswer(invocation -> {
            String origin = invocation.getArgument(0);
            if (origin == null) return null;
            String trimmed = origin.trim();
            if (trimmed.isEmpty()) return null;
            try {
                return Double.valueOf(trimmed);
            } catch (NumberFormatException nfe) {
                try {
                    return Double.valueOf(trimmed.replace(",", "."));
                } catch (NumberFormatException nfe2) {
                    return null;
                }
            }
        });
        return localeService;
    }

    @Bean(name = "org.sakaiproject.section.api.SectionAwareness")
    public SectionAwareness sectionAwareness() {
        return mock(SectionAwareness.class);
    }

    @Bean(name = "org.sakaiproject.plus.api.PlusService")
    public PlusService plusService() {
        return mock(PlusService.class);
    }
}
