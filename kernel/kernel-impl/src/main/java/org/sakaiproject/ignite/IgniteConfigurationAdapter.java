package org.sakaiproject.ignite;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.DeploymentMode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
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

    private static final IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
    private static Boolean configured = Boolean.FALSE;

    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private CacheConfiguration[] cacheConfiguration;
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

            // disable banner
            System.setProperty("IGNITE_NO_ASCII", "true");
            System.setProperty("IGNITE_QUIET", "true");
            System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");

            configureName();
            configureHome();
            configurePort();

            igniteConfiguration.setIgniteHome(home);
            igniteConfiguration.setConsistentId(node);
            igniteConfiguration.setIgniteInstanceName(name);

            if (StringUtils.equalsIgnoreCase("client", mode)) {
                igniteConfiguration.setClientMode(true);
            } else {
                igniteConfiguration.setClientMode(false);
            }

            igniteConfiguration.setDeploymentMode(DeploymentMode.CONTINUOUS);

            igniteConfiguration.setGridLogger(new Slf4jLogger());

            igniteConfiguration.setCacheConfiguration(cacheConfiguration);

            igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);

            //User configuration for metrics update freqency
            igniteConfiguration.setMetricsUpdateFrequency(serverConfigurationService.getLong(IGNITE_METRICS_UPDATE_FREQ, IgniteConfiguration.DFLT_METRICS_UPDATE_FREQ));

            igniteConfiguration.setMetricsLogFrequency(serverConfigurationService.getLong(IGNITE_METRICS_LOG_FREQ, 0L));


            // local node network configuration
            TcpCommunicationSpi tcpCommunication = new TcpCommunicationSpi();
            TcpDiscoverySpi tcpDiscovery = new TcpDiscoverySpi();
            TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();

            List<String> discoveryAddresses = new ArrayList<>();
            if (StringUtils.isNotBlank(address)) {
                tcpCommunication.setLocalAddress(address);
                tcpDiscovery.setLocalAddress(address);
                discoveryAddresses.add(address + ":" + (port + range) + ".." + (port + range + range - 1));
            } else {
                discoveryAddresses.add("127.0.0.1:" + (port + range) + ".." + (port + range + range - 1));
            }

            tcpCommunication.setLocalPort(port);
            tcpCommunication.setLocalPortRange(range - 1);

            tcpDiscovery.setLocalPort(port + range);
            tcpDiscovery.setLocalPortRange(range - 1);

            // remote node network configuration, 1.2.3.5:49000..49009
            if (remoteAddresses != null && remoteAddresses.length > 0) {
                discoveryAddresses.addAll(Arrays.asList(remoteAddresses));
            }

            finder.setAddresses(discoveryAddresses);
            tcpDiscovery.setIpFinder(finder);

            igniteConfiguration.setDiscoverySpi(tcpDiscovery);
            igniteConfiguration.setCommunicationSpi(tcpCommunication);

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
}
