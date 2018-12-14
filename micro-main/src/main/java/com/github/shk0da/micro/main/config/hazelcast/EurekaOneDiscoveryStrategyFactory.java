package com.github.shk0da.micro.main.config.hazelcast;

import com.google.common.collect.Lists;
import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;
import com.netflix.discovery.EurekaClient;

import java.util.Collection;
import java.util.Map;

public class EurekaOneDiscoveryStrategyFactory implements DiscoveryStrategyFactory {

    private final String applicationName;
    private final Integer hazelcastPort;
    private final EurekaClient eurekaClient;

    public EurekaOneDiscoveryStrategyFactory(String applicationName, Integer hazelcastPort, EurekaClient eurekaClient) {
        this.applicationName = applicationName;
        this.hazelcastPort = hazelcastPort;
        this.eurekaClient = eurekaClient;
    }

    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return EurekaOneDiscoveryStrategy.class;
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode, ILogger logger, Map<String, Comparable> properties) {
        return new EurekaOneDiscoveryStrategy.EurekaOneDiscoveryStrategyBuilder()
                .setDiscoveryNode(discoveryNode)
                .setILogger(logger)
                .setProperties(properties)
                .setApplicationName(applicationName)
                .setHazelcastPort(hazelcastPort)
                .setEurekaClient(eurekaClient)
                .build();
    }

    @Override
    public Collection<PropertyDefinition> getConfigurationProperties() {
        return Lists.newArrayList();
    }
}
