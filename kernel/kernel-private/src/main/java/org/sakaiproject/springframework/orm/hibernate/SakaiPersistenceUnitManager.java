package org.sakaiproject.springframework.orm.hibernate;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import lombok.Setter;

import org.hibernate.cfg.AvailableSettings;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.hibernate.AssignableUUIDGenerator;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

/**
 * Created by enietzel on 6/22/17.
 */
public class SakaiPersistenceUnitManager extends DefaultPersistenceUnitManager {

    private String defaultPersistenceUnitName = "sakai";
    private PersistenceUnitInfo defaultPersistenceUnitInfo;
    @Setter private DataSource dataSource;
    @Setter private DataSource jtaDataSource;
    @Setter private ServerConfigurationService serverConfigurationService;

    @Override
    public void preparePersistenceUnitInfos() {
        MutablePersistenceUnitInfo pui = new MutablePersistenceUnitInfo();
        pui.setPersistenceUnitName(defaultPersistenceUnitName);
        pui.setExcludeUnlistedClasses(true);

        if (pui.getJtaDataSource() == null) {
            pui.setJtaDataSource(jtaDataSource);
        }
        if (pui.getNonJtaDataSource() == null) {
            pui.setNonJtaDataSource(dataSource);
        }

        // override default UUIDGenerator with AssignableUUIDGenerator
        pui.addProperty(org.hibernate.jpa.AvailableSettings.IDENTIFIER_GENERATOR_STRATEGY_PROVIDER, SakaiIdentifierGeneratorProvider.class.getName());
        AssignableUUIDGenerator.setServerConfigurationService(serverConfigurationService);

        postProcessPersistenceUnitInfo(pui);

        Boolean autoddl = serverConfigurationService.getBoolean("auto.ddl", true);
        String hbm2ddl = serverConfigurationService.getString(AvailableSettings.HBM2DDL_AUTO, "update");
        if (!autoddl) {
            // if sakai auto.ddl is turned off then set to validate
            hbm2ddl = "validate";
        }
        pui.getProperties().setProperty(AvailableSettings.HBM2DDL_AUTO, hbm2ddl);

        defaultPersistenceUnitInfo = pui;
    }

    @Override
    public PersistenceUnitInfo obtainDefaultPersistenceUnitInfo() {
        return defaultPersistenceUnitInfo;
    }
}