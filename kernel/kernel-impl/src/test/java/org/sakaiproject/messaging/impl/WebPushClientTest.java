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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Base64;
import java.util.List;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Utils;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

public class WebPushClientTest {

    /** Subscription registration rejects endpoints that violate the public HTTPS policy. */
    @Test
    public void rejectsUnsafeSubscriptionEndpoints() {
        for (String endpoint : List.of(
                "http://1.1.1.1/push", "file:///tmp/push", "https://user@1.1.1.1/push", "https://1.1.1.1/push#fragment",
                "https://localhost/push", "https://127.0.0.1/push", "https://10.0.0.1/push",
                "https://172.16.0.1/push", "https://192.168.0.1/push", "https://100.64.0.1/push",
                "https://169.254.169.254/push", "https://168.63.129.16/push",
                "https://[::1]/push", "https://[::ffff:127.0.0.1]/push",
                "https://[fd00::1]/push", "https://[fe80::1]/push", "https://[2002:7f00:1::1]/push")) {
            assertThrows(endpoint, IllegalArgumentException.class, () -> WebPushClient.validateSubscriptionEndpoint(endpoint));
        }

        // Numeric addresses make these checks independent of DNS or an external service.
        WebPushClient.validateSubscriptionEndpoint("https://1.1.1.1/push?token=example");
        WebPushClient.validateSubscriptionEndpoint("https://[2606:4700:4700::1111]/push");
    }

    /** Delivery rechecks stored endpoints and blocks loopback before connecting. */
    @Test
    public void storedLoopbackEndpointIsBlockedBeforeConnecting() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
        generator.initialize(ECNamedCurveTable.getParameterSpec("prime256v1"));
        KeyPair server = generator.generateKeyPair();
        KeyPair receiver = generator.generateKeyPair();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        try (ServerSocket listener = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
                WebPushClient client = new WebPushClient(
                    encoder.encodeToString(Utils.encode((ECPublicKey) server.getPublic())),
                    encoder.encodeToString(Utils.encode((ECPrivateKey) server.getPrivate())))) {
            String endpoint = "https://127.0.0.1:" + listener.getLocalPort() + "/push";
            Subscription.Keys keys = new Subscription.Keys(
                encoder.encodeToString(Utils.encode((ECPublicKey) receiver.getPublic())),
                encoder.encodeToString(new byte[16]));
            Notification notification = new Notification(new Subscription(endpoint, keys), "test notification");

            // Exercise delivery directly, as if the endpoint was stored before registration validation.
            Notification httpNotification = new Notification(
                new Subscription("http://127.0.0.1:" + listener.getLocalPort() + "/push", keys), "test notification");
            assertThrows(IllegalArgumentException.class, () -> client.send(httpNotification));

            UnknownHostException exception = assertThrows(UnknownHostException.class, () -> client.send(notification));
            assertEquals("Web Push destination has a non-public address", exception.getMessage());
            listener.setSoTimeout(100);
            assertThrows(SocketTimeoutException.class, listener::accept);
        }
    }
}
