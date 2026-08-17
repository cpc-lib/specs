package com.company.marketplace.system.domain.access;
public record SystemUser(long id, String username, Status status) { public enum Status { ACTIVE, DISABLED, LOCKED } }
