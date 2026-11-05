/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.util;

import java.io.IOException;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Timeout;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.scheduling.api.SchedulingService;

/** HTTP requests to user-supplied public destinations, without proxies. */
public final class PublicHttpClient implements AutoCloseable {

    // Allow cartridge downloads as well as calendar feeds, while bounding response bodies.
    private static final long MAX_RESPONSE_BYTES = 100L * 1024 * 1024;
    private static final long RESPONSE_DEADLINE_MILLIS = TimeUnit.SECONDS.toMillis(30);

    private static final Pattern MIME_TYPE = Pattern.compile("[a-zA-Z0-9!#$&^_.+-]+/[a-zA-Z0-9!#$&^_.+-]+");

    private static final DnsResolver PUBLIC_DNS = new DnsResolver() {
        /** Resolves the host and rejects it if any address is non-public. */
        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (!isPublicAddress(address)) {
                    throw new UnknownHostException("Web Push destination has a non-public address");
                }
            }
            return addresses;
        }

        /** Preserves the original hostname without a reverse DNS lookup. */
        @Override
        public String resolveCanonicalHostname(String host) {
            return host;
        }
    };

    private final CloseableHttpClient client = create();

    /**
     * Parses an absolute HTTP(S) URL without credentials or a fragment.
     *
     * @param url destination URL
     * @return the parsed URL; address checks occur separately during connection
     * @throws IllegalArgumentException if the URL is missing or violates these constraints
     */
    public static URI parseUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("Missing HTTP URL");
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid HTTP URL");
        }
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null
                || uri.getPort() == 0 || uri.getPort() > 65535) {
            throw new IllegalArgumentException("Invalid HTTP URL");
        }
        return uri;
    }

    /** A preflight address check. The connection manager enforces the same policy again when sending. */
    public static void requirePublicAddress(URI uri) throws UnknownHostException {
        PUBLIC_DNS.resolve(uri.getHost());
    }

    /** Creates a client with public-address enforcement, timeouts, and no redirects or retries. */
    private static CloseableHttpClient create() {
        return create(false);
    }

    /** Creates a GET/HEAD client with bounded redirects and public-address checks at every hop. */
    private static CloseableHttpClient create(boolean followRedirects) {
        Timeout timeout = Timeout.ofSeconds(10);
        HttpClientBuilder builder = HttpClients.custom()
            // The connection uses the checked addresses while TLS verifies the original hostname.
            .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                .setDnsResolver(PUBLIC_DNS)
                .setDefaultConnectionConfig(ConnectionConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build())
                .build())
            .setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(timeout).setResponseTimeout(timeout)
                .setMaxRedirects(5).build())
            .setRedirectStrategy(new PublicRedirectStrategy())
            .disableAutomaticRetries()
            .disableCookieManagement();
        if (!followRedirects) {
            builder.disableRedirectHandling();
        }
        return builder.build();
    }

    /** Validates resolved redirect URLs; the connection manager pins each new connection to checked addresses. */
    static final class PublicRedirectStrategy extends DefaultRedirectStrategy {
        /** Resolves relative locations and rejects unsafe destinations before following a redirect. */
        @Override
        public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException {
            URI uri = super.getLocationURI(request, response, context);
            try {
                URI target = parseUrl(uri.toString());
                // Precheck every hop; PUBLIC_DNS checks again and pins addresses when connecting.
                requirePublicAddress(target);
                return target;
            } catch (IllegalArgumentException | UnknownHostException e) {
                throw new ProtocolException("Redirect destination must be a public HTTP(S) URL", e);
            }
        }
    }

    /**
     * Posts a body and headers to a public HTTP(S) destination without following redirects.
     *
     * @return the HTTP response status
     * @throws IOException if the destination cannot be reached or the request fails
     */
    public int post(URI uri, Map<String, String> headers, byte[] body) throws IOException {
        URI destination = parseUrl(uri.toString());
        requirePublicAddress(destination);
        HttpPost request = new HttpPost(destination);
        headers.forEach(request::setHeader);
        if (body != null) {
            request.setEntity(new ByteArrayEntity(body, null));
        }
        return requestStatus(request, client, ComponentManager.get(SchedulingService.class), RESPONSE_DEADLINE_MILLIS);
    }

    /** Returns only the status and cancels before response closure to avoid consuming an untrusted body. */
    static int requestStatus(HttpUriRequestBase request, CloseableHttpClient client,
            SchedulingService scheduler, long timeoutMillis) throws IOException {
        try (RequestDeadline deadline = new RequestDeadline(scheduler, timeoutMillis, request)) {
            return executeStatus(request, client);
        }
    }

    /** Reads the status and immediately releases the response without draining its body. */
    private static int executeStatus(HttpUriRequestBase request, CloseableHttpClient client) throws IOException {
        CloseableHttpResponse response = CloseableHttpResponse.adapt(client.executeOpen(null, request, null));
        try {
            return response.getCode();
        } finally {
            request.cancel();
            response.close(CloseMode.IMMEDIATE);
        }
    }

    /** Closes the client and releases its connection resources. */
    @Override
    public void close() throws IOException {
        client.close();
    }

    /**
     * Probes a public URL with HEAD, falling back to GET headers, and returns its MIME type without parameters.
     *
     * @return empty for non-2xx responses or a missing or invalid Content-Type
     * @throws IOException if the destination cannot be reached or the request fails
     */
    public static Optional<String> contentType(String url) throws IOException {
        URI uri = parseUrl(url);
        requirePublicAddress(uri);
        try (CloseableHttpClient client = create(true)) {
            return detectContentType(uri, client, ComponentManager.get(SchedulingService.class), RESPONSE_DEADLINE_MILLIS);
        }
    }

    /** Falls back to GET headers when HEAD supplies no successful MIME type. */
    static Optional<String> detectContentType(URI uri, CloseableHttpClient client,
            SchedulingService scheduler, long timeoutMillis) throws IOException {
        HttpHead head = new HttpHead(uri);
        HttpGet get = new HttpGet(uri);
        try (RequestDeadline deadline = new RequestDeadline(scheduler, timeoutMillis, head, get)) {
            Optional<String> mimeType = requestContentType(head, client);
            return mimeType.isPresent() ? mimeType : requestContentType(get, client);
        }
    }

    /** Reads only response headers and aborts the request before closing so GET bodies are not downloaded. */
    private static Optional<String> requestContentType(HttpUriRequestBase request, CloseableHttpClient client) throws IOException {
        CloseableHttpResponse response = CloseableHttpResponse.adapt(client.executeOpen(null, request, null));
        try {
            return responseContentType(response);
        } finally {
            request.cancel();
            response.close(CloseMode.IMMEDIATE);
        }
    }

    /** Extracts a valid MIME type only from a successful response. */
    static Optional<String> responseContentType(ClassicHttpResponse response) {
        if (response.getCode() < 200 || response.getCode() >= 300) {
            return Optional.empty();
        }
        return Optional.ofNullable(response.getFirstHeader(HttpHeaders.CONTENT_TYPE))
            .map(Header::getValue)
            .map(value -> value.split(";", 2)[0].trim())
            .filter(value -> MIME_TYPE.matcher(value).matches());
    }

    /**
     * Returns a HEAD response header regardless of the HTTP response status.
     *
     * @param userAgent optional User-Agent header, or null to use the client default
     * @return empty if the requested header is absent or has no value
     * @throws IOException if the destination cannot be reached or the request fails
     */
    public static Optional<String> headHeader(String url, String userAgent, String headerName) throws IOException {
        URI uri = parseUrl(url);
        requirePublicAddress(uri);
        HttpHead request = new HttpHead(uri);
        setUserAgent(request, userAgent);
        try (CloseableHttpClient client = create(true)) {
            return requestHeader(request, client, headerName,
                    ComponentManager.get(SchedulingService.class), RESPONSE_DEADLINE_MILLIS);
        }
    }

    /** Bounds header execution and keeps cancellation armed until immediate response cleanup finishes. */
    static Optional<String> requestHeader(HttpUriRequestBase request, CloseableHttpClient client,
            String headerName, SchedulingService scheduler, long timeoutMillis) throws IOException {
        try (RequestDeadline deadline = new RequestDeadline(scheduler, timeoutMillis, request)) {
            CloseableHttpResponse response = CloseableHttpResponse.adapt(client.executeOpen(null, request, null));
            try {
                Header header = response.getFirstHeader(headerName);
                return header == null ? Optional.empty() : Optional.ofNullable(header.getValue());
            } finally {
                request.cancel();
                response.close(CloseMode.IMMEDIATE);
            }
        }
    }

    /** Consumes a response body before its owning response is closed. */
    @FunctionalInterface
    public interface BodyReader<T, E extends Exception> {
        /** Reads the body, propagating I/O errors and caller-specific exceptions. */
        T read(InputStream stream) throws IOException, E;
    }

    /**
     * Reads a successful public HTTP(S) response through a stream limited to 100 MiB and a 30-second request deadline.
     *
     * @return the reader's result
     * @throws IOException for an unsuccessful or missing response body, I/O failure,
     *         or an attempted read beyond the response size limit
     * @throws E if the reader raises a caller-specific exception
     */
    public static <T, E extends Exception> T read(String url, String userAgent, BodyReader<T, E> reader) throws IOException, E {
        URI uri = parseUrl(url);
        requirePublicAddress(uri);
        HttpGet request = new HttpGet(uri);
        setUserAgent(request, userAgent);
        try (CloseableHttpClient client = create(true)) {
            return readWithDeadline(client, request, reader,
                    ComponentManager.get(SchedulingService.class), RESPONSE_DEADLINE_MILLIS);
        }
    }

    /** Aborts blocked requests, body reads, and skips at the deadline, retaining the timer through cleanup. */
    static <T, E extends Exception> T readWithDeadline(CloseableHttpClient client, HttpUriRequestBase request,
            BodyReader<T, E> reader, SchedulingService schedulingService, long timeoutMillis) throws IOException, E {
        try (RequestDeadline deadline = new RequestDeadline(schedulingService, timeoutMillis, request)) {
            CloseableHttpResponse response = CloseableHttpResponse.adapt(client.executeOpen(null, request, null));
            try {
                return readResponse(response, request, reader);
            } finally {
                request.cancel();
                response.close(CloseMode.IMMEDIATE);
            }
        }
    }

    /** One operation deadline cancels every request, including a MIME probe's fallback GET. */
    private static final class RequestDeadline implements AutoCloseable {
        private final HttpUriRequestBase[] requests;
        private final ScheduledFuture<?> task;

        /** Arms cancellation before any request waits for headers or follows redirects. */
        private RequestDeadline(SchedulingService scheduler, long timeoutMillis, HttpUriRequestBase... requests) {
            this.requests = requests;
            task = scheduler.schedule(this::cancelRequests, timeoutMillis, TimeUnit.MILLISECONDS);
        }

        /** Cancels both active and not-yet-started requests when their shared budget expires. */
        private void cancelRequests() {
            for (HttpUriRequestBase request : requests) {
                request.cancel();
            }
        }

        /** Removes the timer only after response cleanup and cancellation of all operation requests. */
        @Override
        public void close() {
            try {
                cancelRequests();
            } finally {
                task.cancel(false);
            }
        }
    }

    /** Supplies a bounded GET body and cancels before stream cleanup can drain unread content. */
    static <T, E extends Exception> T readResponse(ClassicHttpResponse response, HttpUriRequestBase request,
            BodyReader<T, E> reader) throws IOException, E {
        if (response.getCode() < 200 || response.getCode() >= 300 || response.getEntity() == null) {
            throw new IOException("HTTP request failed with status " + response.getCode());
        }
        try (InputStream content = new FilterInputStream(response.getEntity().getContent()) {
                /** Readers may close the supplied stream themselves, including while unwinding a size-limit failure. */
                @Override
                public void close() throws IOException {
                    request.cancel();
                    super.close();
                }
            };
                InputStream stream = BoundedInputStream.builder()
                    .setInputStream(content)
                    .setMaxCount(MAX_RESPONSE_BYTES)
                    .setOnMaxCount((max, count) -> {
                        // Distinguish an exact-size body from a truncated oversized response.
                        if (content.read() != -1) {
                            throw new IOException("HTTP response exceeds " + MAX_RESPONSE_BYTES + " bytes");
                        }
                    })
                    .get()) {
            return reader.read(stream);
        }
    }

    /** Sets the optional User-Agent header when supplied by the caller. */
    private static void setUserAgent(HttpUriRequestBase request, String userAgent) {
        if (userAgent != null) {
            request.setHeader(HttpHeaders.USER_AGENT, userAgent);
        }
    }

    /** Rejects local, private, reserved, and other non-public destination addresses. */
    private static boolean isPublicAddress(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        byte[] bytes = address.getAddress();
        int first = bytes[0] & 0xff;
        int second = bytes[1] & 0xff;
        int third = bytes[2] & 0xff;
        if (address instanceof Inet4Address) {
            // Exclude shared, reserved, documentation and other special-purpose ranges.
            return switch (first) {
                case 0 -> false;
                case 100 -> second < 64 || second > 127;
                // Azure exposes this public-format virtual address only inside its network.
                case 168 -> !"168.63.129.16".equals(address.getHostAddress());
                case 192 -> !(second == 0 && (third == 0 || third == 2) || second == 88 && third == 99);
                case 198 -> !(second == 18 || second == 19 || second == 51 && third == 100);
                case 203 -> !(second == 0 && third == 113);
                default -> first < 224;
            };
        }

        int prefix = (first << 8) | second;
        // Allow global unicast, excluding special-purpose, documentation and 6to4 ranges.
        return (prefix & 0xe000) == 0x2000
            && !(prefix == 0x2001 && (third < 2 || (third == 0x0d && bytes[3] == (byte) 0xb8)))
            && prefix != 0x2002
            && !(prefix == 0x3fff && third < 16);
    }

}
