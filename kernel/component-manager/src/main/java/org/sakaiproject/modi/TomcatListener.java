package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tomcat Bootstrapper for Sakai. Built as a LifecycleListener to be registered in server.xml, to start up all
 * required components early, rather than waiting for them to be tripped by the first webapp.
 * FIXME: this needs rewrite with new design
 */
@Slf4j
public class TomcatListener implements LifecycleListener {
    protected final Launcher launcher;

    public TomcatListener() {
        launcher = new Launcher(getCatalina());
    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        switch (event.getType()) {
            case Lifecycle.START_EVENT: launcher.start(); break;
            case Lifecycle.STOP_EVENT: launcher.stop(); break;
        }
    }

    /**
     * Check the environment for catalina's base or home directory.
     *
     * @return Catalina's base or home directory.
     * @throws RuntimeException if no base directory can be found
     */
    protected Path getCatalina() {
        Path catalina = Path.of(System.getProperty("catalina.base", System.getProperty("catalina.home")));
        if (!Files.isDirectory(catalina)) {
            throw new RuntimeException("Fatal error launching Sakai: Tomcat's catalina.base or catalina.home must available as system properties.");
        }
        return catalina;
    }
}