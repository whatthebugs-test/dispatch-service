package io.kestrelfreight.dispatch.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestrelfreight.dispatch.service.DispatchService;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(ShipmentEventListener.class);

    private final ObjectMapper objectMapper;
    private final DispatchService dispatchService;
    private final MeterRegistry meterRegistry;

    public ShipmentEventListener(ObjectMapper objectMapper, DispatchService dispatchService, MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;
        this.dispatchService = dispatchService;
        this.meterRegistry = meterRegistry;
    }

    @KafkaListener(topics = "${dispatch.kafka.shipment-events-topic}", groupId = "dispatch-service")
    public void onMessage(String payload) {
        ShipmentEvent event;
        try {
            event = objectMapper.readValue(payload, ShipmentEvent.class);
        } catch (JsonProcessingException e) {
            // Poison messages are counted and skipped; the topic is replayable from shipment-api's outbox.
            meterRegistry.counter("dispatch.shipment_events.malformed").increment();
            log.warn("skipping malformed shipment event: {}", e.getOriginalMessage());
            return;
        }

        switch (event.event()) {
            case ShipmentEvent.TENDERED -> dispatchService.onTendered(event);
            case ShipmentEvent.CANCELLED -> dispatchService.onCancelled(event.shipmentId());
            default -> log.debug("ignoring shipment event {} for {}", event.event(), event.shipmentId());
        }
        meterRegistry.counter("dispatch.shipment_events.processed", "event", event.event()).increment();
    }
}
