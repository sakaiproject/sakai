package org.sakaiproject.ignite;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.ShutdownPolicy;
import org.apache.ignite.failure.AbstractFailureHandler;
import org.apache.ignite.failure.FailureContext;
import org.apache.ignite.internal.IgnitionEx;
import org.apache.ignite.internal.util.typedef.internal.U;

public class IgniteStopNodeAndExitHandler extends AbstractFailureHandler {

    private static final int TIME_OUT = 60;

    @Override
    protected boolean handle(Ignite ignite, FailureContext failureCtx) {
        IgniteLogger log = ignite.log();

        final CountDownLatch latch = new CountDownLatch(1);

        new Thread(
                () -> {
                    U.error(log, "Stopping local node on Ignite failure: [failureCtx=" + failureCtx + ']');
                    IgnitionEx.stop(ignite.name(), false, ShutdownPolicy.IMMEDIATE,false);
                    latch.countDown();
                },
                "ignite-node-stop"
        ).start();

        new Thread(
                () -> {
                    try {
                        if (latch.await(TIME_OUT, TimeUnit.SECONDS)) {
                            // attempt to shut down jvm gracefully with a grace period
                            U.error(log, "JVM will exit due to the failure: [failureCtx=" + failureCtx + ']');
                            Runtime.getRuntime().exit(Ignition.KILL_EXIT_CODE);
                        }
                        // if time out reached then halt the jvm
                        U.error(log, "JVM will be halted due to the failure: [failureCtx=" + failureCtx + ']');
                        Runtime.getRuntime().halt(Ignition.KILL_EXIT_CODE);
                    } catch (InterruptedException e) {
                        // No-op.
                    }
                },
                "jvm-exit-or-halt-on-stop-timeout"
        ).start();

        return true;
    }
}
