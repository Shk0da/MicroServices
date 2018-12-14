package com.github.shk0da.micro.main.config;

import com.github.shk0da.micro.main.config.hazelcast.EurekaOneDiscoveryStrategyFactory;
import com.google.common.collect.Lists;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Properties;

@Configuration
@ConditionalOnProperty(value = "hazelcast.enable", havingValue = "true", matchIfMissing = true)
public class HazelcastConfig {

    private final static Logger log = LoggerFactory.getLogger(HazelcastConfig.class);

    @Bean
    public HazelcastInstance hazelcastInstance(@Value("${spring.application.name}") String applicationName,
                                               @Value("${hazelcast.port}") Integer hazelcastPort,
                                               EurekaClient eurekaClient) {
        Properties properties = new Properties();
        List<DiscoveryStrategyConfig> discoveryStrategyConfigs = Lists.newArrayList();
        if (eurekaClient == null) {
            log.warn("EurekaClient is null. Hazelcast cluster disabled!");
        } else {
            properties.put("hazelcast.discovery.enabled", "true");
            discoveryStrategyConfigs.add(new DiscoveryStrategyConfig(
                    new EurekaOneDiscoveryStrategyFactory(applicationName, hazelcastPort, eurekaClient))
            );
            log.debug("Hazelcast discovery strategies: {}", discoveryStrategyConfigs);
        }

        Config config = new Config();
        config.setProperties(properties);
        config.setInstanceName(applicationName);
        NetworkConfig networkConfig = new NetworkConfig();
        JoinConfig joinConfig = new JoinConfig();
        MulticastConfig multicastConfig = new MulticastConfig();
        multicastConfig.setEnabled(false);
        joinConfig.setMulticastConfig(multicastConfig);
        AwsConfig awsConfig = new AwsConfig();
        awsConfig.setEnabled(false);
        joinConfig.setAwsConfig(awsConfig);
        TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.setEnabled(true);
        joinConfig.setTcpIpConfig(tcpIpConfig);
        DiscoveryConfig discoveryConfig = new DiscoveryConfig();
        discoveryConfig.setDiscoveryStrategyConfigs(discoveryStrategyConfigs);
        joinConfig.setDiscoveryConfig(discoveryConfig);
        networkConfig.setJoin(joinConfig);
        networkConfig.setPort(hazelcastPort);
        config.setNetworkConfig(networkConfig);

        return Hazelcast.newHazelcastInstance(config);
    }
}
