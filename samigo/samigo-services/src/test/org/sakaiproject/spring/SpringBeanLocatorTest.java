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
