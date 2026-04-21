package io.kestrelfreight.dispatch.service;

import io.kestrelfreight.dispatch.domain.Assignment;
import io.kestrelfreight.dispatch.domain.AssignmentStatus;
import io.kestrelfreight.dispatch.domain.Driver;
import io.kestrelfreight.dispatch.kafka.ShipmentEvent;
import io.kestrelfreight.dispatch.repo.AssignmentRepository;
import io.kestrelfreight.dispatch.repo.DriverRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);
    private static final EnumSet<AssignmentStatus> OPEN =
            EnumSet.of(AssignmentStatus.UNASSIGNED, AssignmentStatus.OFFERED, AssignmentStatus.ACCEPTED);

    private final AssignmentRepository assignments;
    private final DriverRepository drivers;
    private final Clock clock;

    public DispatchService(AssignmentRepository assignments, DriverRepository drivers, Clock clock) {
        this.assignments = assignments;
        this.drivers = drivers;
        this.clock = clock;
    }

    @Transactional
    public void onTendered(ShipmentEvent event) {
        if (assignments.existsById(event.shipmentId())) {
            return; // at-least-once delivery from Kafka and the HTTP nudge from shipment-api
        }
        Assignment assignment = assignments.save(new Assignment(
                event.shipmentId(), event.proNumber(), event.originTerminal(), event.pickupDate(),
                event.pickupLat(), event.pickupLng(), event.weightLbs(), event.hazmat()));
        offerToNearestDriver(assignment);
    }

    @Transactional
    public void onCancelled(String shipmentId) {
        assignments.findById(shipmentId).ifPresent(Assignment::cancel);
    }

    @Transactional
    public Assignment accept(String shipmentId, String driverId) {
        Assignment assignment = assignments.findById(shipmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(shipmentId));
        assignment.accept(driverId, clock.instant());
        return assignment;
    }

    @Transactional(readOnly = true)
    public List<Assignment> board(String terminal, LocalDate date) {
        return assignments.findByTerminalAndPickupDateAndStatusInOrderByProNumber(terminal, date, OPEN);
    }

    Optional<Driver> offerToNearestDriver(Assignment assignment) {
        Optional<Driver> nearest = drivers.findByTerminalAndOnDutyTrue(assignment.getTerminal()).stream()
                .filter(d -> d.canHaul(assignment))
                .filter(d -> d.getLastLat() != null && d.getLastLng() != null)
                .min(Comparator.comparingDouble(d -> Geo.haversineMiles(
                        d.getLastLat(), d.getLastLng(), assignment.getPickupLat(), assignment.getPickupLng())));

        nearest.ifPresentOrElse(
                d -> assignment.offerTo(d, clock.instant()),
                () -> log.info("no eligible driver for {} at terminal {}", assignment.getProNumber(), assignment.getTerminal()));
        return nearest;
    }
}
