
package com.github.shk0da.micro.main.config.hazelcast;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.NoLogFactory;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.hazelcast.spi.discovery.multicast.MulticastDiscoveryStrategy;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

final class EurekaOneDiscoveryStrategy extends MulticastDiscoveryStrategy {

    static final class EurekaOneDiscoveryStrategyBuilder {
        private String applicationName;
        private Integer hazelcastPort;
        private EurekaClient eurekaClient;
        private DiscoveryNode discoveryNode;
        private ILogger logger = new NoLogFactory().getLogger(EurekaOneDiscoveryStrategy.class.getName());
        private Map<String, Comparable> properties = Collections.emptyMap();

        EurekaOneDiscoveryStrategyBuilder setApplicationName(final String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        EurekaOneDiscoveryStrategyBuilder setHazelcastPort(final Integer hazelcastPort) {
            this.hazelcastPort = hazelcastPort;
            return this;
        }

        EurekaOneDiscoveryStrategyBuilder setEurekaClient(final EurekaClient eurekaClient) {
            this.eurekaClient = eurekaClient;
            return this;
        }

        EurekaOneDiscoveryStrategyBuilder setDiscoveryNode(final DiscoveryNode discoveryNode) {
            this.discoveryNode = discoveryNode;
            return this;
        }

        EurekaOneDiscoveryStrategyBuilder setILogger(final ILogger logger) {
            this.logger = logger;
            return this;
        }

        EurekaOneDiscoveryStrategyBuilder setProperties(final Map<String, Comparable> properties) {
            this.properties = properties;
            return this;
        }

        EurekaOneDiscoveryStrategy build() {
            return new EurekaOneDiscoveryStrategy(this);
        }
    }

    private static final int NUM_RETRIES = 5;
    private static final int VERIFICATION_WAIT_TIMEOUT = 5;
    private static final int DISCOVERY_RETRY_TIMEOUT = 1;

    private final String applicationName;
    private final Integer hazelcastPort;
    private final EurekaClient eurekaClient;

    private EurekaOneDiscoveryStrategy(final EurekaOneDiscoveryStrategyBuilder builder) {
        super(builder.discoveryNode, builder.logger, builder.properties);
        this.applicationName = builder.applicationName;
        this.hazelcastPort = builder.hazelcastPort;
        this.eurekaClient = builder.eurekaClient;
    }

    @Override
    public void start() {
        eurekaClient.getApplicationInfoManager().setInstanceStatus(InstanceInfo.InstanceStatus.UP);
        Application application;
        do {
            getLogger().info("Waiting for registration with Eureka...");
            application = eurekaClient.getApplication(applicationName);
            if (application != null) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(VERIFICATION_WAIT_TIMEOUT);
            } catch (InterruptedException almostIgnore) {
                Thread.currentThread().interrupt();
            }
        } while (true);
    }

    @Override
    public void destroy() {
        if (null != eurekaClient) {
            eurekaClient.getApplicationInfoManager().setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
            eurekaClient.shutdown();
        }
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        Application application = null;
        for (int i = 0; i < NUM_RETRIES; i++) {
            application = eurekaClient.getApplication(applicationName);
            if (application != null) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(DISCOVERY_RETRY_TIMEOUT);
            } catch (InterruptedException almostIgnore) {
                Thread.currentThread().interrupt();
            }
        }

        return getDiscoveryNodes(application);
    }

    private List<DiscoveryNode> getDiscoveryNodes(Application application) {
        List<DiscoveryNode> nodes = new ArrayList<>();
        if (application == null) return nodes;

        String currentMember = getCurrentMember();
        application.getInstances().stream()
                .filter(instanceInfo -> instanceInfo.getStatus() == InstanceInfo.InstanceStatus.UP)
                .forEach(instanceInfo -> {
                    Integer port = hazelcastPort;
                    try {
                        String memberHazelcastPort = instanceInfo.getMetadata().get("hazelcastPort");
                        if (memberHazelcastPort != null && !memberHazelcastPort.isEmpty() && !"0".equals(memberHazelcastPort)) {
                            port = Integer.valueOf(memberHazelcastPort);
                        }
                    } catch (NumberFormatException e) {
                        getLogger().warning("Port could not be determined: " + e.getMessage());
                    }

                    if (!currentMember.equals(instanceInfo.getIPAddr() + ":" + port)) {
                        try {
                            InetAddress address = InetAddress.getByName(instanceInfo.getIPAddr());
                            nodes.add(new SimpleDiscoveryNode(
                                    new Address(address, port), (Map) instanceInfo.getMetadata()
                            ));
                        } catch (UnknownHostException e) {
                            getLogger().warning("InstanceInfo '" + instanceInfo + "' could not be resolved");
                        }
                    }
                });

        return nodes;
    }

    private String getCurrentMember() {
        String currentIPAddress = "localhost";
        try {
            currentIPAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            getLogger().warning("Сould not find current ip address: " + e.getMessage());
        }
        return currentIPAddress + ":" + hazelcastPort;
    }
}
