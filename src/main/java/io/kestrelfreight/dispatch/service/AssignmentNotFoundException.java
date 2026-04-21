package io.kestrelfreight.dispatch.service;

public class AssignmentNotFoundException extends RuntimeException {
    public AssignmentNotFoundException(String shipmentId) {
        super("no assignment for shipment " + shipmentId);
    }
}
