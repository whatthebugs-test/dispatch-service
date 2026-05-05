package io.kestrelfreight.dispatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kestrelfreight.dispatch.domain.Assignment;
import io.kestrelfreight.dispatch.domain.AssignmentStatus;
import io.kestrelfreight.dispatch.domain.Driver;
import io.kestrelfreight.dispatch.kafka.ShipmentEvent;
import io.kestrelfreight.dispatch.repo.AssignmentRepository;
import io.kestrelfreight.dispatch.repo.DriverRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-18T13:30:00Z");

    @Mock AssignmentRepository assignments;
    @Mock DriverRepository drivers;

    DispatchService service;

    @BeforeEach
    void setUp() {
        service = new DispatchService(assignments, drivers, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    // Memphis terminal 482; pickup near the airport.
    private Assignment assignment(double weight, boolean hazmat) {
        return new Assignment("shp_01JD3K8W1V", "4820019375", "482", LocalDate.of(2026, 11, 18),
                35.0421, -89.9767, weight, hazmat);
    }

    private Driver driver(String id, double lat, double lng, boolean hazmat, double capacity) {
        Driver d = new Driver(id, id, "482", hazmat, capacity);
        d.ping(lat, lng, NOW.minusSeconds(60));
        return d;
    }

    @Test
    void offersToNearestEligibleDriver() {
        Driver far = driver("drv_far", 35.1495, -90.0490, false, 20_000);
        Driver near = driver("drv_near", 35.0500, -89.9800, false, 20_000);
        when(drivers.findByTerminalAndOnDutyTrue("482")).thenReturn(List.of(far, near));

        Assignment a = assignment(8_200, false);
        assertThat(service.offerToNearestDriver(a)).contains(near);
        assertThat(a.getStatus()).isEqualTo(AssignmentStatus.OFFERED);
        assertThat(a.getOfferedAt()).isEqualTo(NOW);
    }

    @Test
    void skipsDriversWithoutCapacityOrHazmatEndorsement() {
        Driver full = driver("drv_full", 35.0430, -89.9770, true, 2_000);
        Driver noHazmat = driver("drv_nohaz", 35.0440, -89.9770, false, 20_000);
        Driver ok = driver("drv_ok", 35.2000, -89.8000, true, 20_000);
        when(drivers.findByTerminalAndOnDutyTrue("482")).thenReturn(List.of(full, noHazmat, ok));

        assertThat(service.offerToNearestDriver(assignment(8_200, true))).contains(ok);
    }

    @Test
    void leavesAssignmentUnassignedWhenNobodyCanTakeIt() {
        when(drivers.findByTerminalAndOnDutyTrue("482")).thenReturn(List.of());

        Assignment a = assignment(8_200, false);
        assertThat(service.offerToNearestDriver(a)).isEmpty();
        assertThat(a.getStatus()).isEqualTo(AssignmentStatus.UNASSIGNED);
    }

    @Test
    void duplicateTenderEventsAreIgnored() {
        when(assignments.existsById("shp_01JD3K8W1V")).thenReturn(true);

        service.onTendered(new ShipmentEvent("tendered", "shp_01JD3K8W1V", "4820019375", "482",
                LocalDate.of(2026, 11, 18), 35.04, -89.97, 8_200, false));

        verify(assignments, never()).save(any());
    }

    @Test
    void onlyTheOfferedDriverCanAccept() {
        Driver near = driver("drv_near", 35.0500, -89.9800, false, 20_000);
        Assignment a = assignment(8_200, false);
        a.offerTo(near, NOW);
        when(assignments.findById("shp_01JD3K8W1V")).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> service.accept("shp_01JD3K8W1V", "drv_other"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(service.accept("shp_01JD3K8W1V", "drv_near").getStatus()).isEqualTo(AssignmentStatus.ACCEPTED);
    }
}
