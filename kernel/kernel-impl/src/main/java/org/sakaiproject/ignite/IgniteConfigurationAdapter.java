package org.sakaiproject.ignite;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import lombok.Getter;
import lombok.Setter;

public class IgniteConfigurationAdapter extends AbstractFactoryBean<IgniteConfiguration> {

    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private CacheConfiguration[] cacheConfiguration;

    @Getter @Setter private String node;
    @Getter @Setter private String name;
    @Getter @Setter private String home;
    @Getter @Setter private String mode;

    public void init() {
        node = serverConfigurationService.getString("ignite.node", serverConfigurationService.getString(node));
        name = serverConfigurationService.getString("ignite.name", name);
        home = serverConfigurationService.getString("ignite.home", home);
        mode = serverConfigurationService.getString("ignite.mode", mode);
    }

    @Override
    public Class<?> getObjectType() {
        return IgniteConfiguration.class;
    }

    @Override
    protected IgniteConfiguration createInstance() throws Exception {
        configureHome();

        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        igniteConfiguration.setIgniteHome(home);
        igniteConfiguration.setConsistentId(node);
        igniteConfiguration.setIgniteInstanceName(name);

        if (StringUtils.equalsAnyIgnoreCase("client", mode)) {
            igniteConfiguration.setClientMode(true);
        } else {
            igniteConfiguration.setClientMode(false);
        }

        igniteConfiguration.setGridLogger(new Slf4jLogger());

        igniteConfiguration.setCacheConfiguration(cacheConfiguration);

        return igniteConfiguration;
    }

    private void configureHome() {
        File igniteHome = new File(home);
        if (!igniteHome.exists()) igniteHome.mkdir();
    }
}
