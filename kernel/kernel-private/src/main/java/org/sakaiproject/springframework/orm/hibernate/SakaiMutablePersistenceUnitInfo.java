package org.sakaiproject.springframework.orm.hibernate;

import org.springframework.instrument.classloading.SimpleThrowawayClassLoader;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

public class SakaiMutablePersistenceUnitInfo extends MutablePersistenceUnitInfo {
    @Override
    public ClassLoader getNewTempClassLoader() {
        return new SimpleThrowawayClassLoader(this.getClassLoader());
    }
}
