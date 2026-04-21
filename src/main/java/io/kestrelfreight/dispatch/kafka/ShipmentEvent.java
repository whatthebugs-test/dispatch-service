package io.kestrelfreight.dispatch.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/** Payload on {@code shipment-events.v1}, published by shipment-api. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ShipmentEvent(
        @JsonProperty("event") String event,
        @JsonProperty("shipment_id") String shipmentId,
        @JsonProperty("pro_number") String proNumber,
        @JsonProperty("origin_terminal") String originTerminal,
        @JsonProperty("pickup_date") LocalDate pickupDate,
        @JsonProperty("pickup_lat") double pickupLat,
        @JsonProperty("pickup_lng") double pickupLng,
        @JsonProperty("weight_lbs") double weightLbs,
        @JsonProperty("hazmat") boolean hazmat) {

    public static final String TENDERED = "tendered";
    public static final String CANCELLED = "cancelled";
}
