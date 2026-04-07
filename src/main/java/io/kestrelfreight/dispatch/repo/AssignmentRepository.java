package io.kestrelfreight.dispatch.repo;

import io.kestrelfreight.dispatch.domain.Assignment;
import io.kestrelfreight.dispatch.domain.AssignmentStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, String> {

    List<Assignment> findByTerminalAndPickupDateAndStatusInOrderByProNumber(
            String terminal, LocalDate pickupDate, Collection<AssignmentStatus> statuses);

    List<Assignment> findByDriverIdAndStatusIn(String driverId, Collection<AssignmentStatus> statuses);
}
