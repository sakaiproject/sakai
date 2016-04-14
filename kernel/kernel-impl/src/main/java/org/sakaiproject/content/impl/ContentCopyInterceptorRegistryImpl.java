package org.sakaiproject.content.impl;

import org.sakaiproject.content.api.ContentCopyInterceptorRegistry;
import org.sakaiproject.content.api.ContentCopyTextInterceptor;
import org.sakaiproject.content.api.ContentCopyUrlInterceptor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Colin Hebert
 */
public class ContentCopyInterceptorRegistryImpl implements ContentCopyInterceptorRegistry {
    private final Collection<ContentCopyUrlInterceptor> urlInterceptors = new LinkedList<ContentCopyUrlInterceptor>();
    private final List<ContentCopyTextInterceptor> textInterceptors = new LinkedList<ContentCopyTextInterceptor>();

    public void registerUrlInterceptor(ContentCopyUrlInterceptor copyUrlInterceptor) {
        urlInterceptors.add(copyUrlInterceptor);
    }

    public ContentCopyUrlInterceptor getUrlInterceptor(String url) {
        for(ContentCopyUrlInterceptor urlInterceptor : urlInterceptors){
            if(urlInterceptor.isUrlHandled(url))
                return urlInterceptor;
        }

        return null;
    }

    public void registerTextInterceptor(ContentCopyTextInterceptor copyTextInterceptor) {
        textInterceptors.add(copyTextInterceptor);
    }

    public List<ContentCopyTextInterceptor> getTextInterceptors() {
        return textInterceptors;
    }
}
