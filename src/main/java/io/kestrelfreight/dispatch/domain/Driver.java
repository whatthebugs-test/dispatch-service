package io.kestrelfreight.dispatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 3)
    private String terminal;

    private boolean hazmatEndorsed;
    private boolean onDuty;

    /** Remaining trailer capacity reported by the ELD integration. */
    private double availableCapacityLbs;

    private Double lastLat;
    private Double lastLng;
    private Instant lastPingAt;

    protected Driver() {}

    public Driver(String id, String name, String terminal, boolean hazmatEndorsed, double availableCapacityLbs) {
        this.id = id;
        this.name = name;
        this.terminal = terminal;
        this.hazmatEndorsed = hazmatEndorsed;
        this.availableCapacityLbs = availableCapacityLbs;
        this.onDuty = true;
    }

    public boolean canHaul(Assignment assignment) {
        return onDuty
                && availableCapacityLbs >= assignment.getWeightLbs()
                && (!assignment.isHazmat() || hazmatEndorsed);
    }

    public void ping(double lat, double lng, Instant at) {
        this.lastLat = lat;
        this.lastLng = lng;
        this.lastPingAt = at;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getTerminal() { return terminal; }
    public boolean isHazmatEndorsed() { return hazmatEndorsed; }
    public boolean isOnDuty() { return onDuty; }
    public double getAvailableCapacityLbs() { return availableCapacityLbs; }
    public Double getLastLat() { return lastLat; }
    public Double getLastLng() { return lastLng; }
    public Instant getLastPingAt() { return lastPingAt; }
}
