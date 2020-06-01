/**
 * Copyright (c) 2006-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SiteStatsPersistenceConfig {

    @Lazy
    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory globalSessionFactory;

    @Lazy
    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager globalTransactionManager;

    @Lazy
    @Resource
    private ServerConfigurationService serverConfigurationService;

    private PlatformTransactionManager siteStatsTransactionManager;
    private SessionFactory siteStatsSessionFactory;
    private HikariDataSource externalDataSource;

    @Bean(name = "org.sakaiproject.sitestats.SiteStatsTransactionManager")
    public PlatformTransactionManager getSiteStatsTransactionManager() throws IOException {
        if (siteStatsTransactionManager == null) {
            String whichDb = serverConfigurationService.getString("sitestats.db", "internal");
            if ("external".equals(whichDb)) {
                HibernateTransactionManager tx = new HibernateTransactionManager();
                tx.setSessionFactory(getSiteStatsSessionFactory());
                siteStatsTransactionManager = tx;
            } else {
                siteStatsTransactionManager = globalTransactionManager;
            }
        }
        return siteStatsTransactionManager;
    }

    @Bean(name = "org.sakaiproject.sitestats.SiteStatsSessionFactory")
    public SessionFactory getSiteStatsSessionFactory() throws IOException {
        if (siteStatsSessionFactory == null) {
            String whichDb = serverConfigurationService.getString("sitestats.db", "internal");
            if ("external".equals(whichDb)) {
                LocalSessionFactoryBuilder sfb = new LocalSessionFactoryBuilder(getExternalDataSource());
                getAdditionalHibernateMappings().processAdditionalMappings(sfb);
                sfb.addProperties(getHibernateExternalProperties());
                siteStatsSessionFactory = sfb.buildSessionFactory();
            } else {
                siteStatsSessionFactory = globalSessionFactory;
            }
        }
        return siteStatsSessionFactory;
    }

    @Bean(name = "org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings.sitestats")
    public AdditionalHibernateMappings getAdditionalHibernateMappings() {

        AdditionalHibernateMappings hibernateMappings = new AdditionalHibernateMappingsImpl();
        hibernateMappings.setMappingResources(getHibernateMappings());
        return hibernateMappings;
    }

    @Bean(name = "org.sakaiproject.sitestats.HibernateMappings")
    public String[] getHibernateMappings() {
        return new String[] {
                "org/sakaiproject/sitestats/impl/hbm/PrefsImpl.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/EventStatImpl.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/DetailedEventImpl.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/LessonBuilderStatImpl.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/ResourceStatImpl.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/SiteVisitsImpl.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/SiteActivityImpl.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/SitePresenceImpl.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/SitePresenceTotalImpl.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/JobRunImpl.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/ReportDef.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/ServerStat.hbm.xml",
                "org/sakaiproject/sitestats/impl/hbm/UserStat.hbm.xml"
        };
    }

    @PreDestroy
    public void close() {
        if (externalDataSource != null && !externalDataSource.isClosed()) {
            log.info("SiteStats closing external database with pool name {}", externalDataSource.getPoolName());
            externalDataSource.close();
        }
    }

    private Properties getHibernateExternalProperties() {
        Properties p = new Properties();
        p.setProperty("hibernate.dialect", serverConfigurationService.getString("sitestats.externalDb.hibernate.dialect", "org.hibernate.dialect.HSQLDialect"));
        String autoDdl = serverConfigurationService.getString("sitestats.externalDb.auto.ddl", "update");
        if ("true".equals(autoDdl)) {
            autoDdl = "update";
            log.info("Auto DDL has been set to update based on old value of true, please update the property sitestats.externalDb.auto.ddl");
        } else if ("false".equals(autoDdl)) {
            autoDdl = "validate";
            log.info("Auto DDL has been set to validate based on old value of false, please update the property sitestats.externalDb.auto.ddl");
        }

        p.setProperty("hibernate.hbm2ddl.auto", autoDdl);
        p.setProperty("hibernate.show_sql", serverConfigurationService.getString("sitestats.externalDb.hibernate.show_sql", "false"));
        p.setProperty("hibernate.query.substitutions", "true 1, false 0, yes 'Y', no 'N'");
        p.setProperty("hibernate.jdbc.use_streams_for_binary", "true");
        p.setProperty("hibernate.cache.use_query_cache", "true");
        p.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.SingletonEhCacheRegionFactory");
        return p;
    }

    private HikariDataSource getExternalDataSource() {
        if (externalDataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setUsername(serverConfigurationService.getString("sitestats.externalDb.username", serverConfigurationService.getString("username@org.sakaiproject.sitestats.externalDbDataSource", "sa")));
            config.setPassword(serverConfigurationService.getString("sitestats.externalDb.password", serverConfigurationService.getString("password@org.sakaiproject.sitestats.externalDbDataSource", "")));
            config.setJdbcUrl(serverConfigurationService.getString("sitestats.externalDb.jdbcUrl", serverConfigurationService.getString("url@org.sakaiproject.sitestats.externalDbDataSource", "jdbc:hsqldb:mem:sitestats_db")));
            config.setDriverClassName(serverConfigurationService.getString("sitestats.externalDb.driverClassName", serverConfigurationService.getString("driverClassName@org.sakaiproject.sitestats.externalDbDataSource", "org.hsqldb.jdbcDriver")));
            config.setPoolName(serverConfigurationService.getString("sitestats.externalDb.poolName", "externalDBCP"));
            config.setMaximumPoolSize(serverConfigurationService.getInt("sitestats.externalDb.maxPoolSize", 5));
            externalDataSource = new HikariDataSource(config);
            log.info("SiteStats configuring external database with pool name {}", externalDataSource.getPoolName());
        }
        return externalDataSource;
    }
}
