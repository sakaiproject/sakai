/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.scheduling.api;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A facade for Java's ScheduledExecutorService. The number of threads in the pool can be configured
 * using the property <code>schedulingservice.poolsize</code>
 */
public interface SchedulingService {

    /**
     * @see java.util.concurrent.ScheduledExecutorService#schedule(Runnable, long, TimeUnit)
     */
    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

    /**
     * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     */
    ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                long initialDelay,
                                                long delay,
                                                TimeUnit unit);

    /**
     * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
     */
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                long initialDelay,
                                                long delay,
                                                TimeUnit unit);
}


