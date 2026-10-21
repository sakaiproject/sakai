/**
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0.
 * http://opensource.org/licenses/ecl2
 */
package org.sakaiproject.ignite;

import org.junit.After;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.sakaiproject.modi.BeanDefinitionSource;
import org.sakaiproject.util.SakaiApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/** The same handoff contract through the traditional component manager. */
public class HibernateCacheLegacyConfigurationTest extends HibernateCacheConfigurationTest {

    @Override
    protected ConfigurableApplicationContext createKernelContext(BeanDefinitionSource definitions) throws Exception {
        System.setProperty("sakai.modi.enabled", "false");
        System.setProperty("sakai.components.root", temporary.newFolder("components").getAbsolutePath());
        ComponentManager.setLateRefresh(true);
        SpringCompMgr manager = (SpringCompMgr) ComponentManager.getInstance();
        SakaiApplicationContext kernel = (SakaiApplicationContext) manager.getApplicationContext();
        // Select the same real configuration graph as the MODI variant, without
        // loading unrelated services or starting a node in this focused test.
        kernel.setConfigLocations(new String[0]);
        kernel.setInitialSingletonNames(new String[0]);
        definitions.registerBeans(kernel);
        return kernel;
    }

    @After
    public void restoreRefreshMode() {
        ComponentManager.setLateRefresh(false);
    }
}
