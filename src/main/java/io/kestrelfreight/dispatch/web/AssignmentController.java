package io.kestrelfreight.dispatch.web;

import io.kestrelfreight.dispatch.domain.Assignment;
import io.kestrelfreight.dispatch.service.AssignmentNotFoundException;
import io.kestrelfreight.dispatch.service.DispatchService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private final DispatchService dispatchService;

    public AssignmentController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @GetMapping
    public List<AssignmentView> board(
            @RequestParam String terminal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dispatchService.board(terminal, date).stream().map(AssignmentView::from).toList();
    }

    @PostMapping("/{shipmentId}/accept")
    public AssignmentView accept(@PathVariable String shipmentId, @RequestBody AcceptRequest body) {
        return AssignmentView.from(dispatchService.accept(shipmentId, body.driverId()));
    }

    @ExceptionHandler(AssignmentNotFoundException.class)
    ProblemDetail notFound(AssignmentNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail conflict(IllegalStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    public record AcceptRequest(String driverId) {}

    public record AssignmentView(String shipmentId, String proNumber, String status, String driverId,
                                 LocalDate pickupDate, double weightLbs, boolean hazmat) {
        static AssignmentView from(Assignment a) {
            return new AssignmentView(a.getShipmentId(), a.getProNumber(), a.getStatus().name(),
                    a.getDriver() == null ? null : a.getDriver().getId(), a.getPickupDate(), a.getWeightLbs(), a.isHazmat());
        }
    }
}
