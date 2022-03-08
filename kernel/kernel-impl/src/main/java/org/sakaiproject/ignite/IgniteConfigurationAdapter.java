package org.sakaiproject.ignite;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.ignite.failure.FailureHandler;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.plugin.segmentation.SegmentationPolicy;
import org.apache.ignite.spi.checkpoint.cache.CacheCheckpointSpi;
import org.apache.ignite.spi.collision.fifoqueue.FifoQueueCollisionSpi;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.sakaiproject.component.api.ServerConfigurationService;
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

    private static final IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
    private static Boolean configured = Boolean.FALSE;

    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private List<CacheConfiguration> hibernateCacheConfiguration;
    @Setter private List<CacheConfiguration> requiredCacheConfiguration;
    @Setter private List<IgniteConditionalCache> conditionalCacheConfiguration;
    @Setter private DataStorageConfiguration dataStorageConfiguration;

    @Getter @Setter private String address;
    @Getter @Setter private String home;
    @Getter @Setter private String[] remoteAddresses;
    @Getter @Setter private int port;
    @Getter @Setter private int range;
    @Getter @Setter private String mode;
    @Getter @Setter private String name;
    @Getter @Setter private String node;

    @Override
    public Class<?> getObjectType() {
        return IgniteConfiguration.class;
    }

    @Override
    protected IgniteConfiguration createInstance() {
        if (!configured) {
            address = serverConfigurationService.getString(IGNITE_ADDRESS);
            home = serverConfigurationService.getString(IGNITE_HOME);
            remoteAddresses = serverConfigurationService.getStrings(IGNITE_ADDRESSES);
            port = serverConfigurationService.getInt(IGNITE_PORT, 0);
            range = serverConfigurationService.getInt(IGNITE_RANGE, 10);
            mode = serverConfigurationService.getString(IGNITE_MODE, "server");
            name = serverConfigurationService.getServerName();
            node = serverConfigurationService.getServerId();
            int tcpMessageQueueLimit = serverConfigurationService.getInt(IGNITE_TCP_MESSAGE_QUEUE_LIMIT, 1024);
            int tcpSlowClientMessageQueueLimit = serverConfigurationService.getInt(IGNITE_TCP_SLOW_CLIENT_MESSAGE_QUEUE_LIMIT, tcpMessageQueueLimit / 2);
            boolean stopOnFailure = serverConfigurationService.getBoolean(IGNITE_STOP_ON_FAILURE, true);

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

            if (StringUtils.equalsIgnoreCase("client", mode)) {
                igniteConfiguration.setClientMode(true);
            } else {
                igniteConfiguration.setClientMode(false);
            }

            igniteConfiguration.setCollisionSpi(new FifoQueueCollisionSpi());
            igniteConfiguration.setCheckpointSpi(new CacheCheckpointSpi());

            TransactionConfiguration transactionConfiguration = new TransactionConfiguration();
            transactionConfiguration.setDefaultTxConcurrency(TransactionConcurrency.OPTIMISTIC);
            transactionConfiguration.setDefaultTxIsolation(TransactionIsolation.READ_COMMITTED);
            transactionConfiguration.setDefaultTxTimeout(30 * 1000);
            igniteConfiguration.setTransactionConfiguration(transactionConfiguration);

            igniteConfiguration.setDeploymentMode(DeploymentMode.CONTINUOUS);

            igniteConfiguration.setGridLogger(new Slf4jLogger());

            configureCaches();

            igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);

            // configuration for metrics update frequency
            igniteConfiguration.setMetricsUpdateFrequency(serverConfigurationService.getLong(IGNITE_METRICS_UPDATE_FREQ, IgniteConfiguration.DFLT_METRICS_UPDATE_FREQ));
            igniteConfiguration.setMetricsLogFrequency(serverConfigurationService.getLong(IGNITE_METRICS_LOG_FREQ, 0L));

            igniteConfiguration.setFailureDetectionTimeout(20000);
            if (stopOnFailure) {
                IgniteStopNodeAndExitHandler failureHandler = new IgniteStopNodeAndExitHandler();
                failureHandler.setIgnoredFailureTypes(Collections.emptySet());
                igniteConfiguration.setFailureHandler(failureHandler);
            }

            igniteConfiguration.setSystemWorkerBlockedTimeout(20000);
            igniteConfiguration.setSegmentationPolicy(SegmentationPolicy.NOOP);

            // local node network configuration
            TcpCommunicationSpi tcpCommunication = new TcpCommunicationSpi();
            TcpDiscoverySpi tcpDiscovery = new TcpDiscoverySpi();
            TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();

            // limits outbound/inbound message queue
            tcpCommunication.setMessageQueueLimit(tcpMessageQueueLimit);

            if (tcpSlowClientMessageQueueLimit > tcpMessageQueueLimit) {
                tcpSlowClientMessageQueueLimit = tcpMessageQueueLimit;
            }
            // detection of a nodes with a high outbound message queue
            tcpCommunication.setSlowClientQueueLimit(tcpSlowClientMessageQueueLimit);

            Set<String> discoveryAddresses = new HashSet<>();
            String localDiscoveryAddress;
            if (StringUtils.isNotBlank(address)) {
                tcpCommunication.setLocalAddress(address);
                tcpDiscovery.setLocalAddress(address);
                localDiscoveryAddress = address;
            } else {
                localDiscoveryAddress = "127.0.0.1";
            }

            if (range - 1 == 0) {
                discoveryAddresses.add(localDiscoveryAddress + ":" + (port + range));
            } else {
                discoveryAddresses.add(localDiscoveryAddress + ":" + (port + range) + ".." + (port + range + range - 1));
            }

            tcpCommunication.setLocalPort(port);
            tcpCommunication.setLocalPortRange(range - 1);

            tcpDiscovery.setLocalPort(port + range);
            tcpDiscovery.setLocalPortRange(range - 1);

            // remote node network configuration, 1.2.3.5:49000..49009
            if (remoteAddresses != null && remoteAddresses.length > 0) {
                discoveryAddresses.addAll(Arrays.asList(remoteAddresses));
            }

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

            configured = Boolean.TRUE;
        }
        return igniteConfiguration;
    }

    private void configureName() {
        name = StringUtils.replaceChars(name, '.', '-');
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

    private void configureHome() {
        String path = home;
        if (StringUtils.isBlank(path)) {
            // if the root path is not specified use sakai.home/ignite/
            path = serverConfigurationService.getSakaiHomePath();
            path = path + File.separator + "ignite";
        }

        if (StringUtils.isNotBlank(node)) {
            if (!StringUtils.endsWith(path, File.separator)) {
                path = path + File.separator;
            }
            path = path + node;
        }

        File igniteHome = new File(path);
        if (!igniteHome.exists()) igniteHome.mkdirs();

        // return the absolute path
        home = igniteHome.getAbsolutePath();
    }

    private void configureCaches() {
        List<CacheConfiguration> caches = new ArrayList<>();
        caches.addAll(hibernateCacheConfiguration);
        caches.addAll(requiredCacheConfiguration);
        conditionalCacheConfiguration.stream().filter(IgniteConditionalCache::exists).map(IgniteConditionalCache::getCacheConfiguration).forEach(caches::add);
        igniteConfiguration.setCacheConfiguration(caches.toArray(new CacheConfiguration[]{}));
    }
}
