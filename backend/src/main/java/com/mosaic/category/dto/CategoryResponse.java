package com.mosaic.category.dto;

import com.mosaic.category.entity.Category;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String icon,
        String color,
        UUID parentId,
        boolean isSystem,
        int sortOrder
) {
    public static CategoryResponse fromEntity(Category category) {
        if (category == null) return null;
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getIcon(),
                category.getColor(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.isSystem(),
                category.getSortOrder()
        );
    }
}
