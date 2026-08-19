package io.kestrelfreight.dispatch.web;

import io.kestrelfreight.dispatch.service.DispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Low-latency nudge from shipment-api's Celery worker. Only cancellations act
 * immediately; tenders carry too little here and arrive in full on Kafka.
 */
@RestController
public class ShipmentEventController {

    private final DispatchService dispatchService;

    public ShipmentEventController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/internal/shipment-events")
    public ResponseEntity<Void> nudge(@RequestBody Nudge body) {
        if ("cancelled".equals(body.event())) {
            dispatchService.onCancelled(body.shipment_id());
        }
        return ResponseEntity.accepted().build();
    }

    public record Nudge(String shipment_id, String event) {}
}
