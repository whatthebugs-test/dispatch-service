package io.kestrelfreight.dispatch.repo;

import io.kestrelfreight.dispatch.domain.Driver;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, String> {

    List<Driver> findByTerminalAndOnDutyTrue(String terminal);
}
