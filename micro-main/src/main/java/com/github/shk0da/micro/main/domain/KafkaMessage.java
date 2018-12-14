package com.github.shk0da.micro.main.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public class KafkaMessage<T> {

    private UUID id;
    private KafkaMessageService service;
    private KafkaMessageType type;
    private T message;

    public KafkaMessage() {
        this.id = randomUUID();
        this.service = new KafkaMessageService();
    }

    public KafkaMessage(KafkaMessageType type, T message) {
        this();
        this.type = type;
        this.message = message;
    }

    public KafkaMessage(String id, KafkaMessageService service, KafkaMessageType type, T message) {
        this.id = UUID.fromString(id);
        this.service = service;
        this.type = type;
        this.message = message;
    }

    public String getId() {
        return id.toString();
    }

    public KafkaMessageService getService() {
        return service;
    }

    public KafkaMessageType getType() {
        return type != null ? type : KafkaMessageType.NOT_DEFINED;
    }

    public void setType(KafkaMessageType type) {
        this.type = type;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "KafkaMessage{" +
                "id='" + id + '\'' +
                ", service='" + service + '\'' +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    @SuppressWarnings("unchecked")
    public static KafkaMessage.Builder fromMessage(Object message) {
        return new KafkaMessage.Builder().message(message);
    }

    public static class Builder<T> {

        private KafkaMessage kafkaMessage;

        public Builder() {
            kafkaMessage = new KafkaMessage();
        }

        public Builder id(String id) {
            kafkaMessage.id = UUID.fromString(id);
            return this;
        }

        public Builder service(KafkaMessageService service) {
            kafkaMessage.service = service;
            return this;
        }

        public Builder type(KafkaMessageType type) {
            kafkaMessage.type = type;
            return this;
        }

        public Builder message(T message) {
            kafkaMessage.message = message;
            if (kafkaMessage.type == null) {
                kafkaMessage.type = KafkaMessageType.of(message.getClass());
            }

            return this;
        }

        public KafkaMessage build() {
            return kafkaMessage;
        }
    }

    public static class HandlingTools {
        private static final Gson messageMapper = new GsonBuilder()
                .registerTypeAdapter(
                        Date.class,
                        (JsonDeserializer) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong())
                )
                .create();

        public static ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new SimpleModule().addDeserializer(KafkaMessage.class, new StdDeserializer<KafkaMessage>(KafkaMessage.class) {
                @Override
                public KafkaMessage deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
                    JsonNode node = parser.getCodec().readTree(parser);
                    String id = node.has("id") ? node.get("id").asText() : null;
                    KafkaMessageService service = node.has("service")
                            ? objectMapper.readValue(node.get("service").traverse(), KafkaMessageService.class)
                            : null;
                    KafkaMessageType messageType = node.has("type")
                            ? objectMapper.readValue(node.get("type").traverse(), KafkaMessageType.class)
                            : KafkaMessageType.NOT_DEFINED;
                    @SuppressWarnings("unchecked")
                    Object message = node.has("message")
                            ? messageMapper
                            .fromJson(
                                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node.findValue("message")),
                                    messageType.getMessageClass()
                            )
                            : null;
                    return new KafkaMessage<>(id, service, messageType, message);
                }
            }));

            return objectMapper;
        }
    }
}
