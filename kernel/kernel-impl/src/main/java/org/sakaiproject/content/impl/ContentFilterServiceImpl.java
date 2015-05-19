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
