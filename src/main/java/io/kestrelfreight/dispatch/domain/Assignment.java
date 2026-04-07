package io.kestrelfreight.dispatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

/** A pickup that dispatch owns: one per tendered shipment, offered to drivers out of the origin terminal. */
@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    private String shipmentId;

    @Column(nullable = false, length = 12)
    private String proNumber;

    @Column(nullable = false, length = 3)
    private String terminal;

    @Column(nullable = false)
    private LocalDate pickupDate;

    private double pickupLat;
    private double pickupLng;

    @Column(nullable = false)
    private double weightLbs;

    private boolean hazmat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AssignmentStatus status = AssignmentStatus.UNASSIGNED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    private Instant offeredAt;
    private Instant acceptedAt;

    @Version
    private long version;

    protected Assignment() {}

    public Assignment(String shipmentId, String proNumber, String terminal, LocalDate pickupDate,
                      double pickupLat, double pickupLng, double weightLbs, boolean hazmat) {
        this.shipmentId = shipmentId;
        this.proNumber = proNumber;
        this.terminal = terminal;
        this.pickupDate = pickupDate;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.weightLbs = weightLbs;
        this.hazmat = hazmat;
    }

    public void offerTo(Driver driver, Instant now) {
        if (status != AssignmentStatus.UNASSIGNED && status != AssignmentStatus.OFFERED) {
            throw new IllegalStateException("cannot offer assignment in status " + status);
        }
        this.driver = driver;
        this.status = AssignmentStatus.OFFERED;
        this.offeredAt = now;
    }

    public void accept(String driverId, Instant now) {
        if (status != AssignmentStatus.OFFERED || driver == null || !driver.getId().equals(driverId)) {
            throw new IllegalStateException("assignment " + shipmentId + " is not offered to " + driverId);
        }
        this.status = AssignmentStatus.ACCEPTED;
        this.acceptedAt = now;
    }

    public void cancel() {
        this.status = AssignmentStatus.CANCELLED;
        this.driver = null;
    }

    public String getShipmentId() { return shipmentId; }
    public String getProNumber() { return proNumber; }
    public String getTerminal() { return terminal; }
    public LocalDate getPickupDate() { return pickupDate; }
    public double getPickupLat() { return pickupLat; }
    public double getPickupLng() { return pickupLng; }
    public double getWeightLbs() { return weightLbs; }
    public boolean isHazmat() { return hazmat; }
    public AssignmentStatus getStatus() { return status; }
    public Driver getDriver() { return driver; }
    public Instant getOfferedAt() { return offeredAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
}
