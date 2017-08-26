package org.sakaiproject.content.api;

import java.util.List;

/**
 * Registry service for interceptors used during a site copy.
 *
 * @author Colin Hebert
 */
public interface ContentCopyInterceptorRegistry {
    /**
     * Add a new {@link ContentCopyUrlInterceptor}
     *
     * @param copyUrlInterceptor Interceptor to register
     */
    void registerUrlInterceptor(ContentCopyUrlInterceptor copyUrlInterceptor);

    /**
     * Find the first available {@link ContentCopyUrlInterceptor} for a given url
     *
     * @param url for which an interceptor is searched
     * @return an urlInterceptor for the given URL, null if there is no interceptor matching the url
     */
    ContentCopyUrlInterceptor getUrlInterceptor(String url);

    /**
     * Add a new {@link ContentCopyTextInterceptor}
     *
     * @param copyTextInterceptor Interceptor to register
     */
    void registerTextInterceptor(ContentCopyTextInterceptor copyTextInterceptor);

    /**
     * Get all the registeredTextInterceptor in the order in which they will be run
     *
     * @return Every textInterceptor registered
     */
    List<ContentCopyTextInterceptor> getTextInterceptors();
}
