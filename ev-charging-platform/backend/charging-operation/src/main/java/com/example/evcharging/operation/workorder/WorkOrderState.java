package com.example.evcharging.operation.workorder;

public enum WorkOrderState {
    PENDING_ASSIGNMENT,
    ASSIGNED,
    IN_PROGRESS,
    WAIT_VERIFY,
    CLOSED,
    CANCELLED;

    public boolean terminal(){return this==CLOSED||this==CANCELLED;}
}
