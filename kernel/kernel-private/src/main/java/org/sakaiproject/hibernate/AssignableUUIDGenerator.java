package org.sakaiproject.hibernate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.type.Type;
import org.sakaiproject.component.api.ServerConfigurationService;

import lombok.Setter;

public class AssignableUUIDGenerator extends UUIDGenerator implements IdentifierGenerator, Configurable {

    public static final String HIBERNATE_ASSIGNABLE_ID_CLASSES = "hibernate.assignable.id.classes";

    @Setter
    private static ServerConfigurationService serverConfigurationService;

    @Override
    public void configure(Type type, Properties params, Dialect d) throws MappingException {
        super.configure(type, params, d);
    }

    @Override
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        if (serverConfigurationService != null) {
            String[] classes = serverConfigurationService.getStrings(HIBERNATE_ASSIGNABLE_ID_CLASSES);
            if (classes != null) {
                String entityName = object.getClass().getName();
                if (Arrays.asList(classes).contains(entityName)) {
                    final Serializable id = session.getEntityPersister(entityName, object).getIdentifier(object, session);
                    if (id != null) return id;
                }
            }
        }
        return super.generate(session, object);
    }
}
