package com.mosaic.common.exception;

/**
 * Thrown when a resource already exists (e.g., duplicate email on registration,
 * or re-importing a CSV that was already processed).
 */
public class DuplicateResourceException extends RuntimeException {

    private final String resourceType;
    private final String field;

    public DuplicateResourceException(String resourceType, String field, String value) {
        super(String.format("%s already exists with %s: %s", resourceType, field, value));
        this.resourceType = resourceType;
        this.field = field;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getField() {
        return field;
    }
}
