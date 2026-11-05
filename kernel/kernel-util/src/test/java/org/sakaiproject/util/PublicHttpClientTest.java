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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.NullInputStream;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.Test;
import org.sakaiproject.scheduling.api.SchedulingService;

public class PublicHttpClientTest {

    /** Servers that reject HEAD can supply a MIME type through GET headers without downloading a body. */
    @Test
    public void fallsBackToGetHeadersWhenHeadIsRejected() throws Exception {
        try (TestSchedulingService scheduler = new TestSchedulingService();
                ResponseFixtureClient client = new ResponseFixtureClient(405, "text/html")) {
            assertEquals(Optional.of("image/png"), PublicHttpClient.detectContentType(
                URI.create("https://1.1.1.1/image.png"), client, scheduler, 30000));
            assertEquals(List.of("HEAD", "GET"), client.methods);
            assertTrue(client.requests.stream().allMatch(HttpUriRequestBase::isCancelled));
        }
    }

    /** A successful HEAD response needs no GET probe. */
    @Test
    public void avoidsGetWhenHeadSuppliesMimeType() throws Exception {
        try (TestSchedulingService scheduler = new TestSchedulingService();
                ResponseFixtureClient client = new ResponseFixtureClient(200, "video/mp4; charset=UTF-8")) {
            assertEquals(Optional.of("video/mp4"), PublicHttpClient.detectContentType(
                URI.create("https://1.1.1.1/video.mp4"), client, scheduler, 30000));
            assertEquals(List.of("HEAD"), client.methods);
        }
    }

    /** POST handling returns the status without consuming the response body and releases the response. */
    @Test
    public void returnsPostStatusWithoutReadingBody() throws Exception {
        try (TestSchedulingService scheduler = new TestSchedulingService();
                ResponseFixtureClient client = new ResponseFixtureClient(200, "text/plain")) {
            HttpPost request = new HttpPost("https://1.1.1.1/push");
            assertEquals(202, PublicHttpClient.requestStatus(request, client, scheduler, 30000));
            assertTrue(request.isCancelled());
            assertTrue(client.responseClosed);
        }
    }

    /** Failure while reading the status still cancels the request and closes its response. */
    @Test
    public void cleansUpWhenReadingPostStatusFails() throws Exception {
        try (TestSchedulingService scheduler = new TestSchedulingService();
                ResponseFixtureClient client = new ResponseFixtureClient(200, "text/plain")) {
            client.failStatus = true;
            HttpPost request = new HttpPost("https://1.1.1.1/push");
            assertThrows(IllegalStateException.class, () -> PublicHttpClient.requestStatus(request, client, scheduler, 30000));
            assertTrue(request.isCancelled());
            assertTrue(client.responseClosed);
        }
    }

    /** HEAD header lookup remains status-agnostic and does not consume a response body. */
    @Test
    public void preservesHeaderOnErrorResponse() throws Exception {
        try (TestSchedulingService scheduler = new TestSchedulingService();
                ResponseFixtureClient client = new ResponseFixtureClient(403, "text/plain")) {
            HttpHead request = new HttpHead("https://1.1.1.1/fixture");
            assertEquals(Optional.of("text/plain"), PublicHttpClient.requestHeader(
                    request, client, "Content-Type", scheduler, 30000));
            assertTrue(request.isCancelled());
            assertTrue(client.responseClosed);
            assertTrue(scheduler.lastTask.isCancelled());
        }
    }

    /** Supplies HTTP response fixtures at the transport boundary while exercising real MIME detection. */
    private static final class ResponseFixtureClient extends CloseableHttpClient {
        private final int headStatus;
        private final String headType;
        private boolean failStatus;
        private boolean responseClosed;
        private BasicClassicHttpResponse fixedResponse;
        private CountDownLatch waitingForHeaders;
        private boolean blockGetOnly;
        private final List<String> methods = new ArrayList<>();
        private final List<HttpUriRequestBase> requests = new ArrayList<>();

        /** Configures the HEAD response; the GET response advertises a PNG image. */
        private ResponseFixtureClient(int headStatus, String headType) {
            this.headStatus = headStatus;
            this.headType = headType;
        }

