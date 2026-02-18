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
package org.sakaiproject.scheduling.impl;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.scheduling.api.SchedulingService;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchedulingServiceImpl implements SchedulingService {

    @Autowired private ServerConfigurationService serverConfigurationService;

    private static int DEFAULT_POOL_SIZE = 8;

    private ScheduledThreadPoolExecutor executor;

    public void init() {

        int poolSize = serverConfigurationService.getInt("schedulingservice.poolsize", DEFAULT_POOL_SIZE);
        if (poolSize <= 0) {
          log.warn("schedulingservice.poolsize is invalid. Defaulting to {}", DEFAULT_POOL_SIZE);
          poolSize = DEFAULT_POOL_SIZE;
        }

        executor = new ScheduledThreadPoolExecutor(poolSize);
        executor.setRemoveOnCancelPolicy(true);
    }

    public void destroy() {

        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {

        return executor.schedule(command, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                long initialDelay,
                                                long period,
                                                TimeUnit unit) {

        return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                long initialDelay,
                                                long delay,
                                                TimeUnit unit) {

        return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}


