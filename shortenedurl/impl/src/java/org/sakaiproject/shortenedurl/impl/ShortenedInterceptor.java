package org.sakaiproject.shortenedurl.impl;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCopyInterceptorRegistry;
import org.sakaiproject.content.api.ContentCopyTextInterceptor;
import org.sakaiproject.content.api.ContentCopyUrlInterceptor;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UrlInterceptor in charge of changing shortened url back into the original url
 *
 * @author Colin Hebert
 */
public class ShortenedInterceptor implements ContentCopyUrlInterceptor, ContentCopyTextInterceptor {
    private static Pattern PATH_PATTERN = Pattern.compile("/+x/+(.+)");
    private Pattern shortenedUrlPattern;
    private ShortenedUrlService shortenedUrlService;
    private ServerConfigurationService scs;
    private Collection<String> serverNames;
    private ContentCopyInterceptorRegistry registry;

    public void init() {
        registry.registerUrlInterceptor(this);
        registry.registerTextInterceptor(this);

        StringBuilder servers = new StringBuilder();
        for (Iterator<String> iterator = getServerNames().iterator(); iterator.hasNext(); ) {
            servers.append(Pattern.quote(iterator.next()));
            if (iterator.hasNext())
                servers.append('|');
        }
        shortenedUrlPattern = Pattern.compile("(https?://)?(?:" + servers + ")(?::\\d+)?/+x/+\\w+", Pattern.CASE_INSENSITIVE);
    }

    public boolean isUrlHandled(String url) {
		// generally the stored URLs are not encoded correctly, manually encode spaces as a quick fix
        URI uri = URI.create(url.replace(" ", "%20"));
        return (isLocalUri(uri) && PATH_PATTERN.matcher(uri.getPath()).matches());
    }

    private boolean isLocalUri(URI uri) {
        return ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) &&
                (uri.getHost() == null || getServerNames().contains(uri.getHost()));
    }

    public String convertUrl(String originalUrl) {
        URI uri = URI.create(originalUrl);
        Matcher pathMatcher = PATH_PATTERN.matcher(uri.getPath());
        if (pathMatcher.matches()) {
            return shortenedUrlService.resolve(pathMatcher.group(1));
        } else {
            throw new RuntimeException("Couldn't find the ShortenedUrl key in '" + originalUrl + "'");
        }
    }

    public String convertProcessedUrl(String processedUrl) {
        return shortenedUrlService.shorten(processedUrl);
    }

    public void setShortenedUrlService(ShortenedUrlService shortenedUrlService) {
        this.shortenedUrlService = shortenedUrlService;
    }

    public void setScs(ServerConfigurationService scs) {
        this.scs = scs;
    }

    private Collection<String> getServerNames() {
        if (serverNames != null)
            return serverNames;

        serverNames = new ArrayList<String>(scs.getServerNameAliases());
        serverNames.add(scs.getServerName());
        return serverNames;
    }

    public String runTextInterceptor(String interceptedText, Map<String, String> replacements) {
        Matcher matcher = shortenedUrlPattern.matcher(interceptedText);
        StringBuffer sb = new StringBuffer(interceptedText.length());

        while (matcher.find()) {
            String matchedUrl = matcher.group(0);
            if(matcher.group(1) == null)
                matchedUrl = "http://"+matchedUrl;
            matcher.appendReplacement(sb, updateTextShortenedUrl(matchedUrl, replacements));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String updateTextShortenedUrl(String shortenedUrl, Map<String, String> replacements){
        String newUrl = convertUrl(shortenedUrl);

        for(Map.Entry<String, String> replacement: replacements.entrySet()){
            newUrl = newUrl.replace(replacement.getKey(), replacement.getValue());
        }

        return convertProcessedUrl(newUrl);
    }

    public void setRegistry(ContentCopyInterceptorRegistry registry) {
        this.registry = registry;
    }
}