        /** Returns real response objects whose body fails the test if it is consumed. */
        @Override
        protected CloseableHttpResponse doExecute(HttpHost target, ClassicHttpRequest request, HttpContext context) throws IOException {
            methods.add(request.getMethod());
            requests.add((HttpUriRequestBase) request);
            if (waitingForHeaders != null && (!blockGetOnly || "GET".equals(request.getMethod()))) {
                ((HttpUriRequestBase) request).setDependency(() -> {
                    waitingForHeaders.countDown();
                    return true;
                });
                try {
                    assertTrue("Deadline must abort the blocked request", waitingForHeaders.await(3, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException(e);
                }
                throw new IOException("Request cancelled before headers");
            }
            if (fixedResponse != null) {
                return CloseableHttpResponse.adapt(fixedResponse);
            }
            boolean head = "HEAD".equals(request.getMethod());
            BasicClassicHttpResponse response = new BasicClassicHttpResponse(head ? headStatus : "POST".equals(request.getMethod()) ? 202 : 200) {
                /** Simulates a response failure at the external HTTP boundary. */
                @Override
                public int getCode() {
                    if (failStatus) {
                        throw new IllegalStateException("Status is unavailable");
                    }
                    return super.getCode();
                }

                /** Records response closure while retaining the real entity cleanup behavior. */
                @Override
                public void close() throws IOException {
                    responseClosed = true;
                    super.close();
                }
            };
            response.setHeader("Content-Type", head ? headType : "image/png");
            response.setEntity(new InputStreamEntity(new InputStream() {
                /** MIME detection must stop at the headers. */
                @Override
                public int read() {
                    throw new AssertionError("MIME detection must not read the response body");
                }
            }, ContentType.APPLICATION_OCTET_STREAM));
            return CloseableHttpResponse.adapt(response);
        }

        /** No network resources are opened by this fixture. */
        @Override
        public void close() {}

        /** No network resources are opened by this fixture. */
        @Override
        public void close(CloseMode mode) {}
    }

    /** Early returns and reader failures cancel before the underlying HTTP body stream is closed. */
    @Test
    public void cancelsGetBeforeClosingUnreadBody() throws Exception {
        for (String mode : List.of("return", "throw", "close")) {
            HttpGet request = new HttpGet("https://1.1.1.1/fixture");
            try (BasicClassicHttpResponse response = new BasicClassicHttpResponse(200)) {
                response.setEntity(new InputStreamEntity(new ByteArrayInputStream(new byte[] {1, 2, 3}) {
                    /** Closing an HTTP body must happen only after cancellation prevents draining. */
                    @Override
                    public void close() throws IOException {
                        assertTrue("Request must be cancelled before body cleanup", request.isCancelled());
                        super.close();
                    }
                }, ContentType.APPLICATION_OCTET_STREAM));
                if ("throw".equals(mode)) {
                    assertThrows(IOException.class, () -> PublicHttpClient.readResponse(response, request, stream -> {
                        throw new IOException("Reader failed");
                    }));
                } else if ("close".equals(mode)) {
                    assertEquals(Integer.valueOf(1), PublicHttpClient.readResponse(response, request, stream -> {
                        int value = stream.read();
                        stream.close();
                        return value;
                    }));
                } else {
                    assertEquals(Integer.valueOf(1), PublicHttpClient.readResponse(response, request, InputStream::read));
                }
            }
        }
    }

    /** A deadline cancels the live request even while its body reader is blocked in read or skip. */
    @Test(timeout = 8000)
    public void deadlineAbortsBlockedReadsAndSkips() throws Exception {
        for (boolean skip : new boolean[] {false, true}) {
            HttpGet request = new HttpGet("https://1.1.1.1/fixture");
            CountDownLatch cancelled = new CountDownLatch(1);
            request.setDependency(() -> {
                cancelled.countDown();
                return true;
            });
            BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
            response.setEntity(new InputStreamEntity(new InputStream() {
                /** Models the HTTP transport unblocking when cancellation closes its socket. */
                @Override
                public int read() throws IOException {
                    try {
                        assertTrue("Deadline must abort the blocked transfer", cancelled.await(3, TimeUnit.SECONDS));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException(e);
                    }
                    throw new IOException("Request cancelled");
                }
            }, ContentType.APPLICATION_OCTET_STREAM));
            try (TestSchedulingService scheduler = new TestSchedulingService();
                    ResponseFixtureClient client = new ResponseFixtureClient(200, "text/plain")) {
                client.fixedResponse = response;
                assertThrows(IOException.class, () -> PublicHttpClient.readWithDeadline(
                    client, request, stream -> {
                        if (skip) {
                            stream.skip(1);
                        } else {
                            stream.read();
                        }
                        return null;
                    }, scheduler, 50));
                assertTrue(request.isCancelled());
            }
        }
    }

    /** The deadline is armed before execution so a server cannot stall indefinitely before sending headers. */
    @Test(timeout = 5000)
    public void deadlineAbortsBlockedResponseHeaders() throws Exception {
        HttpGet request = new HttpGet("https://1.1.1.1/fixture");
        CountDownLatch cancelled = new CountDownLatch(1);
        request.setDependency(() -> {
            cancelled.countDown();
            return true;
        });
        try (TestSchedulingService scheduler = new TestSchedulingService();
                ResponseFixtureClient client = new ResponseFixtureClient(200, "text/plain")) {
            client.waitingForHeaders = cancelled;
            assertThrows(IOException.class, () -> PublicHttpClient.readWithDeadline(
                client, request, InputStream::read, scheduler, 50));
            assertTrue(request.isCancelled());
        }
    }

    /** Status, HEAD headers, and both MIME probes abort blocked headers under the operation deadline. */
    @Test(timeout = 8000)
    public void deadlinesAbortStatusHeaderAndMimeProbes() throws Exception {
        for (String mode : List.of("status", "header", "mime-head", "mime-get")) {
            try (TestSchedulingService scheduler = new TestSchedulingService();
                    ResponseFixtureClient client = new ResponseFixtureClient(405, "text/plain")) {
                client.waitingForHeaders = new CountDownLatch(1);
                client.blockGetOnly = "mime-get".equals(mode);
                HttpUriRequestBase request = "header".equals(mode)
                        ? new HttpHead("https://1.1.1.1/fixture") : new HttpPost("https://1.1.1.1/fixture");
                if ("status".equals(mode)) {
                    assertThrows(IOException.class, () -> PublicHttpClient.requestStatus(request, client, scheduler, 100));
                } else if ("header".equals(mode)) {
                    assertThrows(IOException.class, () -> PublicHttpClient.requestHeader(request, client,
                            "Content-Type", scheduler, 100));
                } else {
                    assertThrows(IOException.class, () -> PublicHttpClient.detectContentType(
                            URI.create("https://1.1.1.1/fixture"), client, scheduler, 100));
                }
                assertTrue(client.requests.stream().allMatch(HttpUriRequestBase::isCancelled));
                assertEquals("One deadline covers the entire operation", 1, scheduler.taskCount);
                if ("mime-get".equals(mode)) {
                    assertEquals(List.of("HEAD", "GET"), client.methods);
                }
            }
        }
    }

    /** Completed transfers remove their deadline task instead of leaving it queued in the shared scheduler. */
    @Test
    public void successfulReadCancelsDeadlineTask() throws Exception {
        HttpGet request = new HttpGet("https://1.1.1.1/fixture");
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
        response.setEntity(new InputStreamEntity(new ByteArrayInputStream(new byte[] {1}), ContentType.APPLICATION_OCTET_STREAM));
        try (TestSchedulingService scheduler = new TestSchedulingService();
                ResponseFixtureClient client = new ResponseFixtureClient(200, "text/plain")) {
            client.fixedResponse = response;
            assertEquals(Integer.valueOf(1), PublicHttpClient.readWithDeadline(
                client, request, InputStream::read, scheduler, 30000));
            assertTrue(scheduler.lastTask.isCancelled());
        }
    }

    /** Uses a real JDK scheduler at the Kernel scheduling boundary without starting a Sakai component container. */
    private static final class TestSchedulingService implements SchedulingService, AutoCloseable {
        private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        private ScheduledFuture<?> lastTask;
        private int taskCount;

        /** Schedules the production cancellation callback on the real executor. */
        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            taskCount++;
            lastTask = executor.schedule(command, delay, unit);
            return lastTask;
        }

        /** Delegates the remaining facade operation to the same real scheduler. */
        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return executor.scheduleAtFixedRate(command, initialDelay, delay, unit);
        }

