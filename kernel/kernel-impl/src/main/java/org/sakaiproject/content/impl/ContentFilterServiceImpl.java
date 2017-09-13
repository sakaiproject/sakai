/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.content.impl;

import org.sakaiproject.content.api.ContentFilter;
import org.sakaiproject.content.api.ContentFilterService;
import org.sakaiproject.content.api.ContentResource;

import java.util.Collections;
import java.util.List;

/**
 * A simple implementation of the output filtering.
 */
public class ContentFilterServiceImpl implements ContentFilterService {

    protected List<ContentFilter> m_outputFilters = Collections.emptyList();

    public void setOutputFilters(List<ContentFilter> outputFilters)
    {
        this.m_outputFilters = outputFilters;
    }

    @Override
    public ContentResource wrap(ContentResource resource) {
        // Wrap up the resource if we need to.
        for (ContentFilter filter: m_outputFilters)
        {
            resource = filter.wrap(resource);
        }
        return resource;
    }
}
