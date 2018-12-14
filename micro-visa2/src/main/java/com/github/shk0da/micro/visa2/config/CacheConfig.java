package com.github.shk0da.micro.visa2.config;

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
                .addMapConfig(new MapConfig().setName("receiverCache").setTimeToLiveSeconds((int) TimeUnit.MINUTES.toSeconds(1)));
        this.receiverCache = hazelcastInstance.getMap("receiverCache");
    }

    public Object getFromReceiverCache(String messageId) {
        return receiverCache.get(messageId);
    }

    public void setToReceiverCache(String messageId, Object object) {
        receiverCache.set(messageId, object);
    }
}
