/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.ignite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.DeploymentMode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.TransactionConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.plugin.segmentation.SegmentationPolicy;
import org.apache.ignite.spi.checkpoint.cache.CacheCheckpointSpi;
import org.apache.ignite.spi.collision.fifoqueue.FifoQueueCollisionSpi;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.sakaiproject.component.api.ConfiguredContext;

import static org.sakaiproject.modi.SysProp.sakai_home;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IgniteConfigurationAdapter extends AbstractFactoryBean<IgniteConfiguration> {

    public static final String IGNITE_ADDRESS = "ignite.address";
    public static final String IGNITE_ADDRESSES = "ignite.addresses";
    public static final String IGNITE_HOME = "ignite.home";
    public static final String IGNITE_MODE = "ignite.mode";
    public static final String IGNITE_PORT = "ignite.port";
    public static final String IGNITE_RANGE = "ignite.range";
    public static final String IGNITE_METRICS_UPDATE_FREQ = "ignite.metrics.update.freq";
    public static final String IGNITE_METRICS_LOG_FREQ = "ignite.metrics.log.freq";
    public static final String IGNITE_TCP_MESSAGE_QUEUE_LIMIT = "ignite.tcpMessageQueueLimit";
    public static final String IGNITE_TCP_SLOW_CLIENT_MESSAGE_QUEUE_LIMIT = "ignite.tcpSlowClientMessageQueueLimit";
    public static final String IGNITE_STOP_ON_FAILURE = "ignite.stopOnFailure";

    /** Special property that we can use to append an index to the node name for concurrency/reuse in testing. */
    public static final String IGNITE_INSTANCE_INDEX = "ignite.instance.index";

    // Deprecated property constants
    private static final String _SERVER_NAME = "serverName";
    private static final String _SERVER_ID = "serverId";
    private static final String _DEFAULT_HOSTNAME = "localhost";

    /**
     * These are bindings to magic property names and values. Their use should
     * be considered deprecated. The ServerConfigurationService exposes this kind
     * of magic property as methods, but does not distinguish between those that
     * are essential for bootstrapping or must remain fixed.
     * <p>
     * This blurry, special nature of a few properties is not reproduced on the
     * {@link ConfiguredContext}. Rather, those truly special properties
     * should be bundled together as a strongly-typed configuration that can
     * be injected. Here, we read them as undifferentiated properties, and in
     * the case of the default hostname, a literal value.
     * <p>
     * See also:
     * - {@link org.sakaiproject.modi.Environment}
     * - {@link org.sakaiproject.modi.SysProp}
     */
    private static final String[] __DEPRECATED_PROPERTY_USAGE = {_SERVER_NAME, _SERVER_ID, _DEFAULT_HOSTNAME};

    @Setter private ConfiguredContext configuredContext;
    @Setter private List<CacheConfiguration> hibernateCacheConfiguration;
    @Setter private List<CacheConfiguration> requiredCacheConfiguration;
    @Setter private List<IgniteConditionalCache> conditionalCacheConfiguration;
    @Setter private DataStorageConfiguration dataStorageConfiguration;

    @Getter @Setter private String address;
    @Getter @Setter private String home;
    @Getter @Setter private List<String> remoteAddresses;
    @Getter @Setter private int port;
    @Getter @Setter private int range;
    @Getter @Setter private String mode;
    @Getter @Setter private String name;
    @Getter @Setter private String node;

    /**
     * The instance index is used as a suffix on the instance name to allow restarting the
     * kernel within the same JVM. In normal operation, the lifetimes of the JVM, the application
     * context, and the single Ignite instance are bound together. However, with the new
     * component manager, the kernel and application context lifetimes have been separated from
     * the JVM (and/or servlet container) lifetime. This suffix allows a suite of integration
     * tests to run and generate isolated application contexts, each with a fresh kernel and
     * isolated Ignite instance by setting the index between dirtied test contexts.
     * <p>
     * If the index is 0, it will not be used; if it is >0, it will be appended to the name.
     */
    @Getter @Setter private int instanceIndex = 0;

    @Override
    public Class<?> getObjectType() {
        return IgniteConfiguration.class;
    }

    @Override
    protected IgniteConfiguration createInstance() {
        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        // FIXME: The body of this method is left indented to minimize the diff
            address = configuredContext.getString(IGNITE_ADDRESS, "127.0.0.1");
            home = configuredContext.getString(IGNITE_HOME, fallbackBaseDirectory());
            remoteAddresses = configuredContext.getStrings(IGNITE_ADDRESSES);
            port = configuredContext.getInt(IGNITE_PORT, 0);
            range = configuredContext.getInt(IGNITE_RANGE, 10);
            mode = configuredContext.getString(IGNITE_MODE, "server");
            name = configuredContext.getString(_SERVER_NAME, _DEFAULT_HOSTNAME);
            node = configuredContext.getString(_SERVER_ID, _DEFAULT_HOSTNAME);
            instanceIndex = configuredContext.getInt(IGNITE_INSTANCE_INDEX, instanceIndex);
            int tcpMessageQueueLimit = configuredContext.getInt(IGNITE_TCP_MESSAGE_QUEUE_LIMIT, 1024);
            int tcpSlowClientMessageQueueLimit = configuredContext.getInt(IGNITE_TCP_SLOW_CLIENT_MESSAGE_QUEUE_LIMIT, tcpMessageQueueLimit / 2);
            boolean stopOnFailure = configuredContext.getBoolean(IGNITE_STOP_ON_FAILURE, true);

            Map<String, Object> attributes = new HashMap<>();
            // disable banner
            System.setProperty("IGNITE_NO_ASCII", "true");
            System.setProperty("IGNITE_QUIET", "true");
            System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");

            configureName();
            configureHome();
            configurePort();

            igniteConfiguration.setIgniteHome(home);
            igniteConfiguration.setWorkDirectory(home + File.separator + "work");
            igniteConfiguration.setConsistentId(node);
            igniteConfiguration.setIgniteInstanceName(name);
            igniteConfiguration.setDeploymentMode(DeploymentMode.CONTINUOUS);

            igniteConfiguration.setGridLogger(new Slf4jLogger());

            // configuration for metrics update frequency
            igniteConfiguration.setMetricsUpdateFrequency(configuredContext.getLong(IGNITE_METRICS_UPDATE_FREQ, IgniteConfiguration.DFLT_METRICS_UPDATE_FREQ));
            igniteConfiguration.setMetricsLogFrequency(configuredContext.getLong(IGNITE_METRICS_LOG_FREQ, 0L));

            TcpCommunicationSpi tcpCommunication = new TcpCommunicationSpi();
            TcpDiscoverySpi tcpDiscovery = new TcpDiscoverySpi();
            TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();
            Set<String> discoveryAddresses = new HashSet<>();

            String localInterfaceAddress = address;

            if (StringUtils.equalsIgnoreCase("client", mode)) {
                igniteConfiguration.setClientMode(true);
            } else {
                igniteConfiguration.setClientMode(false);

                igniteConfiguration.setCollisionSpi(new FifoQueueCollisionSpi());
                igniteConfiguration.setCheckpointSpi(new CacheCheckpointSpi());

                igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);

                igniteConfiguration.setFailureDetectionTimeout(20000);
                if (stopOnFailure) {
                    IgniteStopNodeAndExitHandler failureHandler = new IgniteStopNodeAndExitHandler();
                    failureHandler.setIgnoredFailureTypes(Collections.emptySet());
                    igniteConfiguration.setFailureHandler(failureHandler);
                }

                igniteConfiguration.setSystemWorkerBlockedTimeout(20000);
                igniteConfiguration.setSegmentationPolicy(SegmentationPolicy.NOOP);
            }

            TransactionConfiguration transactionConfiguration = new TransactionConfiguration();
            transactionConfiguration.setDefaultTxConcurrency(TransactionConcurrency.OPTIMISTIC);
            transactionConfiguration.setDefaultTxIsolation(TransactionIsolation.READ_COMMITTED);
            transactionConfiguration.setDefaultTxTimeout(30 * 1000);
            igniteConfiguration.setTransactionConfiguration(transactionConfiguration);

            configureCaches(igniteConfiguration);

            // which interface tcp communication will use
            tcpCommunication.setLocalAddress(localInterfaceAddress);

            if (range - 1 == 0) {
                discoveryAddresses.add(localInterfaceAddress + ":" + (port + range));
            } else {
                discoveryAddresses.add(localInterfaceAddress + ":" + (port + range) + ".." + (port + range + range - 1));
            }

            tcpCommunication.setLocalPort(port);
            tcpCommunication.setLocalPortRange(range - 1);

            tcpDiscovery.setLocalPort(port + range);
            tcpDiscovery.setLocalPortRange(range - 1);

            // limits outbound/inbound message queue
            tcpCommunication.setMessageQueueLimit(tcpMessageQueueLimit);

            // detection of nodes with a high outbound message queue
            if (tcpSlowClientMessageQueueLimit > tcpMessageQueueLimit) {
                tcpSlowClientMessageQueueLimit = tcpMessageQueueLimit;
            }
            tcpCommunication.setSlowClientQueueLimit(tcpSlowClientMessageQueueLimit);

            // which interface discovery will use
            tcpDiscovery.setLocalAddress(localInterfaceAddress);

            // remote node network configuration, 1.2.3.5:49000..49009
            discoveryAddresses.addAll(remoteAddresses);

            attributes.put("DiscoveryAddressesSize", discoveryAddresses.size());

            finder.setAddresses(discoveryAddresses);
            tcpDiscovery.setIpFinder(finder);

            igniteConfiguration.setDiscoverySpi(tcpDiscovery);
            igniteConfiguration.setCommunicationSpi(tcpCommunication);

            igniteConfiguration.setUserAttributes(attributes);

            log.info("Ignite configured with home=[{}], node=[{}], name=[{}], client mode=[{}], tcp ports=[{}..{}], discovery ports=[{}..{}]",
                    igniteConfiguration.getIgniteHome(),
                    igniteConfiguration.getConsistentId(),
                    igniteConfiguration.getIgniteInstanceName(),
                    igniteConfiguration.isClientMode(),
                    tcpCommunication.getLocalPort(),
                    tcpCommunication.getLocalPort() + tcpCommunication.getLocalPortRange(),
                    tcpDiscovery.getLocalPort(),
                    tcpDiscovery.getLocalPort() + tcpDiscovery.getLocalPortRange());

        return igniteConfiguration;
    }

    private void configureName() {
        name = StringUtils.replaceChars(name, '.', '-');
        if (instanceIndex > 0) {
            name += "-" + instanceIndex;
        }
    }

    private void configurePort() {
        if (port < 1024) {
            port = dynamicPort((name + node).hashCode());
        }
    }

    private int dynamicPort(int number) {
        // 65535 - range
        if (number > (65535 - (range * 2))) {
            return dynamicPort(number >> 1);
        } else if (number < 49152) {
            return dynamicPort((number / 3) + number);
        }
        return number;
    }

    /**
     * When the node is named, the "Ignite home directory" is one level under the
     * configured IGNITE_HOME / ignite.home path. For example, this name will almost
     * always be the FDQN of the server and match the serverId property. The usual
     * path will be of the form:   ${sakai.home}/ignite/[machine-name]
     */
    private void configureHome() {
        var base = Path.of(home);
        var igniteHome = StringUtils.isBlank(node) ? base : base.resolve(node);

        try {
            Files.createDirectories(igniteHome);
            if (!Files.isWritable(igniteHome)) throw cannotWriteToIgniteDirectory(igniteHome);
            home = igniteHome.toAbsolutePath().toString();
        } catch (IOException e) {
            throw cannotCreateIgniteDirectory(igniteHome);
        }
    }

    /**
     * The default Ignite base directory is ${sakai.home}/ignite/
     */
    protected String fallbackBaseDirectory() {
        return sakai_home.getPathPlus("ignite")
                .map(Path::toString)
                .orElseThrow(this::cannotStartWithoutHomeDirectory);
    }

    private void configureCaches(IgniteConfiguration igniteConfiguration) {
        List<CacheConfiguration> caches = new ArrayList<>();
        caches.addAll(hibernateCacheConfiguration);
        caches.addAll(requiredCacheConfiguration);
        conditionalCacheConfiguration.stream().filter(IgniteConditionalCache::exists).map(IgniteConditionalCache::getCacheConfiguration).forEach(caches::add);
        igniteConfiguration.setCacheConfiguration(caches.toArray(new CacheConfiguration[]{}));
    }

    private IllegalStateException cannotStartWithoutHomeDirectory() {
        return new IllegalStateException("Critical error -- cannot start Ignite without either ignite.home or sakai.home set!");
    }

    private IllegalStateException cannotCreateIgniteDirectory(Path path) {
        return new IllegalStateException(
                String.format("Critical error -- cannot create Ignite home directory [%s], so cannot start. Check ignite.home and sakai.home properties.", path.toAbsolutePath()));
    }

    private IllegalStateException cannotWriteToIgniteDirectory(Path path) {
        return new IllegalStateException(
                String.format("Critical error -- cannot write to Ignite home directory [%s], so cannot start. Check ignite.home and sakai.home properties.", path.toAbsolutePath()));
    }
}
