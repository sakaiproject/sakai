package org.sakaiproject.ignite;

import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IgniteEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    public void setIgnite(IgniteConfiguration igniteConfiguration) {
        String igniteInstanceName = igniteConfiguration.getIgniteInstanceName();
        getJpaPropertyMap().put("org.apache.ignite.hibernate.ignite_instance_name", igniteInstanceName);
        log.info("Ignite instance name [{}] configured as hibernate cache provider", igniteInstanceName);
    }
}
