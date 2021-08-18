package org.sakaiproject.ignite;

import org.apache.ignite.configuration.CacheConfiguration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class IgniteConditionalCache {
    private String className;
    private CacheConfiguration cacheConfiguration;

    /**
     * Checks to see if the class stored className exists. Note this works because all of our data classes are in shared.
     * @return true if the class exists and false if it doesn't
     */
    public boolean exists() {
        try {
            Class.forName(className);
            log.info("Conditional cache {} detected for class {}, adding cache", cacheConfiguration.getName(), className);
            return true;
        } catch (ClassNotFoundException e) {
            log.debug("Conditional cache {} not detected for class {}, skipping cache", cacheConfiguration.getName(), className);
            return false;
        }
    }
}
