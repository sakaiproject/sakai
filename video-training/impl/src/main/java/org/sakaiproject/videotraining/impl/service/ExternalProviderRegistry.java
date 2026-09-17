/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.videotraining.impl.service;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.service.ExternalVideoProviderStrategy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ExternalProviderRegistry implements org.sakaiproject.videotraining.api.service.ExternalProviderRegistry, ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private Map<VideoProviderType, ExternalVideoProviderStrategy> providers = Collections.emptyMap();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        if (applicationContext == null) {
            providers = Collections.emptyMap();
            return;
        }

        Map<String, ExternalVideoProviderStrategy> discovered = applicationContext.getBeansOfType(ExternalVideoProviderStrategy.class);
        Map<VideoProviderType, ExternalVideoProviderStrategy> collected = new EnumMap<>(VideoProviderType.class);
        for (ExternalVideoProviderStrategy strategy : discovered.values()) {
            if (strategy != null && strategy.getProviderType() != null) {
                collected.put(strategy.getProviderType(), strategy);
            }
        }
        providers = Collections.unmodifiableMap(collected);
    }

    public ExternalVideoProviderStrategy getProvider(VideoProviderType type) {
        ExternalVideoProviderStrategy provider = providers.get(type);
        if (provider == null) {
            throw new IllegalStateException("No external video provider strategy is registered for " + type);
        }
        return provider;
    }

    public Collection<ExternalVideoProviderStrategy> getProviders() {
        return providers.values();
    }

    public Optional<ExternalVideoProviderStrategy> findProviderByUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return Optional.empty();
        }

        for (ExternalVideoProviderStrategy provider : providers.values()) {
            if (provider != null && StringUtils.isNotBlank(provider.extractVideoId(url))) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }
}