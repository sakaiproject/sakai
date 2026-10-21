/**
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0.
 * http://opensource.org/licenses/ecl2
 */
package org.sakaiproject.ignite;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.sakaiproject.content.api.persistence.FileConversionQueueItem.Status;
import org.sakaiproject.content.api.persistence.FileConversionQueueItem;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/** Real provider, Spring owner, two JPA contexts, and a real Sakai cached mapping; no mocks. */
public class HibernateCacheOwnershipTest {

    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    @Rule public Timeout timeout = Timeout.seconds(90);
    private AnnotationConfigApplicationContext owner;
    private final List<AnnotationConfigApplicationContext> factories = new ArrayList<>();

    @Before
    public void startOwner() throws Exception {
        Path work = temporary.newFolder("ignite").toPath();
        String nodeName = "sakai-jcache-" + UUID.randomUUID();
        String xml;
        try (InputStream source = getClass().getResourceAsStream("/hibernate-cache/ignite-jcache.xml")) {
            assertNotNull(source);
            xml = new String(source.readAllBytes(), StandardCharsets.UTF_8);
        }
        // Reserve free ports together. Never use Sakai's configured/default discovery range.
        try (ServerSocket discovery = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
                ServerSocket communication = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            xml = xml.replace("@DISCOVERY_PORT@", Integer.toString(discovery.getLocalPort()))
                    .replace("@COMMUNICATION_PORT@", Integer.toString(communication.getLocalPort()));
        }
        xml = xml.replace("@NODE@", nodeName).replace("@WORK@",
                work.toString().replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;"));
        Path config = temporary.newFile("ignite.xml").toPath();
        Files.writeString(config, xml);
        owner = new AnnotationConfigApplicationContext();
        // Use the production owner/borrower wiring with an isolated provider URI.
        try (GenericXmlApplicationContext configuration = new GenericXmlApplicationContext(config.toUri().toString())) {
            IgniteConfiguration settings = configuration.getBean(IgniteConfiguration.class);
            settings.setClassLoader(SharedJCacheManager.class.getClassLoader());
            owner.registerBean("org.sakaiproject.ignite.SakaiIgniteConfiguration", IgniteConfiguration.class, () -> settings);
        }
        new XmlBeanDefinitionReader(owner).loadBeanDefinitions("classpath:org/sakaiproject/ignite/jcache-components.xml");
        owner.registerBean("org.sakaiproject.ignite.JCacheConfigurationResource", FileSystemResource.class,
                () -> new FileSystemResource(config));
        owner.register(OwnerConfiguration.class);
        owner.refresh();
        assertEquals(1, owner.getBean(Ignite.class).cluster().nodes().size());
    }

    @After
    public void closeContexts() {
        try {
            for (AnnotationConfigApplicationContext factory : factories.reversed()) {
                factory.close();
            }
        } finally {
            if (owner != null) {
                owner.close();
            }
        }
    }

    @Test
    public void borrowedJCacheHandlesPreserveOperationsAndOwner() {
        SharedJCacheManager borrowed = owner.getBean(SharedJCacheManager.class);
        CacheManager manager = owner.getBean("org.sakaiproject.ignite.JCacheManager", CacheManager.class);
        Cache<String, String> cache = borrowed.createCache("borrowed-api",
                new MutableConfiguration<String, String>().setTypes(String.class, String.class));
        cache.put("key", "value");
        assertSame(borrowed, cache.getCacheManager());
        assertSame(borrowed, borrowed.unwrap(CacheManager.class));
        assertSame(cache, cache.unwrap(Cache.class));
        assertEquals("value", borrowed.getCache("borrowed-api", String.class, String.class).get("key"));
        cache.close();
        borrowed.close();
        assertFalse(manager.isClosed());
        assertFalse(cache.isClosed());
        assertEquals("value", cache.get("key"));
        assertEquals("value", manager.getCache("borrowed-api", String.class, String.class).get("key"));
        owner.close();
        assertTrue(borrowed.isClosed());
        assertTrue(cache.isClosed());
    }

    @Test
    public void stockProviderManagerOwnsTheSharedNode() {
        CacheManager manager = owner.getBean("org.sakaiproject.ignite.JCacheManager", CacheManager.class);
        Ignite node = owner.getBean(Ignite.class);
        assertSame(node, manager.unwrap(Ignite.class));
        assertSame(manager, owner.getBean(org.apache.ignite.cache.CachingProvider.class).findManager(node));
        assertEquals(1, node.cluster().nodes().size());
        owner.getBean(IgniteSpringCacheManager.class).getCache("lifecycle-spring").put("key", "value");
        assertEquals("value", manager.getCache("lifecycle-spring").get("key"));
    }

    @Test
    public void closingOneFactoryPreservesEntityQueryAndSpringCaches() {
        AnnotationConfigApplicationContext first = openFactory("update", true);
        AnnotationConfigApplicationContext second = openFactory("update", true);
        EntityManagerFactory firstFactory = first.getBean(EntityManagerFactory.class);
        EntityManagerFactory secondFactory = second.getBean(EntityManagerFactory.class);
        CacheManager manager = owner.getBean("org.sakaiproject.ignite.JCacheManager", CacheManager.class);
        Ignite node = owner.getBean(Ignite.class);
        IgniteCache<Object, Object> entityCache = node.cache(FileConversionQueueItem.class.getName());
        IgniteCache<Object, Object> queryCache = node.cache("default-query-results-region");
        IgniteCache<Object, Object> timestamps = node.cache("default-update-timestamps-region");
        Long id = insert(firstFactory, "warm");
        firstFactory.getCache().evictAll();
        assertEquals("warm", reference(secondFactory, id));
        Statistics statistics = secondFactory.unwrap(SessionFactory.class).getStatistics();
        long entityHits = statistics.getSecondLevelCacheHitCount();
        long statements = statistics.getPrepareStatementCount();
        assertEquals("warm", reference(secondFactory, id));
        assertTrue(statistics.getSecondLevelCacheHitCount() > entityHits);
        assertEquals(statements, statistics.getPrepareStatementCount());
        assertEquals(List.of("warm"), references(secondFactory));
        assertEquals(List.of("warm"), references(secondFactory));
        assertTrue(statistics.getQueryCacheHitCount() > 0);
        org.springframework.cache.Cache springCache = owner.getBean(IgniteSpringCacheManager.class).getCache("lifecycle-spring");
        springCache.put("key", "retained");

        first.close();

        assertFalse(manager.isClosed());
        assertFalse(entityCache.isClosed());
        assertFalse(queryCache.isClosed());
        assertFalse(timestamps.isClosed());
        assertEquals("retained", springCache.get("key", String.class));
        entityHits = statistics.getSecondLevelCacheHitCount();
        long queryHits = statistics.getQueryCacheHitCount();
        assertEquals("warm", reference(secondFactory, id));
        assertTrue(statistics.getSecondLevelCacheHitCount() > entityHits);
        assertEquals(List.of("warm"), references(secondFactory));
        assertTrue(statistics.getQueryCacheHitCount() > queryHits);
        assertSame(node, manager.unwrap(Ignite.class));
    }

    @Test
    public void stockHibernateFactoryDemonstratesTheOwnershipProblem() {
        AnnotationConfigApplicationContext factory = openFactory("update", false);
        CacheManager manager = owner.getBean("org.sakaiproject.ignite.JCacheManager", CacheManager.class);
        String name = owner.getBean(Ignite.class).name();
        factory.close();
        assertTrue(manager.isClosed());
        assertEquals(IgniteState.STOPPED, Ignition.state(name));
    }

    @Test
    public void failedBootstrapPreservesTheOwnerAndRegionHandles() {
        CacheManager manager = owner.getBean("org.sakaiproject.ignite.JCacheManager", CacheManager.class);
        IgniteCache<Object, Object> entityCache = owner.getBean(Ignite.class).cache(FileConversionQueueItem.class.getName());
        BeanCreationException failure = assertThrows(BeanCreationException.class, () -> openFactory("validate", true));
        assertTrue(failure.getMostSpecificCause().getMessage().contains("missing table"));
        assertFalse(manager.isClosed());
        assertFalse(entityCache.isClosed());
        EntityManagerFactory replacement = openFactory("update", true).getBean(EntityManagerFactory.class);
        assertEquals("replacement", reference(replacement, insert(replacement, "replacement")));
    }

    @Test
    public void springOwnerClosesTheManagerAndNode() {
        AnnotationConfigApplicationContext factory = openFactory("update", true);
        CacheManager manager = owner.getBean("org.sakaiproject.ignite.JCacheManager", CacheManager.class);
        String name = owner.getBean(Ignite.class).name();
        factory.close();
        assertFalse(manager.isClosed());
        owner.close();
        assertTrue(manager.isClosed());
        assertEquals(IgniteState.STOPPED, Ignition.state(name));
    }

    @Test
    public void rollbackAndCommittedMutationPreserveEntityAndQueryResults() {
        EntityManagerFactory factory = openFactory("update", true).getBean(EntityManagerFactory.class);
        Long id = insert(factory, "original");
        assertEquals("original", reference(factory, id));
        assertEquals(List.of("original"), references(factory));
        assertEquals(List.of("original"), references(factory));
        EntityManager transaction = factory.createEntityManager();
        try {
            transaction.getTransaction().begin();
            transaction.find(FileConversionQueueItem.class, id).setReference("rolled-back");
            transaction.flush();
            transaction.getTransaction().rollback();
        } finally {
            transaction.close();
        }
        assertEquals("original", reference(factory, id));
        assertEquals(List.of("original"), references(factory));
        transaction = factory.createEntityManager();
        try {
            transaction.getTransaction().begin();
            transaction.find(FileConversionQueueItem.class, id).setReference("committed");
            transaction.getTransaction().commit();
        } finally {
            transaction.close();
        }
        assertEquals("committed", reference(factory, id));
        assertEquals(List.of("committed"), references(factory));
    }

    @Test
    public void springTransactionManagerPreservesCommittedAndRolledBackValues() {
        AnnotationConfigApplicationContext context = openFactory("update", true);
        EntityManagerFactory factory = context.getBean(EntityManagerFactory.class);
        TransactionTemplate transactions = new TransactionTemplate(context.getBean(JpaTransactionManager.class));
        Long id = insert(factory, "original");
        transactions.executeWithoutResult(status -> {
            EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(factory);
            assertNotNull(entityManager);
            entityManager.find(FileConversionQueueItem.class, id).setReference("rolled-back");
            entityManager.flush();
            status.setRollbackOnly();
        });
        assertEquals("original", reference(factory, id));
        transactions.executeWithoutResult(status -> {
            EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(factory);
            assertNotNull(entityManager);
            entityManager.find(FileConversionQueueItem.class, id).setReference("committed");
        });
        assertEquals("committed", reference(factory, id));
    }

    private AnnotationConfigApplicationContext openFactory(String schemaAction, boolean shared) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        factories.add(context);
        context.setParent(owner);
        // Start the real global JPA factory definition with only database and
        // managed-type boundaries narrowed to this fixture.
        try (GenericApplicationContext definitions = new GenericApplicationContext()) {
            new XmlBeanDefinitionReader(definitions).loadBeanDefinitions(
                    new FileSystemResource("src/main/webapp/WEB-INF/db-components.xml"));
            BeanDefinition factory = definitions.getBeanDefinition("entityManagerFactory");
            factory.getPropertyValues().removePropertyValue("persistenceUnitManager");
            factory.getPropertyValues().add("dataSource", new RuntimeBeanReference("dataSource"));
            factory.getPropertyValues().add("managedTypes", PersistenceManagedTypes.of(FileConversionQueueItem.class.getName()));
            Object configured = factory.getPropertyValues().get("jpaPropertyMap");
            assertTrue(configured instanceof Map<?, ?>);
            ManagedMap<String, Object> properties = new ManagedMap<>();
            ((Map<?, ?>) configured).forEach((key, value) -> properties.put(key.toString(), value));
            properties.put("hibernate.hbm2ddl.auto", schemaAction);
            properties.put("hibernate.generate_statistics", true);
            properties.put("hibernate.show_sql", false);
            if (!shared) {
                properties.put("hibernate.javax.cache.cache_manager",
                        new RuntimeBeanReference("org.sakaiproject.ignite.JCacheManager"));
            }
            factory.getPropertyValues().add("jpaPropertyMap", properties);
            context.registerBeanDefinition("entityManagerFactory", factory);
        }
        context.register(HibernateConfiguration.class);
        context.refresh();
        return context;
    }

