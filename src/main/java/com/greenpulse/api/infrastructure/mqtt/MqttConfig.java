package com.greenpulse.api.infrastructure.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenpulse.api.features.telemetry.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MqttConfig {

    private final TelemetryService telemetryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                "tcp://broker.emqx.io:1883", "greenpulse-backend-client", "elroi/farms/+/telemetry");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
            String payload = message.getPayload().toString();
            log.info("MQTT Ingress on {}: {}", topic, payload);

            try {
                JsonNode json = objectMapper.readTree(payload);
                String deviceId = topic.split("/")[2];
                Double temp = json.get("temperature").asDouble();
                Double hum = json.get("humidity").asDouble();
                Double soil = json.get("soilMoisture").asDouble();

                telemetryService.processTelemetry(deviceId, temp, hum, soil);
            } catch (Exception e) {
                log.error("Failed to parse telemetry: {}", e.getMessage());
            }
        };
    }
}
