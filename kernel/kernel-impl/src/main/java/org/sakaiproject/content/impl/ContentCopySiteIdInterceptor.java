package org.sakaiproject.content.impl;

import org.sakaiproject.content.api.ContentCopyInterceptorRegistry;
import org.sakaiproject.content.api.ContentCopyTextInterceptor;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Colin Hebert
 */
public class ContentCopySiteIdInterceptor implements ContentCopyTextInterceptor {
    private ContentCopyInterceptorRegistry interceptorRegistry;

    public void init(){
        interceptorRegistry.registerTextInterceptor(this);
    }

    public String runTextInterceptor(String interceptedText, Map<String, String> replacements) {
        for(Map.Entry<String, String> replacement : replacements.entrySet()){
            interceptedText = interceptedText.replaceAll(Pattern.quote(replacement.getKey()), replacement.getValue());
        }
        return interceptedText;
    }

    public void setInterceptorRegistry(ContentCopyInterceptorRegistry interceptorRegistry) {
        this.interceptorRegistry = interceptorRegistry;
    }
}
