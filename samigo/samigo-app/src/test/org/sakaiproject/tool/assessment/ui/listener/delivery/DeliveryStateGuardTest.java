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
package org.sakaiproject.tool.assessment.ui.listener.delivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryStateGuard.Decision;

/**
 * SAK-44349: decision logic of the delivery stale-tab guard.
 */
public class DeliveryStateGuardTest {

    @Test
    public void acceptedTokenRotatesSoTheSameFormCannotPostTwice() {
        DeliveryStateGuard guard = new DeliveryStateGuard();
        String issued = guard.getToken();

        assertTrue(guard.compareAndRotate(issued));
        assertNotEquals(issued, guard.getToken());
        assertFalse("second post of the same form must lose", guard.compareAndRotate(issued));
    }

    @Test
    public void missingOrForeignTokensAreRejected() {
        DeliveryStateGuard guard = new DeliveryStateGuard();
        assertFalse(guard.compareAndRotate(null));
        assertFalse(guard.compareAndRotate(""));
        assertFalse(guard.compareAndRotate("not-the-token"));
        // and rejection must not rotate: the real token still works
        assertTrue(guard.compareAndRotate(guard.getToken()));
    }

    @Test
    public void regenerateInvalidatesOutstandingForms() {
        DeliveryStateGuard guard = new DeliveryStateGuard();
        String issued = guard.getToken();
        guard.regenerate();
        assertFalse("a re-render must invalidate previously issued forms", guard.compareAndRotate(issued));
    }

    @Test
    public void concurrentDuplicatePostsAcceptExactlyOne() throws Exception {
        int threads = 8;
        for (int round = 0; round < 25; round++) {
            DeliveryStateGuard guard = new DeliveryStateGuard();
            String issued = guard.getToken();
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            List<Future<Boolean>> results = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                results.add(pool.submit((Callable<Boolean>) () -> {
                    start.await();
                    return guard.compareAndRotate(issued);
                }));
            }
            start.countDown();
            int accepted = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    accepted++;
                }
            }
            pool.shutdown();
            assertEquals("exactly one of the concurrent duplicate posts may win", 1, accepted);
        }
    }

    @Test
    public void evaluateBypassesWhenDisabledOrNoGuard() {
        DeliveryStateGuard guard = new DeliveryStateGuard();
        String issued = guard.getToken();

        assertEquals(Decision.BYPASS, DeliveryStateGuard.evaluate(guard, "junk", false, false, false));
        assertEquals(Decision.BYPASS, DeliveryStateGuard.evaluate(null, "junk", true, false, false));
        // bypass must not consume the token
        assertEquals(issued, guard.getToken());
    }

    @Test
    public void evaluateAcceptsCurrentTokenAndRotates() {
        DeliveryStateGuard guard = new DeliveryStateGuard();
        String issued = guard.getToken();

        assertEquals(Decision.ACCEPT, DeliveryStateGuard.evaluate(guard, issued, true, false, false));
        assertEquals(Decision.REJECT_RESYNC, DeliveryStateGuard.evaluate(guard, issued, true, false, false));
    }

    @Test
    public void timeoutSubmitGetsFullAuthorityWhenCurrentButIsNeverBlocked() {
        DeliveryStateGuard guard = new DeliveryStateGuard();
        String issued = guard.getToken();

        // single-tab case: the timeout submit carries the current token and
        // must be verified (full authority), consuming the token
        assertEquals(Decision.ACCEPT, DeliveryStateGuard.evaluate(guard, issued, true, true, false));
        // stale timeout submit: never blocked, but unverified (backstop applies)
        assertEquals(Decision.BYPASS, DeliveryStateGuard.evaluate(guard, issued, true, true, false));
        assertEquals(Decision.BYPASS, DeliveryStateGuard.evaluate(guard, null, true, true, false));
    }

    @Test
    public void autosaveVerifiesWithoutRotatingSoTheTabStaysValid() {
        DeliveryStateGuard guard = new DeliveryStateGuard();
        String issued = guard.getToken();

        // accepted autosave must NOT advance the sequence (Moodle-style):
        // the same tab's next post still carries a valid token
        assertEquals(Decision.ACCEPT, DeliveryStateGuard.evaluate(guard, issued, true, false, true));
        assertEquals(issued, guard.getToken());
        assertEquals(Decision.ACCEPT, DeliveryStateGuard.evaluate(guard, issued, true, false, true));
        assertEquals(Decision.ACCEPT, DeliveryStateGuard.evaluate(guard, issued, true, false, false));
    }

    @Test
    public void staleAutosaveIsRejectedSilentlyStaleInteractiveGetsResync() {
        DeliveryStateGuard guard = new DeliveryStateGuard();

        assertEquals(Decision.REJECT_SILENT, DeliveryStateGuard.evaluate(guard, "stale", true, false, true));
        assertEquals(Decision.REJECT_RESYNC, DeliveryStateGuard.evaluate(guard, "stale", true, false, false));
        assertEquals(Decision.REJECT_RESYNC, DeliveryStateGuard.evaluate(guard, null, true, false, false));
    }
}
