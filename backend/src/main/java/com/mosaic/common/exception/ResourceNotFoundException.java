package com.mosaic.common.exception;

/**
 * Thrown when a requested resource does not exist or is not accessible
 * by the current user.
 *
 * We intentionally return 404 (not 403) to avoid revealing whether
 * a resource exists for another user — this prevents enumeration attacks.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
