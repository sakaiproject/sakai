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

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.videotraining.api.model.MetadataFetchResult;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoPublicationStatus;
import org.sakaiproject.videotraining.api.model.VideoVisibilityScope;
import org.sakaiproject.videotraining.api.service.ExternalMetadataService;
import org.sakaiproject.videotraining.api.service.ExternalVideoProviderStrategy;

public class ExternalMetadataServiceImpl implements ExternalMetadataService {

    private final ExternalProviderRegistry providerRegistry;

    public ExternalMetadataServiceImpl(ExternalProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    @Override
    public MetadataFetchResult fetch(String url) {
        ExternalVideoProviderStrategy provider = resolveProvider(url);
        if (provider == null) {
            return new MetadataFetchResult("", "");
        }
        return provider.fetchMetadata(url);
    }

    @Override
    public String retrieveVideoProvider(String url) {
        ExternalVideoProviderStrategy provider = resolveProvider(url);
        if (provider == null) {
            return "Unsupported";
        }
        return formatProviderName(provider.getProviderType());
    }

    @Override
    public String extractVideoId(String url) {
        ExternalVideoProviderStrategy provider = resolveProvider(url);
        if (provider == null) {
            return null;
        }
        return provider.extractVideoId(url);
    }

    @Override
    public boolean syncPrivacy(String url, VideoPublicationStatus publicationStatus, VideoVisibilityScope visibilityScope) {
        ExternalVideoProviderStrategy provider = resolveProvider(url);
        if (provider == null) {
            return false;
        }
        return provider.syncPrivacy(url, publicationStatus, visibilityScope);
    }

    private ExternalVideoProviderStrategy resolveProvider(String url) {
        if (StringUtils.isBlank(url) || providerRegistry == null) {
            return null;
        }
        return providerRegistry.findProviderByUrl(url).orElse(null);
    }

    private String formatProviderName(VideoProviderType providerType) {
        if (providerType == null) {
            return "Unsupported";
        }

        String normalized = providerType.name().replace("_UPLOAD", "").toLowerCase();
        String[] parts = normalized.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (StringUtils.isBlank(part)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            String lower = part.toLowerCase();
            if ("youtube".equals(lower)) {
                builder.append("YouTube");
            } else {
                builder.append(StringUtils.capitalize(lower));
            }
        }
        String result = builder.toString();
        return StringUtils.defaultIfBlank(result, "Unsupported");
    }
}