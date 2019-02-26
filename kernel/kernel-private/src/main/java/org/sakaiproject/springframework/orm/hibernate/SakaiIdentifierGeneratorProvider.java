package org.sakaiproject.springframework.orm.hibernate;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.jpa.spi.IdentifierGeneratorStrategyProvider;
import org.sakaiproject.hibernate.AssignableUUIDGenerator;

public class SakaiIdentifierGeneratorProvider implements IdentifierGeneratorStrategyProvider {

    @Override
    public Map<String, Class<?>> getStrategies() {
        Map<String, Class<?>> generatorStrategies = new HashMap<>();
        generatorStrategies.put("uuid2", AssignableUUIDGenerator.class);
        return generatorStrategies;
    }
}
