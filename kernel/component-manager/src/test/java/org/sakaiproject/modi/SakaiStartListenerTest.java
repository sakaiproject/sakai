/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.modi;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

public class SakaiStartListenerTest {
    @Before
    public void setup() {
        GlobalApplicationContext.destroyContext();
        System.setProperty("catalina.base", Paths.get("src/test/resources/fake-tomcat").toString());
    }

    @Test
    public void givenNoGlobalContext_whenListenerReceivesStart_thenTheContextIsActivated() {
        SakaiStartListener listener = new SakaiStartListener();

        LifecycleEvent start = new LifecycleEvent(mock(Lifecycle.class), Lifecycle.START_EVENT, null);
        listener.lifecycleEvent(start);

        SharedApplicationContext context = GlobalApplicationContext.getContext();
        assertThat(context.isActive()).isTrue();
    }

    @Test
    public void givenANormalLaunch_whenListenerReceivesStop_thenTheContextIsClosed() {
        SakaiStartListener listener = new SakaiStartListener();
        LifecycleEvent start = new LifecycleEvent(mock(Lifecycle.class), Lifecycle.START_EVENT, null);
        listener.lifecycleEvent(start);

        LifecycleEvent stop = new LifecycleEvent(mock(Lifecycle.class), Lifecycle.STOP_EVENT, null);
        listener.lifecycleEvent(stop);

        SharedApplicationContext context = GlobalApplicationContext.getContext();
        assertThat(context.isActive()).isFalse();

    }
}
