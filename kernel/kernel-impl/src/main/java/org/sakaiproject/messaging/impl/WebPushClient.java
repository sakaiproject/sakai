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
package org.sakaiproject.messaging.impl;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import nl.martijndwars.webpush.AbstractPushService;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.HttpRequest;
import nl.martijndwars.webpush.Notification;

import org.jose4j.lang.JoseException;
import org.sakaiproject.util.PublicHttpClient;

/** Delivers Web Push only to public HTTPS destinations. */
final class WebPushClient extends AbstractPushService<WebPushClient> implements AutoCloseable {

    private final PublicHttpClient httpClient;

    /** Initializes VAPID signing and the HTTP client that enforces public destinations. */
    WebPushClient(String publicKey, String privateKey) throws GeneralSecurityException {
        super(publicKey, privateKey);
        httpClient = new PublicHttpClient();
    }

    /** Rejects subscriptions without an HTTPS endpoint resolving exclusively to public addresses. */
    static void validateSubscriptionEndpoint(String endpoint) {
        URI uri = httpsEndpoint(endpoint);
        try {
            PublicHttpClient.requirePublicAddress(uri);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Web Push endpoint must resolve to a public address");
        }
    }

    /** Encrypts and signs a notification, validates its destination, and returns the POST status. */
    int send(Notification notification) throws GeneralSecurityException, IOException, JoseException {
        httpsEndpoint(notification.getEndpoint());
        HttpRequest request = prepareRequest(notification, Encoding.AESGCM);
        return httpClient.post(httpsEndpoint(request.getUrl()), request.getHeaders(), request.getBody());
    }

    /** Parses an endpoint and rejects schemes other than HTTPS. */
    private static URI httpsEndpoint(String endpoint) {
        URI uri = PublicHttpClient.parseUrl(endpoint);
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Web Push requires HTTPS");
        }
        return uri;
    }

    /** Releases the HTTP client and its connections. */
    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