        /** Delegates the remaining facade operation to the same real scheduler. */
        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }

        /** Stops the test executor and removes any remaining delayed tasks. */
        @Override
        public void close() {
            executor.shutdownNow();
        }
    }

    /** Redirect locations use the same URL and address restrictions as initial requests. */
    @Test
    public void rejectsUnsafeRedirectDestinations() throws Exception {
        PublicHttpClient.PublicRedirectStrategy strategy = new PublicHttpClient.PublicRedirectStrategy();
        HttpGet request = new HttpGet("https://1.1.1.1/start");
        for (String location : new String[] {"http://127.0.0.1/image.png", "http://169.254.169.254/",
                "http://10.0.0.1/", "http://[fd00::1]/", "file:///tmp/private", "https://user@1.1.1.1/"}) {
            try (BasicClassicHttpResponse response = new BasicClassicHttpResponse(302)) {
                response.setHeader("Location", location);
                assertThrows(location, ProtocolException.class,
                    () -> strategy.getLocationURI(request, response, new BasicHttpContext()));
            }
        }
    }

    /** Relative and cross-host public redirects retain their resolved destinations. */
    @Test
    public void resolvesPublicRedirectDestinations() throws Exception {
        PublicHttpClient.PublicRedirectStrategy strategy = new PublicHttpClient.PublicRedirectStrategy();
        HttpGet request = new HttpGet("https://1.1.1.1/start");
        try (BasicClassicHttpResponse response = new BasicClassicHttpResponse(302)) {
            response.setHeader("Location", "/image.png");
            assertEquals("https://1.1.1.1/image.png", strategy.getLocationURI(request, response, new BasicHttpContext()).toString());
            response.setHeader("Location", "https://8.8.8.8/image.png");
            assertEquals("https://8.8.8.8/image.png", strategy.getLocationURI(request, response, new BasicHttpContext()).toString());
        }
    }

    /** Error and redirect response headers must not determine a link's MIME type. */
    @Test
    public void ignoresContentTypeFromUnsuccessfulResponses() throws Exception {
        for (int status : new int[] {199, 300, 302, 404, 500}) {
            try (BasicClassicHttpResponse response = new BasicClassicHttpResponse(status)) {
                response.setHeader("Content-Type", "text/html; charset=UTF-8");
                assertEquals(Optional.empty(), PublicHttpClient.responseContentType(response));
            }
        }
    }

    /** Successful responses retain MIME normalization and rejection of missing or invalid values. */
    @Test
    public void parsesContentTypeFromSuccessfulResponses() throws Exception {
        for (int status : new int[] {200, 204, 299}) {
            try (BasicClassicHttpResponse response = new BasicClassicHttpResponse(status)) {
                assertEquals(Optional.empty(), PublicHttpClient.responseContentType(response));
                response.setHeader("Content-Type", " text/html ; charset=UTF-8");
                assertEquals(Optional.of("text/html"), PublicHttpClient.responseContentType(response));
                response.setHeader("Content-Type", "invalid mime type");
                assertEquals(Optional.empty(), PublicHttpClient.responseContentType(response));
            }
        }
    }

    /** The bounded stream preserves body bytes and returns the reader result unchanged. */
    @Test
    public void preservesResponseBodyAndReaderResult() throws Exception {
        byte[] body = {1, 2, 3};
        try (BasicClassicHttpResponse response = new BasicClassicHttpResponse(200)) {
            response.setEntity(new InputStreamEntity(new ByteArrayInputStream(body), ContentType.APPLICATION_OCTET_STREAM));
            assertEquals("123", PublicHttpClient.readResponse(response, new HttpGet("https://1.1.1.1/fixture"),
                stream -> "" + stream.read() + stream.read() + stream.read()));
        }
    }

    /** A body exactly at the size limit remains readable through EOF. */
    @Test
    public void acceptsBodyAtResponseLimit() throws Exception {
        long limit = 100L * 1024 * 1024;
        try (BasicClassicHttpResponse response = new BasicClassicHttpResponse(200)) {
            response.setEntity(new InputStreamEntity(new NullInputStream(limit), ContentType.APPLICATION_OCTET_STREAM));
            assertEquals(Long.valueOf(limit), PublicHttpClient.readResponse(response, new HttpGet("https://1.1.1.1/fixture"),
                stream -> stream.transferTo(OutputStream.nullOutputStream())));
        }
    }

    /** Streaming responses cannot evade the size limit by omitting Content-Length. */
    @Test
    public void rejectsOversizedBodyWithoutContentLength() throws Exception {
        long limit = 100L * 1024 * 1024;
        try (BasicClassicHttpResponse response = new BasicClassicHttpResponse(200)) {
            response.setEntity(new InputStreamEntity(new NullInputStream(limit + 1), ContentType.APPLICATION_OCTET_STREAM));
            assertEquals(-1, response.getEntity().getContentLength());
            assertThrows(IOException.class, () -> PublicHttpClient.readResponse(response, new HttpGet("https://1.1.1.1/fixture"),
                stream -> stream.transferTo(OutputStream.nullOutputStream())));
        }
    }

    /** Skipped bytes count toward the limit enforced on subsequent single-byte reads. */
    @Test
    public void rejectsSingleByteReadPastLimitAfterSkipping() throws Exception {
        long limit = 100L * 1024 * 1024;
        try (BasicClassicHttpResponse response = new BasicClassicHttpResponse(200)) {
            response.setEntity(new InputStreamEntity(new NullInputStream(limit + 1), ContentType.APPLICATION_OCTET_STREAM));
            assertThrows(IOException.class, () -> PublicHttpClient.readResponse(response, new HttpGet("https://1.1.1.1/fixture"), stream -> {
                assertEquals(limit, stream.skip(limit));
                return stream.read();
            }));
        }
    }

    /** Missing bodies and unsuccessful GET responses fail before invoking the reader. */
    @Test
    public void preservesResponseValidation() throws Exception {
        try (BasicClassicHttpResponse response = new BasicClassicHttpResponse(200)) {
            assertThrows(IOException.class, () -> PublicHttpClient.readResponse(response, new HttpGet("https://1.1.1.1/fixture"), stream -> stream.read()));
            response.setCode(302);
            response.setEntity(new InputStreamEntity(new ByteArrayInputStream(new byte[0]), ContentType.APPLICATION_OCTET_STREAM));
            assertThrows(IOException.class, () -> PublicHttpClient.readResponse(response, new HttpGet("https://1.1.1.1/fixture"), stream -> stream.read()));
        }
    }

    /** Unsupported schemes and embedded credentials are rejected before network access. */
    @Test
    public void rejectsLocalFilesAndUrlCredentials() {
        assertThrows(IllegalArgumentException.class,
            () -> PublicHttpClient.read("file:///tmp/calendar.ics", null, stream -> stream.read()));
        assertThrows(IllegalArgumentException.class,
            () -> PublicHttpClient.contentType("https://user@example.org/image.png"));
    }

    /** HEAD and GET requests reject loopback destinations without opening a connection. */
    @Test
    public void blocksHeadAndGetBeforeConnectingToLoopback() throws Exception {
        try (ServerSocket listener = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            String url = "http://127.0.0.1:" + listener.getLocalPort() + "/fixture";
            assertThrows(UnknownHostException.class, () -> PublicHttpClient.contentType(url));
            assertThrows(UnknownHostException.class, () -> PublicHttpClient.headHeader(url, null, "X-Frame-Options"));
            assertThrows(UnknownHostException.class, () -> PublicHttpClient.read(url, null, stream -> stream.read()));
            listener.setSoTimeout(100);
            assertThrows(SocketTimeoutException.class, listener::accept);
        }
    }
}