    private Long insert(EntityManagerFactory factory, String reference) {
        EntityManager entityManager = factory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            FileConversionQueueItem item = new FileConversionQueueItem();
            item.setReference(reference);
            item.setStatus(Status.NOT_STARTED);
            item.setAttempts(0);
            entityManager.persist(item);
            entityManager.getTransaction().commit();
            return item.getId();
        } finally {
            entityManager.close();
        }
    }

    private String reference(EntityManagerFactory factory, Long id) {
        EntityManager entityManager = factory.createEntityManager();
        try {
            return entityManager.find(FileConversionQueueItem.class, id).getReference();
        } finally {
            entityManager.close();
        }
    }

    private List<String> references(EntityManagerFactory factory) {
        EntityManager entityManager = factory.createEntityManager();
        try {
            return entityManager.createQuery("select item.reference from FileConversionQueueItem item order by item.reference", String.class)
                    .setHint("org.hibernate.cacheable", true).getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class OwnerConfiguration {
        @Bean(initMethod = "init")
        public IgniteSpringCacheManager springCacheManager(Ignite node) {
            IgniteSpringCacheManager manager = new IgniteSpringCacheManager();
            manager.setSakaiIgnite(node);
            manager.setIgniteConfiguration(node.configuration());
            return manager;
        }

        @Bean(destroyMethod = "shutdown")
        public EmbeddedDatabase dataSource() {
            return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).generateUniqueName(true).build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class HibernateConfiguration {
        @Bean
        public JpaTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
    }
}
