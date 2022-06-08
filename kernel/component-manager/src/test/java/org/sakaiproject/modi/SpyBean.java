package org.sakaiproject.modi;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple class to spy on creation from the fixture bean definition files.
 *
 * Reset in a @Before and track instance count or set values on an instance.
 */
@Slf4j
public class SpyBean {
    public static Integer instances = 0;

    @Getter private Integer number;

    @Getter @Setter private String name;

    public SpyBean() {
        instances++;
        number = instances;
        log.debug("Creating a SpyBean, instances now: {}", instances);
    }
}