package com.github.shk0da.micro.orchestrator.config;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    private final IMap<String, Object> receiverCache;

    public CacheConfig(HazelcastInstance hazelcastInstance) {
        hazelcastInstance.getConfig()
                .addMapConfig(new MapConfig().setName("receiverCache").setTimeToLiveSeconds((int) TimeUnit.MINUTES.toSeconds(5)));
        this.receiverCache = hazelcastInstance.getMap("receiverCache");
    }

    public Object getFromReceiverCache(String id) {
        return receiverCache.get(id);
    }

    public Object getFromReceiverCache(String messageId, String serviceName) {
        return getFromReceiverCache(messageId + "_" + serviceName);
    }

    public void setToReceiverCache(String id, Object object) {
        receiverCache.set(id, object);
    }

    public void setToReceiverCache(String messageId, String serviceName, Object object) {
        setToReceiverCache(messageId + "_" + serviceName, object);
    }
}
