/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * This tests the concurrence of SpringBeanLocator.
 */
public class SpringBeanLocatorTest {

    @Test
    public void testWait() throws InterruptedException {
        ConfigurableApplicationContext context = new GenericApplicationContext();
        ConfigurableListableBeanFactory factory = context.getBeanFactory();
        Object bean = new Object();
        factory.registerSingleton("test", bean);
        context.refresh();
        Locator get1 = new Locator(SpringBeanLocator.getInstance());
        Locator get2 = new Locator(SpringBeanLocator.getInstance());
        get1.start();
        get2.start();

        // Increase the chances that the other threads will have run
        Thread.yield();

        SpringBeanLocator.setApplicationContext(context);
        get1.join(10000);
        get2.join(10000);
        // Cross thread assertions
        Assert.assertTrue(get1.good);
        Assert.assertTrue(get2.good);
    }

    class Locator extends Thread {

        final SpringBeanLocator locator;
        boolean good = false;

        Locator(SpringBeanLocator locator) {
            this.locator = locator;
        }

        @Override
        public void run() {
            good = locator.getBean("test") != null;
        }
    }
}
