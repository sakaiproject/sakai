package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

/**
 * Tomcat Bootstrapper for Sakai. Built as a LifecycleListener to be registered in server.xml, to start up all required
 * components early, rather than waiting for them to be tripped by the first webapp.
 * <p>
 * Delegates all real work to the {@link Launcher}.
 */
@Slf4j
public class SakaiStartListener implements LifecycleListener {
    protected final Launcher launcher;

    public SakaiStartListener() {
        launcher = new Launcher();
    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        switch (event.getType()) {
            case Lifecycle.START_EVENT: launcher.start(); break;
            case Lifecycle.STOP_EVENT: launcher.stop(); break;
        }
    }
}
