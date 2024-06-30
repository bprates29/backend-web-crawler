package com.axreng.backend.model;

public enum Status {
    ACTIVE, DONE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
