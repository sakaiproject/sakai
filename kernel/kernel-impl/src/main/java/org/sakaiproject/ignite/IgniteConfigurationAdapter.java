package org.sakaiproject.ignite;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.BasicConfigItem;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IgniteConfigurationAdapter extends AbstractFactoryBean<IgniteConfiguration> {

    private static final IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
    private static Boolean configured = Boolean.FALSE;

    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private CacheConfiguration[] cacheConfiguration;

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
            address = serverConfigurationService.getString("ignite.address");
            home = serverConfigurationService.getString("ignite.home", serverConfigurationService.getSakaiHomePath());
            remoteAddresses = serverConfigurationService.getStrings("ignite.addresses");
            port = serverConfigurationService.getInt("ignite.port", 49000);
            range = serverConfigurationService.getInt("ignite.range", 10);
            mode = serverConfigurationService.getString("ignite.mode", "worker");
            name = StringUtils.defaultIfBlank(serverConfigurationService.getServerName(), "localhost");
            node = serverConfigurationService.getServerId();

            // disable banner
            System.setProperty("IGNITE_NO_ASCII", "true");
            System.setProperty("IGNITE_QUIET", "true");
            System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true");

            igniteConfiguration.setIgniteHome(configureHome(home, node));
            igniteConfiguration.setConsistentId(node);
            igniteConfiguration.setIgniteInstanceName(name);

            if (StringUtils.equalsIgnoreCase("client", mode)) {
                igniteConfiguration.setClientMode(true);
            } else {
                igniteConfiguration.setClientMode(false);
            }

            igniteConfiguration.setGridLogger(new Slf4jLogger());

            igniteConfiguration.setCacheConfiguration(cacheConfiguration);

            // local node network configuration
            TcpCommunicationSpi tcpCommunication = new TcpCommunicationSpi();
            if (StringUtils.isNotBlank(address)) tcpCommunication.setLocalAddress(address);
            tcpCommunication.setLocalPort(port);
            tcpCommunication.setLocalPortRange(range);
            igniteConfiguration.setCommunicationSpi(tcpCommunication);

            // remote node network configuration, 1.2.3.5:49000..49009
            if (remoteAddresses != null) {
                TcpDiscoverySpi discovery = new TcpDiscoverySpi();
                TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();
                finder.setAddresses(Arrays.asList(remoteAddresses));
                discovery.setIpFinder(finder);
                igniteConfiguration.setDiscoverySpi(discovery);
            }

            log.info("Ignite configured with home=[{}], node=[{}], name=[{}], client mode=[{}]",
                    igniteConfiguration.getIgniteHome(),
                    igniteConfiguration.getConsistentId(),
                    igniteConfiguration.getIgniteInstanceName(),
                    igniteConfiguration.isClientMode());

            configured = Boolean.TRUE;
        }
        return igniteConfiguration;
    }

    private String configureHome(String path, String node) {
        if (StringUtils.isBlank(path)) {
            path = serverConfigurationService.getSakaiHomePath();
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
        return igniteHome.getAbsolutePath();
    }
}
