package org.sakaiproject.hibernate;

import java.io.Serializable;
import java.util.Arrays;

import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDGenerator;
import org.sakaiproject.component.api.ServerConfigurationService;

public class AssignableUUIDGenerator extends UUIDGenerator implements IdentifierGenerator {

    public static final String HIBERNATE_ASSIGNABLE_ID_CLASSES = "hibernate.assignable.id.classes";

    @Setter
    private static ServerConfigurationService serverConfigurationService;

    @Override
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        if (serverConfigurationService != null) {
            String[] classes = serverConfigurationService.getStrings(HIBERNATE_ASSIGNABLE_ID_CLASSES);
            if (classes != null) {
                String entityName = object.getClass().getName();
                if (Arrays.stream(classes).anyMatch(c -> c.equals(entityName))) {
                    final Serializable id = session.getEntityPersister(entityName, object).getIdentifier(object, session);
                    if (id != null) return id;
                }
            }
        }
        return super.generate(session, object);
    }
}
