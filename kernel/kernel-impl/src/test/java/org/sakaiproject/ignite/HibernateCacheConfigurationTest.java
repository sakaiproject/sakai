/**
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0.
 * http://opensource.org/licenses/ecl2
 */
package org.sakaiproject.ignite;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataPageEvictionMode;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.BasicConfigurationService;
import org.sakaiproject.modi.BeanDefinitionSource;
import org.sakaiproject.modi.GlobalApplicationContext;
import org.sakaiproject.modi.SharedApplicationContext;
import org.sakaiproject.util.SakaiProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/** Real kernel configuration graph and service locator; deliberately never starts a node. */
public class HibernateCacheConfigurationTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private final Map<String, String> originalProperties = new HashMap<>();

    @Before
    public void configureContext() {
        for (String key : new String[] {"sakai.modi.enabled", "sakai.home", "sakai.components.root", "IGNITE_NO_ASCII", "IGNITE_QUIET",
                "IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED"}) {
            originalProperties.put(key, System.getProperty(key));
        }
        System.setProperty("sakai.modi.enabled", "true");
        System.setProperty("sakai.home", temporary.getRoot().getAbsolutePath() + "/");
        GlobalApplicationContext.destroyContext();
    }

    @After
    public void closeContext() {
        try {
            ComponentManager.shutdown();
            GlobalApplicationContext.destroyContext();
        } finally {
            originalProperties.forEach((key, value) -> {
                if (value == null) {
                    System.clearProperty(key);
                } else {
                    System.setProperty(key, value);
                }
            });
        }
    }

    @Test
    public void providerUriReusesTheConfiguredKernelBeanDuringBootstrap() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("serverName", "jcache.example.test");
        properties.setProperty("serverId", "jcache-investigation");
        properties.setProperty("ignite.home", temporary.getRoot().getAbsolutePath());
        properties.setProperty("ignite.address", "127.0.0.1");
        properties.setProperty("ignite.port", "49175");
        properties.setProperty("ignite.range", "1");
        properties.setProperty("ignite.stopOnFailure", "false");
        BeanDefinitionSource definitions = new BeanDefinitionSource() {
            @Override
            public String getName() {
                return "kernel-ignite-configuration";
            }

            @Override
            public void registerBeans(BeanDefinitionRegistry registry) {
                try (GenericApplicationContext source = new GenericApplicationContext()) {
                    XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(source);
                    Path components = Path.of("src/main/webapp/WEB-INF");
                    for (String name : new String[] {"ignite-components.xml", "component-components.xml", "util-components.xml"}) {
                        reader.loadBeanDefinitions(new FileSystemResource(components.resolve(name)));
                    }
                    // Load the normal configuration graph, excluding the old node/cache-manager owner
                    // and unrelated services. No collaborators are mocked or their methods overridden.
                    for (String name : source.getBeanDefinitionNames()) {
                        if ((name.startsWith("org.sakaiproject.ignite.")
                                && !name.equals("org.sakaiproject.ignite.SakaiIgnite")
                                && !name.equals("org.sakaiproject.ignite.SakaiCacheManager")
                                && !name.equals("org.sakaiproject.ignite.api.CacheAdminService"))
                                || name.equals("org.sakaiproject.component.api.ServerConfigurationService")
                                || name.equals("org.sakaiproject.thread_local.api.ThreadLocalManager")) {
                            registry.registerBeanDefinition(name, source.getBeanDefinition(name));
                            if (name.startsWith("org.sakaiproject.ignite.JCache")
                                    || name.equals("org.sakaiproject.ignite.HibernateCacheManager")) {
                                registry.getBeanDefinition(name).setLazyInit(true);
                            }
                        }
                    }
                }
                RootBeanDefinition sakaiProperties = new RootBeanDefinition(SakaiProperties.class);
                sakaiProperties.getPropertyValues().add("properties", properties);
                registry.registerBeanDefinition("org.sakaiproject.component.SakaiProperties", sakaiProperties);
                RootBeanDefinition probe = new RootBeanDefinition(ProviderConfigurationProbe.class);
                probe.setDependsOn("org.sakaiproject.ignite.SakaiIgniteConfiguration");
                probe.getPropertyValues().add("configurationUri",
                        new RuntimeBeanReference("org.sakaiproject.ignite.JCacheConfigurationUri"));
                registry.registerBeanDefinition("providerConfigurationProbe", probe);
            }
        };
        ConfigurableApplicationContext kernel = createKernelContext(definitions);
        kernel.refresh();
        IgniteConfiguration expected = kernel.getBean("org.sakaiproject.ignite.SakaiIgniteConfiguration", IgniteConfiguration.class);
        ProviderConfigurationProbe probe = kernel.getBean(ProviderConfigurationProbe.class);
        assertSame(expected, probe.configuration);
        assertSame(expected.getClassLoader(), kernel.getBean("org.sakaiproject.ignite.JCacheClassLoader"));
        assertTrue(probe.configurationUri.toString().endsWith("/org/sakaiproject/ignite/jcache-configuration.xml"));
        assertSame(IgniteConfigurationAdapter.class.getClassLoader(), expected.getClassLoader());
        assertEquals("jcache-example-test", expected.getIgniteInstanceName());
        assertEquals("jcache-investigation", expected.getConsistentId());
        assertTrue(kernel.getBean("org.sakaiproject.component.api.ServerConfigurationService") instanceof BasicConfigurationService);
        TcpDiscoverySpi discovery = (TcpDiscoverySpi) expected.getDiscoverySpi();
        assertEquals(49176, discovery.getIpFinder().getRegisteredAddresses().iterator().next().getPort());
        Map<String, CacheConfiguration<?, ?>> caches = new HashMap<>();
        for (CacheConfiguration<?, ?> cache : expected.getCacheConfiguration()) {
            caches.put(cache.getName(), cache);
        }
        assertTrue(caches.size() > 10);
        CacheConfiguration<?, ?> timestamps = caches.get("default-update-timestamps-region");
        assertEquals(CacheMode.REPLICATED, timestamps.getCacheMode());
        assertEquals(CacheWriteSynchronizationMode.FULL_SYNC, timestamps.getWriteSynchronizationMode());
        assertEquals("hibernate_timestamps_region", timestamps.getDataRegionName());
        assertTrue(timestamps.getExpiryPolicyFactory().create().getExpiryForCreation().isEternal());
        for (DataRegionConfiguration region : expected.getDataStorageConfiguration().getDataRegionConfigurations()) {
            if (region.getName().equals("hibernate_timestamps_region")) {
                assertEquals(DataPageEvictionMode.DISABLED, region.getPageEvictionMode());
            }
        }
        assertTrue(Ignition.allGrids().isEmpty());

        // Deployments resolve the provider URI from a component JAR, not target/classes.
        Path jar = temporary.newFile("provider-resource.jar").toPath();
        String resourceName = "org/sakaiproject/ignite/jcache-configuration.xml";
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar));
                InputStream input = new ClassPathResource(resourceName).getInputStream()) {
            output.putNextEntry(new JarEntry(resourceName));
            input.transferTo(output);
            output.closeEntry();
        }
        URI jarUri = URI.create("jar:" + jar.toUri() + "!/" + resourceName);
        try (GenericXmlApplicationContext packaged = new GenericXmlApplicationContext(jarUri.toString())) {
            assertSame(expected, packaged.getBean(IgniteConfiguration.class));
        }
        assertSame(expected, kernel.getBean("org.sakaiproject.ignite.SakaiIgniteConfiguration"));
        assertTrue(Ignition.allGrids().isEmpty());
    }

    /** Each component bootstrap variant exercises the same configuration contract. */
    protected ConfigurableApplicationContext createKernelContext(BeanDefinitionSource definitions) throws Exception {
        SharedApplicationContext kernel = GlobalApplicationContext.getContext();
        kernel.registerBeanSource(definitions);
        return kernel;
    }

    /** Reproduces the provider's separate Spring XML load while the kernel is refreshing. */
    public static class ProviderConfigurationProbe implements InitializingBean, DisposableBean {
        private GenericXmlApplicationContext providerContext;
        private IgniteConfiguration configuration;
        private URI configurationUri;

        public void setConfigurationUri(URI configurationUri) {
            this.configurationUri = configurationUri;
        }

        @Override
        public void afterPropertiesSet() {
            providerContext = new GenericXmlApplicationContext(configurationUri.toString());
            Map<String, IgniteConfiguration> configurations = providerContext.getBeansOfType(IgniteConfiguration.class);
            assertEquals(1, configurations.size());
            configuration = configurations.values().iterator().next();
        }

        @Override
        public void destroy() {
            if (providerContext != null) {
                providerContext.close();
            }
        }
    }
}
