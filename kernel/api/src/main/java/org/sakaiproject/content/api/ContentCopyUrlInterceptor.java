package org.sakaiproject.content.api;

/**
 * Interceptor able to convert an URL on the fly when a site content is copied into another site.
 * <p>
 * When a content is copied from one site to another with the siteCopy function, it can be modified so the URL in it
 * point toward the second site (eg. a document with a link to site1/wiki should point toward site2/wiki).
 * </p>
 * <p>
 * Some URL may be not readable and changed on the fly, especially URL coming from an URL shortener (bit.ly).
 * Those URL need to be converted back to their original value before being processed.
 * </p>
 * <p>
 * This interceptor will be in charge for doing this conversion.
 * </p>
 *
 * @author Colin Hebert
 */
public interface ContentCopyUrlInterceptor {
    /**
     * Checks if the given URL is handled by the current interceptor
     *
     * @param url url to convert
     * @return true if the URL is handled, false otherwise
     */
    boolean isUrlHandled(String url);

    /**
     * Converts an URL to another format (usually expand a shortened URL)
     *
     * @param originalUrl Original URL to convert
     * @return converted URL
     */
    String convertUrl(String originalUrl);

    /**
     * Once the URL has been processed it can be converted back to its original form (usually a shortened URL)
     *
     * @param processedUrl processedUrl ready to be converted
     * @return Converted url
     */
    String convertProcessedUrl(String processedUrl);
}
