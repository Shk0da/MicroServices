package com.github.shk0da.micro.main.domain;

import java.io.Serializable;

import static com.github.shk0da.micro.main.provider.ApplicationContextProvider.*;

public class KafkaMessageService implements Serializable {

    private String name;
    private String topicIn;
    private String topicOut;

    public KafkaMessageService() {
        this.name = getServiceName();
        this.topicIn = getServiceTopicIn();
        this.topicOut = getServiceTopicOut();
    }

    public KafkaMessageService(String name) {
        this.name = name;
    }

    public KafkaMessageService(String name, String topicIn, String topicOut) {
        this.name = name;
        this.topicIn = topicIn;
        this.topicOut = topicOut;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopicIn() {
        return topicIn;
    }

    public void setTopicIn(String topicIn) {
        this.topicIn = topicIn;
    }

    public String getTopicOut() {
        return topicOut;
    }

    public void setTopicOut(String topicOut) {
        this.topicOut = topicOut;
    }

    @Override
    public String toString() {
        return "KafkaMessageService{" +
                "name='" + name + '\'' +
                ", topicIn='" + topicIn + '\'' +
                ", topicOut='" + topicOut + '\'' +
                '}';
    }
}
