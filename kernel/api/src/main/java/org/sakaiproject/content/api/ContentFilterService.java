package org.sakaiproject.content.api;

/**
 * This is a service that holds all the ContentFilters and allows streams to be filtered.
 * It's exposed as a service so that tools serving up thier own content can do it in the
 * same way as the kernel.
 * @see ContentFilter
 */
public interface ContentFilterService {

    /**
     * This applies all the filters defined to the supplied content resource.
     * @param resource The content resource to wrap cannot be <code>null</code>.
     * @return A new content resource wrapped up or if the original resource if no filters apply.
     */
    ContentResource wrap(ContentResource resource);

}
