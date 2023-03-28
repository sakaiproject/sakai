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

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A launcher for Sakai in its traditional directory structure and conventions. This serves as the main entrypoint in
 * "modern" mode. It is used to kick off the configuration and component loading process and ensure that the shared
 * application context is ready for webapps to load.
 * <p>
 * The {@link SakaiStartListener} is the main consumer of this class, and there is some conventional overlap and Tomcat
 * naming exposed, despite very little in the way of actual Tomcat dependency. It is conceivable that another container
 * could serve.
 * <p>
 * The actual conventions of which system properties, files, and directories are used are housed in the
 * {@link Environment}. The launcher is responsible for initializing the environment, shared application context, and
 * the components, so the webapps have all everything set up when they start.
 * <p>
 * The Launcher is meant to start up a deployed instance. It would not be used during integration tests, which would do
 * some similar setup, not depending on the conventions of deployment.
 */
@Slf4j
public class Launcher {
    /**
     * Create a Sakai launcher.
     * <p>
     * Calling {@link #start()} will begin the boot process immediately, loading configs and the default components in
     * the global application context. Calling {@link #stop()} will shut down the context and unwind all of the beans
     * and webapps.
     * <p>
     * There is a shim set up to preserve the legacy ComponentManager API for those services and tools that do not make
     * use of dependency injection.
     * <p>
     * {@link GlobalApplicationContext} {@link SharedApplicationContext}
     * {@link org.sakaiproject.component.impl.ComponentManagerShim}
     */
    public Launcher() {
        this.env = Environment.initialize();
    }

    protected final Environment env;
    protected ComponentsDirectory components;
    protected SharedApplicationContext context;

    /**
     * Start the Sakai instance.
     * <p>
     * This begins the traditional boot process, checking and ensuring properties/variables like sakai.home, and then
     * loading all of the shared components into the shared application context, with a ComponentManager shim in place
     * to preserve the legacy API.
     * <p>
     * The {@link SharedApplicationContext} is both refreshed (loaded) and started (beans created/wired). We make no
     * distinction between the refresh/start phases here.
     *
     * @throws MissingConfigurationException if sakai-configuration.xml is specified and cannot be read
     * @throws InitializationException       if there are unresolvable problems with the core directories
     */
    public void start() throws MissingConfigurationException, InitializationException {
        log.info("Booting Sakai in Modern Dependency Injection Mode");
        System.setProperty("sakai.modi.enabled", "true");

        context = GlobalApplicationContext.getContext();
        context.registerBeanSource(getConfiguration());

        components = new ComponentsDirectory(env.getComponentsRoot(), env.getOverridesFolder());
        components.starting(context);

        context.refresh();
        context.start();
    }

    /**
     * Shut down Sakai. Notify the component loader that we are stopping, then stop and close the global context.
     * <p>
     * The {@link ComponentsDirectory} loader does not play an active role in stopping components. This is primarily a
     * lifecycle hook to keep start/stop model consistent from Tomcat through to "the components". Generally, the
     * context manages the beans and publishes richer events that can be used to trigger component-specific behavior.
     */
    public void stop() {
        log.info("Stopping Sakai components");
        if (components != null) {
            components.stopping();
        }
        context.stop();
        context.close();
    }

    protected Configuration getConfiguration() throws MissingConfigurationException {
        Path localConfig = env.getConfigurationFile();
        return Files.isRegularFile(localConfig)
                ? new Configuration(localConfig)
                : new Configuration();
    }
}
